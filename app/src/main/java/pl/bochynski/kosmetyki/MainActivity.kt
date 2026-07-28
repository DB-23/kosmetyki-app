package pl.bochynski.kosmetyki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.bochynski.kosmetyki.data.repository.TrybMotywu
import pl.bochynski.kosmetyki.ui.navigation.KosmetykiNavHost
import pl.bochynski.kosmetyki.ui.theme.KosmetykiappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val aplikacja = application as KosmetykiApplication
        setContent {
            val trybMotywu by aplikacja.ustawieniaRepository.obserwujTrybMotywu().collectAsStateWithLifecycle()
            val ciemnyMotyw = when (trybMotywu) {
                TrybMotywu.SYSTEMOWY -> isSystemInDarkTheme()
                TrybMotywu.JASNY -> false
                TrybMotywu.CIEMNY -> true
            }
            KosmetykiappTheme(darkTheme = ciemnyMotyw) {
                KosmetykiNavHost(
                    kategoriaRepository = aplikacja.kategoriaRepository,
                    produktRepository = aplikacja.produktRepository,
                    ustawieniaRepository = aplikacja.ustawieniaRepository,
                    databaseSeeder = aplikacja.databaseSeeder,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
