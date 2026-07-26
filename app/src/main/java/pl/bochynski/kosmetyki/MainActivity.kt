package pl.bochynski.kosmetyki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import pl.bochynski.kosmetyki.ui.navigation.KosmetykiNavHost
import pl.bochynski.kosmetyki.ui.theme.KosmetykiappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val aplikacja = application as KosmetykiApplication
        setContent {
            KosmetykiappTheme {
                KosmetykiNavHost(
                    kategoriaRepository = aplikacja.kategoriaRepository,
                    produktRepository = aplikacja.produktRepository,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
