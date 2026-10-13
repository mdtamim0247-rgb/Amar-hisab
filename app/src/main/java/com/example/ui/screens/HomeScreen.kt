package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoseExpense
import com.example.ui.util.Formatters

private val expenseCategories = listOf(
  "খাবার / মেস খরচ",
  "রুম ভাড়া",
  "আকামা / কাফালা ফি",
  "মোবাইল / ইন্টারনেট",
  "আল-বাইক / রেস্তোরাঁ",
  "যাতায়াত / ট্যাক্সি",
  "চিকিৎসা / ওষুধ",
  "কাপড় / শপিং",
  "অন্যান্য খরচ"
)

private val incomeCategories = listOf(
  "মূল বেতন",
  "ওভারটাইম",
  "পার্ট-টাইম / বাড়তি কাজ",
  "বোনাস / টিপস",
  "অন্যান্য আয়"
)

@Composable
fun HomeScreen(
  transactions: List<TransactionEntity>,
  currentRate: Double,
  onAddTransaction: (type: String, category: String, amount: Double, note: String) -> Unit,
  onDeleteTransaction: (id: Long) -> Unit,
  modifier: Modifier = Modifier
) {
  var txType by remember { mutableStateOf("expense") }
  var selectedCategory by remember { mutableStateOf(expenseCategories[0]) }
  var categoryDropdownOpen by remember { mutableStateOf(false) }
  var amountInput by remember { mutableStateOf("") }
  var noteInput by remember { mutableStateOf("") }
  var inputError by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Add Transaction Glass Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("add_transaction_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(EmeraldPrimary.copy(alpha = 0.25f))
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AddCircle,
              contentDescription = null,
              tint = EmeraldPrimary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "নতুন আয় বা খরচ যোগ করুন",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
              )
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Type Selector: Expense vs Income
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF0F172A))
              .padding(4.dp)
          ) {
            // Expense Tab
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (txType == "expense") RoseExpense.copy(alpha = 0.25f) else Color.Transparent)
                .border(
                  width = if (txType == "expense") 1.dp else 0.dp,
                  color = if (txType == "expense") RoseExpense else Color.Transparent,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                  txType = "expense"
                  selectedCategory = expenseCategories[0]
                }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "💸 খরচ (Expense)",
                style = MaterialTheme.typography.labelMedium.copy(
                  color = if (txType == "expense") Color.White else Color(0xFF94A3B8),
                  fontWeight = FontWeight.SemiBold
                )
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Income Tab
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (txType == "income") EmeraldPrimary.copy(alpha = 0.25f) else Color.Transparent)
                .border(
                  width = if (txType == "income") 1.dp else 0.dp,
                  color = if (txType == "income") EmeraldPrimary else Color.Transparent,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                  txType = "income"
                  selectedCategory = incomeCategories[0]
                }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "💰 আয় / বেতন (Income)",
                style = MaterialTheme.typography.labelMedium.copy(
                  color = if (txType == "income") Color.White else Color(0xFF94A3B8),
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Category Dropdown
          Box(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .clickable { categoryDropdownOpen = true }
                .padding(horizontal = 14.dp, vertical = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = getCategoryIcon(selectedCategory),
                  contentDescription = null,
                  tint = if (txType == "income") EmeraldPrimary else RoseExpense,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = selectedCategory,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                  )
                )
              }
              Text(
                text = "▼",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
              )
            }

            DropdownMenu(
              expanded = categoryDropdownOpen,
              onDismissRequest = { categoryDropdownOpen = false }
            ) {
              val currentCats = if (txType == "income") incomeCategories else expenseCategories
              currentCats.forEach { cat ->
                DropdownMenuItem(
                  text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        imageVector = getCategoryIcon(cat),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (txType == "income") EmeraldPrimary else RoseExpense
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(cat)
                    }
                  },
                  onClick = {
                    selectedCategory = cat
                    categoryDropdownOpen = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Amount & Note inputs
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = amountInput,
              onValueChange = {
                amountInput = it
                inputError = false
              },
              placeholder = { Text("পরিমাণ (SAR)", fontSize = 12.sp) },
              isError = inputError,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("tx_amount_input"),
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

            OutlinedTextField(
              value = noteInput,
              onValueChange = { noteInput = it },
              placeholder = { Text("বিবরণ (যেমন: রুম ভাড়া)", fontSize = 12.sp) },
              singleLine = true,
              modifier = Modifier
                .weight(1.2f)
                .testTag("tx_note_input"),
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
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Submit Button
          Button(
            onClick = {
              val amt = amountInput.toDoubleOrNull()
              if (amt != null && amt > 0) {
                onAddTransaction(txType, selectedCategory, amt, noteInput.trim())
                amountInput = ""
                noteInput = ""
                inputError = false
              } else {
                inputError = true
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("add_tx_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (txType == "income") EmeraldDark else Color(0xFFBE123C)
            )
          ) {
            Text(
              text = if (txType == "income") "আয় হিসাবে যোগ করুন" else "খরচ হিসাবে যোগ করুন",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }
        }
      }
    }

    // Transactions Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.ListAlt,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "সাম্প্রতিক লেনদেন",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color(0xFFF1F5F9)
            )
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E293B))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${transactions.size} টি",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color(0xFF94A3B8),
              fontWeight = FontWeight.Medium
            )
          )
        }
      }
    }

    // Transaction Items
    if (transactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard.copy(alpha = 0.5f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "এখনও কোনো লেনদেন যুক্ত করা হয়নি",
              style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF94A3B8)
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "উপরের ফরম থেকে নতুন আয় বা খরচ যোগ করুন",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF64748B)
              )
            )
          }
        }
      }
    } else {
      items(transactions, key = { it.id }) { tx ->
        TransactionCard(
          transaction = tx,
          currentRate = currentRate,
          onDelete = { onDeleteTransaction(tx.id) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun TransactionCard(
  transaction: TransactionEntity,
  currentRate: Double,
  onDelete: () -> Unit
) {
  val isIncome = transaction.type == "income"
  val amountBdt = transaction.amountSar * currentRate

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("tx_item_${transaction.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(
        if (isIncome) EmeraldPrimary.copy(alpha = 0.2f) else RoseExpense.copy(alpha = 0.2f)
      )
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left Icon + Details
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isIncome) EmeraldPrimary.copy(alpha = 0.15f) else RoseExpense.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = getCategoryIcon(transaction.category),
            contentDescription = null,
            tint = if (isIncome) EmeraldPrimary else RoseExpense,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = transaction.category,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.SemiBold
            )
          )
          if (transaction.note.isNotBlank()) {
            Text(
              text = transaction.note,
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF94A3B8)
              ),
              maxLines = 1
            )
          }
          Text(
            text = Formatters.formatDate(transaction.dateMillis),
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color(0xFF64748B),
              fontSize = 10.sp
            )
          )
        }
      }

      // Right Amount & Delete
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "${if (isIncome) "+" else "-"}%.2f SAR".format(transaction.amountSar),
            style = MaterialTheme.typography.bodyMedium.copy(
              color = if (isIncome) EmeraldPrimary else RoseExpense,
              fontWeight = FontWeight.Bold
            )
          )
          Text(
            text = "≈ ${Formatters.formatBdt(amountBdt)}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = GoldAccent.copy(alpha = 0.85f),
              fontSize = 11.sp
            )
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "মুছে ফেলুন",
            tint = Color(0xFF64748B),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

private fun getCategoryIcon(category: String): ImageVector {
  return when {
    category.contains("খাবার") || category.contains("রেস্তোরাঁ") || category.contains("আল-বাইক") -> Icons.Default.Fastfood
    category.contains("রুম") -> Icons.Default.Home
    category.contains("আকামা") || category.contains("কাফালা") -> Icons.Default.Receipt
    category.contains("মোবাইল") || category.contains("ইন্টারনেট") -> Icons.Default.PhoneAndroid
    category.contains("যাতায়াত") || category.contains("ট্যাক্সি") -> Icons.Default.LocalTaxi
    category.contains("চিকিৎসা") || category.contains("ওষুধ") -> Icons.Default.LocalHospital
    category.contains("শপিং") || category.contains("কাপড়") -> Icons.Default.ShoppingBag
    category.contains("বেতন") || category.contains("ওভারটাইম") || category.contains("বোনাস") || category.contains("আয়") -> Icons.Default.Payments
    else -> Icons.Default.Work
  }
}
