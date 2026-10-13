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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.example.data.model.RemittanceEntity
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.SkyAccent
import com.example.ui.util.Formatters

private val remittanceMethods = listOf(
  "বিকাশ (bKash)",
  "ব্যাংক একাউন্ট",
  "নগদ (Nagad)",
  "ক্যাশ পিকআপ",
  "STC Pay / Urpay"
)

@Composable
fun RemittanceScreen(
  remittances: List<RemittanceEntity>,
  currentRate: Double,
  onAddRemittance: (receiver: String, method: String, amountSar: Double, feeSar: Double, hasBonus: Boolean, note: String) -> Unit,
  onDeleteRemittance: (id: Long) -> Unit,
  modifier: Modifier = Modifier
) {
  var receiverInput by remember { mutableStateOf("") }
  var selectedMethod by remember { mutableStateOf(remittanceMethods[0]) }
  var methodDropdownOpen by remember { mutableStateOf(false) }
  var amountSarInput by remember { mutableStateOf("") }
  var feeSarInput by remember { mutableStateOf("15") }
  var hasBonus by remember { mutableStateOf(true) }
  var noteInput by remember { mutableStateOf("") }
  var inputError by remember { mutableStateOf(false) }

  // Live calculation preview
  val parsedSar = amountSarInput.toDoubleOrNull() ?: 0.0
  val bonusMult = if (hasBonus) 1.025 else 1.0
  val previewBdt = (parsedSar * currentRate) * bonusMult

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Add Remittance Form Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("add_remittance_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(GoldAccent.copy(alpha = 0.35f))
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = null,
              tint = GoldAccent,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "দেশে টাকা পাঠানোর হিসাব (রেমিট্যান্স)",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = GoldAccent
              )
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Receiver name & method row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = receiverInput,
              onValueChange = {
                receiverInput = it
                inputError = false
              },
              placeholder = { Text("প্রাপকের নাম (যেমন: মা)", fontSize = 12.sp) },
              isError = inputError && receiverInput.isBlank(),
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("remit_receiver_input"),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A),
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )

            // Method Picker
            Box(modifier = Modifier.weight(1.1f)) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFF0F172A))
                  .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                  .clickable { methodDropdownOpen = true }
                  .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = selectedMethod,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                  ),
                  maxLines = 1
                )
                Text("▼", color = Color(0xFF94A3B8), fontSize = 10.sp)
              }

              DropdownMenu(
                expanded = methodDropdownOpen,
                onDismissRequest = { methodDropdownOpen = false }
              ) {
                remittanceMethods.forEach { m ->
                  DropdownMenuItem(
                    text = { Text(m) },
                    onClick = {
                      selectedMethod = m
                      methodDropdownOpen = false
                    }
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // SAR Amount & Fee row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = amountSarInput,
              onValueChange = {
                amountSarInput = it
                inputError = false
              },
              placeholder = { Text("পাঠানো রিয়াল (SAR)", fontSize = 12.sp) },
              isError = inputError && parsedSar <= 0,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier
                .weight(1.2f)
                .testTag("remit_amount_input"),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A),
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )

            OutlinedTextField(
              value = feeSarInput,
              onValueChange = { feeSarInput = it },
              placeholder = { Text("ফি (SAR)", fontSize = 12.sp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier
                .weight(0.8f)
                .testTag("remit_fee_input"),
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A),
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 2.5% Bangladesh Govt Bonus & Live Preview Card
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF0F172A))
              .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { hasBonus = !hasBonus }
              ) {
                Checkbox(
                  checked = hasBonus,
                  onCheckedChange = { hasBonus = it },
                  colors = CheckboxDefaults.colors(
                    checkedColor = EmeraldPrimary,
                    uncheckedColor = Color(0xFF64748B)
                  )
                )
                Text(
                  text = "২.৫% সরকারি প্রণোদনা",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFE2E8F0),
                    fontWeight = FontWeight.Medium
                  )
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "দেশে পাবে:",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                  )
                )
                Text(
                  text = Formatters.formatBdt(previewBdt),
                  style = MaterialTheme.typography.titleMedium.copy(
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier.testTag("remit_preview_bdt")
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = noteInput,
            onValueChange = { noteInput = it },
            placeholder = { Text("নোট / ট্রানজেকশন নম্বর (ঐচ্ছিক)", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF0F172A),
              unfocusedContainerColor = Color(0xFF0F172A),
              focusedBorderColor = GoldAccent,
              unfocusedBorderColor = Color(0xFF334155),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Save button
          Button(
            onClick = {
              val amt = amountSarInput.toDoubleOrNull()
              val fee = feeSarInput.toDoubleOrNull() ?: 15.0
              if (receiverInput.isNotBlank() && amt != null && amt > 0) {
                onAddRemittance(
                  receiverInput.trim(),
                  selectedMethod,
                  amt,
                  fee,
                  hasBonus,
                  noteInput.trim()
                )
                receiverInput = ""
                amountSarInput = ""
                noteInput = ""
                inputError = false
              } else {
                inputError = true
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("save_remit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldDark)
          ) {
            Text(
              text = "রেমিট্যান্স সেভ করুন",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }
        }
      }
    }

    // Remittance List Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "দেশে পাঠানো টাকার তালিকা",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF1F5F9)
          )
        )
        Text(
          text = "${remittances.size} টি",
          style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
        )
      }
    }

    if (remittances.isEmpty()) {
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
              text = "কোনো রেমিট্যান্সের রেকর্ড নেই",
              style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "উপরে দেশে টাকা পাঠানোর পরিমাণ লিখে সেভ করুন",
              style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
            )
          }
        }
      }
    } else {
      items(remittances, key = { it.id }) { remit ->
        RemittanceCard(
          remittance = remit,
          onDelete = { onDeleteRemittance(remit.id) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun RemittanceCard(
  remittance: RemittanceEntity,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("remit_item_${remittance.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(GoldAccent.copy(alpha = 0.25f))
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(GoldAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = getMethodIcon(remittance.method),
              contentDescription = null,
              tint = GoldAccent,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = remittance.receiverName,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
            Text(
              text = "${remittance.method} • ${Formatters.formatShortDate(remittance.dateMillis)}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
              )
            )
          }
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "মুছে ফেলুন",
            tint = Color(0xFF64748B),
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Breakdown Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF0F172A))
          .padding(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "পাঠানো: %.2f SAR (ফি %.0f SAR)".format(remittance.amountSar, remittance.feeSar),
              style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1))
            )
            Text(
              text = "রেট: %.2f ৳ ${if (remittance.hasBonus) "+ ২.৫% বোনাস" else ""}".format(remittance.rate),
              style = MaterialTheme.typography.labelSmall.copy(
                color = EmeraldPrimary,
                fontSize = 10.sp
              )
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "দেশে পেয়েছে:",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
              )
            )
            Text(
              text = Formatters.formatBdt(remittance.totalBdt),
              style = MaterialTheme.typography.titleMedium.copy(
                color = GoldAccent,
                fontWeight = FontWeight.Bold
              )
            )
          }
        }
      }

      if (remittance.note.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "নোট: ${remittance.note}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
          )
        )
      }
    }
  }
}

private fun getMethodIcon(method: String): ImageVector {
  return when {
    method.contains("বিকাশ") || method.contains("নগদ") || method.contains("Pay") -> Icons.Default.PhoneAndroid
    method.contains("ব্যাংক") -> Icons.Default.AccountBalance
    else -> Icons.Default.Payments
  }
}
