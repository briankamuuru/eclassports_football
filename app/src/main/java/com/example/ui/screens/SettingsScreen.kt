package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.util.NotificationHelper
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.TipCategory
import com.example.data.TipEntity
import com.example.ui.components.AddTipDialog
import com.example.ui.components.DrawerCard
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageManager
import com.example.ui.theme.BlackOutline
import com.example.ui.theme.DarkText
import com.example.ui.theme.PinkDrawerFill

@Composable
fun SettingsScreen(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onResetData: () -> Unit,
    onAddTip: (TipEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAddTipDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notifications enabled!", Toast.LENGTH_SHORT).show()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Free VIP Access Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PinkDrawerFill, RoundedCornerShape(8.dp))
                .border(1.5.dp, BlackOutline, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = BlackOutline,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column {
                    Text(
                        text = LanguageManager.getString("free_access_banner", currentLanguage),
                        color = DarkText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = LanguageManager.getString("free_access_desc", currentLanguage),
                        color = DarkText.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        Text(
            text = LanguageManager.getString("settings", currentLanguage).uppercase(),
            color = DarkText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
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

        // Drawer 2: Add New Prediction
        DrawerCard(
            title = LanguageManager.getString("add_tip", currentLanguage),
            subtitle = "Custom fixture with auto date grouping",
            icon = Icons.Default.AddCircle,
            onClick = { showAddTipDialog = true },
            testTag = "settings_drawer_add_tip"
        )

        // Drawer 3: Privacy Policy
        DrawerCard(
            title = LanguageManager.getString("privacy_policy", currentLanguage),
            subtitle = "Data protection & sports disclaimer",
            icon = Icons.Default.PrivacyTip,
            onClick = { showPrivacyDialog = true },
            testTag = "settings_drawer_privacy"
        )

        // Drawer 4: About eClassports
        DrawerCard(
            title = LanguageManager.getString("about", currentLanguage),
            subtitle = "Version 2.4 • Analytical football tips",
            icon = Icons.Default.Info,
            onClick = { showAboutDialog = true },
            testTag = "settings_drawer_about"
        )

        // Drawer 5: Push Notifications & Alerts
        DrawerCard(
            title = LanguageManager.getString("notifications", currentLanguage),
            subtitle = LanguageManager.getString("notifications_desc", currentLanguage),
            icon = Icons.Default.Notifications,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                showNotificationDialog = true
            },
            testTag = "settings_drawer_notifications"
        )

        // Drawer 6: Reset Demo Data
        DrawerCard(
            title = LanguageManager.getString("reset_data", currentLanguage),
            subtitle = LanguageManager.getString("reset_data_desc", currentLanguage),
            icon = Icons.Default.RestartAlt,
            onClick = { showResetConfirmDialog = true },
            testTag = "settings_drawer_reset"
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
                Text(
                    text = LanguageManager.getString("about", currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DarkText
                )
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

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = LanguageManager.getString("reset_data", currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DarkText
                )
            },
            text = {
                Text(
                    text = LanguageManager.getString("reset_data_desc", currentLanguage),
                    color = DarkText,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onResetData()
                    showResetConfirmDialog = false
                }) {
                    Text(text = "Reset", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(LanguageManager.getString("cancel", currentLanguage), color = DarkText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(10.dp)
        )
    }

    // Notification Dialog
    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = {
                Text(
                    text = LanguageManager.getString("notifications", currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DarkText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "eClassports Football Push Notifications alert you whenever new Free tips, 2+ Tips VIP, 3+ Tips VIP, or Special VIP banker predictions are published.",
                        color = DarkText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PinkDrawerFill, RoundedCornerShape(6.dp))
                            .border(1.dp, BlackOutline, RoundedCornerShape(6.dp))
                            .clickable {
                                NotificationHelper.showTipNotification(
                                    context = context,
                                    title = LanguageManager.getString("test_notification_title", currentLanguage),
                                    message = LanguageManager.getString("test_notification_body", currentLanguage)
                                )
                                Toast.makeText(context, "Push alert dispatched!", Toast.LENGTH_SHORT).show()
                                showNotificationDialog = false
                            }
                            .padding(vertical = 10.dp, horizontal = 12.dp)
                            .testTag("send_test_notification_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LanguageManager.getString("send_test_notification", currentLanguage),
                            color = DarkText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text(LanguageManager.getString("cancel", currentLanguage), color = DarkText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(10.dp)
        )
    }

    // Add Tip Dialog
    if (showAddTipDialog) {
        AddTipDialog(
            initialCategory = TipCategory.FREE,
            language = currentLanguage,
            onSave = { tip ->
                onAddTip(tip)
                showAddTipDialog = false
            },
            onDismiss = { showAddTipDialog = false }
        )
    }
}
