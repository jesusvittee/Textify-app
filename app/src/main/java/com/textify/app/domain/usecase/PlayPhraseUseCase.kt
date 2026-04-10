package com.textify.app.domain.usecase

import com.textify.app.domain.model.Phrase
import com.textify.app.services.audio.TextToSpeechManager
import com.textify.app.ui.screens.profile.VoiceGender

class PlayPhraseUseCase(
    private val ttsManager: TextToSpeechManager
) {
    operator fun invoke(phrase: Phrase, voiceId: String, gender: VoiceGender) {
        ttsManager.speak(phrase.text, voiceId, gender)
    }
}
