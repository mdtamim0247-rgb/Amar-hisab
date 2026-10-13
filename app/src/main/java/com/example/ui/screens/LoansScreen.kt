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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanEntity
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseExpense
import com.example.ui.theme.SkyAccent
import com.example.ui.util.Formatters

@Composable
fun LoansScreen(
  loans: List<LoanEntity>,
  currentRate: Double,
  onAddLoan: (person: String, type: String, amountSar: Double, note: String) -> Unit,
  onToggleSettled: (id: Long, currentSettled: Boolean) -> Unit,
  onDeleteLoan: (id: Long) -> Unit,
  modifier: Modifier = Modifier
) {
  var personInput by remember { mutableStateOf("") }
  var loanType by remember { mutableStateOf("given") } // "given" (পাবো) or "taken" (দেবো)
  var amountInput by remember { mutableStateOf("") }
  var noteInput by remember { mutableStateOf("") }
  var inputError by remember { mutableStateOf(false) }

  var filterStatus by remember { mutableStateOf("all") } // "all", "given", "taken"

  val pendingGiven = loans.filter { it.type == "given" && !it.isSettled }.sumOf { it.amountSar }
  val pendingTaken = loans.filter { it.type == "taken" && !it.isSettled }.sumOf { it.amountSar }

  val displayedLoans = when (filterStatus) {
    "given" -> loans.filter { it.type == "given" }
    "taken" -> loans.filter { it.type == "taken" }
    else -> loans
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 2 Summary Cards: মানুষের কাছে পাবো vs মানুষ আমার কাছে পাবে
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Given (পাবো)
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.5f)),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(EmeraldPrimary.copy(alpha = 0.4f))
          )
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "মানুষের কাছে পাবো (পাওনা)",
              style = MaterialTheme.typography.labelSmall.copy(
                color = EmeraldPrimary,
                fontWeight = FontWeight.Medium
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "%.0f SAR".format(pendingGiven),
              style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
            )
            Text(
              text = "≈ ${Formatters.formatBdt(pendingGiven * currentRate)}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFFA7F3D0),
                fontSize = 10.sp
              )
            )
          }
        }

        // Taken (দেনা)
        Card(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF881337).copy(alpha = 0.4f)),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(RoseExpense.copy(alpha = 0.4f))
          )
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "মানুষ পাবে (আমার দেনা)",
              style = MaterialTheme.typography.labelSmall.copy(
                color = RoseExpense,
                fontWeight = FontWeight.Medium
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "%.0f SAR".format(pendingTaken),
              style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
            )
            Text(
              text = "≈ ${Formatters.formatBdt(pendingTaken * currentRate)}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFFFECDD3),
                fontSize = 10.sp
              )
            )
          }
        }
      }
    }

    // Add Loan Card Form
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("add_loan_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(SkyAccent.copy(alpha = 0.35f))
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Handshake,
              contentDescription = null,
              tint = SkyAccent,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "ধার বা কর্জের হিসাব যোগ করুন",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SkyAccent
              )
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Person & Type Toggle
          OutlinedTextField(
            value = personInput,
            onValueChange = {
              personInput = it
              inputError = false
            },
            placeholder = { Text("ব্যক্তির নাম (যেমন: রফিক ভাই, করিম কাকা)", fontSize = 12.sp) },
            isError = inputError && personInput.isBlank(),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("loan_person_input"),
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

          Spacer(modifier = Modifier.height(10.dp))

          // Loan Type Segmented Selector
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF0F172A))
              .padding(4.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (loanType == "given") EmeraldPrimary.copy(alpha = 0.25f) else Color.Transparent)
                .border(
                  width = if (loanType == "given") 1.dp else 0.dp,
                  color = if (loanType == "given") EmeraldPrimary else Color.Transparent,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { loanType = "given" }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "আমি ধার দিয়েছি (পাবো)",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (loanType == "given") Color.White else Color(0xFF94A3B8),
                  fontWeight = FontWeight.SemiBold
                )
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (loanType == "taken") RoseExpense.copy(alpha = 0.25f) else Color.Transparent)
                .border(
                  width = if (loanType == "taken") 1.dp else 0.dp,
                  color = if (loanType == "taken") RoseExpense else Color.Transparent,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { loanType = "taken" }
                .padding(vertical = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "আমি ধার নিয়েছি (দেবো)",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (loanType == "taken") Color.White else Color(0xFF94A3B8),
                  fontWeight = FontWeight.SemiBold
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

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
              isError = inputError && (amountInput.toDoubleOrNull() ?: 0.0) <= 0,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("loan_amount_input"),
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

            OutlinedTextField(
              value = noteInput,
              onValueChange = { noteInput = it },
              placeholder = { Text("নোট / সময়কাল", fontSize = 12.sp) },
              singleLine = true,
              modifier = Modifier.weight(1.3f),
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
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              val amt = amountInput.toDoubleOrNull()
              if (personInput.isNotBlank() && amt != null && amt > 0) {
                onAddLoan(personInput.trim(), loanType, amt, noteInput.trim())
                personInput = ""
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
              .testTag("save_loan_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
          ) {
            Text(
              text = "ধারের খাতায় যোগ করুন",
              style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }
        }
      }
    }

    // Filter Chips
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = filterStatus == "all",
          onClick = { filterStatus = "all" },
          label = { Text("সব (${loans.size})") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SkyAccent,
            selectedLabelColor = Color.Black
          )
        )
        FilterChip(
          selected = filterStatus == "given",
          onClick = { filterStatus = "given" },
          label = { Text("আমি পাবো") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = EmeraldPrimary,
            selectedLabelColor = Color.Black
          )
        )
        FilterChip(
          selected = filterStatus == "taken",
          onClick = { filterStatus = "taken" },
          label = { Text("আমি দেবো") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = RoseExpense,
            selectedLabelColor = Color.White
          )
        )
      }
    }

    // Loans List
    if (displayedLoans.isEmpty()) {
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
              text = "কোনো ধার বা কর্জের হিসাব নেই",
              style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
            )
          }
        }
      }
    } else {
      items(displayedLoans, key = { it.id }) { loan ->
        LoanCard(
          loan = loan,
          currentRate = currentRate,
          onToggleSettled = { onToggleSettled(loan.id, loan.isSettled) },
          onDelete = { onDeleteLoan(loan.id) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun LoanCard(
  loan: LoanEntity,
  currentRate: Double,
  onToggleSettled: () -> Unit,
  onDelete: () -> Unit
) {
  val isGiven = loan.type == "given"

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("loan_item_${loan.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (loan.isSettled) DarkSurfaceCard.copy(alpha = 0.6f) else DarkSurfaceCard
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(
        if (loan.isSettled) Color(0xFF475569)
        else if (isGiven) EmeraldPrimary.copy(alpha = 0.3f)
        else RoseExpense.copy(alpha = 0.3f)
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
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        // Toggle Settled Button
        IconButton(
          onClick = onToggleSettled,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = if (loan.isSettled) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
            contentDescription = "পরিশোধিত টগল",
            tint = if (loan.isSettled) EmeraldPrimary else Color(0xFF64748B),
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = loan.personName,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (loan.isSettled) Color(0xFF94A3B8) else Color.White
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                  if (loan.isSettled) Color(0xFF334155)
                  else if (isGiven) EmeraldPrimary.copy(alpha = 0.2f)
                  else RoseExpense.copy(alpha = 0.2f)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = if (loan.isSettled) "পরিশোধিত"
                else if (isGiven) "পাবো" else "দেনা",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (loan.isSettled) Color(0xFF94A3B8)
                  else if (isGiven) EmeraldPrimary else RoseExpense,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              )
            }
          }

          if (loan.note.isNotBlank()) {
            Text(
              text = loan.note,
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
              )
            )
          }

          Text(
            text = Formatters.formatShortDate(loan.dateMillis),
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color(0xFF64748B),
              fontSize = 10.sp
            )
          )
        }
      }

      // Amount & Delete
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "%.0f SAR".format(loan.amountSar),
            style = MaterialTheme.typography.bodyMedium.copy(
              color = if (loan.isSettled) Color(0xFF94A3B8)
              else if (isGiven) EmeraldPrimary else RoseExpense,
              fontWeight = FontWeight.Bold
            )
          )
          Text(
            text = "≈ ${Formatters.formatBdt(loan.amountSar * currentRate)}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color(0xFF94A3B8),
              fontSize = 10.sp
            )
          )
        }

        Spacer(modifier = Modifier.width(6.dp))

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
    }
  }
}
