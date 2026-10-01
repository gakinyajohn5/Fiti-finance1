package com.fitifinance.comrade.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glassmorphism design tokens.
 *
 * Everything is derived from the active MaterialTheme color scheme rather than
 * hard-coded, so the same components render as navy glass in Midnight mode and
 * remain readable (subtler) in the other modes.
 *
 * Note on "glass": Compose cannot blur the pixels *behind* a composable on all
 * supported API levels, so the effect is built from a translucent gradient fill,
 * a light-catching gradient border, and soft colored glows painted on the
 * screen background that the translucent cards pick up.
 */
@Immutable
data class GlassColors(
    val fillTop: Color,
    val fillBottom: Color,
    val borderStart: Color,
    val borderEnd: Color,
    val accentStart: Color,
    val accentEnd: Color,
    val glowPrimary: Color,
    val glowSecondary: Color,
    val track: Color,
    val textMuted: Color
) {
    /** Horizontal electric-blue to cyan sweep used for fills and indicators. */
    val accent: Brush get() = Brush.horizontalGradient(listOf(accentStart, accentEnd))
    /** Same sweep, bottom to top, for bar charts. */
    val accentVertical: Brush get() = Brush.verticalGradient(listOf(accentEnd, accentStart))
}

@Composable
fun glassColors(): GlassColors {
    val cs = MaterialTheme.colorScheme
    return GlassColors(
        fillTop = cs.onSurface.copy(alpha = 0.09f),
        fillBottom = cs.onSurface.copy(alpha = 0.03f),
        borderStart = cs.onSurface.copy(alpha = 0.22f),
        borderEnd = cs.onSurface.copy(alpha = 0.05f),
        accentStart = cs.primary,
        accentEnd = cs.secondary,
        glowPrimary = cs.primary.copy(alpha = 0.30f),
        glowSecondary = cs.secondary.copy(alpha = 0.16f),
        track = cs.onSurface.copy(alpha = 0.10f),
        textMuted = cs.onSurface.copy(alpha = 0.62f)
    )
}

/** Screen background: theme background plus two soft glows for the glass cards to catch. */
@Composable
fun GlassBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val g = glassColors()
    val base = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .background(base)
            .drawBehind {
                val r = size.width * 0.9f
                val topRight = Offset(size.width * 0.95f, size.height * 0.04f)
                val lowerLeft = Offset(size.width * 0.0f, size.height * 0.55f)
                drawCircle(
                    brush = Brush.radialGradient(listOf(g.glowPrimary, Color.Transparent), center = topRight, radius = r),
                    radius = r,
                    center = topRight
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(g.glowSecondary, Color.Transparent), center = lowerLeft, radius = r),
                    radius = r,
                    center = lowerLeft
                )
            },
        content = content
    )
}

/** Translucent card with a soft gradient border. Pass [onClick] to make it tappable. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    contentPadding: PaddingValues = PaddingValues(18.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val g = glassColors()
    var card = modifier
        .clip(shape)
        .background(Brush.verticalGradient(listOf(g.fillTop, g.fillBottom)))
        .border(1.dp, Brush.linearGradient(listOf(g.borderStart, g.borderEnd)), shape)
    if (onClick != null) {
        card = card.clickable(role = Role.Button, onClick = onClick)
    }
    Column(
        modifier = card.padding(contentPadding),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

/** Rounded progress bar with the blue-to-cyan gradient. Animates in once on first display. */
@Composable
fun NeonProgressBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    val g = glassColors()
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val animated by animateFloatAsState(
        targetValue = if (started) fraction.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "neonBar"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(g.track)
    ) {
        if (animated > 0f) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated)
                    .clip(CircleShape)
                    .background(g.accent)
            )
        }
    }
}
