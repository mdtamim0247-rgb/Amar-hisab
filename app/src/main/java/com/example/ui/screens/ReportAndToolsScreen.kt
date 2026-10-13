package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.AmarHisabViewModel
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.MonthTrendData
import com.example.ui.components.MonthlySpendingTrendChart
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoseExpense
import com.example.ui.theme.SkyAccent
import com.example.ui.util.Formatters
import kotlinx.coroutines.launch

@Composable
fun ReportAndToolsScreen(
  viewModel: AmarHisabViewModel,
  transactions: List<TransactionEntity>,
  currentRate: Double,
  monthlyTrends: List<MonthTrendData>,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var showRestoreDialog by remember { mutableStateOf(false) }
  var restoreJsonInput by remember { mutableStateOf("") }
  var showExportDialog by remember { mutableStateOf(false) }
  var exportedJsonText by remember { mutableStateOf("") }
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  val expenses = transactions.filter { it.type == "expense" }
  val totalExpense = expenses.sumOf { it.amountSar }
  val categoryMap = expenses.groupBy { it.category }
    .mapValues { entry -> entry.value.sumOf { it.amountSar } }
    .toList()
    .sortedByDescending { it.second }

  val topCategory = categoryMap.firstOrNull()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Interactive Category Spending Donut Chart
    if (categoryMap.isNotEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("expense_charts_card"),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(RoseExpense.copy(alpha = 0.35f))
          )
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.PieChart,
                  contentDescription = null,
                  tint = RoseExpense,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "খাত অনুযায়ী মাসিক খরচের চার্ট",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(RoseExpense.copy(alpha = 0.2f))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "${categoryMap.size}টি খাত",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = RoseExpense,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Donut Chart Component
            CategoryDonutChart(
              categoryAmounts = categoryMap,
              currentRate = currentRate
            )

            // Top Spending Highlight Insight Card
            if (topCategory != null && totalExpense > 0) {
              val topPercentage = ((topCategory.second / totalExpense) * 100).toInt()
              Spacer(modifier = Modifier.height(14.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFF0F172A))
                  .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                  .padding(12.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "সর্বোচ্চ খরচের খাত: ${topCategory.first} (${topPercentage}%)",
                      style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                      )
                    )
                    Text(
                      text = "এই খাতে খরচ হয়েছে %.0f SAR (≈ %s টাকা)। বাজেট নিয়ন্ত্রণে রাখতে নজর রাখুন।".format(
                        topCategory.second,
                        Formatters.formatBdt(topCategory.second * currentRate)
                      ),
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                      )
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 2. Multi-Month Trend Comparison Bar Chart
    if (monthlyTrends.isNotEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(SkyAccent.copy(alpha = 0.3f))
          )
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = SkyAccent,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "মাসিক খরচের ট্রেন্ড ওভারভিউ",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            MonthlySpendingTrendChart(
              monthlyData = monthlyTrends,
              currentRate = currentRate
            )
          }
        }
      }
    }

    // 3. WhatsApp / IMO Report Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("whatsapp_report_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(EmeraldPrimary.copy(alpha = 0.35f))
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "হোয়াটসঅ্যাপ / ইমু রিপোর্ট",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "এক ক্লিকে আপনার চলতি মাসের আয়, খরচ ও রেমিট্যান্সের সম্পূর্ণ হিসাব বাংলায় কপি করে পরিবার বা প্রিয়জনকে পাঠান।",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Preview snippet box
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF0F172A))
              .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            val reportPreview = viewModel.generateWhatsAppReport()
            Text(
              text = reportPreview.take(280) + if (reportPreview.length > 280) "..." else "",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFFA7F3D0),
                fontSize = 11.sp,
                lineHeight = 16.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Copy Button
            Button(
              onClick = {
                val report = viewModel.generateWhatsAppReport()
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Amar Hisab Report", report))
              },
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("copy_report_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("কপি করুন", fontSize = 12.sp)
            }

            // Share Button
            Button(
              onClick = {
                val report = viewModel.generateWhatsAppReport()
                val intent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_SUBJECT, "আমার হিসাব - প্রবাসী মাসিক রিপোর্ট")
                  putExtra(Intent.EXTRA_TEXT, report)
                }
                context.startActivity(Intent.createChooser(intent, "রিপোর্ট পাঠান"))
              },
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("share_report_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("শেয়ার করুন", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }

    // 4. Backup & Restore Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("backup_restore_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(SkyAccent.copy(alpha = 0.3f))
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Storage,
              contentDescription = null,
              tint = SkyAccent,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "ডাটা ব্যাকআপ ও রিস্টোর (JSON)",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SkyAccent
              )
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "আপনার সমস্ত হিসাব নিরাপদ রাখতে ব্যাকআপ ফাইল ডাউনলোড করুন বা পুরোনো ব্যাকআপ ফাইল থেকে রিস্টোর করুন।",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Export Button
            OutlinedButton(
              onClick = {
                coroutineScope.launch {
                  exportedJsonText = viewModel.getExportJson()
                  showExportDialog = true
                }
              },
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("export_backup_button"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("ব্যাকআপ কপি", color = EmeraldPrimary, fontSize = 12.sp)
            }

            // Import Button
            OutlinedButton(
              onClick = {
                restoreJsonInput = ""
                showRestoreDialog = true
              },
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("import_backup_button"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = null,
                tint = SkyAccent,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("রিস্টোর করুন", color = SkyAccent, fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Reset Sample Data Button
          TextButton(
            onClick = { showResetConfirmDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = null,
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "নমুনা ডাটা পুনরায় লোড করুন",
              style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // Export Dialog
  if (showExportDialog) {
    AlertDialog(
      onDismissRequest = { showExportDialog = false },
      title = { Text("ব্যাকআপ ডাটা (JSON)") },
      text = {
        Column {
          Text(
            text = "নিচের টেক্সটটি কপি করে নিরাপদ স্থানে সংরক্ষণ করুন অথবা ফাইল হিসেবে শেয়ার করুন:",
            style = MaterialTheme.typography.bodySmall
          )
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(150.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF0F172A))
              .padding(8.dp)
          ) {
            Text(
              text = exportedJsonText,
              style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFA7F3D0), fontSize = 10.sp)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Amar Hisab Backup", exportedJsonText))
            showExportDialog = false
          }
        ) {
          Text("ক্লিপবোর্ডে কপি করুন")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            val intent = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(Intent.EXTRA_SUBJECT, "Amar_Hisab_Backup.json")
              putExtra(Intent.EXTRA_TEXT, exportedJsonText)
            }
            context.startActivity(Intent.createChooser(intent, "ব্যাকআপ শেয়ার করুন"))
            showExportDialog = false
          }
        ) {
          Text("শেয়ার করুন")
        }
      }
    )
  }

  // Restore Dialog
  if (showRestoreDialog) {
    AlertDialog(
      onDismissRequest = { showRestoreDialog = false },
      title = { Text("ব্যাকআপ রিস্টোর") },
      text = {
        Column {
          Text(
            text = "পূর্বে সেভ করা ব্যাকআপ JSON কোডটি নিচে পেস্ট করুন:",
            style = MaterialTheme.typography.bodySmall
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = restoreJsonInput,
            onValueChange = { restoreJsonInput = it },
            placeholder = { Text("এখানে JSON পেস্ট করুন...", fontSize = 11.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF0F172A),
              unfocusedContainerColor = Color(0xFF0F172A),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (restoreJsonInput.isNotBlank()) {
              viewModel.importBackup(restoreJsonInput.trim())
              showRestoreDialog = false
            }
          }
        ) {
          Text("রিস্টোর সম্পন্ন করুন")
        }
      },
      dismissButton = {
        TextButton(onClick = { showRestoreDialog = false }) {
          Text("বাতিল")
        }
      }
    )
  }

  // Reset confirmation dialog
  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      title = { Text("নমুনা ডাটা লোড করবেন?") },
      text = {
        Text(
          "বর্তমান সকল লেনদেন মুছে সৌদি প্রবাসীদের জন্য সাধারণ নমুনা হিসাব (বেতন, রুম ভাড়া, রেমিট্যান্স) যুক্ত করা হবে।"
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetToSampleData()
            showResetConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
        ) {
          Text("হ্যাঁ, লোড করুন")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text("বাতিল")
        }
      }
    )
  }
}
