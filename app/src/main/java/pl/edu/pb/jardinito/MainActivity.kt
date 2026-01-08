package pl.edu.pb.jardinito

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import pl.edu.pb.jardinito.ui.navigation.AppNavGraph
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tworzymy ViewModel przez ViewModelProvider
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Launcher dla Google Sign-In
        val googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account?.idToken
                    if (idToken != null) {
                        // Przekazujemy token do ViewModelu, który wywoła backend
                        authViewModel.loginWithGoogle(idToken)
                    } else {
                        Log.e("JARDINITO", "Google ID Token is null")
                    }
                } catch (e: ApiException) {
                    Log.e("JARDINITO", "Google sign in failed", e)
                }
            }

        // Compose content
        setContent {
            JardinitoTheme {
                AppNavGraph(
                    authViewModel = authViewModel,
                    onGoogleSignInClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id)) // Twój client ID Androida
                            .requestEmail()
                            .build()

                        val googleSignInClient = GoogleSignIn.getClient(this, gso)
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }
                )
            }
        }
    }
}
