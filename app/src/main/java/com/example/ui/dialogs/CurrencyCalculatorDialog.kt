package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.util.Formatters

@Composable
fun EditRateDialog(
  currentRate: Double,
  onDismiss: () -> Unit,
  onRateSaved: (Double) -> Unit
) {
  var rateInput by remember { mutableStateOf("%.2f".format(currentRate)) }
  var isError by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CurrencyExchange,
          contentDescription = null,
          tint = GoldAccent,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("আজকের SAR রেট পরিবর্তন")
      }
    },
    text = {
      Column {
        Text(
          text = "১ সৌদি রিয়ালের (SAR) বিপরীতে বর্তমান বাংলাদেশি টাকার (BDT) মান লিখুন:",
          style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = rateInput,
          onValueChange = {
            rateInput = it
            isError = false
          },
          label = { Text("১ SAR = কত টাকা?") },
          placeholder = { Text("যেমন: 32.50") },
          isError = isError,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("rate_input_field"),
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
          val newRate = rateInput.toDoubleOrNull()
          if (newRate != null && newRate > 0) {
            onRateSaved(newRate)
          } else {
            isError = true
          }
        },
        modifier = Modifier.testTag("save_rate_dialog_button"),
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
      ) {
        Text("সংরক্ষণ করুন", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("বাতিল")
      }
    }
  )
}

@Composable
fun QuickCalculatorDialog(
  currentRate: Double,
  onDismiss: () -> Unit
) {
  var sarAmountInput by remember { mutableStateOf("1000") }
  var feeInput by remember { mutableStateOf("15") }
  var withBonus by remember { mutableStateOf(true) }

  val sarAmt = sarAmountInput.toDoubleOrNull() ?: 0.0
  val feeAmt = feeInput.toDoubleOrNull() ?: 0.0
  val baseBdt = sarAmt * currentRate
  val bonusBdt = if (withBonus) baseBdt * 0.025 else 0.0
  val totalBdt = baseBdt + bonusBdt
  val totalSarDeducted = sarAmt + feeAmt

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Calculate,
          contentDescription = null,
          tint = GoldAccent,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("প্রবাসী মুদ্রা ক্যালকুলেটর")
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "বর্তমান রেট: ১ SAR = %.2f ৳".format(currentRate),
          style = MaterialTheme.typography.labelMedium.copy(color = EmeraldPrimary)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = sarAmountInput,
            onValueChange = { sarAmountInput = it },
            label = { Text("রিয়াল (SAR)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1.2f),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF0F172A),
              unfocusedContainerColor = Color(0xFF0F172A),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )

          OutlinedTextField(
            value = feeInput,
            onValueChange = { feeInput = it },
            label = { Text("ফি (SAR)", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(0.8f),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF0F172A),
              unfocusedContainerColor = Color(0xFF0F172A),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { withBonus = !withBonus }
        ) {
          Checkbox(
            checked = withBonus,
            onCheckedChange = { withBonus = it },
            colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
          )
          Text(
            text = "২.৫% সরকারি প্রণোদনা যুক্ত করুন",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE2E8F0))
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Result calculation box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("মূল বিনিময়:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
              Text(Formatters.formatBdt(baseBdt), style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
            }
            if (withBonus) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("২.৫% সরকারি বোনাস:", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldPrimary))
                Text("+ ${Formatters.formatBdt(bonusBdt)}", style = MaterialTheme.typography.bodySmall.copy(color = EmeraldPrimary))
              }
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("হাতে খরচ হবে:", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8)))
              Text("%.2f SAR".format(totalSarDeducted), style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFDE68A)))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFF334155)))
            Spacer(modifier = Modifier.height(4.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                "দেশে পাবে (সর্বমোট):",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
              )
              Text(
                Formatters.formatBdt(totalBdt),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
      ) {
        Text("ঠিক আছে", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    }
  )
}
