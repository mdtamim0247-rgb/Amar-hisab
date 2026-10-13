package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.model.LoanEntity
import com.example.data.model.RemittanceEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SummaryUiState(
  val netBalanceSar: Double = 0.0,
  val netBalanceBdt: Double = 0.0,
  val totalIncomeSar: Double = 0.0,
  val totalExpenseSar: Double = 0.0,
  val totalRemitSar: Double = 0.0,
  val totalRemitBdt: Double = 0.0,
  val loanGivenPendingSar: Double = 0.0, // মানুষের কাছে পাবো
  val loanTakenPendingSar: Double = 0.0  // মানুষকে দিতে হবে
)

class AmarHisabViewModel(application: Application) : AndroidViewModel(application) {
  val repository = AppRepository(application)

  private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
  private val currentMonthStr = monthFormat.format(Date())

  private val _selectedMonth = MutableStateFlow(currentMonthStr)
  val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

  val currentRate: StateFlow<Double> = repository.currentRate.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = 32.50
  )

  val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val allRemittances: StateFlow<List<RemittanceEntity>> = repository.allRemittances.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val allLoans: StateFlow<List<LoanEntity>> = repository.allLoans.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  private val _userMessage = MutableSharedFlow<String>()
  val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

  init {
    viewModelScope.launch {
      repository.populateSampleDataIfEmpty()
    }
  }

  // Filtered transactions based on selected month
  val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
    allTransactions,
    _selectedMonth
  ) { list, month ->
    if (month == "ALL") list else list.filter { it.yearMonth == month }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Filtered remittances based on selected month
  val filteredRemittances: StateFlow<List<RemittanceEntity>> = combine(
    allRemittances,
    _selectedMonth
  ) { list, month ->
    if (month == "ALL") list else list.filter { it.yearMonth == month }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Available months list for picker
  val availableMonths: StateFlow<List<String>> = combine(
    allTransactions,
    allRemittances
  ) { txs, remits ->
    val set = mutableSetOf(currentMonthStr)
    txs.forEach { set.add(it.yearMonth) }
    remits.forEach { set.add(it.yearMonth) }
    set.sortedDescending()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(currentMonthStr))

  // Monthly spending trends for charts
  val monthlySpendingTrends: StateFlow<List<com.example.ui.components.MonthTrendData>> = allTransactions
    .map { txList ->
      val expenses = txList.filter { it.type == "expense" }
      val grouped = expenses.groupBy { it.yearMonth }
      val sortedMonths = grouped.keys.sorted()
      val displayMonths = if (sortedMonths.size > 5) sortedMonths.takeLast(5) else sortedMonths
      displayMonths.map { ym ->
        val monthTxs = grouped[ym] ?: emptyList()
        val total = monthTxs.sumOf { it.amountSar }
        val catMap = monthTxs.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { tx -> tx.amountSar } }
        com.example.ui.components.MonthTrendData(
          yearMonth = ym,
          label = com.example.ui.util.Formatters.formatMonthLabel(ym).substringBefore(" "),
          totalExpenseSar = total,
          categoryAmounts = catMap
        )
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Real-time summary figures
  val summaryState: StateFlow<SummaryUiState> = combine(
    filteredTransactions,
    filteredRemittances,
    allLoans,
    currentRate
  ) { txs, remits, loans, rate ->
    var incomeSar = 0.0
    var expenseSar = 0.0
    for (t in txs) {
      if (t.type == "income") {
        incomeSar += t.amountSar
      } else {
        expenseSar += t.amountSar
      }
    }

    var remitSar = 0.0
    var remitBdt = 0.0
    for (r in remits) {
      remitSar += (r.amountSar + r.feeSar)
      remitBdt += r.totalBdt
    }

    val netSar = incomeSar - expenseSar - remitSar
    val netBdt = netSar * rate

    var loanGiven = 0.0
    var loanTaken = 0.0
    for (l in loans) {
      if (!l.isSettled) {
        if (l.type == "given") {
          loanGiven += l.amountSar
        } else {
          loanTaken += l.amountSar
        }
      }
    }

    SummaryUiState(
      netBalanceSar = netSar,
      netBalanceBdt = netBdt,
      totalIncomeSar = incomeSar,
      totalExpenseSar = expenseSar,
      totalRemitSar = remitSar,
      totalRemitBdt = remitBdt,
      loanGivenPendingSar = loanGiven,
      loanTakenPendingSar = loanTaken
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryUiState())

  fun setMonth(month: String) {
    _selectedMonth.value = month
  }

  fun updateRate(newRate: Double) {
    if (newRate > 0) {
      repository.updateRate(newRate)
      emitMessage("আজকের রেট আপডেট হয়েছে: ১ SAR = $newRate ৳")
    }
  }

  fun addTransaction(
    type: String,
    category: String,
    amountSar: Double,
    note: String,
    customDateMillis: Long? = null
  ) {
    viewModelScope.launch {
      val date = customDateMillis ?: System.currentTimeMillis()
      val yMonth = monthFormat.format(Date(date))
      val tx = TransactionEntity(
        type = type,
        category = category,
        amountSar = amountSar,
        note = note,
        dateMillis = date,
        yearMonth = yMonth
      )
      repository.insertTransaction(tx)
      emitMessage(if (type == "income") "আয় যোগ করা হয়েছে" else "খরচ যোগ করা হয়েছে")
    }
  }

  fun deleteTransaction(id: Long) {
    viewModelScope.launch {
      repository.deleteTransaction(id)
      emitMessage("লেনদেন মুছে ফেলা হয়েছে")
    }
  }

  fun addRemittance(
    receiverName: String,
    method: String,
    amountSar: Double,
    feeSar: Double,
    hasBonus: Boolean,
    note: String,
    customDateMillis: Long? = null
  ) {
    viewModelScope.launch {
      val date = customDateMillis ?: System.currentTimeMillis()
      val yMonth = monthFormat.format(Date(date))
      val rate = currentRate.value
      val bonusMultiplier = if (hasBonus) 1.025 else 1.0
      val totalBdt = (amountSar * rate) * bonusMultiplier

      val remit = RemittanceEntity(
        receiverName = receiverName,
        method = method,
        amountSar = amountSar,
        feeSar = feeSar,
        hasBonus = hasBonus,
        rate = rate,
        totalBdt = totalBdt,
        note = note,
        dateMillis = date,
        yearMonth = yMonth
      )
      repository.insertRemittance(remit)
      emitMessage("রেমিট্যান্স রেকর্ড সংরক্ষিত হয়েছে")
    }
  }

  fun deleteRemittance(id: Long) {
    viewModelScope.launch {
      repository.deleteRemittance(id)
      emitMessage("রেমিট্যান্স রেকর্ড মুছে ফেলা হয়েছে")
    }
  }

  fun addLoan(
    personName: String,
    type: String,
    amountSar: Double,
    note: String
  ) {
    viewModelScope.launch {
      val loan = LoanEntity(
        personName = personName,
        type = type,
        amountSar = amountSar,
        note = note,
        isSettled = false,
        dateMillis = System.currentTimeMillis()
      )
      repository.insertLoan(loan)
      emitMessage("ধার/দেনার হিসাব যোগ করা হয়েছে")
    }
  }

  fun toggleLoanSettled(id: Long, currentSettled: Boolean) {
    viewModelScope.launch {
      repository.updateLoanSettled(id, !currentSettled)
      emitMessage(if (!currentSettled) "হিসাব পরিশোধিত চিহ্নিত হয়েছে" else "হিসাব বাকি হিসেবে চিহ্নিত হয়েছে")
    }
  }

  fun deleteLoan(id: Long) {
    viewModelScope.launch {
      repository.deleteLoan(id)
      emitMessage("ধারের রেকর্ড মুছে ফেলা হয়েছে")
    }
  }

  fun resetToSampleData() {
    viewModelScope.launch {
      repository.clearAllData()
      repository.populateSampleDataIfEmpty()
      emitMessage("নমুনা ডাটা পুনরায় লোড করা হয়েছে")
    }
  }

  fun importBackup(jsonStr: String) {
    viewModelScope.launch {
      val success = repository.importBackupJson(jsonStr)
      if (success) {
        emitMessage("ডাটা সফলভাবে রিস্টোর হয়েছে!")
      } else {
        emitMessage("ভুল ফাইল ফরম্যাট! রিস্টোর ব্যর্থ হয়েছে")
      }
    }
  }

  suspend fun getExportJson(): String {
    return repository.exportBackupJson()
  }

  fun generateWhatsAppReport(): String {
    val summary = summaryState.value
    val month = selectedMonth.value
    val txs = filteredTransactions.value
    val remits = filteredRemittances.value
    val rate = currentRate.value

    val sb = StringBuilder()
    sb.append("📋 *আমার হিসাব - সৌদি প্রবাসী ফাইন্যান্স রিপোর্ট*\n")
    sb.append("📅 মাস: $month | আজকের রেট: ১ SAR = $rate ৳\n")
    sb.append("───────────────────────\n")
    sb.append("💰 *মোট আয়/বেতন:* ${"%.2f".format(summary.totalIncomeSar)} SAR (≈ ${"%,.0f".format(summary.totalIncomeSar * rate)} ৳)\n")
    sb.append("💸 *মোট খরচ:* ${"%.2f".format(summary.totalExpenseSar)} SAR (≈ ${"%,.0f".format(summary.totalExpenseSar * rate)} ৳)\n")
    sb.append("🇧🇩 *দেশে পাঠানো:* ${"%.2f".format(summary.totalRemitSar)} SAR (পাবে ≈ ${"%,.0f".format(summary.totalRemitBdt)} ৳)\n")
    sb.append("───────────────────────\n")
    sb.append("💵 *হাতে অবশিষ্ট ব্যালেন্স:* ${"%.2f".format(summary.netBalanceSar)} SAR (≈ ${"%,.0f".format(summary.netBalanceBdt)} ৳)\n")

    if (remits.isNotEmpty()) {
      sb.append("\n✈️ *দেশে পাঠানো বিস্তারিত:*\n")
      remits.forEach { r ->
        sb.append("• ${r.receiverName} (${r.method}): ${"%.2f".format(r.amountSar)} SAR (ফি ${"%.0f".format(r.feeSar)} SAR) ➔ ${"%,.0f".format(r.totalBdt)} ৳\n")
      }
    }

    if (txs.any { it.type == "expense" }) {
      sb.append("\n🛒 *প্রধান খরচসমূহ:*\n")
      val topExpenses = txs.filter { it.type == "expense" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amountSar } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

      topExpenses.forEach { (cat, amt) ->
        sb.append("• $cat: ${"%.2f".format(amt)} SAR\n")
      }
    }

    if (summary.loanGivenPendingSar > 0 || summary.loanTakenPendingSar > 0) {
      sb.append("\n🤝 *ধার-দেনা অবস্থা:*\n")
      if (summary.loanGivenPendingSar > 0) {
        sb.append("• মানুষের কাছে পাওনা: ${"%.2f".format(summary.loanGivenPendingSar)} SAR\n")
      }
      if (summary.loanTakenPendingSar > 0) {
        sb.append("• মানুষকে দেওয়ার বাকি: ${"%.2f".format(summary.loanTakenPendingSar)} SAR\n")
      }
    }

    sb.append("\n_আমার হিসাব অ্যাপের মাধ্যমে তৈরিকৃত_ 🇸🇦 🇧🇩")
    return sb.toString()
  }

  private val aiService = com.example.data.ai.AiFinancialAdvisorService()

  private val _aiAdviceText = MutableStateFlow("")
  val aiAdviceText: StateFlow<String> = _aiAdviceText.asStateFlow()

  private val _isAiAdviceLoading = MutableStateFlow(false)
  val isAiAdviceLoading: StateFlow<Boolean> = _isAiAdviceLoading.asStateFlow()

  private val _smartParsedEntry = MutableStateFlow<com.example.data.ai.SmartParsedEntry?>(null)
  val smartParsedEntry: StateFlow<com.example.data.ai.SmartParsedEntry?> = _smartParsedEntry.asStateFlow()

  private val _isSmartParsing = MutableStateFlow(false)
  val isSmartParsing: StateFlow<Boolean> = _isSmartParsing.asStateFlow()

  private val _aiAnswerText = MutableStateFlow("")
  val aiAnswerText: StateFlow<String> = _aiAnswerText.asStateFlow()

  private val _isAiAnswerLoading = MutableStateFlow(false)
  val isAiAnswerLoading: StateFlow<Boolean> = _isAiAnswerLoading.asStateFlow()

  fun requestAiAdvice() {
    viewModelScope.launch {
      _isAiAdviceLoading.value = true
      _aiAdviceText.value = ""
      val summary = summaryState.value
      val txs = filteredTransactions.value
      val topExpenses = txs.filter { it.type == "expense" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amountSar } }
        .toList()
        .sortedByDescending { it.second }
      val remits = filteredRemittances.value
      val rate = currentRate.value

      try {
        aiService.generateFinancialAdviceStream(summary, topExpenses, rate, remits).collect { chunk ->
          _aiAdviceText.value += chunk
        }
      } catch (e: Exception) {
        _aiAdviceText.value = "পরামর্শ আনতে ব্যর্থ হয়েছে: ${e.localizedMessage}"
      } finally {
        _isAiAdviceLoading.value = false
      }
    }
  }

  fun parseNaturalLanguage(input: String) {
    if (input.isBlank()) return
    viewModelScope.launch {
      _isSmartParsing.value = true
      _smartParsedEntry.value = null
      try {
        val result = aiService.parseNaturalLanguageInput(input)
        _smartParsedEntry.value = result
        if (result != null) {
          emitMessage("এআই হিসাব বিশ্লেষণ সম্পন্ন করেছে")
        } else {
          emitMessage("তথ্য পাওয়া যায়নি। অনুগ্রহ করে আবার স্পষ্ট করে লিখুন।")
        }
      } catch (e: Exception) {
        emitMessage("ত্রুটি: ${e.localizedMessage}")
      } finally {
        _isSmartParsing.value = false
      }
    }
  }

  fun saveParsedEntry(entry: com.example.data.ai.SmartParsedEntry) {
    when (entry.entryType) {
      "expense", "income" -> {
        addTransaction(
          type = entry.entryType,
          category = entry.categoryOrMethod,
          amountSar = entry.amountSar,
          note = entry.note
        )
      }
      "remittance" -> {
        addRemittance(
          receiverName = if (entry.personOrReceiver.isNotBlank()) entry.personOrReceiver else "পরিবার",
          method = if (entry.categoryOrMethod.isNotBlank()) entry.categoryOrMethod else "বিকাশ (bKash)",
          amountSar = entry.amountSar,
          feeSar = 15.0,
          hasBonus = true,
          note = entry.note
        )
      }
      "loan" -> {
        addLoan(
          personName = if (entry.personOrReceiver.isNotBlank()) entry.personOrReceiver else "পরিচিত",
          type = if (entry.categoryOrMethod.contains("পাওনা") || entry.categoryOrMethod.contains("given")) "given" else "taken",
          amountSar = entry.amountSar,
          note = entry.note
        )
      }
    }
    _smartParsedEntry.value = null
  }

  fun clearParsedEntry() {
    _smartParsedEntry.value = null
  }

  fun askAiQuestion(question: String) {
    if (question.isBlank()) return
    viewModelScope.launch {
      _isAiAnswerLoading.value = true
      _aiAnswerText.value = ""
      try {
        aiService.askFinancialQuestionStream(question).collect { chunk ->
          _aiAnswerText.value += chunk
        }
      } catch (e: Exception) {
        _aiAnswerText.value = "উত্তর প্রদান ব্যর্থ: ${e.localizedMessage}"
      } finally {
        _isAiAnswerLoading.value = false
      }
    }
  }

  private fun emitMessage(msg: String) {
    viewModelScope.launch {
      _userMessage.emit(msg)
    }
  }
}
