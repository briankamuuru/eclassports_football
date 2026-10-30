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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TipCategory
import com.example.ui.components.GridDrawerCard
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageManager
import com.example.ui.theme.BlackOutline
import com.example.ui.theme.DarkText
import com.example.ui.theme.PinkDrawerFill

/**
 * History VIP Screen.
 * Contains four independent category drawers arranged in two columns:
 * Row 1: Left: 2+ Tips VIP | Right: 3+ Tips VIP
 * Row 2: Left: 5+ Tips VIP | Right: Special VIP
 *
 * Each drawer has:
 * - Outline: Black (1.5.dp)
 * - Fill colour: Pink
 * - Text: Black or dark readable
 * - Icons: Small, simple, clear
 */
@Composable
fun HistoryVipScreen(
    language: AppLanguage,
    historyCounts: Map<String, Int>,
    onSelectHistoryCategory: (TipCategory) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
            .testTag("history_vip_hub_screen")
    ) {
        // Back Navigation Bar with Pink Drawer Style
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PinkDrawerFill, RoundedCornerShape(8.dp))
                    .border(1.5.dp, BlackOutline, RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack)
                    .testTag("history_vip_back_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = LanguageManager.getString("back", language),
                    tint = BlackOutline,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(PinkDrawerFill, RoundedCornerShape(8.dp))
                    .border(1.5.dp, BlackOutline, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = LanguageManager.getString("history_vip", language),
                    color = DarkText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Text(
            text = "SELECT SETTLED VIP CATEGORY",
            color = DarkText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Four independent category drawers arranged in two columns
        // Row 1: Left: 2+ Tips VIP | Right: 3+ Tips VIP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GridDrawerCard(
                title = LanguageManager.getString("vip_2_plus", language),
                subtitle = "Settled 2.00+ matches",
                icon = Icons.Default.TrendingUp,
                badgeCount = historyCounts[TipCategory.VIP_2_PLUS.id],
                onClick = { onSelectHistoryCategory(TipCategory.VIP_2_PLUS) },
                modifier = Modifier.weight(1f),
                testTag = "drawer_history_cat_vip_2_plus"
            )

            GridDrawerCard(
                title = LanguageManager.getString("vip_3_plus", language),
                subtitle = "Settled 3.00+ matches",
                icon = Icons.Default.MilitaryTech,
                badgeCount = historyCounts[TipCategory.VIP_3_PLUS.id],
                onClick = { onSelectHistoryCategory(TipCategory.VIP_3_PLUS) },
                modifier = Modifier.weight(1f),
                testTag = "drawer_history_cat_vip_3_plus"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Left: 5+ Tips VIP | Right: Special VIP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GridDrawerCard(
                title = LanguageManager.getString("vip_5_plus", language),
                subtitle = "Settled 5.00+ matches",
                icon = Icons.Default.Bolt,
                badgeCount = historyCounts[TipCategory.VIP_5_PLUS.id],
                onClick = { onSelectHistoryCategory(TipCategory.VIP_5_PLUS) },
                modifier = Modifier.weight(1f),
                testTag = "drawer_history_cat_vip_5_plus"
            )

            GridDrawerCard(
                title = LanguageManager.getString("vip_special", language),
                subtitle = "Settled special picks",
                icon = Icons.Default.Star,
                badgeCount = historyCounts[TipCategory.VIP_SPECIAL.id],
                onClick = { onSelectHistoryCategory(TipCategory.VIP_SPECIAL) },
                modifier = Modifier.weight(1f),
                testTag = "drawer_history_cat_vip_special"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
