package pl.edu.pb.jardinito
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.edu.pb.jardinito.ui.navigation.AppNavGraph
import pl.edu.pb.jardinito.ui.theme.JardinitoTheme
import pl.edu.pb.jardinito.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JardinitoTheme {
                val authViewModel: AuthViewModel = viewModel()
                AppNavGraph(authViewModel)
            }
        }

    }
}