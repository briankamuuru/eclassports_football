package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import java.util.Locale

/**
 * Football Prediction Card.
 * Complies with all visual rules:
 * - League heading: Bright green
 * - Team names & prediction text: Black / dark text
 * - Odds: Normal dark text when pending, green when won, red when lost
 * - Checkbox: Unticked when pending, ticked when won or lost
 * - NO WRITTEN Won, Lost, or Pending labels.
 */
@Composable
fun PredictionCard(
    tip: TipEntity,
    onSettleClick: () -> Unit,
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
            .testTag("prediction_card_${tip.id}")
            .clickable(onClick = onSettleClick),
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
            // Checkbox for result state (unticked: pending, ticked: won/lost)
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onSettleClick() },
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
                        color = LeagueHeadingBrightGreen, // Bright green
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = tip.kickoffTime,
                        color = DarkText.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
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
            // NO text label for Won/Lost/Pending!
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
