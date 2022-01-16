package nick.template.di

import android.content.Context
import android.speech.SpeechRecognizer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import nick.template.initializers.AppInitializer
import nick.template.initializers.Initializer
import nick.template.initializers.MainInitializer
import nick.template.speech.AndroidSpeechRepository
import nick.template.speech.SpeechRepository

@Module
@InstallIn(SingletonComponent::class)
interface AppModule {
    companion object {
        @Provides
        fun speechRecognizer(@ApplicationContext context: Context): SpeechRecognizer {
            return SpeechRecognizer.createSpeechRecognizer(context)
        }
    }

    @Binds
    @IntoSet
    fun mainInitializer(mainInitializer: MainInitializer): Initializer

    @Binds
    fun appInitializers(appInitializer: AppInitializer): Initializer

    @Binds
    fun speechRepository(repository: AndroidSpeechRepository): SpeechRepository
}
