package com.example.ui.rainbow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
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
        viewModelScope.launch {
            while (isActive) {
                runCatching { BtcRepository.spot() }.onSuccess { p ->
                    _state.update { it.copy(live = p, lastUpdate = System.currentTimeMillis()) }
                }
                delay(15_000)
            }
        }
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

/** Ιστορικό + σημερινή live τιμή ως τελευταίο σημείο. */
fun buildSeries(history: List<PricePoint>, live: Double?, today: Long): List<PricePoint> {
    if (history.isEmpty()) return emptyList()
    val past = history.filter { it.day < today }
    return past + PricePoint(today, live ?: history.last().price)
}
