package pl.edu.pb.jardinito
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pl.edu.pb.jardinito.ui.screens.LoginScreen
import pl.edu.pb.jardinito.ui.screens.RegisterScreen
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JardinitoTheme {
                LoginScreen()
//                RegisterScreen()
            }
        }
    }
}