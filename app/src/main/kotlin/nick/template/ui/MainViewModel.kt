package nick.template.ui

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import nick.template.speech.SpeechRepository

class MainViewModel(
    private val handle: SavedStateHandle,
    private val speechRepository: SpeechRepository
) : ViewModel() {
    private val events = MutableSharedFlow<Event>()

    fun results(): Flow<String> = events.flatMapLatest { event ->
        when (event) {
            Event.Start -> speechRepository.listen().transform { result ->
                when (result) {
                    is SpeechRepository.Result.Recognition -> emit(result.data)
                    is SpeechRepository.Result.Ended -> stop()
                }
            }
            Event.Stop -> emptyFlow()
        }
    }

    fun start() {
        event(Event.Start)
    }

    fun stop() {
        event(Event.Stop)
    }

    private fun event(event: Event) {
        viewModelScope.launch {
            events.emit(event)
        }
    }

    private enum class Event {
        Start, Stop
    }

    class Factory @Inject constructor(
        private val speechRepository: SpeechRepository
    ) {
        fun create(owner: SavedStateRegistryOwner): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, null) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(handle, speechRepository) as T
                }
            }
        }
    }
}
