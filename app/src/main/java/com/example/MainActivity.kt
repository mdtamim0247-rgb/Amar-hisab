package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AmarHisabViewModel
import com.example.ui.components.BalanceHeroCard
import com.example.ui.components.TopRateHeader
import com.example.ui.dialogs.EditRateDialog
import com.example.ui.dialogs.QuickCalculatorDialog
import com.example.ui.screens.AiAdvisorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoansScreen
import com.example.ui.screens.RemittanceScreen
import com.example.ui.screens.ReportAndToolsScreen
import com.example.ui.theme.AmarHisabTheme
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.util.configureAppCheck
import kotlinx.coroutines.flow.collectLatest

enum class MainTab(
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
) {
  HOME("হোম ও খরচ", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
  REMIT("রেমিট্যান্স", Icons.Filled.Send, Icons.Outlined.Send, "nav_remit"),
  LOANS("ধার-দেনা", Icons.Filled.Handshake, Icons.Outlined.Handshake, "nav_loans"),
  AI_ADVISOR("এআই গাইড", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_ai"),
  REPORT("রিপোর্ট", Icons.Filled.PieChart, Icons.Outlined.PieChart, "nav_report")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Configure App Check before UI or Firebase operations as required by Firebase AI Logic
    configureAppCheck(this, intent)
    enableEdgeToEdge()
    setContent {
      AmarHisabTheme {
        AmarHisabApp()
      }
    }
  }
}

@Composable
fun AmarHisabApp(viewModel: AmarHisabViewModel = viewModel()) {
  var selectedTab by remember { mutableStateOf(MainTab.HOME) }
  var showRateDialog by remember { mutableStateOf(false) }
  var showCalculatorDialog by remember { mutableStateOf(false) }

  val snackbarHostState = remember { SnackbarHostState() }

  // Observe ViewModel State
  val currentRate by viewModel.currentRate.collectAsStateWithLifecycle()
  val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
  val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
  val summaryState by viewModel.summaryState.collectAsStateWithLifecycle()
  val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
  val filteredRemittances by viewModel.filteredRemittances.collectAsStateWithLifecycle()
  val allLoans by viewModel.allLoans.collectAsStateWithLifecycle()
  val monthlyTrends by viewModel.monthlySpendingTrends.collectAsStateWithLifecycle()

  // Gemini AI States
  val aiAdviceText by viewModel.aiAdviceText.collectAsStateWithLifecycle()
  val isAiAdviceLoading by viewModel.isAiAdviceLoading.collectAsStateWithLifecycle()
  val smartParsedEntry by viewModel.smartParsedEntry.collectAsStateWithLifecycle()
  val isSmartParsing by viewModel.isSmartParsing.collectAsStateWithLifecycle()
  val aiAnswerText by viewModel.aiAnswerText.collectAsStateWithLifecycle()
  val isAiAnswerLoading by viewModel.isAiAnswerLoading.collectAsStateWithLifecycle()

  // Handle Back press if not on Home tab
  BackHandler(enabled = selectedTab != MainTab.HOME) {
    selectedTab = MainTab.HOME
  }

  // Collect user messages for snackbar
  LaunchedEffect(Unit) {
    viewModel.userMessage.collectLatest { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBackground),
    containerColor = DarkBackground,
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      NavigationBar(
        modifier = Modifier
          .fillMaxWidth()
          .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = Color(0xFF0F172A),
        contentColor = Color(0xFF94A3B8)
      ) {
        MainTab.values().forEach { tab ->
          val isSelected = selectedTab == tab
          NavigationBarItem(
            modifier = Modifier.testTag(tab.testTag),
            selected = isSelected,
            onClick = { selectedTab = tab },
            icon = {
              Icon(
                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.title
              )
            },
            label = {
              Text(
                text = tab.title,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 10.sp
                ),
                maxLines = 1
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = EmeraldPrimary,
              selectedTextColor = EmeraldPrimary,
              indicatorColor = EmeraldPrimary.copy(alpha = 0.2f),
              unselectedIconColor = Color(0xFF94A3B8),
              unselectedTextColor = Color(0xFF94A3B8)
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .windowInsetsPadding(WindowInsets.statusBars)
    ) {
      // Top Rate & Header Bar
      TopRateHeader(
        currentRate = currentRate,
        onEditRateClick = { showRateDialog = true },
        onOpenCalculatorClick = { showCalculatorDialog = true }
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Main Hero Card
      Box(modifier = Modifier.padding(horizontal = 16.dp)) {
        BalanceHeroCard(
          summaryState = summaryState,
          selectedMonth = selectedMonth,
          availableMonths = availableMonths,
          onMonthSelected = { viewModel.setMonth(it) }
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Content for selected tab
      Box(modifier = Modifier.fillMaxSize()) {
        when (selectedTab) {
          MainTab.HOME -> {
            HomeScreen(
              transactions = filteredTransactions,
              currentRate = currentRate,
              onAddTransaction = { type, cat, amt, note ->
                viewModel.addTransaction(type, cat, amt, note)
              },
              onDeleteTransaction = { id ->
                viewModel.deleteTransaction(id)
              }
            )
          }
          MainTab.REMIT -> {
            RemittanceScreen(
              remittances = filteredRemittances,
              currentRate = currentRate,
              onAddRemittance = { receiver, method, amt, fee, bonus, note ->
                viewModel.addRemittance(receiver, method, amt, fee, bonus, note)
              },
              onDeleteRemittance = { id ->
                viewModel.deleteRemittance(id)
              }
            )
          }
          MainTab.LOANS -> {
            LoansScreen(
              loans = allLoans,
              currentRate = currentRate,
              onAddLoan = { person, type, amt, note ->
                viewModel.addLoan(person, type, amt, note)
              },
              onToggleSettled = { id, currentSettled ->
                viewModel.toggleLoanSettled(id, currentSettled)
              },
              onDeleteLoan = { id ->
                viewModel.deleteLoan(id)
              }
            )
          }
          MainTab.AI_ADVISOR -> {
            AiAdvisorScreen(
              viewModel = viewModel,
              aiAdviceText = aiAdviceText,
              isAiAdviceLoading = isAiAdviceLoading,
              smartParsedEntry = smartParsedEntry,
              isSmartParsing = isSmartParsing,
              aiAnswerText = aiAnswerText,
              isAiAnswerLoading = isAiAnswerLoading,
              currentRate = currentRate
            )
          }
          MainTab.REPORT -> {
            ReportAndToolsScreen(
              viewModel = viewModel,
              transactions = filteredTransactions,
              currentRate = currentRate,
              monthlyTrends = monthlyTrends
            )
          }
        }
      }
    }
  }

  // Dialogs
  if (showRateDialog) {
    EditRateDialog(
      currentRate = currentRate,
      onDismiss = { showRateDialog = false },
      onRateSaved = { newRate ->
        viewModel.updateRate(newRate)
        showRateDialog = false
      }
    )
  }

  if (showCalculatorDialog) {
    QuickCalculatorDialog(
      currentRate = currentRate,
      onDismiss = { showCalculatorDialog = false }
    )
  }
}
