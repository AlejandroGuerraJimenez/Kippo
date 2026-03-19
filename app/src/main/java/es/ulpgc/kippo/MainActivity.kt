package es.ulpgc.kippo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import es.ulpgc.kippo.ui.RegisterScreen
import es.ulpgc.kippo.ui.LoginScreen
import es.ulpgc.kippo.ui.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val auth = FirebaseAuth.getInstance()
                    val initial = if (auth.currentUser != null) "home" else "register"
                    val screenState = remember { mutableStateOf(initial) }

                    // Keep screenState in sync with FirebaseAuth using an AuthStateListener
                    DisposableEffect(Unit) {
                        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                            val user = firebaseAuth.currentUser
                            screenState.value = if (user != null) "home" else "login"
                        }
                        auth.addAuthStateListener(listener)
                        onDispose {
                            auth.removeAuthStateListener(listener)
                        }
                    }

                    when (screenState.value) {
                        "register" -> RegisterScreen(
                            onRegisterSuccess = { screenState.value = "home" },
                            onNavigateToLogin = { screenState.value = "login" }
                        )
                        "login" -> LoginScreen(
                            onLoginSuccess = { screenState.value = "home" },
                            onNavigateToRegister = { screenState.value = "register" }
                        )
                        "home" -> HomeScreen(onSignOut = {
                            FirebaseAuth.getInstance().signOut()
                            screenState.value = "login"
                        })
                        else -> RegisterScreen(onRegisterSuccess = { screenState.value = "home" })
                    }
                }
            }
        }
    }
}