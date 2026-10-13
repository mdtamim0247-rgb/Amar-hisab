package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SummaryUiState
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoseExpense
import com.example.ui.util.Formatters

@Composable
fun BalanceHeroCard(
  summaryState: SummaryUiState,
  selectedMonth: String,
  availableMonths: List<String>,
  onMonthSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var monthDropdownExpanded by remember { mutableStateOf(false) }

  val cardGradient = Brush.verticalGradient(
    colors = listOf(
      Color(0xFF065F46), // Emerald 800
      Color(0xFF064E3B), // Emerald 900
      Color(0xFF0F172A)  // Slate 900
    )
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(24.dp))
      .background(cardGradient)
      .border(1.dp, EmeraldPrimary.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
      .padding(18.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Top row: Balance title & Month picker
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "হাতে বর্তমান জমা (নেট ব্যালেন্স)",
            style = MaterialTheme.typography.labelMedium.copy(
              color = EmeraldLight.copy(alpha = 0.9f)
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "%.2f".format(summaryState.netBalanceSar),
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              ),
              modifier = Modifier.testTag("net_balance_sar")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "SAR",
              style = MaterialTheme.typography.titleMedium.copy(
                color = EmeraldPrimary,
                fontWeight = FontWeight.SemiBold
              ),
              modifier = Modifier.padding(bottom = 2.dp)
            )
          }

          Text(
            text = "≈ ${Formatters.formatBdt(summaryState.netBalanceBdt)} (BDT)",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = GoldAccent,
              fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.testTag("net_balance_bdt")
          )
        }

        // Month Picker Dropdown
        Box {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .testTag("month_selector")
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF0F172A).copy(alpha = 0.9f))
              .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
              .clickable { monthDropdownExpanded = true }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "মাস নির্বাচন",
              tint = EmeraldPrimary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = Formatters.formatMonthLabel(selectedMonth),
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Medium
              )
            )
            Icon(
              imageVector = Icons.Default.ArrowDropDown,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
          }

          DropdownMenu(
            expanded = monthDropdownExpanded,
            onDismissRequest = { monthDropdownExpanded = false }
          ) {
            DropdownMenuItem(
              text = { Text("সকল সময় (All Time)") },
              onClick = {
                onMonthSelected("ALL")
                monthDropdownExpanded = false
              }
            )
            availableMonths.forEach { m ->
              DropdownMenuItem(
                text = { Text(Formatters.formatMonthLabel(m)) },
                onClick = {
                  onMonthSelected(m)
                  monthDropdownExpanded = false
                }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3 Summary Grid Boxes
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Income
        SummaryPill(
          title = "মোট আয়/বেতন",
          amountSar = summaryState.totalIncomeSar,
          accentColor = EmeraldPrimary,
          modifier = Modifier.weight(1f)
        )

        // Expense
        SummaryPill(
          title = "মোট খরচ",
          amountSar = summaryState.totalExpenseSar,
          accentColor = RoseExpense,
          modifier = Modifier.weight(1f)
        )

        // Remittance
        SummaryPill(
          title = "দেশে পাঠানো",
          amountSar = summaryState.totalRemitSar,
          accentColor = GoldAccent,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun SummaryPill(
  title: String,
  amountSar: Double,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .background(Color(0xFF0F172A).copy(alpha = 0.7f))
      .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
      .padding(vertical = 8.dp, horizontal = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall.copy(
        color = accentColor,
        fontSize = 11.sp
      )
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = "%.0f".format(amountSar),
      style = MaterialTheme.typography.titleMedium.copy(
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp
      )
    )
    Text(
      text = "SAR",
      style = MaterialTheme.typography.labelSmall.copy(
        color = Color(0xFF94A3B8),
        fontSize = 9.sp
      )
    )
  }
}
