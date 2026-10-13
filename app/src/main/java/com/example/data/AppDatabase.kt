package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.LoanDao
import com.example.data.dao.RemittanceDao
import com.example.data.dao.TransactionDao
import com.example.data.model.LoanEntity
import com.example.data.model.RemittanceEntity
import com.example.data.model.TransactionEntity

@Database(
  entities = [
    TransactionEntity::class,
    RemittanceEntity::class,
    LoanEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun transactionDao(): TransactionDao
  abstract fun remittanceDao(): RemittanceDao
  abstract fun loanDao(): LoanDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "amar_hisab_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
