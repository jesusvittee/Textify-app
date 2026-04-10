package com.textify.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.textify.app.ai.stt.SpeechRecognizer
import com.textify.app.services.audio.TextToSpeechManager
import com.textify.app.data.repository.ConversationRepositoryImpl
import com.textify.app.data.repository.MessageRepositoryImpl
import com.textify.app.data.repository.PhraseRepositoryImpl
import com.textify.app.domain.usecase.*
import com.textify.app.ui.Routes
import com.textify.app.ui.components.BottomNavBar
import com.textify.app.ui.screens.splash.SplashScreen
import com.textify.app.ui.screens.auth.LoginScreen
import com.textify.app.ui.screens.auth.RegisterScreen
import com.textify.app.ui.screens.chat.ChatScreen
import com.textify.app.ui.screens.chat.ChatViewModel
import com.textify.app.ui.screens.phrases.PhrasesScreen
import com.textify.app.ui.screens.phrases.PhrasesViewModel
import com.textify.app.ui.screens.profile.ProfileScreen
import com.textify.app.ui.screens.profile.ProfileViewModel
import com.textify.app.ui.theme.TextifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            setContent {
                val profileViewModel: ProfileViewModel = viewModel()
                val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
                
                TextifyTheme(
                    darkTheme = uiState.isDarkMode,
                    fontScale = uiState.fontScale
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        TextifyNavigation(profileViewModel)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error al iniciar Compose: ${e.message}")
        }
    }
}

@Composable
fun TextifyNavigation(profileViewModel: ProfileViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val app = context.applicationContext as TextifyApp
    val database = app.database
    
    // Repositorios
    val messageRepository = remember { MessageRepositoryImpl(database.messageDao()) }
    val phraseRepository = remember { PhraseRepositoryImpl(database.phraseDao()) }
    val conversationRepository = remember { ConversationRepositoryImpl(database.conversationDao()) }
    
    // IA Managers
    val ttsManager = remember { TextToSpeechManager(context) }
    val speechRecognizer = remember { SpeechRecognizer(context) }

    // Casos de Uso
    val getMessagesUseCase = remember { GetMessagesUseCase(messageRepository) }
    val sendMessageUseCase = remember { SendMessageUseCase(messageRepository) }
    val getPhrasesUseCase = remember { GetPhrasesUseCase(phraseRepository) }
    val addPhraseUseCase = remember { AddPhraseUseCase(phraseRepository) }
    val deletePhraseUseCase = remember { DeletePhraseUseCase(phraseRepository) }
    val playPhraseUseCase = remember { PlayPhraseUseCase(ttsManager) }
    val getConversationsUseCase = remember { GetConversationsUseCase(conversationRepository) }
    val createConversationUseCase = remember { CreateConversationUseCase(conversationRepository) }
    val updateConversationUseCase = remember { UpdateConversationUseCase(conversationRepository) }
    val deleteConversationUseCase = remember { DeleteConversationUseCase(conversationRepository) }

    // ViewModels compartidos (Scanned to Activity) para mantener el estado entre pestañas
    val activity = LocalContext.current as ComponentActivity
    
    val chatViewModel: ChatViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = ChatViewModel.Factory(
            app,
            getMessagesUseCase, sendMessageUseCase, speechRecognizer,
            getConversationsUseCase, createConversationUseCase,
            updateConversationUseCase, deleteConversationUseCase
        )
    )
    
    val phrasesViewModel: PhrasesViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = PhrasesViewModel.Factory(
            getPhrasesUseCase, addPhraseUseCase, deletePhraseUseCase, playPhraseUseCase
        )
    )

    // Sincronizar configuraciones de voz
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(profileUiState.selectedVoiceId, profileUiState.voiceGender) {
        chatViewModel.updateVoiceSettings(profileUiState.selectedVoiceId, profileUiState.voiceGender)
        phrasesViewModel.updateVoiceSettings(profileUiState.selectedVoiceId, profileUiState.voiceGender)
    }

    val routesWithNavbar = listOf(Routes.CHAT, Routes.PHRASES, Routes.PROFILE)
    val showNavbar = currentRoute in routesWithNavbar

    Scaffold(
        bottomBar = {
            if (showNavbar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH
        ) {
            composable(route = Routes.SPLASH) {
                SplashScreen(onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                })
            }
            
            composable(route = Routes.LOGIN) {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(Routes.CHAT) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER)
                    }
                )
            }
            
            composable(route = Routes.REGISTER) {
                RegisterScreen(
                    onRegister = { gender ->
                        profileViewModel.setVoiceGender(gender)
                        navController.navigate(Routes.CHAT) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            
            composable(route = Routes.CHAT) {
                ChatScreen(
                    navController = navController, 
                    viewModel = chatViewModel,
                    profileViewModel = profileViewModel,
                    contentPadding = paddingValues
                )
            }
            
            composable(route = Routes.PHRASES) {
                PhrasesScreen(
                    navController = navController, 
                    viewModel = phrasesViewModel,
                    contentPadding = paddingValues
                )
            }
            
            composable(route = Routes.PROFILE) {
                ProfileScreen(
                    navController = navController, 
                    viewModel = profileViewModel,
                    contentPadding = paddingValues
                )
            }
        }
    }
}
