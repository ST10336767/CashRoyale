package com.example.cashroyale.Models

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cashroyale.DAO.UserDAO

/**
 * The Room database for the CashRoyale application.
 * Defines the entities and DAOs used to interact with the database.
 * Includes migration strategies for database schema updates.
 */
@Database(entities = [User::class], version = 8)
abstract class AppDatabase : RoomDatabase() {

    /** Provides access to the [User] data access object. */
    abstract fun userDAO(): UserDAO


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the [AppDatabase].
         * If an instance already exists, it is returned; otherwise, a new instance is created.
         *
         * @param context The application context.
         * @return The singleton instance of the database.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cash_royale_db" // The name of the database file
                )
                    // Add migrations to handle schema updates
                    .addMigrations(
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Migration from version 2 to 3:
         * Creates a new `Category_New` table, copies data from the old `Category` table,
         * drops the old table, and renames the new table to `Category`.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `Category_New` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `color` TEXT NOT NULL
                    )
                """.trimIndent())
                database.execSQL("INSERT INTO `Category_New` (`name`, `color`) SELECT `name`, `color` FROM `Category`")
                database.execSQL("DROP TABLE `Category`")
                database.execSQL("ALTER TABLE `Category_New` RENAME TO `Category`")
            }
        }

        /**
         * Migration from version 3 to 4:
         * Creates the `expenses` table.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `expenses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `description` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `date` TEXT NOT NULL,
                        `paymentMethod` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `imageUri` TEXT
                    )
                """.trimIndent())
            }
        }

        /**
         * Migration from version 4 to 5:
         * Adds a `type` column to the `Category` table with a default value of 'expense'.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `Category` ADD COLUMN `type` TEXT NOT NULL DEFAULT 'expense'")
            }
        }

        /**
         * Migration from version 5 to 6:
         * Drops the old `monthly_goals` table (if it exists) and creates a new one
         * with a foreign key constraint to the `User` table and an index on the `userId`.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `monthly_goals`")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `monthly_goals` (
                        `goalId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `goalSet` INTEGER NOT NULL,
                        `maxGoalAmount` REAL NOT NULL,
                        `minGoalAmount` REAL NOT NULL,
                        `userId` TEXT NOT NULL,
                        FOREIGN KEY(`userId`) REFERENCES `User`(`email`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_monthly_goals_userId`
                    ON `monthly_goals` (`userId`)
                """.trimIndent())
            }
        }

        /**
         * Migration from version 6 to 7:
         * Creates the `income` table with a foreign key constraint to the `User` table
         * and an index on the `userId`.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `income` (
                        `incomeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `description` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `date` TEXT NOT NULL,
                        `paymentMethod` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `imageUri` TEXT,
                        `userId` TEXT NOT NULL,
                        FOREIGN KEY(`userId`) REFERENCES `User`(`email`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_income_userId`
                    ON `income` (`userId`)
                """.trimIndent())
            }
        }

        /**
         * Migration from version 7 to 8:
         * Drops the old `income` table and creates a new one without the `userId` column.
         */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS `income`")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `income` (
                        `incomeId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `description` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `date` TEXT NOT NULL,
                        `paymentMethod` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `imageUri` TEXT
                    )
                """.trimIndent())
            }
        }
    }
}