package com.example.ui.rainbow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RainbowUiState(
    val history: List<PricePoint> = emptyList(),
    val live: Double? = null,
    val lastUpdate: Long = 0L,
    val loading: Boolean = true,
    val error: String? = null,
)

class RainbowViewModel : ViewModel() {
    private val _state = MutableStateFlow(RainbowUiState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { BtcRepository.history() }
                .onSuccess { h -> _state.update { it.copy(history = h, loading = false) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message ?: "Network error") } }
        }
    }
}

/** Daily history, plus today's live print only when that print is real. */
fun buildSeries(history: List<PricePoint>, live: Double?, today: Long): List<PricePoint> {
    if (history.isEmpty()) return emptyList()
    val past = history.filter { it.day < today }
    if (live != null && live > 0.0) return past + PricePoint(today, live)
    return past.ifEmpty { history }
}
