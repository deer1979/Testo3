package com.containerpro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.containerpro.ui.theme.*

// ── Button variants ───────────────────────────────────────
enum class ButtonVariant { PRIMARY, SECONDARY, DANGER }

@Composable
fun ProButton(
    text     : String,
    onClick  : () -> Unit,
    modifier : Modifier        = Modifier,
    icon     : ImageVector?    = null,
    variant  : ButtonVariant   = ButtonVariant.PRIMARY,
    enabled  : Boolean         = true,
) {
    val bg = when (variant) {
        ButtonVariant.PRIMARY   -> MaterialTheme.colorScheme.primary
        ButtonVariant.SECONDARY -> MaterialTheme.colorScheme.surfaceVariant
        ButtonVariant.DANGER    -> ErrorCoral
    }
    val fg = when (variant) {
        ButtonVariant.PRIMARY   -> MaterialTheme.colorScheme.onPrimary
        ButtonVariant.SECONDARY -> MaterialTheme.colorScheme.onSurfaceVariant
        ButtonVariant.DANGER    -> MaterialTheme.colorScheme.onError
    }
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier.height(52.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = bg,
            contentColor           = fg,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

// ── Hero stat chip ────────────────────────────────────────
@Composable
fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
    ) {
        Column(
            modifier           = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

// ── Feature card (colored background) ────────────────────
@Composable
fun FeatureCard(
    title   : String,
    sub     : String,
    icon    : ImageVector,
    colors  : CardColors,
    onClick : () -> Unit,
    modifier: Modifier    = Modifier,
    badge   : @Composable (() -> Unit)? = null,
) {
    Surface(
        onClick   = onClick,
        modifier  = modifier
            .heightIn(min = 126.dp)
            .border(1.dp, colors.border, RoundedCornerShape(24.dp)),
        shape     = RoundedCornerShape(24.dp),
        color     = colors.bg,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.icon),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = colors.title, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall,
                    color = colors.title, fontWeight = FontWeight.Bold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(sub, style = MaterialTheme.typography.labelSmall,
                    color = colors.sub, modifier = Modifier.padding(top = 3.dp))
                badge?.invoke()
            }
        }
    }
}

// ── Wide card (horizontal) ────────────────────────────────
@Composable
fun WideCard(
    title   : String,
    sub     : String,
    icon    : ImageVector,
    iconBg  : Color,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick   = onClick,
        modifier  = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp)),
        shape     = RoundedCornerShape(22.dp),
        color     = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
                Text(sub, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp))
            }
            Text("›", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Status pill ───────────────────────────────────────────
@Composable
fun StatusPill(text: String, isActive: Boolean) {
    val bg = if (isActive) SuccessGreen.copy(alpha = 0.18f) else WarningAmber.copy(alpha = 0.15f)
    val fg = if (isActive) SuccessGreen else WarningAmber
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(fg))
        Text(text, style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold, color = fg)
    }
}

// ── Section header ────────────────────────────────────────
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text.uppercase(),
        style    = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.9.sp,
        modifier = modifier.padding(bottom = 10.dp),
    )
}

// ── Metric card ───────────────────────────────────────────
@Composable
fun MetricCard(
    value   : String,
    unit    : String,
    label   : String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        shape    = RoundedCornerShape(20.dp),
        color    = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier           = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, color = valueColor)
            Text(unit, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 3.dp))
        }
    }
}

// ── Theme chips row ───────────────────────────────────────
@Composable
fun ThemeChipRow(
    current  : com.containerpro.ui.theme.AppTheme,
    onChange : (com.containerpro.ui.theme.AppTheme) -> Unit,
) {
    val options = listOf(
        Triple(com.containerpro.ui.theme.AppTheme.DARK,  "🌙", "Oscuro"),
        Triple(com.containerpro.ui.theme.AppTheme.LIGHT, "☀️", "Claro"),
        Triple(com.containerpro.ui.theme.AppTheme.AUTO,  "⚙️", "Auto"),
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(modifier = Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            options.forEach { (theme, emoji, label) ->
                val active = current == theme
                Surface(
                    onClick = { onChange(theme) },
                    modifier= Modifier.weight(1f),
                    shape   = RoundedCornerShape(16.dp),
                    color   = if (active) MaterialTheme.colorScheme.surface
                              else         Color.Transparent,
                    shadowElevation = if (active) 2.dp else 0.dp,
                ) {
                    Row(
                        modifier           = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment  = Alignment.CenterVertically,
                    ) {
                        Text(emoji, fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(label, style = MaterialTheme.typography.labelMedium,
                            color = if (active) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
