package pl.bochynski.kosmetyki.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.bochynski.kosmetyki.data.repository.KategoriaRepository
import pl.bochynski.kosmetyki.data.repository.ProduktRepository
import pl.bochynski.kosmetyki.ui.produkt.ProduktFormRoute
import pl.bochynski.kosmetyki.ui.zapasy.ZapasyRoute

private const val ARG_PRODUKT_ID = "produktId"
private const val BEZ_PRODUKTU = -1L

private object KosmetykiRoutes {
    const val ZAPASY = "zapasy"
    const val PRODUKT_FORM_BAZA = "produktForm"
    const val PRODUKT_FORM = "$PRODUKT_FORM_BAZA?$ARG_PRODUKT_ID={$ARG_PRODUKT_ID}"

    fun produktForm(produktId: Long? = null) =
        "$PRODUKT_FORM_BAZA?$ARG_PRODUKT_ID=${produktId ?: BEZ_PRODUKTU}"
}

@Composable
fun KosmetykiNavHost(
    kategoriaRepository: KategoriaRepository,
    produktRepository: ProduktRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = KosmetykiRoutes.ZAPASY,
        modifier = modifier
    ) {
        composable(KosmetykiRoutes.ZAPASY) {
            ZapasyRoute(
                kategoriaRepository = kategoriaRepository,
                produktRepository = produktRepository,
                naDodajProdukt = { navController.navigate(KosmetykiRoutes.produktForm()) },
                naEdytujProdukt = { produktId -> navController.navigate(KosmetykiRoutes.produktForm(produktId)) }
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
