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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billing.SubscriptionManager
import com.example.billing.VipSection
import com.example.data.TipCategory
import com.example.ui.dialogs.VipSectionSubscriptionDialog
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageManager
import com.example.ui.theme.BlackOutline
import com.example.ui.theme.DarkText
import com.example.ui.theme.PinkDrawerFill

/**
 * VIP Tips Section Screen.
 *
 * Requirements strictly met:
 * 1. Five independent VIP sections: ALL VIPS, 2+ VIP, 3+ VIP, 5+ VIP, SPECIAL VIP.
 * 2. Every VIP card displays ONLY its title name clearly.
 *    No descriptions, subtitles, odds, prices, subscription info, icons with text,
 *    or additional explanations inside the cards.
 * 3. Compact, clean, modern, professionally designed using existing app theme (PinkDrawerFill + BlackOutline).
 * 4. Tapping an unlocked section opens it immediately.
 * 5. Tapping a locked section opens its dedicated subscription screen with Weekly and Monthly options.
 * 6. Successful payment automatically opens the selected section.
 */
@Composable
fun VipTipsScreen(
    language: AppLanguage,
    onSelectVipCategory: (TipCategory) -> Unit,
    onOpenHistoryVip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val subState by SubscriptionManager.subscriptionState.collectAsState()

    var activeDialogSection by remember { mutableStateOf<VipSection?>(null) }

    fun handleSectionClick(section: VipSection) {
        if (subState.isSectionUnlocked(section)) {
            onSelectVipCategory(section.tipCategory)
        } else {
            activeDialogSection = section
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
            .testTag("vip_tips_hub_screen")
    ) {

        Text(
            text = LanguageManager.getString("vip_tips", language).uppercase(),
            color = DarkText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // VIP Section Card 1: ALL VIPS (Full width for master all-VIPs section)
        VipTitleCard(
            title = VipSection.ALL_VIPS.cardTitle,
            onClick = { handleSectionClick(VipSection.ALL_VIPS) },
            modifier = Modifier.fillMaxWidth(),
            testTag = "vip_card_all_vips"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // VIP Section Cards 2 & 3: 2+ VIP & 3+ VIP (Two columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VipTitleCard(
                title = VipSection.VIP_2_PLUS.cardTitle,
                onClick = { handleSectionClick(VipSection.VIP_2_PLUS) },
                modifier = Modifier.weight(1f),
                testTag = "vip_card_2plus_vip"
            )

            VipTitleCard(
                title = VipSection.VIP_3_PLUS.cardTitle,
                onClick = { handleSectionClick(VipSection.VIP_3_PLUS) },
                modifier = Modifier.weight(1f),
                testTag = "vip_card_3plus_vip"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // VIP Section Cards 4 & 5: 5+ VIP & SPECIAL VIP (Two columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VipTitleCard(
                title = VipSection.VIP_5_PLUS.cardTitle,
                onClick = { handleSectionClick(VipSection.VIP_5_PLUS) },
                modifier = Modifier.weight(1f),
                testTag = "vip_card_5plus_vip"
            )

            VipTitleCard(
                title = VipSection.SPECIAL_VIP.cardTitle,
                onClick = { handleSectionClick(VipSection.SPECIAL_VIP) },
                modifier = Modifier.weight(1f),
                testTag = "vip_card_special_vip"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Settled History VIP access drawer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("drawer_history_vip")
                .background(PinkDrawerFill, RoundedCornerShape(8.dp))
                .border(1.5.dp, BlackOutline, RoundedCornerShape(8.dp))
                .clickable(onClick = onOpenHistoryVip)
                .padding(vertical = 16.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "HISTORY VIP",
                color = DarkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dedicated subscription dialog for the selected VIP section
    if (activeDialogSection != null) {
        val targetSection = activeDialogSection!!
        VipSectionSubscriptionDialog(
            section = targetSection,
            language = language,
            onDismiss = { activeDialogSection = null },
            onSubscriptionSuccess = {
                activeDialogSection = null
                onSelectVipCategory(targetSection.tipCategory)
            }
        )
    }
}

/**
 * Clean VIP Title Card displaying ONLY its title name.
 * No descriptions, subtitles, odds, prices, subscription info, or icons with text.
 */
@Composable
fun VipTitleCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .background(PinkDrawerFill, RoundedCornerShape(8.dp))
            .border(1.5.dp, BlackOutline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = DarkText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )
    }
}
