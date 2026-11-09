package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DateGroupingHelper
import com.example.data.TipEntity
import com.example.ui.components.DailySummaryDrawer
import com.example.ui.components.PredictionCard
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageManager
import com.example.ui.theme.BlackOutline
import com.example.ui.theme.DarkText
import com.example.ui.theme.DateHeadingGolden
import com.example.ui.theme.PinkDrawerFill

@Composable
fun FreeTipsScreen(
    tips: List<TipEntity>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    // Group and sort predictions by match date chronologically,
    // and sort predictions by kickoff time within each date
    val dateGroups = remember(tips) {
        DateGroupingHelper.groupAndSortPredictions(tips)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("free_tips_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

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
                // Date heading: Bright golden yellow, displayed ONCE for the group
                item(key = "header_${dateGroup.dateIso}") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 4.dp, start = 2.dp)
                            .testTag("date_heading_${dateGroup.dateIso}")
                    ) {
                        if (dateGroup.isToday) {
                            Box(
                                modifier = Modifier
                                    .background(DateHeadingGolden, RoundedCornerShape(4.dp))
                                    .border(1.dp, BlackOutline, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TODAY",
                                    color = BlackOutline,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.size(6.dp))
                        }
                        Text(
                            text = dateGroup.formattedDate,
                            color = DateHeadingGolden,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Predictions belonging to that date directly below
                items(
                    items = dateGroup.tips,
                    key = { it.id }
                ) { tip ->
                    PredictionCard(tip = tip)
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
    }
}
