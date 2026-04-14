package com.textify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.textify.app.ai.stt.SpeechRecognizer
import com.textify.app.ai.tts.TextToSpeechManager
import com.textify.app.data.repository.ConversationRepositoryImpl
import com.textify.app.data.repository.MessageRepositoryImpl
import com.textify.app.data.repository.PhraseRepositoryImpl
import com.textify.app.domain.usecase.*
import com.textify.app.ui.Routes
import com.textify.app.ui.components.BottomNavBar
import com.textify.app.ui.screens.splash.SplashScreen
import com.textify.app.ui.screens.auth.LoginScreen
import com.textify.app.ui.screens.auth.RegisterScreen
import com.textify.app.ui.screens.auth.AuthViewModel
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
        setContent {
            val context = LocalContext.current
            val app = context.applicationContext as TextifyApp
            
            val ttsManager = remember { TextToSpeechManager(context) }
            
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(app, ttsManager)
            )
            val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
            
            TextifyTheme(
                darkTheme = uiState.isDarkMode,
                fontScale = uiState.fontScale
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TextifyNavigation(profileViewModel, ttsManager)
                }
            }
        }
    }
}

@Composable
fun TextifyNavigation(profileViewModel: ProfileViewModel, ttsManager: TextToSpeechManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val lifecycleState by navBackStackEntry?.lifecycle?.currentStateFlow?.collectAsStateWithLifecycle(Lifecycle.State.INITIALIZED)
        ?: remember { mutableStateOf(Lifecycle.State.INITIALIZED) }

    val context = LocalContext.current
    val app = context.applicationContext as TextifyApp
    val database = app.database
    val scope = rememberCoroutineScope()
    
    val messageRepository = remember { MessageRepositoryImpl(database.messageDao()) }
    val phraseRepository = remember { PhraseRepositoryImpl(database.phraseDao()) }
    val conversationRepository = remember { ConversationRepositoryImpl(database.conversationDao()) }
    val speechRecognizer = remember { SpeechRecognizer(context) }

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

    val activity = LocalContext.current as ComponentActivity
    
    val authViewModel: AuthViewModel = viewModel(viewModelStoreOwner = activity)

    val chatViewModel: ChatViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = ChatViewModel.Factory(
            app, getMessagesUseCase, sendMessageUseCase, speechRecognizer, ttsManager,
            getConversationsUseCase, createConversationUseCase,
            updateConversationUseCase, deleteConversationUseCase
        )
    )
    
    val phrasesViewModel: PhrasesViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = PhrasesViewModel.Factory(
            app, getPhrasesUseCase, addPhraseUseCase, deletePhraseUseCase, playPhraseUseCase
        )
    )

    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(profileUiState.selectedVoiceId, profileUiState.voiceGender) {
        chatViewModel.updateVoiceSettings(profileUiState.selectedVoiceId, profileUiState.voiceGender)
        phrasesViewModel.updateVoiceSettings(profileUiState.selectedVoiceId, profileUiState.voiceGender)
    }

    val routesWithNavbar = remember { listOf(Routes.CHAT, Routes.PHRASES, Routes.PROFILE) }
    
    val showNavbar by remember(currentRoute, lifecycleState) {
        derivedStateOf {
            val isMainRoute = currentRoute in routesWithNavbar
            val isResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
            val fromMainRoute = navController.previousBackStackEntry?.destination?.route in routesWithNavbar
            
            isMainRoute && (isResumed || fromMainRoute)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showNavbar,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut()
            ) {
                BottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        val bottomPadding = if (showNavbar) paddingValues.calculateBottomPadding() else 0.dp

        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
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
                    viewModel = authViewModel,
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
                    viewModel = authViewModel,
                    onNavigateToHome = {
                        navController.navigate(Routes.CHAT) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            
            composable(route = Routes.CHAT) {
                ChatScreen(navController = navController, viewModel = chatViewModel, profileViewModel = profileViewModel)
            }
            
            composable(route = Routes.PHRASES) {
                PhrasesScreen(navController = navController, viewModel = phrasesViewModel)
            }
            
            composable(route = Routes.PROFILE) {
                ProfileScreen(navController = navController, viewModel = profileViewModel)
            }
        }
    }
}
