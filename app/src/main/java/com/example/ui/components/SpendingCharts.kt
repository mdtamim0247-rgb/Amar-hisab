package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RoseExpense
import com.example.ui.util.Formatters
import kotlin.math.PI
import kotlin.math.atan2

val ChartCategoryColors = listOf(
  Color(0xFFF59E0B), // Amber - খাবার
  Color(0xFF3B82F6), // Blue - রুম ভাড়া
  Color(0xFFEF4444), // Rose - আকামা
  Color(0xFF10B981), // Emerald - মোবাইল
  Color(0xFFEC4899), // Pink - আল-বাইক
  Color(0xFF8B5CF6), // Purple - যাতায়াত
  Color(0xFF06B6D4), // Cyan - চিকিৎসা
  Color(0xFFF97316), // Orange - শপিং
  Color(0xFF64748B)  // Slate - অন্যান্য
)

data class CategorySlice(
  val category: String,
  val amountSar: Double,
  val percentage: Float,
  val color: Color,
  val startAngle: Float,
  val sweepAngle: Float
)

data class MonthTrendData(
  val yearMonth: String,
  val label: String,
  val totalExpenseSar: Double,
  val categoryAmounts: Map<String, Double>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryDonutChart(
  categoryAmounts: List<Pair<String, Double>>,
  currentRate: Double,
  modifier: Modifier = Modifier
) {
  val totalAmount = categoryAmounts.sumOf { it.second }
  if (totalAmount <= 0) return

  var selectedIndex by remember { mutableStateOf<Int?>(null) }
  val animProgress = remember { Animatable(0f) }

  LaunchedEffect(categoryAmounts) {
    animProgress.snapTo(0f)
    animProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
  }

  // Pre-calculate slices
  var accumulatedAngle = -90f
  val slices = categoryAmounts.mapIndexed { index, (cat, amt) ->
    val sweep = (amt / totalAmount).toFloat() * 360f
    val slice = CategorySlice(
      category = cat,
      amountSar = amt,
      percentage = ((amt / totalAmount) * 100).toFloat(),
      color = ChartCategoryColors[index % ChartCategoryColors.size],
      startAngle = accumulatedAngle,
      sweepAngle = sweep
    )
    accumulatedAngle += sweep
    slice
  }

  val activeSlice = selectedIndex?.let { slices.getOrNull(it) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("category_donut_chart"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Canvas Donut with interactive center
    Box(
      modifier = Modifier
        .size(240.dp)
        .padding(8.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(
        modifier = Modifier
          .size(220.dp)
          .pointerInput(slices) {
            detectTapGestures { offset ->
              val center = Offset(size.width / 2f, size.height / 2f)
              val dx = offset.x - center.x
              val dy = offset.y - center.y
              val distance = kotlin.math.sqrt(dx * dx + dy * dy)
              val innerRadius = (size.width / 2f) * 0.55f
              val outerRadius = (size.width / 2f)

              if (distance in innerRadius..outerRadius) {
                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                if (angle < -90f) {
                  angle += 360f
                }
                val hitSlice = slices.indexOfFirst { slice ->
                  val start = slice.startAngle
                  val end = slice.startAngle + slice.sweepAngle
                  angle >= start && angle < end
                }
                selectedIndex = if (hitSlice >= 0 && selectedIndex != hitSlice) hitSlice else null
              } else {
                selectedIndex = null
              }
            }
          }
      ) {
        val strokeWidthNormal = 28.dp.toPx()
        val strokeWidthSelected = 36.dp.toPx()
        val arcSize = size.width - strokeWidthSelected

        slices.forEachIndexed { idx, slice ->
          val isSelected = selectedIndex == idx
          val sweepAnimated = slice.sweepAngle * animProgress.value
          val stroke = if (isSelected) strokeWidthSelected else strokeWidthNormal

          drawArc(
            color = if (selectedIndex == null || isSelected) slice.color else slice.color.copy(alpha = 0.35f),
            startAngle = slice.startAngle,
            sweepAngle = sweepAnimated,
            useCenter = false,
            topLeft = Offset(
              (size.width - arcSize) / 2f,
              (size.height - arcSize) / 2f
            ),
            size = Size(arcSize, arcSize),
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
          )
        }
      }

      // Center Information Readout
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
      ) {
        if (activeSlice != null) {
          Text(
            text = activeSlice.category,
            style = MaterialTheme.typography.labelSmall.copy(
              color = activeSlice.color,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            ),
            maxLines = 1
          )
          Text(
            text = "%.0f SAR".format(activeSlice.amountSar),
            style = MaterialTheme.typography.titleMedium.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          )
          Text(
            text = "%.1f%%".format(activeSlice.percentage),
            style = MaterialTheme.typography.labelMedium.copy(
              color = GoldAccent,
              fontWeight = FontWeight.SemiBold
            )
          )
          Text(
            text = "≈ ${Formatters.formatBdt(activeSlice.amountSar * currentRate)}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color(0xFF94A3B8),
              fontSize = 10.sp
            )
          )
        } else {
          Text(
            text = "মোট মাসিক খরচ",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color(0xFF94A3B8),
              fontSize = 11.sp
            )
          )
          Text(
            text = "%.0f SAR".format(totalAmount),
            style = MaterialTheme.typography.titleLarge.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          )
          Text(
            text = "≈ ${Formatters.formatBdt(totalAmount * currentRate)}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = GoldAccent,
              fontSize = 11.sp
            )
          )
          Text(
            text = "ট্যাপ করে বিস্তারিত দেখুন",
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color(0xFF64748B),
              fontSize = 9.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Interactive Legend Chips
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      slices.forEachIndexed { idx, slice ->
        val isSelected = selectedIndex == idx
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) slice.color.copy(alpha = 0.25f) else Color(0xFF0F172A))
            .border(
              width = if (isSelected) 1.dp else 0.5.dp,
              color = if (isSelected) slice.color else Color(0xFF334155),
              shape = RoundedCornerShape(8.dp)
            )
            .clickable {
              selectedIndex = if (selectedIndex == idx) null else idx
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(slice.color)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${slice.category} (%.0f%%)".format(slice.percentage),
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isSelected) Color.White else Color(0xFFCBD5E1),
              fontSize = 10.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
      }
    }
  }
}

@Composable
fun MonthlySpendingTrendChart(
  monthlyData: List<MonthTrendData>,
  currentRate: Double,
  modifier: Modifier = Modifier
) {
  if (monthlyData.isEmpty()) return

  var selectedMonthIndex by remember { mutableStateOf(monthlyData.lastIndex) }
  val maxExpense = monthlyData.maxOfOrNull { it.totalExpenseSar }?.coerceAtLeast(100.0) ?: 100.0

  val animProgress = remember { Animatable(0f) }
  LaunchedEffect(monthlyData) {
    animProgress.snapTo(0f)
    animProgress.animateTo(1f, animationSpec = tween(durationMillis = 700))
  }

  val selectedMonth = monthlyData.getOrNull(selectedMonthIndex)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("monthly_trend_chart")
  ) {
    // Header with selected month total readout
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "মাসিক খরচের তুলনা ও ট্রেন্ড",
          style = MaterialTheme.typography.titleSmall.copy(
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        )
        if (selectedMonth != null) {
          Text(
            text = "${selectedMonth.label}: ${"%.0f".format(selectedMonth.totalExpenseSar)} SAR (≈ ${Formatters.formatBdt(selectedMonth.totalExpenseSar * currentRate)})",
            style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent)
          )
        }
      }

      // Trend Indicator if we have >= 2 months
      if (monthlyData.size >= 2) {
        val curr = monthlyData.last().totalExpenseSar
        val prev = monthlyData[monthlyData.size - 2].totalExpenseSar
        val diff = curr - prev
        val isLower = diff < 0
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isLower) EmeraldPrimary.copy(alpha = 0.2f) else RoseExpense.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = if (isLower) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
            contentDescription = null,
            tint = if (isLower) EmeraldPrimary else RoseExpense,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isLower) "খরচ কমেছে" else "খরচ বৃদ্ধি",
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isLower) EmeraldPrimary else RoseExpense,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Bar chart container
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Color(0xFF0F172A))
        .border(1.dp, Color(0xFF334155), RoundedCornerShape(14.dp))
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
      ) {
        monthlyData.forEachIndexed { index, mData ->
          val isSelected = selectedMonthIndex == index
          val barHeightRatio = ((mData.totalExpenseSar / maxExpense).toFloat() * animProgress.value).coerceIn(0.08f, 1f)

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .clickable { selectedMonthIndex = index }
              .padding(horizontal = 4.dp)
          ) {
            // SAR value above bar
            Text(
              text = "%.0f".format(mData.totalExpenseSar),
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) GoldAccent else Color(0xFF64748B),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // The vertical bar
            Box(
              modifier = Modifier
                .width(if (isSelected) 34.dp else 26.dp)
                .height((100 * barHeightRatio).dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                  if (isSelected) RoseExpense else Color(0xFF475569)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Month Label
            Text(
              text = mData.label,
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            )
          }
        }
      }
    }
  }
}
