package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.LoanEntity
import com.example.data.model.RemittanceEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(context: Context) {
  private val database = AppDatabase.getDatabase(context)
  private val txDao = database.transactionDao()
  private val remitDao = database.remittanceDao()
  private val loanDao = database.loanDao()

  private val prefs: SharedPreferences =
    context.getSharedPreferences("amar_hisab_prefs", Context.MODE_PRIVATE)

  private val _currentRate = MutableStateFlow(
    prefs.getFloat("sar_to_bdt_rate", 32.50f).toDouble()
  )
  val currentRate: Flow<Double> = _currentRate.asStateFlow()

  val allTransactions: Flow<List<TransactionEntity>> = txDao.getAllTransactions()
  val allRemittances: Flow<List<RemittanceEntity>> = remitDao.getAllRemittances()
  val allLoans: Flow<List<LoanEntity>> = loanDao.getAllLoans()

  fun updateRate(newRate: Double) {
    _currentRate.value = newRate
    prefs.edit().putFloat("sar_to_bdt_rate", newRate.toFloat()).apply()
  }

  suspend fun insertTransaction(tx: TransactionEntity): Long {
    return txDao.insertTransaction(tx)
  }

  suspend fun deleteTransaction(id: Long) {
    txDao.deleteTransaction(id)
  }

  suspend fun insertRemittance(remit: RemittanceEntity): Long {
    return remitDao.insertRemittance(remit)
  }

  suspend fun deleteRemittance(id: Long) {
    remitDao.deleteRemittance(id)
  }

  suspend fun insertLoan(loan: LoanEntity): Long {
    return loanDao.insertLoan(loan)
  }

  suspend fun updateLoanSettled(id: Long, settled: Boolean) {
    loanDao.updateSettled(id, settled)
  }

  suspend fun deleteLoan(id: Long) {
    loanDao.deleteLoan(id)
  }

  suspend fun clearAllData() {
    txDao.deleteAllTransactions()
    remitDao.deleteAllRemittances()
    loanDao.deleteAllLoans()
  }

  suspend fun populateSampleDataIfEmpty() = withContext(Dispatchers.IO) {
    val txCount = txDao.getAllTransactions().first().size
    val remitCount = remitDao.getAllRemittances().first().size
    val loanCount = loanDao.getAllLoans().first().size

    if (txCount == 0 && remitCount == 0 && loanCount == 0) {
      val now = System.currentTimeMillis()
      val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
      val currentMonth = monthFormat.format(Date(now))
      val oneDay = 24L * 60L * 60L * 1000L

      val sampleTransactions = listOf(
        TransactionEntity(
          type = "income",
          category = "মূল বেতন",
          amountSar = 3500.0,
          note = "অক্টোবর মাসের মূল বেতন",
          dateMillis = now - 12 * oneDay,
          yearMonth = currentMonth
        ),
        TransactionEntity(
          type = "income",
          category = "ওভারটাইম",
          amountSar = 450.0,
          note = "শুক্রবার এক্সট্রা ডিউটি",
          dateMillis = now - 7 * oneDay,
          yearMonth = currentMonth
        ),
        TransactionEntity(
          type = "expense",
          category = "রুম ভাড়া",
          amountSar = 450.0,
          note = "বাতা রুমের চলতি মাসের ভাড়া",
          dateMillis = now - 10 * oneDay,
          yearMonth = currentMonth
        ),
        TransactionEntity(
          type = "expense",
          category = "খাবার / মেস খরচ",
          amountSar = 350.0,
          note = "মেসের খাবার মিল ও বাজার",
          dateMillis = now - 5 * oneDay,
          yearMonth = currentMonth
        ),
        TransactionEntity(
          type = "expense",
          category = "মোবাইল / ইন্টারনেট",
          amountSar = 115.0,
          note = "STC মাসিক আনলিমিটেড প্যাকেজ",
          dateMillis = now - 4 * oneDay,
          yearMonth = currentMonth
        ),
        TransactionEntity(
          type = "expense",
          category = "আল-বাইক / রেস্তোরাঁ",
          amountSar = 32.0,
          note = "ছুটির দিনে আল-বাইক নাস্তা",
          dateMillis = now - 2 * oneDay,
          yearMonth = currentMonth
        )
      )
      txDao.insertAll(sampleTransactions)

      val sampleRemittances = listOf(
        RemittanceEntity(
          receiverName = "মা",
          method = "বিকাশ (bKash)",
          amountSar = 1500.0,
          feeSar = 15.0,
          hasBonus = true,
          rate = 32.50,
          totalBdt = (1500.0 * 32.50) * 1.025, // 49,968.75 BDT
          note = "সংসার খরচের জন্য বিকাশ পাঠানো",
          dateMillis = now - 8 * oneDay,
          yearMonth = currentMonth
        ),
        RemittanceEntity(
          receiverName = "স্ত্রী (সেভিংস)",
          method = "ব্যাংক একাউন্ট",
          amountSar = 800.0,
          feeSar = 15.0,
          hasBonus = true,
          rate = 32.50,
          totalBdt = (800.0 * 32.50) * 1.025, // 26,650 BDT
          note = "ইসলামী ব্যাংক সঞ্চয় একাউন্ট",
          dateMillis = now - 3 * oneDay,
          yearMonth = currentMonth
        )
      )
      remitDao.insertAll(sampleRemittances)

      val sampleLoans = listOf(
        LoanEntity(
          personName = "রফিক ভাই (রুমমেট)",
          type = "given", // আমি দিয়েছি (পাবো)
          amountSar = 200.0,
          note = "জরুরি খরচে ধার নিয়েছে, আগামী মাসে দিবে",
          isSettled = false,
          dateMillis = now - 6 * oneDay
        ),
        LoanEntity(
          personName = "করিম কাকা (দোকান)",
          type = "taken", // আমি নিয়েছি (দিতে হবে)
          amountSar = 100.0,
          note = "বাকালা মুদির দোকান বাকি",
          isSettled = false,
          dateMillis = now - 1 * oneDay
        )
      )
      loanDao.insertAll(sampleLoans)
    }
  }

  suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
    val txList = txDao.getAllTransactions().first()
    val remitList = remitDao.getAllRemittances().first()
    val loanList = loanDao.getAllLoans().first()

    val root = JSONObject()
    root.put("version", 2)
    root.put("app", "AmarHisab")
    root.put("sarRate", _currentRate.value)
    root.put("exportTime", System.currentTimeMillis())

    val txArray = JSONArray()
    for (t in txList) {
      val obj = JSONObject()
      obj.put("type", t.type)
      obj.put("category", t.category)
      obj.put("amountSar", t.amountSar)
      obj.put("note", t.note)
      obj.put("dateMillis", t.dateMillis)
      obj.put("yearMonth", t.yearMonth)
      txArray.put(obj)
    }
    root.put("transactions", txArray)

    val remitArray = JSONArray()
    for (r in remitList) {
      val obj = JSONObject()
      obj.put("receiverName", r.receiverName)
      obj.put("method", r.method)
      obj.put("amountSar", r.amountSar)
      obj.put("feeSar", r.feeSar)
      obj.put("hasBonus", r.hasBonus)
      obj.put("rate", r.rate)
      obj.put("totalBdt", r.totalBdt)
      obj.put("note", r.note)
      obj.put("dateMillis", r.dateMillis)
      obj.put("yearMonth", r.yearMonth)
      remitArray.put(obj)
    }
    root.put("remittances", remitArray)

    val loanArray = JSONArray()
    for (l in loanList) {
      val obj = JSONObject()
      obj.put("personName", l.personName)
      obj.put("type", l.type)
      obj.put("amountSar", l.amountSar)
      obj.put("note", l.note)
      obj.put("isSettled", l.isSettled)
      obj.put("dateMillis", l.dateMillis)
      loanArray.put(obj)
    }
    root.put("loans", loanArray)

    root.toString(2)
  }

  suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val root = JSONObject(jsonString)
      val importedRate = root.optDouble("sarRate", 32.50)
      updateRate(importedRate)

      val txArray = root.optJSONArray("transactions")
      if (txArray != null) {
        val newTx = mutableListOf<TransactionEntity>()
        for (i in 0 until txArray.length()) {
          val obj = txArray.getJSONObject(i)
          newTx.add(
            TransactionEntity(
              type = obj.getString("type"),
              category = obj.getString("category"),
              amountSar = obj.getDouble("amountSar"),
              note = obj.optString("note", ""),
              dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
              yearMonth = obj.optString("yearMonth", "2026-10")
            )
          )
        }
        if (newTx.isNotEmpty()) {
          txDao.deleteAllTransactions()
          txDao.insertAll(newTx)
        }
      }

      val remitArray = root.optJSONArray("remittances")
      if (remitArray != null) {
        val newRemits = mutableListOf<RemittanceEntity>()
        for (i in 0 until remitArray.length()) {
          val obj = remitArray.getJSONObject(i)
          newRemits.add(
            RemittanceEntity(
              receiverName = obj.getString("receiverName"),
              method = obj.getString("method"),
              amountSar = obj.getDouble("amountSar"),
              feeSar = obj.optDouble("feeSar", 15.0),
              hasBonus = obj.optBoolean("hasBonus", true),
              rate = obj.optDouble("rate", importedRate),
              totalBdt = obj.optDouble("totalBdt", 0.0),
              note = obj.optString("note", ""),
              dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
              yearMonth = obj.optString("yearMonth", "2026-10")
            )
          )
        }
        if (newRemits.isNotEmpty()) {
          remitDao.deleteAllRemittances()
          remitDao.insertAll(newRemits)
        }
      }

      val loanArray = root.optJSONArray("loans")
      if (loanArray != null) {
        val newLoans = mutableListOf<LoanEntity>()
        for (i in 0 until loanArray.length()) {
          val obj = loanArray.getJSONObject(i)
          newLoans.add(
            LoanEntity(
              personName = obj.getString("personName"),
              type = obj.getString("type"),
              amountSar = obj.getDouble("amountSar"),
              note = obj.optString("note", ""),
              isSettled = obj.optBoolean("isSettled", false),
              dateMillis = obj.optLong("dateMillis", System.currentTimeMillis())
            )
          )
        }
        if (newLoans.isNotEmpty()) {
          loanDao.deleteAllLoans()
          loanDao.insertAll(newLoans)
        }
      }
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }
}
