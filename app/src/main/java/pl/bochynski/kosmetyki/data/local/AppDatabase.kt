package pl.bochynski.kosmetyki.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pl.bochynski.kosmetyki.data.local.dao.KategoriaDao
import pl.bochynski.kosmetyki.data.local.dao.ProduktDao
import pl.bochynski.kosmetyki.data.local.entity.KategoriaEntity
import pl.bochynski.kosmetyki.data.local.entity.ProduktEntity
import pl.bochynski.kosmetyki.data.seed.DatabaseSeeder

@Database(
    entities = [KategoriaEntity::class, ProduktEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kategoriaDao(): KategoriaDao
    abstract fun produktDao(): ProduktDao

    companion object {
        private const val NAZWA_BAZY = "kosmetyki.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun pobierzInstancje(context: Context, zasiegAplikacji: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NAZWA_BAZY
                )
                    .addCallback(SeedCallback(context.applicationContext, zasiegAplikacji))
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private class SeedCallback(
            private val context: Context,
            private val zasiegAplikacji: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                zasiegAplikacji.launch {
                    INSTANCE?.let { baza -> DatabaseSeeder(context, baza).zaseeduj() }
                }
            }
        }
    }
}
