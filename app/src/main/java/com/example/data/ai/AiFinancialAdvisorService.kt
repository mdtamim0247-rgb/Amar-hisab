package com.example.data.ai

import android.util.Log
import com.example.data.model.RemittanceEntity
import com.example.ui.SummaryUiState
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiFinancialAdvisor"

data class SmartParsedEntry(
  val entryType: String, // "expense", "income", "remittance", "loan"
  val categoryOrMethod: String,
  val amountSar: Double,
  val note: String,
  val personOrReceiver: String = ""
)

class AiFinancialAdvisorService {
  // General text generation model
  private val textModel = Firebase.ai.generativeModel(
    modelName = "gemini-3.5-flash"
  )

  // Structured schema for natural language smart parser
  private val parseSchema = Schema.obj(
    properties = mapOf(
      "entryType" to Schema.enumeration(
        values = listOf("expense", "income", "remittance", "loan"),
        description = "Type of financial entry: expense, income, remittance, or loan"
      ),
      "categoryOrMethod" to Schema.string(
        description = "For expense: category like খাবার / মেস খরচ, রুম ভাড়া, আকামা ফি, মোবাইল, আল-বাইক, যাতায়াত, চিকিৎসা, শপিং, অন্যান্য. For income: মূল বেতন, ওভারটাইম, বোনাস, ইত্যাদি. For remittance: বিকাশ, ব্যাংক একাউন্ট, নগদ, ক্যাশ পিকআপ. For loan: পাওনা or দেনা"
      ),
      "amountSar" to Schema.double(
        description = "Amount in Saudi Riyals (SAR)"
      ),
      "note" to Schema.string(
        description = "Brief note or description in Bengali"
      ),
      "personOrReceiver" to Schema.string(
        description = "Name of the person (for loan) or receiver (for remittance like মা, স্ত্রী, রফিক ভাই), or empty",
        nullable = true
      )
    ),
    optionalProperties = listOf("personOrReceiver")
  )

  private val parserModel = Firebase.ai.generativeModel(
    modelName = "gemini-3.5-flash",
    generationConfig = generationConfig {
      responseMimeType = "application/json"
      responseSchema = parseSchema
    }
  )

  /**
   * Generates comprehensive, practical financial advice for Saudi expats in Bengali.
   */
  fun generateFinancialAdviceStream(
    summary: SummaryUiState,
    topExpenses: List<Pair<String, Double>>,
    currentRate: Double,
    recentRemittances: List<RemittanceEntity>
  ): Flow<String> = flow {
    val promptBuilder = StringBuilder()
    promptBuilder.append("তুমি একজন অভিজ্ঞ প্রবাসী ফাইন্যান্স এক্সপার্ট। সৌদি আরবে বসবাসরত বাংলাদেশি প্রবাসীদের জীবনযাত্রা, খরচ ও রেমিট্যান্স পাঠানোর বাস্তব পরিস্থিতি বিবেচনা করে নিচের ডেটার ওপর বাস্তবমুখী পরামর্শ দাও।\n\n")
    promptBuilder.append("📊 বর্তমান আর্থিক অবস্থা (চলতি মাস):\n")
    promptBuilder.append("- মূল আয়/বেতন: ${"%.2f".format(summary.totalIncomeSar)} SAR\n")
    promptBuilder.append("- মোট জীবনযাত্রার খরচ: ${"%.2f".format(summary.totalExpenseSar)} SAR\n")
    promptBuilder.append("- দেশে পাঠানো রেমিট্যান্স: ${"%.2f".format(summary.totalRemitSar)} SAR\n")
    promptBuilder.append("- হাতে অবশিষ্ট ব্যালেন্স: ${"%.2f".format(summary.netBalanceSar)} SAR (≈ ${"%,.0f".format(summary.netBalanceBdt)} ৳)\n")
    promptBuilder.append("- বর্তমান রেট: ১ SAR = $currentRate ৳\n\n")

    if (topExpenses.isNotEmpty()) {
      promptBuilder.append("🛒 প্রধান খরচের খাতসমূহ:\n")
      topExpenses.forEach { (cat, amt) ->
        promptBuilder.append("• $cat: ${"%.2f".format(amt)} SAR\n")
      }
      promptBuilder.append("\n")
    }

    if (recentRemittances.isNotEmpty()) {
      promptBuilder.append("🇧🇩 দেশে পাঠানো বিস্তারিত:\n")
      recentRemittances.forEach { r ->
        promptBuilder.append("• ${r.receiverName} (${r.method}): ${"%.2f".format(r.amountSar)} SAR (ফি ${r.feeSar} SAR) ➔ ${"%,.0f".format(r.totalBdt)} ৳\n")
      }
      promptBuilder.append("\n")
    }

    promptBuilder.append("পরামর্শ প্রদানের নির্দেশনাবলী:\n")
    promptBuilder.append("১. খরচ কমানোর সুনির্দিষ্ট কৌশল (মেস খাবার, আল-বাইক/বাইরের খাবার, মোবাইল প্যাকেজ ইত্যাদি ক্ষেত্রে)।\n")
    promptBuilder.append("২. দেশে টাকা পাঠানোর স্মার্ট নিয়ম (সরকারি ২.৫% প্রণোদনা, সঠিক চ্যানেল ও কম ফিতে পাঠানো)।\n")
    promptBuilder.append("৩. আপদকালীন তহবিল (Emergency Fund) এবং আকামা নবায়নের জন্য সেভিংসের পরিকল্পনা।\n")
    promptBuilder.append("৪. সহজ ও উৎসাহব্যঞ্জক বাংলা ভাষায় পয়েন্ট আকারে উপস্থাপন করো।")

    try {
      withTimeout(60_000L) {
        textModel.generateContentStream(promptBuilder.toString()).collect { chunk ->
          val chunkText = chunk.text.orEmpty()
          if (chunkText.isNotEmpty()) {
            emit(chunkText)
          }
        }
      }
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "Financial advice generation timed out", e)
      emit("\n⚠️ অনুরোধটির সময় শেষ হয়ে গেছে (Timeout)। অনুগ্রহ করে পুনরায় চেষ্টা করুন।")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Financial advice failed", e)
      emit("\n⚠️ ত্রুটি: ${e.localizedMessage ?: "পরামর্শ লোড করা সম্ভব হয়নি।"}")
    }
  }

  /**
   * Smart parser: Takes natural Bengali/Arabic/English input and outputs structured financial entry.
   */
  suspend fun parseNaturalLanguageInput(userInput: String): SmartParsedEntry? {
    val prompt = "Analyze this text and extract financial record information:\n\"$userInput\""
    return try {
      val response = withTimeout(60_000L) {
        parserModel.generateContent(prompt)
      }
      val jsonStr = response.text ?: return null
      val json = JSONObject(jsonStr)

      SmartParsedEntry(
        entryType = json.optString("entryType", "expense"),
        categoryOrMethod = json.optString("categoryOrMethod", "অন্যান্য খরচ"),
        amountSar = json.optDouble("amountSar", 0.0),
        note = json.optString("note", userInput),
        personOrReceiver = json.optString("personOrReceiver", "")
      )
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "Natural language parsing timed out", e)
      null
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Failed to parse natural language input", e)
      null
    }
  }

  /**
   * Financial Q&A with Gemini
   */
  fun askFinancialQuestionStream(question: String): Flow<String> = flow {
    val prompt = """
      তুমি সৌদি প্রবাসী বাংলাদেশিদের জন্য একজন বিশ্বস্ত ফাইন্যান্সিয়াল গাইড।
      প্রবাসী ব্যবহারকারীর নিচের প্রশ্নটির সহজ, তথ্যবহুল ও নির্ভুল সমাধান দাও।
      
      প্রশ্ন: "$question"
      
      উত্তরে প্রয়োজনীয় হিসাব, সৌদি রিয়াল ও বাংলাদেশি টাকার তথ্য এবং সতর্কতামূলক টিপস অন্তর্ভুক্ত করো।
    """.trimIndent()

    try {
      withTimeout(60_000L) {
        textModel.generateContentStream(prompt).collect { chunk ->
          val chunkText = chunk.text.orEmpty()
          if (chunkText.isNotEmpty()) {
            emit(chunkText)
          }
        }
      }
    } catch (e: TimeoutCancellationException) {
      Log.e(TAG, "Question stream timed out", e)
      emit("\n⚠️ অনুরোধের সময় শেষ হয়ে গেছে। আবার চেষ্টা করুন।")
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Q&A failed", e)
      emit("\n⚠️ ত্রুটি: ${e.localizedMessage ?: "উত্তর দেওয়া সম্ভব হয়নি।"}")
    }
  }
}
