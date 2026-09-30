package io.github.sergiobe31.vistazo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Session::class, DailyEstimate::class], version = 2, exportSchema = false)
abstract class VistazoDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun estimateDao(): EstimateDao

    companion object {
        @Volatile private var instance: VistazoDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS daily_estimates (" +
                        "dayStartMillis INTEGER NOT NULL PRIMARY KEY, " +
                        "estimatedUnlocks INTEGER NOT NULL)"
                )
            }
        }

        fun get(context: Context): VistazoDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    VistazoDatabase::class.java,
                    "vistazo.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
