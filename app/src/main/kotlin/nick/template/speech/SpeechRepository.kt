package nick.template.speech

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface SpeechRepository {
    fun listen(): Flow<Result>

    sealed class Result {
        data class Recognition(val data: String) : Result()
        data class Ended(val reason: String) : Result()
    }
}

class AndroidSpeechRepository @Inject constructor(
    private val speechRecognizerProvider: Provider<SpeechRecognizer>
) : SpeechRepository {
    override fun listen(): Flow<SpeechRepository.Result> = callbackFlow {
        Log.d("asdf", "started listening")
        val speechRecognizer: SpeechRecognizer = speechRecognizerProvider.get()

        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {
                Log.d("asdf", "onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("asdf", "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
//                Log.d("asdf", "onRmsChanged") // Noisy
            }

            override fun onBufferReceived(buffer: ByteArray) {
                Log.d("asdf", "onBufferReceived")
            }

            override fun onEndOfSpeech() {
                Log.d("asdf", "onEndOfSpeech")
                trySend(SpeechRepository.Result.Ended(reason = "End of speech"))
            }

            override fun onError(error: Int) {
                Log.d("asdf", "onError")
                trySend(SpeechRepository.Result.Ended(reason = "Error: $error"))
            }

            override fun onResults(results: Bundle) {
                val data = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (data != null) {
                    Log.d("asdf", "onResults")
                    trySend(SpeechRepository.Result.Recognition(data))
                }
            }

            override fun onPartialResults(partialResults: Bundle) {
                val data = partialResults
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (data != null) {
                    trySend(SpeechRepository.Result.Recognition(data))
                }
            }

            override fun onEvent(eventType: Int, params: Bundle) {
                Log.d("asdf", "onEvent")
            }
        }

        speechRecognizer.setRecognitionListener(listener)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // These don't seem to do anything on a Pixel 5, unfortunately
            .putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10_000L)
            .putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10_000L)
            .putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10_000L)
        speechRecognizer.startListening(intent)

        awaitClose {
            Log.d("asdf", "stopped listening")
            with(speechRecognizer) {
                stopListening()
                cancel()
                destroy()
            }
        }
    }
}
