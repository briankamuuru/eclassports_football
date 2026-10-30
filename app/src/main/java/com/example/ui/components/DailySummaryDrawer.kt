package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageManager
import com.example.ui.theme.BlackOutline
import com.example.ui.theme.DarkText
import com.example.ui.theme.PinkDrawerFill
import java.util.Locale

/**
 * Compact Daily Summary Drawer displayed at the end of each date group.
 * Matches section drawer styling:
 * - Outline: Black (1.5.dp)
 * - Fill colour: Pink
 * - Displays Number of Odds and Accumulated Odds (multiplied product, 2 decimals)
 */
@Composable
fun DailySummaryDrawer(
    formattedDate: String,
    numberOfOdds: Int,
    accumulatedOdds: Double,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val summaryTitleText = LanguageManager.getString("daily_summary", language)
    val numberOfOddsText = LanguageManager.getString("number_of_odds", language)
    val accumulatedOddsText = LanguageManager.getString("accumulated_odds", language)

    val formattedAccOdds = String.format(Locale.US, "%.2f", accumulatedOdds)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("daily_summary_drawer_${formattedDate.replace(" ", "_")}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = PinkDrawerFill
        ),
        border = BorderStroke(1.5.dp, BlackOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Text(
                text = "$formattedDate — $summaryTitleText",
                color = DarkText,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$numberOfOddsText: ",
                        color = DarkText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$numberOfOdds",
                        color = DarkText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$accumulatedOddsText: ",
                        color = DarkText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formattedAccOdds,
                        color = DarkText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
