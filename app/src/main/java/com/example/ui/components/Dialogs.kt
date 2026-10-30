package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TipCategory
import com.example.data.TipEntity
import com.example.data.TipStatus
import com.example.ui.locale.AppLanguage
import com.example.ui.locale.LanguageManager
import com.example.ui.theme.BlackOutline
import com.example.ui.theme.DarkText
import com.example.ui.theme.OddsLostRed
import com.example.ui.theme.OddsWonGreen
import com.example.ui.theme.PinkDrawerFill

@Composable
fun SettleTipDialog(
    tip: TipEntity,
    language: AppLanguage,
    onSettle: (TipStatus) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val titleText = LanguageManager.getString("settle_tip_title", language)
    val descText = LanguageManager.getString("settle_tip_desc", language)
    val wonText = LanguageManager.getString("mark_won", language)
    val lostText = LanguageManager.getString("mark_lost", language)
    val pendingText = LanguageManager.getString("mark_pending", language)
    val deleteText = LanguageManager.getString("delete", language)
    val cancelText = LanguageManager.getString("cancel", language)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titleText,
                color = DarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "${tip.league} • ${tip.kickoffTime}",
                    color = DarkText.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Text(
                    text = "${tip.homeTeam} vs ${tip.awayTeam}",
                    color = DarkText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${tip.prediction} • Odds: ${String.format(java.util.Locale.US, "%.2f", tip.odds)}",
                    color = DarkText,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = descText,
                    color = DarkText.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onSettle(TipStatus.WON) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_mark_won_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = OddsWonGreen),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = wonText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onSettle(TipStatus.LOST) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_mark_lost_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = OddsLostRed),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = lostText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onSettle(TipStatus.PENDING) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_mark_pending_btn"),
                    border = BorderStroke(1.dp, BlackOutline),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(text = pendingText, color = DarkText, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_delete_tip_btn")
                ) {
                    Text(text = deleteText, color = OddsLostRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = cancelText, color = DarkText)
            }
        },
        shape = RoundedCornerShape(10.dp),
        containerColor = Color.White
    )
}

@Composable
fun AddTipDialog(
    initialCategory: TipCategory,
    language: AppLanguage,
    onSave: (TipEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var matchDate by remember { mutableStateOf("2026-10-12") }
    var kickoffTime by remember { mutableStateOf("17:00") }
    var league by remember { mutableStateOf("Premier League") }
    var homeTeam by remember { mutableStateOf("") }
    var awayTeam by remember { mutableStateOf("") }
    var prediction by remember { mutableStateOf("Home Win") }
    var oddsStr by remember { mutableStateOf("1.80") }
    var category by remember { mutableStateOf(initialCategory) }

    val addTitle = LanguageManager.getString("add_tip", language)
    val saveText = LanguageManager.getString("save", language)
    val cancelText = LanguageManager.getString("cancel", language)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = addTitle,
                color = DarkText,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Category selection
                Text(
                    text = "${LanguageManager.getString("category", language)}:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TipCategory.entries.take(3).forEach { cat ->
                        Button(
                            onClick = { category = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (category == cat) PinkDrawerFill else Color(0xFFF3F4F6)
                            ),
                            border = BorderStroke(1.dp, BlackOutline),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = cat.defaultTitle.split(" ").first(),
                                color = DarkText,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TipCategory.entries.drop(3).forEach { cat ->
                        Button(
                            onClick = { category = cat },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (category == cat) PinkDrawerFill else Color(0xFFF3F4F6)
                            ),
                            border = BorderStroke(1.dp, BlackOutline),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = cat.defaultTitle,
                                color = DarkText,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = matchDate,
                    onValueChange = { matchDate = it },
                    label = { Text(LanguageManager.getString("match_date", language)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_match_date")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = kickoffTime,
                    onValueChange = { kickoffTime = it },
                    label = { Text(LanguageManager.getString("kickoff_time", language)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_kickoff_time")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = league,
                    onValueChange = { league = it },
                    label = { Text(LanguageManager.getString("league", language)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_league")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = homeTeam,
                        onValueChange = { homeTeam = it },
                        label = { Text(LanguageManager.getString("home_team", language)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_home_team")
                    )
                    OutlinedTextField(
                        value = awayTeam,
                        onValueChange = { awayTeam = it },
                        label = { Text(LanguageManager.getString("away_team", language)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_away_team")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = prediction,
                    onValueChange = { prediction = it },
                    label = { Text(LanguageManager.getString("prediction", language)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_prediction")
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = oddsStr,
                    onValueChange = { oddsStr = it },
                    label = { Text(LanguageManager.getString("odds", language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_odds")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedOdds = oddsStr.toDoubleOrNull() ?: 1.50
                    if (homeTeam.isNotBlank() && awayTeam.isNotBlank()) {
                        onSave(
                            TipEntity(
                                category = category.id,
                                matchDate = matchDate.trim(),
                                kickoffTime = kickoffTime.trim(),
                                league = league.trim(),
                                homeTeam = homeTeam.trim(),
                                awayTeam = awayTeam.trim(),
                                prediction = prediction.trim(),
                                odds = parsedOdds,
                                status = TipStatus.PENDING.name
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PinkDrawerFill),
                border = BorderStroke(1.5.dp, BlackOutline),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("dialog_save_tip_btn")
            ) {
                Text(text = saveText, color = DarkText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = cancelText, color = DarkText)
            }
        },
        shape = RoundedCornerShape(10.dp),
        containerColor = Color.White
    )
}
