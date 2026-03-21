package es.ulpgc.kippo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import es.ulpgc.kippo.ui.RegisterScreen
import es.ulpgc.kippo.ui.LoginScreen
import es.ulpgc.kippo.ui.HomeScreen
import es.ulpgc.kippo.ui.CreateHouseholdScreen
import es.ulpgc.kippo.ui.SetupHouseholdScreen
import es.ulpgc.kippo.viewmodel.HomeViewModel
import es.ulpgc.kippo.ui.KippoColors

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
                    val initial = if (auth.currentUser != null) "home_dispatch" else "register"
                    val screenState = remember { mutableStateOf(initial) }

                    DisposableEffect(Unit) {
                        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                            val user = firebaseAuth.currentUser
                            if (user == null) {
                                screenState.value = "login"
                            }
                        }
                        auth.addAuthStateListener(listener)
                        onDispose {
                            auth.removeAuthStateListener(listener)
                        }
                    }

                    when (screenState.value) {
                        "register" -> RegisterScreen(
                            onRegisterSuccess = { screenState.value = "home_dispatch" },
                            onNavigateToLogin = { screenState.value = "login" }
                        )
                        "login" -> LoginScreen(
                            onLoginSuccess = { screenState.value = "home_dispatch" },
                            onNavigateToRegister = { screenState.value = "register" }
                        )
                        "home_dispatch" -> HomeDispatch(
                            onSignOut = {
                                auth.signOut()
                                screenState.value = "login"
                            },
                            onCreateHouseholdRequested = {
                                screenState.value = "create_household"
                            }
                        )
                        "home" -> HomeScreen(
                            onSignOut = {
                                auth.signOut()
                                screenState.value = "login"
                            }
                        )
                        "create_household" -> CreateHouseholdScreen(
                            onHouseholdCreated = {
                                screenState.value = "home_dispatch"
                            }
                        )
                        else -> RegisterScreen(onRegisterSuccess = { screenState.value = "home_dispatch" })
                    }
                }
            }
        }
    }
}

@Composable
fun HomeDispatch(
    onSignOut: () -> Unit,
    onCreateHouseholdRequested: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val hasHousehold by viewModel.hasHousehold.collectAsState()

    when (hasHousehold) {
        true -> HomeScreen(onSignOut = onSignOut)
        false -> SetupHouseholdScreen(
            onCreateHouseholdClick = onCreateHouseholdRequested,
            onSignOut = onSignOut
        )
        null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KippoColors.Teal)
            }
        }
    }
}
