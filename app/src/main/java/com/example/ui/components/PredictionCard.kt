package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TipEntity
import com.example.data.TipStatus
import com.example.ui.theme.BlackOutline
import com.example.ui.theme.DarkText
import com.example.ui.theme.GrayBorder
import com.example.ui.theme.LeagueHeadingBrightGreen
import com.example.ui.theme.OddsLostRed
import com.example.ui.theme.OddsPendingColor
import com.example.ui.theme.OddsWonGreen
import com.example.ui.theme.PinkDrawerFillLight
import java.util.Locale

/**
 * Football Prediction Card (Read-only client view).
 * - League heading: Bright green
 * - Team names & prediction text: Black / dark text
 * - Odds: Normal dark text when pending, green when won, red when lost
 * - Checkbox indicator: Unticked when pending, ticked when won or lost
 * - NO WRITTEN Won, Lost, or Pending labels.
 * - Non-editable / Read-only for standard users.
 */
@Composable
fun PredictionCard(
    tip: TipEntity,
    modifier: Modifier = Modifier
) {
    val isSettled = tip.status != TipStatus.PENDING.name
    val isWon = tip.status == TipStatus.WON.name
    val isLost = tip.status == TipStatus.LOST.name

    // Odds colour based on settlement state:
    // Normal dark text when pending, green when won, red when lost
    val oddsColor = when {
        isWon -> OddsWonGreen
        isLost -> OddsLostRed
        else -> OddsPendingColor
    }

    // Checkbox is ticked if settled (won or lost), unticked if pending
    val isChecked = isSettled

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("prediction_card_${tip.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, GrayBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Read-only indicator checkbox for result state (unticked: pending, ticked: won/lost)
            Checkbox(
                checked = isChecked,
                onCheckedChange = null,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("checkbox_tip_${tip.id}"),
                colors = CheckboxDefaults.colors(
                    checkedColor = if (isWon) OddsWonGreen else OddsLostRed,
                    checkmarkColor = Color.White,
                    uncheckedColor = BlackOutline
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Main Match & Prediction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // League heading (Bright green) & Kickoff time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tip.league,
                        color = LeagueHeadingBrightGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (tip.category != "free") {
                            val badgeLabel = when (tip.category) {
                                "vip_2_plus" -> "2+ VIP"
                                "vip_3_plus" -> "3+ VIP"
                                "vip_5_plus" -> "5+ VIP"
                                "vip_special" -> "SPECIAL"
                                else -> tip.categoryName.ifEmpty { "VIP" }
                            }
                            Box(
                                modifier = Modifier
                                    .background(PinkDrawerFillLight, RoundedCornerShape(3.dp))
                                    .border(0.75.dp, BlackOutline, RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    color = DarkText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Text(
                            text = tip.kickoffTime,
                            color = DarkText.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Teams: Black or dark text
                Text(
                    text = "${tip.homeTeam} vs ${tip.awayTeam}",
                    color = DarkText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Prediction text: Black or dark text
                Text(
                    text = tip.prediction,
                    color = DarkText.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Odds value: Normal dark when pending, green when won, red when lost
            Text(
                text = String.format(Locale.US, "%.2f", tip.odds),
                color = oddsColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.testTag("odds_tip_${tip.id}")
            )
        }
    }
}
