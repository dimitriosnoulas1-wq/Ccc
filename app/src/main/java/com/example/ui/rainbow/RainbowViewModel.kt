package com.example.ui.rainbow

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class RainbowUiState(
    val today: Long = DateUtil.today(),
    val history: List<PricePoint> = emptyList(),
    val shown: PricePoint? = null,
    val shownReason: LiveGate.Reason? = null,
    val live: Double? = null,
    val lastLive: Long = 0L,
    val loading: Boolean = true,
    val offline: Boolean = false,
    val error: String? = null,
)

class RainbowViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = RainbowRepository(app.filesDir)
    private val _state = MutableStateFlow(RainbowUiState())
    val state = _state.asStateFlow()
    private val refreshLock = Mutex()
    private var fetchedDay = -1L

    init {
        viewModelScope.launch {
            val cached = repo.cachedHistory()
            val saved = repo.cachedState()
            val today = DateUtil.today()
            fetchedDay = saved?.fetchedDay ?: -1L
            _state.update {
                it.copy(
                    today = today,
                    history = cached,
                    shown = saved?.shown?.takeIf { p -> p.day == today },
                    loading = cached.isEmpty(),
                )
            }
            refreshHistory(force = cached.isEmpty())
        }
    }

    fun refreshHistory(force: Boolean = false) {
        viewModelScope.launch {
            refreshLock.withLock {
                val today = DateUtil.today()
                if (!force && fetchedDay == today && _state.value.history.isNotEmpty()) return@withLock
                _state.update { it.copy(loading = it.history.isEmpty(), error = null) }
                runCatching { repo.fetchHistory() }
                    .onSuccess { h ->
                        fetchedDay = today
                        _state.update { it.copy(today = today, history = h, loading = false, offline = false) }
                        persist()
                    }
                    .onFailure { e ->
                        _state.update {
                            it.copy(
                                loading = false,
                                offline = it.history.isNotEmpty(),
                                error = if (it.history.isEmpty()) e.message ?: "Network error" else null,
                            )
                        }
                    }
            }
        }
    }

    /** Applies the app's live BTC print through the ±7% / band-change gate. */
    fun applyLive(price: Double) {
        if (price <= 0.0) return
        viewModelScope.launch {
            val today = DateUtil.today()
            if (today != _state.value.today) {
                _state.update { it.copy(today = today) }
                refreshHistory()
            }
            onLive(price, today)
        }
    }

    private suspend fun onLive(price: Double, today: Long) {
        val reason = LiveGate.reason(_state.value.shown, price, today)
        _state.update {
            it.copy(
                live = price,
                lastLive = System.currentTimeMillis(),
                shown = if (reason != null) PricePoint(today, price) else it.shown,
                shownReason = reason ?: it.shownReason,
            )
        }
        if (reason != null) persist()
    }

    private suspend fun persist() {
        repo.saveState(RainbowCacheState(fetchedDay, _state.value.shown))
    }
}
