package com.textify.app.domain.usecase

import com.textify.app.ai.tts.TextToSpeechManager
import com.textify.app.domain.model.Phrase

class PlayPhraseUseCase(
    private val ttsManager: TextToSpeechManager
) {
    operator fun invoke(phrase: Phrase) {
        ttsManager.speak(phrase.text)
    }
}
