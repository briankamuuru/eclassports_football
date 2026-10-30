package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
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
 * VIP Tips Section Hub Screen.
 * Implements the required 2-column by 3-row grid layout:
 * Row 1: Left: 2+ Tips VIP  | Right: 3+ Tips VIP
 * Row 2: Left: 5+ Tips VIP  | Right: Special VIP
 * Row 3: Left: History VIP  | Right: Empty space
 *
 * All drawers styled with:
 * - Outline: Black (1.5.dp)
 * - Fill colour: Pink
 * - Text: Black / Dark readable
 * - Icons: Small, simple, clear
 * - Shape: Compact rectangular or slightly rounded boxes
 */
@Composable
fun VipTipsScreen(
    language: AppLanguage,
    activeCounts: Map<String, Int>,
    onSelectVipCategory: (TipCategory) -> Unit,
    onOpenHistoryVip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
            .testTag("vip_tips_hub_screen")
    ) {
        // Free VIP Access Notice Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(PinkDrawerFill, RoundedCornerShape(8.dp))
                .border(1.5.dp, BlackOutline, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp)
                .testTag("vip_free_access_banner")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = BlackOutline,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Column {
                    Text(
                        text = LanguageManager.getString("free_access_banner", language),
                        color = DarkText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = LanguageManager.getString("free_access_desc", language),
                        color = DarkText.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        Text(
            text = LanguageManager.getString("vip_tips", language).uppercase(),
            color = DarkText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // VIP Subsection Layout: Two-Column, Three-Row Grid
        // Row 1: Left: 2+ Tips VIP | Right: 3+ Tips VIP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GridDrawerCard(
                title = LanguageManager.getString("vip_2_plus", language),
                subtitle = "Safe 2.00+ odds",
                icon = Icons.Default.TrendingUp,
                badgeCount = activeCounts[TipCategory.VIP_2_PLUS.id],
                onClick = { onSelectVipCategory(TipCategory.VIP_2_PLUS) },
                modifier = Modifier.weight(1f),
                testTag = "drawer_vip_2_plus"
            )

            GridDrawerCard(
                title = LanguageManager.getString("vip_3_plus", language),
                subtitle = "Balanced 3.00+ odds",
                icon = Icons.Default.MilitaryTech,
                badgeCount = activeCounts[TipCategory.VIP_3_PLUS.id],
                onClick = { onSelectVipCategory(TipCategory.VIP_3_PLUS) },
                modifier = Modifier.weight(1f),
                testTag = "drawer_vip_3_plus"
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
                subtitle = "High 5.00+ odds",
                icon = Icons.Default.Bolt,
                badgeCount = activeCounts[TipCategory.VIP_5_PLUS.id],
                onClick = { onSelectVipCategory(TipCategory.VIP_5_PLUS) },
                modifier = Modifier.weight(1f),
                testTag = "drawer_vip_5_plus"
            )

            GridDrawerCard(
                title = LanguageManager.getString("vip_special", language),
                subtitle = "Top banker tips",
                icon = Icons.Default.Star,
                badgeCount = activeCounts[TipCategory.VIP_SPECIAL.id],
                onClick = { onSelectVipCategory(TipCategory.VIP_SPECIAL) },
                modifier = Modifier.weight(1f),
                testTag = "drawer_vip_special"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3: Left: History VIP | Right: Empty space
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GridDrawerCard(
                title = LanguageManager.getString("history_vip", language),
                subtitle = "Settled VIP records",
                icon = Icons.Default.History,
                onClick = onOpenHistoryVip,
                modifier = Modifier.weight(1f),
                testTag = "drawer_history_vip"
            )

            // Right drawer: Empty space
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
