package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R

object SocialLinksHelper {
    const val WHATSAPP_URL = "https://wa.me/254742682130"
    const val TELEGRAM_URL = "https://t.me/clubfootbll"

    fun openWhatsApp(context: Context) {
        val uri = Uri.parse(WHATSAPP_URL)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            intent.setPackage("com.whatsapp")
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                intent.setPackage("com.whatsapp.w4b")
                context.startActivity(intent)
            } catch (_: Exception) {
                intent.setPackage(null)
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("SocialLinksHelper", "Error opening WhatsApp: ${e.message}")
                }
            }
        }
    }

    fun openTelegram(context: Context) {
        val uri = Uri.parse(TELEGRAM_URL)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            intent.setPackage("org.telegram.messenger")
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                intent.setPackage("org.thunderdog.challegram")
                context.startActivity(intent)
            } catch (_: Exception) {
                intent.setPackage(null)
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("SocialLinksHelper", "Error opening Telegram: ${e.message}")
                }
            }
        }
    }
}

/**
 * Floating WhatsApp and Telegram contact icons.
 * - Telegram: bottom-left corner
 * - WhatsApp: bottom-right corner
 * - Fixed overlay, visible across all main app screens
 * - Equal vertical alignment and safe area padding
 */
@Composable
fun SocialFloatingActionButtons(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        // 1. Telegram Floating Icon (Bottom-Left)
        FloatingActionButton(
            onClick = { SocialLinksHelper.openTelegram(context) },
            shape = CircleShape,
            containerColor = Color(0xFF229ED9), // Official Telegram Blue
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
                .size(52.dp)
                .shadow(6.dp, CircleShape)
                .testTag("floating_telegram_btn")
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_telegram),
                contentDescription = "Contact on Telegram",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // 2. WhatsApp Floating Icon (Bottom-Right)
        FloatingActionButton(
            onClick = { SocialLinksHelper.openWhatsApp(context) },
            shape = CircleShape,
            containerColor = Color(0xFF25D366), // Official WhatsApp Green
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .size(52.dp)
                .shadow(6.dp, CircleShape)
                .testTag("floating_whatsapp_btn")
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_whatsapp),
                contentDescription = "Contact on WhatsApp",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
