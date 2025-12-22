package pl.edu.pb.jardinito
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import pl.edu.pb.jardinito.data.remote.ApiService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pl.edu.pb.jardinito.ui.screens.LoginScreen
import pl.edu.pb.jardinito.ui.screens.RegisterScreen
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    // tymczasowy Retrofit (szkielet)
    private val apiService = Retrofit.Builder()
        .baseUrl("https://twój-backend.com/") // później podmień na prawdziwy URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    private val authViewModel = AuthViewModel(apiService)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JardinitoTheme {
//                LoginScreen(viewModel = authViewModel)
                RegisterScreen(viewModel = authViewModel)
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JardinitoTheme {
        Greeting("Android")
    }
}