package com.example.ui.dialogs

import androidx.compose.runtime.Composable
import com.example.billing.VipSection
import com.example.ui.locale.AppLanguage

/**
 * Legacy compatibility wrapper that directs to the All VIPs subscription dialog.
 */
@Composable
fun VipPaywallDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    VipSectionSubscriptionDialog(
        section = VipSection.ALL_VIPS,
        language = language,
        onDismiss = onDismiss,
        onSubscriptionSuccess = onDismiss
    )
}
