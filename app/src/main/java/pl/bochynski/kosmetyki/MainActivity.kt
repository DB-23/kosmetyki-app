package pl.bochynski.kosmetyki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.local.entity.StatusProduktu
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.ui.theme.KosmetykiappTheme

/**
 * Tymczasowy ekran weryfikacyjny na potrzeby Fazy 1 (model danych, baza, seedowanie).
 * Zostanie zastąpiony właściwymi ekranami w kolejnych fazach.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val aplikacja = application as KosmetykiApplication
        setContent {
            KosmetykiappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EkranWeryfikacjiBazy(
                        kategoriaRepository = aplikacja.kategoriaRepository,
                        produktRepository = aplikacja.produktRepository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkranWeryfikacjiBazy(
    kategoriaRepository: KategoriaRepository,
    produktRepository: ProduktRepository,
    modifier: Modifier = Modifier
) {
    val dane by combine(
        kategoriaRepository.obserwujKategorie(),
        produktRepository.obserwujWszystkieProdukty()
    ) { kategorie, produkty -> kategorie to produkty }
        .collectAsState(initial = emptyList<KategoriaEntity>() to emptyList<ProduktEntity>())

    val (kategorie, produkty) = dane

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Weryfikacja bazy danych") })
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            item {
                Text("Kategorii: ${kategorie.size}")
                Text("Produktów łącznie: ${produkty.size}")
                Text("Otwartych: ${produkty.count { it.status == StatusProduktu.OTWARTE }}")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
            items(kategorie, key = { it.id }) { kategoria ->
                val produktyKategorii = produkty.filter { it.kategoriaId == kategoria.id }
                Text("${kategoria.nazwa}: ${produktyKategorii.size} szt.")
            }
        }
    }
}
