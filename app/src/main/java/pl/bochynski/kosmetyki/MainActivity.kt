package pl.bochynski.kosmetyki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import pl.bochynski.kosmetyki.ui.theme.KosmetykiappTheme
import pl.bochynski.kosmetyki.ui.zapasy.ZapasyRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val aplikacja = application as KosmetykiApplication
        setContent {
            KosmetykiappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ZapasyRoute(
                        kategoriaRepository = aplikacja.kategoriaRepository,
                        produktRepository = aplikacja.produktRepository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
