package com.textify.app.utils

object Constants {
    const val DATABASE_NAME = "textify_db"
    const val BASE_URL = "https://api.textify.com/"
    
    // ElevenLabs Config (IA de alta calidad)
    const val ELEVENLABS_API_KEY = "TU_API_KEY_AQUI" // El usuario debe poner su clave aquí
    const val ELEVENLABS_BASE_URL = "https://api.elevenlabs.io/v1/"
    
    // IDs de voces de ElevenLabs (Ejemplos)
    const val VOICE_ID_SOFIA = "EXAVITQu4vr4xnSDxMaL" // Bella
    const val VOICE_ID_ALEJANDRO = "ErXw087vS2Wm3qXvVAbF" // Antoni
    
    const val AUDIO_SAMPLE_RATE = 16000
    const val MAX_PHRASE_LENGTH = 200
}
