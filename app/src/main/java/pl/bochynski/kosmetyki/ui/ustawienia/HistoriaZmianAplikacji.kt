package pl.bochynski.kosmetyki.ui.ustawienia

data class WpisHistoriiZmian(
    val wersja: String,
    val data: String,
    val zmiany: List<String>
)

// Uzupelniac o nowy wpis na poczatku listy przy kazdej kolejnej aktualizacji aplikacji.
val HISTORIA_ZMIAN_APLIKACJI = listOf(
    WpisHistoriiZmian(
        wersja = "1.1",
        data = "27.07.2026",
        zmiany = listOf(
            "Nowa ikona aplikacji",
            "Ustawienia: eksport i import bazy produktów do/z pliku, zerowanie bazy danych",
            "Sprawdzanie kodu kreskowego: możliwość ręcznego wpisania kodu EAN (bez aparatu)",
            "Poprawka: średnia cena i najczęstsze miejsce zakupu liczone poprawnie nawet bez podanej daty zakupu",
            "Pojemność produktu: pole liczbowe z wyborem jednostki (ml/g) zamiast dowolnego tekstu",
            "Formularz produktu: kod EAN sprawdza najpierw własną bazę, dopiero potem Open Beauty Facts",
            "Ustawienia: szkielet pod przyszłe połączenie z zewnętrznym serwerem bazy danych produktów",
            "Ustawienia: sekcja historii zmian aplikacji"
        )
    ),
    WpisHistoriiZmian(
        wersja = "1.0",
        data = "26.07.2026",
        zmiany = listOf(
            "Pierwsze wydanie aplikacji",
            "Pulpit z licznikami i statystykami, interaktywne kafelki",
            "Zapasy, Otwarte i Zużyte kosmetyki z kategoriami i wyszukiwarką",
            "Dodawanie i edycja produktów, ulubione, podpowiedzi i autouzupełnianie danych",
            "Historia cen produktów z wykresem",
            "Powiadomienia o zbliżającym się lub minionym terminie ważności",
            "Motyw jasny/ciemny/systemowy i konfigurowalne kolory statusów",
            "Skaner kodów kreskowych EAN (CameraX + ML Kit) i dane z Open Beauty Facts"
        )
    )
)
