package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.LoanEntity
import com.example.data.model.RemittanceEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
  fun getAllTransactions(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions WHERE yearMonth = :yearMonth ORDER BY dateMillis DESC")
  fun getTransactionsByMonth(yearMonth: String): Flow<List<TransactionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTransaction(transaction: TransactionEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(transactions: List<TransactionEntity>)

  @Query("DELETE FROM transactions WHERE id = :id")
  suspend fun deleteTransaction(id: Long)

  @Query("DELETE FROM transactions")
  suspend fun deleteAllTransactions()
}

@Dao
interface RemittanceDao {
  @Query("SELECT * FROM remittances ORDER BY dateMillis DESC")
  fun getAllRemittances(): Flow<List<RemittanceEntity>>

  @Query("SELECT * FROM remittances WHERE yearMonth = :yearMonth ORDER BY dateMillis DESC")
  fun getRemittancesByMonth(yearMonth: String): Flow<List<RemittanceEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRemittance(remittance: RemittanceEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(remittances: List<RemittanceEntity>)

  @Query("DELETE FROM remittances WHERE id = :id")
  suspend fun deleteRemittance(id: Long)

  @Query("DELETE FROM remittances")
  suspend fun deleteAllRemittances()
}

@Dao
interface LoanDao {
  @Query("SELECT * FROM loans ORDER BY dateMillis DESC")
  fun getAllLoans(): Flow<List<LoanEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLoan(loan: LoanEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(loans: List<LoanEntity>)

  @Query("UPDATE loans SET isSettled = :isSettled WHERE id = :id")
  suspend fun updateSettled(id: Long, isSettled: Boolean)

  @Query("DELETE FROM loans WHERE id = :id")
  suspend fun deleteLoan(id: Long)

  @Query("DELETE FROM loans")
  suspend fun deleteAllLoans()
}
