package pl.bochynski.kosmetyki.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.ui.archiwum.ArchiwumRoute
import pl.bochynski.kosmetyki.ui.otwarte.OtwarteRoute
import pl.bochynski.kosmetyki.ui.produkt.ProduktFormRoute
import pl.bochynski.kosmetyki.ui.pulpit.PulpitRoute
import pl.bochynski.kosmetyki.ui.zapasy.ZapasyRoute

private const val ARG_PRODUKT_ID = "produktId"
private const val BEZ_PRODUKTU = -1L

private object KosmetykiRoutes {
    const val PULPIT = "pulpit"
    const val ZAPASY = "zapasy"
    const val OTWARTE = "otwarte"
    const val ARCHIWUM = "archiwum"
    const val PRODUKT_FORM_BAZA = "produktForm"
    const val PRODUKT_FORM = "$PRODUKT_FORM_BAZA?$ARG_PRODUKT_ID={$ARG_PRODUKT_ID}"

    fun produktForm(produktId: Long? = null) =
        "$PRODUKT_FORM_BAZA?$ARG_PRODUKT_ID=${produktId ?: BEZ_PRODUKTU}"
}

private data class PozycjaNawigacji(val trasa: String, val etykieta: String, val ikona: ImageVector)

private val POZYCJE_NAWIGACJI = listOf(
    PozycjaNawigacji(KosmetykiRoutes.PULPIT, "Pulpit", Icons.Filled.Dashboard),
    PozycjaNawigacji(KosmetykiRoutes.ZAPASY, "Zapasy", Icons.Filled.Inventory2),
    PozycjaNawigacji(KosmetykiRoutes.OTWARTE, "Otwarte", Icons.Filled.LockOpen),
    PozycjaNawigacji(KosmetykiRoutes.ARCHIWUM, "Zużyte", Icons.Filled.Delete)
)

@Composable
fun KosmetykiNavHost(
    kategoriaRepository: KategoriaRepository,
    produktRepository: ProduktRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val aktualnaTrasa = backStackEntry?.destination

    val pokazDolnyPasek = POZYCJE_NAWIGACJI.any { pozycja ->
        aktualnaTrasa?.hierarchy?.any { it.route == pozycja.trasa } == true
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (pokazDolnyPasek) {
                NavigationBar {
                    POZYCJE_NAWIGACJI.forEach { pozycja ->
                        NavigationBarItem(
                            selected = aktualnaTrasa?.hierarchy?.any { it.route == pozycja.trasa } == true,
                            onClick = {
                                navController.navigate(pozycja.trasa) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(pozycja.ikona, contentDescription = pozycja.etykieta) },
                            label = { Text(pozycja.etykieta) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = KosmetykiRoutes.ZAPASY,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(KosmetykiRoutes.PULPIT) {
                PulpitRoute(produktRepository = produktRepository)
            }
            composable(KosmetykiRoutes.ZAPASY) {
                ZapasyRoute(
                    kategoriaRepository = kategoriaRepository,
                    produktRepository = produktRepository,
                    naDodajProdukt = { navController.navigate(KosmetykiRoutes.produktForm()) },
                    naEdytujProdukt = { produktId -> navController.navigate(KosmetykiRoutes.produktForm(produktId)) }
                )
            }
            composable(KosmetykiRoutes.OTWARTE) {
                OtwarteRoute(
                    kategoriaRepository = kategoriaRepository,
                    produktRepository = produktRepository,
                    naEdytujProdukt = { produktId -> navController.navigate(KosmetykiRoutes.produktForm(produktId)) }
                )
            }
            composable(KosmetykiRoutes.ARCHIWUM) {
                ArchiwumRoute(
                    kategoriaRepository = kategoriaRepository,
                    produktRepository = produktRepository
                )
            }
            composable(
                route = KosmetykiRoutes.PRODUKT_FORM,
                arguments = listOf(
                    navArgument(ARG_PRODUKT_ID) {
                        type = NavType.LongType
                        defaultValue = BEZ_PRODUKTU
                    }
                )
            ) { backStackEntry ->
                val produktId = backStackEntry.arguments?.getLong(ARG_PRODUKT_ID) ?: BEZ_PRODUKTU
                ProduktFormRoute(
                    produktId = produktId.takeIf { it != BEZ_PRODUKTU },
                    kategoriaRepository = kategoriaRepository,
                    produktRepository = produktRepository,
                    naWstecz = { navController.popBackStack() }
                )
            }
        }
    }
}
