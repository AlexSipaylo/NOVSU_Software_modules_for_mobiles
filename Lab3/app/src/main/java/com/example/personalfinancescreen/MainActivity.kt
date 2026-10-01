package com.example.personalfinancescreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalfinancescreen.ui.theme.FinanceTheme
import androidx.core.content.edit

data class Account(
    val name: String, val amount: Double, val subtitle: String, val icon: ImageVector
)

data class Transaction(
    val title: String,
    val category: String,
    val amount: Double,
    val date: String,
    val icon: ImageVector
)

enum class UiState {
    Loading, Empty, Content
}

enum class Screen {
    Home, Details
}

private val accounts = listOf(
    Account(
        name = "Основная карта",
        amount = 124500.0,
        subtitle = "•••• 4821",
        icon = Icons.Default.Payments
    ), Account(
        name = "Накопления",
        amount = 68000.0,
        subtitle = "Цель: отпуск",
        icon = Icons.Default.Savings
    ), Account(
        name = "Наличные",
        amount = 8500.0,
        subtitle = "Кошелёк",
        icon = Icons.Default.AccountBalanceWallet
    )
)

private val transactions = listOf(
    Transaction(
        title = "Продукты",
        category = "Еда",
        amount = -4250.0,
        date = "Сегодня, 09:40",
        icon = Icons.Default.ShoppingCart
    ), Transaction(
        title = "Проезд",
        category = "Транспорт",
        amount = -920.0,
        date = "Вчера, 18:15",
        icon = Icons.Default.Train
    ), Transaction(
        title = "Кофе",
        category = "Кафе",
        amount = -390.0,
        date = "Вчера, 10:05",
        icon = Icons.Default.Restaurant
    ), Transaction(
        title = "Зарплата",
        category = "Доход",
        amount = 85000.0,
        date = "18 сентября",
        icon = Icons.Default.Payments
    )
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("finance_settings", MODE_PRIVATE)
        setContent {
            var darkTheme by remember {
                mutableStateOf(preferences.getBoolean("dark_theme", false))
            }
            val windowSizeClass = calculateWindowSizeClass(this)
            FinanceTheme(
                darkTheme = darkTheme
            ) {
                FinanceApp(
                    windowSizeClass = windowSizeClass, darkTheme = darkTheme, onThemeChange = {
                        darkTheme = !darkTheme
                        preferences.edit { putBoolean("dark_theme", darkTheme) }
                    })
            }
        }
    }
}

@Composable
fun FinanceApp(windowSizeClass: WindowSizeClass, darkTheme: Boolean, onThemeChange: () -> Unit) {
    var screen by remember {
        mutableStateOf(Screen.Home)
    }
    var state by remember {
        mutableStateOf(UiState.Content)
    }
    when (screen) {
        Screen.Home -> {
            FinanceHomeScreen(
                windowSizeClass = windowSizeClass,
                state = state,
                darkTheme = darkTheme,
                onThemeChange = onThemeChange,
                onStateChange = {
                    state = it
                },
                onOpenDetails = {
                    screen = Screen.Details
                })
        }

        Screen.Details -> {
            TransactionDetailsScreen(
                onBack = {
                    screen = Screen.Home
                })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceHomeScreen(
    windowSizeClass: WindowSizeClass,
    state: UiState,
    darkTheme: Boolean,
    onThemeChange: () -> Unit,
    onStateChange: (UiState) -> Unit,
    onOpenDetails: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Личные финансы", fontWeight = FontWeight.Bold)
                        Text(text = "Ваш финансовый обзор",
                            style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = darkTheme, onCheckedChange = {
                            onThemeChange()
                        }) {
                        Icon(
                            imageVector = if (darkTheme) {
                                Icons.Default.LightMode
                            } else {
                                Icons.Default.DarkMode
                            }, contentDescription = "Переключить тему"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            StateControls(currentState = state, onStateChange = onStateChange)
        }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AnimatedVisibility(
                visible = state == UiState.Loading, modifier = Modifier.fillMaxSize()
            ) {
                LoadingState()
            }
            AnimatedVisibility(
                visible = state == UiState.Empty, modifier = Modifier.fillMaxSize()
            ) {
                EmptyState(
                    onShowData = {
                        onStateChange(UiState.Content)
                    })
            }
            AnimatedVisibility(
                visible = state == UiState.Content, modifier = Modifier.fillMaxSize()
            ) {
                HomeContent(
                    windowSizeClass = windowSizeClass, onOpenDetails = onOpenDetails
                )
            }
        }
    }
}

@Composable
fun HomeContent(windowSizeClass: WindowSizeClass, onOpenDetails: () -> Unit) {
    val isWide = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    if (isWide) {
        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BalanceCard()
                    Spacer(modifier = Modifier.height(16.dp))
                    AccountsSection()
                }
                Column(modifier = Modifier.weight(1f)) {
                    ExpenseDistribution()
                    Spacer(modifier = Modifier.height(16.dp))
                    TransactionsSection(onOpenDetails = onOpenDetails)
                }
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                BalanceCard()
            }
            item {
                AccountsSection()
            }
            item {
                ExpenseDistribution()
            }
            item {
                TransactionsSection(onOpenDetails = onOpenDetails)
            }
        }
    }
}

@Composable
fun BalanceCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Общий баланс", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "201 000 ₽", fontSize = 34.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Доступно сейчас", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AccountsSection() {
    Column {
        SectionTitle(title = "Счета")
        Spacer(modifier = Modifier.height(8.dp))
        accounts.forEach { account ->
            AccountCard(account = account)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AccountCard(account: Account) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(46.dp).background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center) {
                Icon(imageVector = account.icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = account.name, fontWeight = FontWeight.SemiBold)
                Text(text = account.subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "${formatMoney(account.amount)} ₽", fontWeight = FontWeight.Bold,
                fontSize = 16.sp)
        }
    }
}

@Composable
fun ExpenseDistribution() {
    val categories = listOf("Продукты" to 42, "Транспорт" to 24, "Кафе" to 18, "Другое" to 16)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(title = "Расходы по категориям")
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "18 460 ₽ за текущий период", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            categories.forEach { (name, percent) ->
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(text = name, modifier = Modifier.weight(1f))
                    Text(text = "$percent%", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(5.dp))
                LinearProgressIndicator(progress = { percent / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun TransactionsSection(onOpenDetails: () -> Unit) {
    Column {
        SectionTitle(title = "Последние операции")
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Column {
                transactions.forEachIndexed { index, transaction ->
                    TransactionRow(transaction = transaction, onOpenDetails = onOpenDetails)
                    if (index < transactions.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: Transaction, onOpenDetails: () -> Unit) {
    Column {
        ListItem(
            leadingContent = {
                Box(modifier = Modifier.size(42.dp).background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = transaction.icon, contentDescription = null)
                }
            },
            headlineContent = {
                Text(text = transaction.title, fontWeight = FontWeight.SemiBold)
            },
            supportingContent = {
                Text(text = "${transaction.category} • ${transaction.date}")
            },
            trailingContent = {
                Text(text = "${formatMoney(transaction.amount)} ₽",
                    color = if (transaction.amount >= 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    fontWeight = FontWeight.Bold
                )
            })
        Button(onClick = onOpenDetails, modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, bottom = 12.dp)) {
            Text(text = "Открыть детали")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Детали операции")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад")
                    }
                })
        }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(20.dp)) {
                Box(modifier = Modifier.size(64.dp)
                    .background(color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(text = "Продукты", style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "-4 250 ₽", fontSize = 32.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(24.dp))
                DetailRow(label = "Категория", value = "Еда")
                DetailRow(label = "Дата", value = "23 сентября 2026, 09:40")
                DetailRow(label = "Счёт", value = "Основная карта")
                DetailRow(label = "Статус", value = "Завершена")
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Описание", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Покупка продуктов в магазине.")
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider()
    }
}

@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) {
                Card(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                    Box(modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant))
                }
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun EmptyState(onShowData: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Icon(imageVector = Icons.Default.Wallet, contentDescription = null,
                modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Пока нет финансовых данных",
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Добавьте данные или загрузите демонстрационный набор.",
                style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onShowData) {
                Text(text = "Показать данные")
            }
        }
    }
}

@Composable
fun StateControls(currentState: UiState, onStateChange: (UiState) -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { onStateChange(UiState.Loading) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Загрузка")
            }
            Button(onClick = { onStateChange(UiState.Empty) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Пусто")
            }
            Button(onClick = { onStateChange(UiState.Content) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Данные")
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

fun formatMoney(value: Double): String {
    return String.format("%,.0f", value).replace(",", " ")
}


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Cветлая тема", showBackground = true, showSystemUi = true,
    widthDp = 400, heightDp = 800)
@Composable
fun FinanceHomeContentLightPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(
        DpSize(width = 400.dp, height = 800.dp))
    FinanceTheme(darkTheme = false) {
        FinanceHomeScreen(
            windowSizeClass = windowSizeClass,
            state = UiState.Content,
            darkTheme = false,
            onThemeChange = {},
            onStateChange = {},
            onOpenDetails = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Темная тема", showBackground = true, showSystemUi = true,
    widthDp = 400, heightDp = 800)
@Composable
fun FinanceHomeContentDarkPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(
        DpSize(width = 400.dp, height = 800.dp))
    FinanceTheme(darkTheme = true) {
        FinanceHomeScreen(
            windowSizeClass = windowSizeClass,
            state = UiState.Content,
            darkTheme = true,
            onThemeChange = {},
            onStateChange = {},
            onOpenDetails = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Cветлая тема", showBackground = true, showSystemUi = true,
    widthDp = 400, heightDp = 800)
@Composable
fun FinanceHomeLoadingLightPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(
        DpSize(width = 400.dp, height = 800.dp))
    FinanceTheme(darkTheme = false) {
        FinanceHomeScreen(
            windowSizeClass = windowSizeClass,
            state = UiState.Loading,
            darkTheme = false,
            onThemeChange = {},
            onStateChange = {},
            onOpenDetails = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Темная тема", showBackground = true, showSystemUi = true,
    widthDp = 400, heightDp = 800)
@Composable
fun FinanceHomeLoadingDarkPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(
        DpSize(width = 400.dp, height = 800.dp))
    FinanceTheme(darkTheme = true) {
        FinanceHomeScreen(
            windowSizeClass = windowSizeClass,
            state = UiState.Loading,
            darkTheme = true,
            onThemeChange = {},
            onStateChange = {},
            onOpenDetails = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Cветлая тема", showBackground = true, showSystemUi = true,
    widthDp = 400, heightDp = 800)
@Composable
fun FinanceHomeEmptyLightPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(
        DpSize(width = 400.dp, height = 800.dp))
    FinanceTheme(darkTheme = false) {
        FinanceHomeScreen(
            windowSizeClass = windowSizeClass,
            state = UiState.Empty,
            darkTheme = false,
            onThemeChange = {},
            onStateChange = {},
            onOpenDetails = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Темная тема", showBackground = true, showSystemUi = true,
    widthDp = 400, heightDp = 800)
@Composable
fun FinanceHomeEmptyDarkPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(
        DpSize(width = 400.dp, height = 800.dp))
    FinanceTheme(darkTheme = true) {
        FinanceHomeScreen(
            windowSizeClass = windowSizeClass,
            state = UiState.Empty,
            darkTheme = true,
            onThemeChange = {},
            onStateChange = {},
            onOpenDetails = {}
        )
    }
}

@Preview(name = "Светлая тема", showBackground = true, showSystemUi = true,
    widthDp = 400, heightDp = 800)
@Composable
fun TransactionDetailsLightPreview() {
    FinanceTheme(darkTheme = false) {
        TransactionDetailsScreen(onBack = {})
    }
}

@Preview(name = "Тёмная тема", showBackground = true, showSystemUi = true,
    widthDp = 400, heightDp = 800)
@Composable
fun TransactionDetailsDarkPreview() {
    FinanceTheme(darkTheme = true) {
        TransactionDetailsScreen(onBack = {})
    }
}