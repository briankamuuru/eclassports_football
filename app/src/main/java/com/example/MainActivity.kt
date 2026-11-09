package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.billing.SubscriptionManager
import com.example.billing.VipSection
import com.example.data.TipCategory
import com.example.ui.MainSection
import com.example.ui.MainViewModel
import com.example.ui.dialogs.VipPaywallDialog
import com.example.ui.dialogs.VipSectionSubscriptionDialog
import com.example.ui.theme.DateHeadingGolden
import com.example.ui.components.SocialFloatingActionButtons
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageManager
import com.example.ui.screens.FreeTipsScreen
import com.example.ui.screens.HistoryCategoryDetailScreen
import com.example.ui.screens.HistoryVipScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VipActiveSubsectionScreen
import com.example.ui.screens.VipTipsScreen
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BlackOutline
import com.example.ui.theme.DarkText
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PinkDrawerFill
import com.example.ui.theme.PinkDrawerFillLight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.util.NotificationHelper.createNotificationChannel(this)
        setContent {
            MyApplicationTheme {
                MainAppShell()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        android.util.Log.i("MainActivity", "Notification permission granted: $isGranted")
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val currentSection by viewModel.currentSection.collectAsState()
    val currentVipCategory by viewModel.currentVipCategory.collectAsState()
    val isHistoryVipOpen by viewModel.isHistoryVipOpen.collectAsState()
    val currentHistoryCategory by viewModel.currentHistoryCategory.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()

    val subState by SubscriptionManager.subscriptionState.collectAsState()
    var showPaywallDialog by remember { mutableStateOf(false) }

    // Tip lists
    val freeTips by viewModel.freeTips.collectAsState()
    val allVipActive by viewModel.allVipActive.collectAsState()
    val vip2PlusActive by viewModel.vip2PlusActive.collectAsState()
    val vip3PlusActive by viewModel.vip3PlusActive.collectAsState()
    val vip5PlusActive by viewModel.vip5PlusActive.collectAsState()
    val vipSpecialActive by viewModel.vipSpecialActive.collectAsState()

    val allVipHistory by viewModel.allVipHistory.collectAsState()
    val vip2PlusHistory by viewModel.vip2PlusHistory.collectAsState()
    val vip3PlusHistory by viewModel.vip3PlusHistory.collectAsState()
    val vip5PlusHistory by viewModel.vip5PlusHistory.collectAsState()
    val vipSpecialHistory by viewModel.vipSpecialHistory.collectAsState()

    val activeCounts = mapOf(
        TipCategory.ALL_VIP.id to allVipActive.size,
        TipCategory.VIP_2_PLUS.id to vip2PlusActive.size,
        TipCategory.VIP_3_PLUS.id to vip3PlusActive.size,
        TipCategory.VIP_5_PLUS.id to vip5PlusActive.size,
        TipCategory.VIP_SPECIAL.id to vipSpecialActive.size
    )

    val historyCounts = mapOf(
        TipCategory.ALL_VIP.id to allVipHistory.size,
        TipCategory.VIP_2_PLUS.id to vip2PlusHistory.size,
        TipCategory.VIP_3_PLUS.id to vip3PlusHistory.size,
        TipCategory.VIP_5_PLUS.id to vip5PlusHistory.size,
        TipCategory.VIP_SPECIAL.id to vipSpecialHistory.size
    )

    // Handle Android system back gestures
    val canGoBack = currentHistoryCategory != null || isHistoryVipOpen || currentVipCategory != null || currentSection != MainSection.FREE_TIPS
    BackHandler(enabled = canGoBack) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                // Main App Top Bar
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "eClassports Football Logo",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LanguageManager.getString("app_title", language),
                                color = DarkText,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    },
                    actions = {
                        // VIP status / Upgrade pill
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .background(
                                    if (subState.isVip) DateHeadingGolden.copy(alpha = 0.25f) else PinkDrawerFill,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.2.dp, BlackOutline, RoundedCornerShape(6.dp))
                                .clickable { showPaywallDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("topbar_vip_pill")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = BlackOutline,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (subState.isVip) "VIP" else "UPGRADE",
                                    color = DarkText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Language pill button
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .background(PinkDrawerFillLight, RoundedCornerShape(6.dp))
                                .border(1.dp, BlackOutline, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${language.flag} ${language.code.uppercase()}",
                                color = DarkText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )

                // Main Section Drawers (Free Tips, VIP Tips, Settings)
                // Complies strictly with section drawer design:
                // - Outline: Black (1.5.dp)
                // - Fill colour: Pink
                // - Text: Black or dark readable colour
                // - Icons: Small, simple, and clear
                // - Shape: Compact rectangular or slightly rounded boxes
                // - Spacing: Minimal, consistent padding and margins
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MainSectionDrawerItem(
                        title = LanguageManager.getString("free_tips", language),
                        icon = Icons.Default.CardGiftcard,
                        isSelected = currentSection == MainSection.FREE_TIPS && currentVipCategory == null && !isHistoryVipOpen && currentHistoryCategory == null,
                        onClick = { viewModel.selectSection(MainSection.FREE_TIPS) },
                        modifier = Modifier.weight(1f),
                        testTag = "main_drawer_free_tips"
                    )

                    MainSectionDrawerItem(
                        title = LanguageManager.getString("vip_tips", language),
                        icon = Icons.Default.WorkspacePremium,
                        isSelected = currentSection == MainSection.VIP_TIPS,
                        onClick = { viewModel.selectSection(MainSection.VIP_TIPS) },
                        modifier = Modifier.weight(1f),
                        testTag = "main_drawer_vip_tips"
                    )

                    MainSectionDrawerItem(
                        title = LanguageManager.getString("settings", language),
                        icon = Icons.Default.Settings,
                        isSelected = currentSection == MainSection.SETTINGS,
                        onClick = { viewModel.selectSection(MainSection.SETTINGS) },
                        modifier = Modifier.weight(1f),
                        testTag = "main_drawer_settings"
                    )
                }
            }
        },
        containerColor = BackgroundCanvas
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // 1. History VIP Category Detail Screen
                currentHistoryCategory != null -> {
                    val category = currentHistoryCategory!!
                    val historyTips = when (category) {
                        TipCategory.ALL_VIP -> allVipHistory
                        TipCategory.VIP_2_PLUS -> vip2PlusHistory
                        TipCategory.VIP_3_PLUS -> vip3PlusHistory
                        TipCategory.VIP_5_PLUS -> vip5PlusHistory
                        TipCategory.VIP_SPECIAL -> vipSpecialHistory
                        TipCategory.FREE -> emptyList()
                    }
                    HistoryCategoryDetailScreen(
                        category = category,
                        tips = historyTips,
                        language = language,
                        onBack = { viewModel.navigateBack() }
                    )
                }

                // 2. History VIP Hub Screen
                isHistoryVipOpen -> {
                    HistoryVipScreen(
                        language = language,
                        historyCounts = historyCounts,
                        onSelectHistoryCategory = { cat -> viewModel.selectHistoryCategory(cat) },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                // 3. Active VIP Subsection Screen (Guarded by section entitlement)
                currentVipCategory != null -> {
                    val category = currentVipCategory!!
                    val vipSection = VipSection.fromTipCategory(category)
                    val isUnlocked = vipSection == null || SubscriptionManager.isSectionUnlocked(vipSection)

                    LaunchedEffect(category) {
                        SubscriptionManager.refreshCustomerInfo()
                    }

                    if (!isUnlocked && vipSection != null) {
                        VipSectionSubscriptionDialog(
                            section = vipSection,
                            language = language,
                            onDismiss = { viewModel.navigateBack() },
                            onSubscriptionSuccess = {
                                // Section unlocked
                            }
                        )
                    } else {
                        val activeTips = when (category) {
                            TipCategory.ALL_VIP -> allVipActive
                            TipCategory.VIP_2_PLUS -> vip2PlusActive
                            TipCategory.VIP_3_PLUS -> vip3PlusActive
                            TipCategory.VIP_5_PLUS -> vip5PlusActive
                            TipCategory.VIP_SPECIAL -> vipSpecialActive
                            TipCategory.FREE -> emptyList()
                        }
                        VipActiveSubsectionScreen(
                            category = category,
                            tips = activeTips,
                            language = language,
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }

                // 4. Main Section screens
                currentSection == MainSection.FREE_TIPS -> {
                    FreeTipsScreen(
                        tips = freeTips,
                        language = language
                    )
                }

                currentSection == MainSection.VIP_TIPS -> {
                    VipTipsScreen(
                        language = language,
                        onSelectVipCategory = { cat -> viewModel.selectVipCategory(cat) },
                        onOpenHistoryVip = { viewModel.openHistoryVip() }
                    )
                }

                currentSection == MainSection.SETTINGS -> {
                    SettingsScreen(
                        currentLanguage = language,
                        onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                        onRefreshData = { viewModel.refreshFirestoreTips() }
                    )
                }
            }

            // VIP Paywall Dialog (Google Play Billing & RevenueCat)
            if (showPaywallDialog) {
                VipSectionSubscriptionDialog(
                    section = VipSection.ALL_VIPS,
                    language = language,
                    onDismiss = { showPaywallDialog = false },
                    onSubscriptionSuccess = { showPaywallDialog = false }
                )
            }

            // Floating WhatsApp (bottom-right) and Telegram (bottom-left) contact icons
            SocialFloatingActionButtons()
        }
    }
}

/**
 * Main Section Drawer item.
 * Black outline, pink fill, black/dark text, small clear icons, compact shape.
 */
@Composable
fun MainSectionDrawerItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .background(PinkDrawerFill, RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.dp else 1.5.dp,
                color = BlackOutline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BlackOutline,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                color = DarkText,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
