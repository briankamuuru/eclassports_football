package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DrawerCard
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageManager
import com.example.ui.theme.BlackOutline
import com.example.ui.theme.DarkText

import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.runtime.collectAsState
import com.example.billing.SubscriptionManager
import com.example.billing.VipSection
import com.example.ui.dialogs.VipSectionSubscriptionDialog

@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onRefreshData: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val subState by SubscriptionManager.subscriptionState.collectAsState()
    var showPaywallDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = LanguageManager.getString("settings", currentLanguage).uppercase(),
            color = DarkText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )

        // Drawer 0: VIP Subscription & Google Play Billing
        DrawerCard(
            title = LanguageManager.getString("vip_subscription", currentLanguage),
            subtitle = if (subState.isVip) {
                "Active VIP Member • ${subState.unlockedSections.size} Section(s) Unlocked"
            } else {
                "Unlock VIP predictions • Google Play Billing"
            },
            icon = Icons.Default.WorkspacePremium,
            badgeText = if (subState.isVip) "ACTIVE" else "PLANS",
            onClick = { showPaywallDialog = true },
            testTag = "settings_drawer_subscription"
        )

        // Drawer 1: Language Selection
        DrawerCard(
            title = LanguageManager.getString("language", currentLanguage),
            subtitle = "${currentLanguage.flag} ${currentLanguage.displayName}",
            icon = Icons.Default.Language,
            badgeText = currentLanguage.code.uppercase(),
            onClick = { showLanguageDialog = true },
            testTag = "settings_drawer_language"
        )

        // Drawer 2: Privacy Policy
        DrawerCard(
            title = LanguageManager.getString("privacy_policy", currentLanguage),
            subtitle = "Data protection & sports disclaimer",
            icon = Icons.Default.PrivacyTip,
            onClick = { showPrivacyDialog = true },
            testTag = "settings_drawer_privacy"
        )

        // Drawer 3: About eClassports
        DrawerCard(
            title = LanguageManager.getString("about", currentLanguage),
            subtitle = "Version 2.4 • Analytical football tips",
            icon = Icons.Default.Info,
            onClick = { showAboutDialog = true },
            testTag = "settings_drawer_about"
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = LanguageManager.getString("select_language", currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DarkText
                )
            },
            text = {
                Column {
                    AppLanguage.entries.forEach { lang ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageChange(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp)
                                .testTag("lang_option_${lang.code}")
                        ) {
                            RadioButton(
                                selected = (lang == currentLanguage),
                                onClick = {
                                    onLanguageChange(lang)
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = BlackOutline,
                                    unselectedColor = BlackOutline
                                )
                            )
                            Text(
                                text = "${lang.flag} ${lang.displayName}",
                                color = DarkText,
                                fontSize = 14.sp,
                                fontWeight = if (lang == currentLanguage) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(LanguageManager.getString("cancel", currentLanguage), color = DarkText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(10.dp)
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = LanguageManager.getString("privacy_policy", currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DarkText
                )
            },
            text = {
                Text(
                    text = LanguageManager.getString("privacy_policy_content", currentLanguage),
                    color = DarkText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("OK", color = DarkText, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(10.dp)
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "eClassports Football Crest",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = LanguageManager.getString("about", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = DarkText
                    )
                }
            },
            text = {
                Text(
                    text = LanguageManager.getString("about_content", currentLanguage),
                    color = DarkText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = DarkText, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(10.dp)
        )
    }

    if (showPaywallDialog) {
        VipSectionSubscriptionDialog(
            section = VipSection.ALL_VIPS,
            language = currentLanguage,
            onDismiss = { showPaywallDialog = false },
            onSubscriptionSuccess = { showPaywallDialog = false }
        )
    }
}

