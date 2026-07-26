package pl.bochynski.kosmetyki.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PLIK_USTAWIEN = "ustawienia"
private const val KLUCZ_PROG_POWIADOMIEN_DNI = "prog_powiadomien_dni"
const val DOMYSLNY_PROG_POWIADOMIEN_DNI = 90

interface UstawieniaRepository {
    suspend fun pobierzProgPowiadomienDni(): Int
    suspend fun ustawProgPowiadomienDni(dni: Int)
}

class UstawieniaRepositoryImpl(
    context: Context
) : UstawieniaRepository {
    private val preferencje = context.getSharedPreferences(PLIK_USTAWIEN, Context.MODE_PRIVATE)

    override suspend fun pobierzProgPowiadomienDni(): Int = withContext(Dispatchers.IO) {
        preferencje.getInt(KLUCZ_PROG_POWIADOMIEN_DNI, DOMYSLNY_PROG_POWIADOMIEN_DNI)
    }

    override suspend fun ustawProgPowiadomienDni(dni: Int) = withContext(Dispatchers.IO) {
        preferencje.edit().putInt(KLUCZ_PROG_POWIADOMIEN_DNI, dni).apply()
    }
}
