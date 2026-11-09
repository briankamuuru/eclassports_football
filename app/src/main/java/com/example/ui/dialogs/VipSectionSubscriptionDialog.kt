package com.example.ui.dialogs

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.billing.PlanDuration
import com.example.billing.SectionPlan
import com.example.billing.SubscriptionManager
import com.example.billing.VipSection
import com.example.ui.locale.AppLanguage
import com.example.ui.theme.BlackOutline
import com.example.ui.theme.DarkText
import com.example.ui.theme.DateHeadingGolden
import com.example.ui.theme.OddsWonGreen
import com.example.ui.theme.PinkDrawerFill
import com.example.ui.theme.PinkDrawerFillLight

/**
 * Dedicated subscription dialog for an independent VIP section.
 * Presents exactly two subscription options:
 * - Weekly Subscription (7 days access)
 * - Monthly Subscription (1 month access)
 */
@Composable
fun VipSectionSubscriptionDialog(
    section: VipSection,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSubscriptionSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val subState by SubscriptionManager.subscriptionState.collectAsState()

    val availablePlans = SubscriptionManager.getPlansForSection(section)
    var selectedDuration by remember { mutableStateOf(PlanDuration.MONTHLY) }

    val selectedPlan = availablePlans.firstOrNull { it.duration == selectedDuration }
        ?: availablePlans.firstOrNull()

    var isProcessing by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .testTag("vip_section_subscription_dialog_${section.id}"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(2.dp, BlackOutline),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row with Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "eClassports Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "eClassports Football",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = DarkText
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("section_sub_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = BlackOutline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Section Badge Header
                Box(
                    modifier = Modifier
                        .background(PinkDrawerFill, RoundedCornerShape(6.dp))
                        .border(1.5.dp, BlackOutline, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = section.cardTitle,
                        color = DarkText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Unlock ${section.cardTitle} Access",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkText,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Select a weekly or monthly subscription to access live predictions and updates for this section.",
                    fontSize = 12.sp,
                    color = DarkText.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // The exactly two subscription plans for this section:
                availablePlans.forEach { plan ->
                    val isSelected = (plan.duration == selectedDuration)
                    SectionPlanCard(
                        plan = plan,
                        isSelected = isSelected,
                        onClick = { selectedDuration = plan.duration }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Subscribe Button (Triggers Google Play Billing through RevenueCat)
                Button(
                    onClick = {
                        if (activity != null && selectedPlan != null) {
                            isProcessing = true
                            SubscriptionManager.purchaseSectionPlan(activity, section, selectedPlan) { success, msg ->
                                isProcessing = false
                                Toast.makeText(
                                    context,
                                    msg ?: if (success) "Subscription active!" else "Failed",
                                    Toast.LENGTH_SHORT
                                ).show()

                                if (success) {
                                    onSubscriptionSuccess()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Activity not ready", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("section_sub_subscribe_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PinkDrawerFill,
                        contentColor = DarkText
                    ),
                    border = BorderStroke(1.5.dp, BlackOutline),
                    enabled = !isProcessing && selectedPlan != null
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = BlackOutline,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = BlackOutline,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SUBSCRIBE WITH GOOGLE PLAY",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Restore Purchases Button
                TextButton(
                    onClick = {
                        isProcessing = true
                        SubscriptionManager.restorePurchases { success, msg ->
                            isProcessing = false
                            Toast.makeText(context, msg ?: if (success) "Restored" else "No purchases", Toast.LENGTH_LONG).show()
                            if (SubscriptionManager.isSectionUnlocked(section)) {
                                onSubscriptionSuccess()
                            }
                        }
                    },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("section_sub_restore_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = DarkText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Restore Purchases",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    }
                }

                // Footer Notice
                Text(
                    text = "Subscription renews automatically via Google Play. Access expires automatically after the subscription period unless renewed. Cancel anytime in Google Play Subscriptions.",
                    fontSize = 10.sp,
                    color = DarkText.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionPlanCard(
    plan: SectionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("plan_card_${plan.duration.name.lowercase()}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PinkDrawerFillLight else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BlackOutline else Color.LightGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = BlackOutline,
                    unselectedColor = BlackOutline
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (plan.duration == PlanDuration.WEEKLY) "WEEKLY SUBSCRIPTION" else "MONTHLY SUBSCRIPTION",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = DarkText
                    )
                    if (plan.duration == PlanDuration.MONTHLY) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(DateHeadingGolden, RoundedCornerShape(4.dp))
                                .border(0.75.dp, BlackOutline, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "POPULAR",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BlackOutline
                            )
                        }
                    }
                }

                Text(
                    text = plan.planTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText.copy(alpha = 0.85f)
                )

                Text(
                    text = plan.accessDescription,
                    fontSize = 10.sp,
                    color = DarkText.copy(alpha = 0.65f),
                    lineHeight = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = plan.priceFormatted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = DarkText
                )
                Text(
                    text = plan.periodFormatted,
                    fontSize = 10.sp,
                    color = DarkText.copy(alpha = 0.6f)
                )
            }
        }
    }
}
