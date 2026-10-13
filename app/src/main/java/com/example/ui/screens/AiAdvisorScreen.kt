package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sparkles
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.SmartParsedEntry
import com.example.ui.AmarHisabViewModel
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoseExpense
import com.example.ui.theme.SkyAccent
import com.example.ui.util.Formatters

@Composable
fun AiAdvisorScreen(
  viewModel: AmarHisabViewModel,
  aiAdviceText: String,
  isAiAdviceLoading: Boolean,
  smartParsedEntry: SmartParsedEntry?,
  isSmartParsing: Boolean,
  aiAnswerText: String,
  isAiAnswerLoading: Boolean,
  currentRate: Double,
  modifier: Modifier = Modifier
) {
  var naturalInputText by remember { mutableStateOf("") }
  var questionInputText by remember { mutableStateOf("") }

  val samplePrompts = listOf(
    "আল-বাইক খেলাম ৩৫ রিয়াল",
    "মাকে বিকাশে ১৫০০ রিয়াল পাঠালাম",
    "রফিক ভাইকে ২০০ রিয়াল ধার দিয়েছি",
    "শুক্রবার ওভারটাইম পেয়েছি ৪৫০ রিয়াল",
    "এসটিসি ইন্টারনেট প্যাকেজ ১১৫ রিয়াল"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Natural Language Smart AI Parser Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ai_smart_entry_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(EmeraldPrimary.copy(alpha = 0.35f))
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(EmeraldPrimary.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "স্মার্ট এআই অটো-এন্ট্রি",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = EmeraldPrimary
                )
              )
              Text(
                text = "বাংলায় যেকোনো হিসাব লিখুন, এআই স্বয়ংক্রিয়ভাবে ফর্ম পূরণ করবে",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFF94A3B8),
                  fontSize = 11.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = naturalInputText,
            onValueChange = { naturalInputText = it },
            placeholder = { Text("যেমন: মাকে বিকাশে ১৫০০ রিয়াল পাঠালাম", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("ai_natural_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF0F172A),
              unfocusedContainerColor = Color(0xFF0F172A),
              focusedBorderColor = EmeraldPrimary,
              unfocusedBorderColor = Color(0xFF334155),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Example quick chips
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(samplePrompts) { prompt ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF0F172A))
                  .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                  .clickable { naturalInputText = prompt }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = prompt,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFFCBD5E1),
                    fontSize = 10.sp
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = { viewModel.parseNaturalLanguage(naturalInputText.trim()) },
            enabled = naturalInputText.isNotBlank() && !isSmartParsing,
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("ai_parse_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
          ) {
            if (isSmartParsing) {
              CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("এআই বিশ্লেষণ করছে...", fontSize = 12.sp)
            } else {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("হিসাব বিশ্লেষণ করুন (Gemini AI)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          // Parsed result preview card
          AnimatedVisibility(visible = smartParsedEntry != null) {
            smartParsedEntry?.let { entry ->
              Spacer(modifier = Modifier.height(12.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color(0xFF0F172A))
                  .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                  .padding(14.dp)
              ) {
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "শনাক্তকৃত হিসাব:",
                      style = MaterialTheme.typography.labelMedium.copy(
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                      )
                    )
                    IconButton(
                      onClick = { viewModel.clearParsedEntry() },
                      modifier = Modifier.size(24.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "মুছে ফেলুন",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(6.dp))

                  val typeText = when (entry.entryType) {
                    "income" -> "আয় / বেতন"
                    "remittance" -> "রেমিট্যান্স"
                    "loan" -> "ধার-দেনা"
                    else -> "খরচ"
                  }

                  Text("• ধরন: $typeText", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                  Text("• খাত/মাধ্যম: ${entry.categoryOrMethod}", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                  Text("• পরিমাণ: ${"%.2f".format(entry.amountSar)} SAR (≈ ${Formatters.formatBdt(entry.amountSar * currentRate)})", style = MaterialTheme.typography.bodySmall.copy(color = EmeraldPrimary, fontWeight = FontWeight.Bold))
                  if (entry.personOrReceiver.isNotBlank()) {
                    Text("• প্রাপক/ব্যক্তি: ${entry.personOrReceiver}", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                  }
                  if (entry.note.isNotBlank()) {
                    Text("• নোট: ${entry.note}", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  Button(
                    onClick = {
                      viewModel.saveParsedEntry(entry)
                      naturalInputText = ""
                    },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(40.dp)
                      .testTag("ai_confirm_save_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                  ) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = Color.Black,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("হিসাবে যুক্ত করুন", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }
      }
    }

    // 2. Gemini Financial Health & Budget Advice Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ai_advice_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(GoldAccent.copy(alpha = 0.35f))
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                .background(GoldAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Lightbulb,
                  contentDescription = null,
                  tint = GoldAccent,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "স্মার্ট প্রবাসী ফাইন্যান্স গাইড",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                  )
                )
                Text(
                  text = "আপনার চলতি মাসের আয়, খরচ ও রেমিট্যান্সের এআই বিশ্লেষণ",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = { viewModel.requestAiAdvice() },
            enabled = !isAiAdviceLoading,
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("ai_get_advice_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
          ) {
            if (isAiAdviceLoading) {
              CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("এআই পরামর্শ তৈরি করছে...", fontSize = 12.sp)
            } else {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("আমার চলতি মাসের এআই পরামর্শ দেখুন", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          // Advice Text Output Box
          if (aiAdviceText.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A))
                .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                .padding(14.dp)
            ) {
              Text(
                text = aiAdviceText,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color(0xFFE2E8F0),
                  lineHeight = 22.sp,
                  fontSize = 12.sp
                )
              )
            }
          }
        }
      }
    }

    // 3. Expat Financial Q&A Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ai_qa_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(SkyAccent.copy(alpha = 0.35f))
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SkyAccent.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.QuestionAnswer,
                contentDescription = null,
                tint = SkyAccent,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "প্রবাসী ফাইন্যান্স জিজ্ঞাসা",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = SkyAccent
                )
              )
              Text(
                text = "সৌদি খরচ, আকামা নবায়ন, ব্যাংকিং বা সঞ্চয় সংক্রান্ত যেকোনো প্রশ্ন",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFF94A3B8),
                  fontSize = 11.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = questionInputText,
              onValueChange = { questionInputText = it },
              placeholder = { Text("যেমন: আকামা নবায়ন করতে কত টাকা সঞ্চয় করা উচিত?", fontSize = 11.sp) },
              modifier = Modifier
                .weight(1f)
                .testTag("ai_question_input"),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A),
                focusedBorderColor = SkyAccent,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )

            IconButton(
              onClick = {
                val q = questionInputText.trim()
                if (q.isNotBlank()) {
                  viewModel.askAiQuestion(q)
                }
              },
              enabled = questionInputText.isNotBlank() && !isAiAnswerLoading,
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SkyAccent)
                .testTag("ai_ask_button")
            ) {
              Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "পাঠান",
                tint = Color.Black
              )
            }
          }

          if (isAiAnswerLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(vertical = 4.dp)
            ) {
              CircularProgressIndicator(
                color = SkyAccent,
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("উত্তর লোড হচ্ছে...", style = MaterialTheme.typography.labelSmall.copy(color = SkyAccent))
            }
          }

          if (aiAnswerText.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F172A))
                .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
                .padding(14.dp)
            ) {
              Text(
                text = aiAnswerText,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color(0xFFE2E8F0),
                  lineHeight = 22.sp,
                  fontSize = 12.sp
                )
              )
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
