package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DateGroupingHelper
import com.example.data.TipCategory
import com.example.data.TipEntity
import com.example.data.TipStatus
import com.example.ui.components.AddTipDialog
import com.example.ui.components.DailySummaryDrawer
import com.example.ui.components.PredictionCard
import com.example.ui.components.SettleTipDialog
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageManager
import com.example.ui.theme.BlackOutline
import com.example.ui.theme.DarkText
import com.example.ui.theme.DateHeadingGolden
import com.example.ui.theme.PinkDrawerFill

@Composable
fun VipActiveSubsectionScreen(
    category: TipCategory,
    tips: List<TipEntity>,
    language: AppLanguage,
    onBack: () -> Unit,
    onSettleTip: (Long, TipStatus) -> Unit,
    onDeleteTip: (TipEntity) -> Unit,
    onAddTip: (TipEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTipForSettle by remember { mutableStateOf<TipEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val categoryTitleKey = when (category) {
        TipCategory.VIP_2_PLUS -> "vip_2_plus"
        TipCategory.VIP_3_PLUS -> "vip_3_plus"
        TipCategory.VIP_5_PLUS -> "vip_5_plus"
        TipCategory.VIP_SPECIAL -> "vip_special"
        TipCategory.FREE -> "free_tips"
    }
    val categoryTitle = LanguageManager.getString(categoryTitleKey, language)

    // Group and sort predictions by match date chronologically,
    // and sort predictions by kickoff time within each date
    val dateGroups = remember(tips) {
        DateGroupingHelper.groupAndSortPredictions(tips)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("vip_active_screen_${category.id}")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header Bar with Back Button & Category Drawer Title
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PinkDrawerFill, RoundedCornerShape(8.dp))
                            .border(1.5.dp, BlackOutline, RoundedCornerShape(8.dp))
                            .clickable(onClick = onBack)
                            .testTag("vip_active_back_btn"),
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
                            text = categoryTitle,
                            color = DarkText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            if (dateGroups.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LanguageManager.getString("empty_tips", language),
                            color = DarkText.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Iterate through each date group
            dateGroups.forEach { dateGroup ->
                // Date heading: Bright golden yellow, displayed ONCE per date group
                item(key = "header_${dateGroup.dateIso}") {
                    Text(
                        text = dateGroup.formattedDate,
                        color = DateHeadingGolden, // Bright golden yellow
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 4.dp, start = 2.dp)
                            .testTag("date_heading_${dateGroup.dateIso}")
                    )
                }

                // Predictions belonging to that date directly below
                items(
                    items = dateGroup.tips,
                    key = { it.id }
                ) { tip ->
                    PredictionCard(
                        tip = tip,
                        onSettleClick = { selectedTipForSettle = tip }
                    )
                }

                // Daily Summary Drawer: immediately after the last prediction of this date
                item(key = "summary_${dateGroup.dateIso}") {
                    DailySummaryDrawer(
                        formattedDate = dateGroup.formattedDate,
                        numberOfOdds = dateGroup.numberOfOdds,
                        accumulatedOdds = dateGroup.accumulatedOdds,
                        language = language
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        // Floating Action Button to add predictions
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_vip_tip"),
            containerColor = PinkDrawerFill,
            contentColor = BlackOutline,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Prediction", tint = BlackOutline)
        }
    }

    selectedTipForSettle?.let { tip ->
        SettleTipDialog(
            tip = tip,
            language = language,
            onSettle = { status ->
                onSettleTip(tip.id, status)
                selectedTipForSettle = null
            },
            onDelete = {
                onDeleteTip(tip)
                selectedTipForSettle = null
            },
            onDismiss = { selectedTipForSettle = null }
        )
    }

    if (showAddDialog) {
        AddTipDialog(
            initialCategory = category,
            language = language,
            onSave = { newTip ->
                onAddTip(newTip)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}
