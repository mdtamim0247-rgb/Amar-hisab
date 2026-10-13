package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val type: String, // "income" or "expense"
  val category: String,
  val amountSar: Double,
  val note: String,
  val dateMillis: Long,
  val yearMonth: String // e.g. "2026-10"
)

@Entity(tableName = "remittances")
data class RemittanceEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val receiverName: String,
  val method: String,
  val amountSar: Double,
  val feeSar: Double,
  val hasBonus: Boolean, // 2.5% Bangladesh Govt remittance incentive
  val rate: Double,
  val totalBdt: Double,
  val note: String,
  val dateMillis: Long,
  val yearMonth: String
)

@Entity(tableName = "loans")
data class LoanEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val personName: String,
  val type: String, // "given" (পাওনা - I will receive) or "taken" (দেনা - I must pay)
  val amountSar: Double,
  val note: String,
  val isSettled: Boolean,
  val dateMillis: Long
)
