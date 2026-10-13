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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun TopRateHeader(
  currentRate: Double,
  onEditRateClick: () -> Unit,
  onOpenCalculatorClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val headerGradient = Brush.horizontalGradient(
    colors = listOf(
      Color(0xFF064E3B), // Deep emerald
      Color(0xFF0F766E), // Teal 700
      Color(0xFF0F172A)  // Slate 900
    )
  )

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = Color.Transparent
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(headerGradient)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Top row: App branding & quick actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(EmeraldPrimary.copy(alpha = 0.2f))
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Wallet,
                contentDescription = "Wallet Icon",
                tint = EmeraldPrimary,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "আমার হিসাব",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.25f))
                    .border(0.5.dp, Color(0xFF34D399), RoundedCornerShape(10.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "🇸🇦 🇧🇩",
                    fontSize = 11.sp
                  )
                }
              }
              Text(
                text = "সৌদি প্রবাসী ফাইন্যান্স ও রেমিট্যান্স অ্যাপ",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFFA7F3D0)
                )
              )
            }
          }

          // Calculator action button
          IconButton(
            onClick = onOpenCalculatorClick,
            modifier = Modifier
              .testTag("calculator_button")
              .clip(RoundedCornerShape(12.dp))
              .background(EmeraldDark.copy(alpha = 0.6f))
          ) {
            Icon(
              imageVector = Icons.Default.Calculate,
              contentDescription = "ক্যালকুলেটর",
              tint = GoldAccent,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        // Rate ticker bar
        Spacer(modifier = Modifier.padding(top = 10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.85f))
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable { onEditRateClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(EmeraldPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "আজকের রেট: ১ রিয়াল (SAR) =",
              style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFFCBD5E1)
              )
            )
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF1E293B))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "%.2f".format(currentRate),
              style = MaterialTheme.typography.labelLarge.copy(
                color = GoldAccent,
                fontWeight = FontWeight.Bold
              )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "টাকা",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFFE2E8F0)
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "রেট পরিবর্তন",
              tint = EmeraldPrimary,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }
  }
}
