package com.containerpro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.containerpro.ui.theme.CardPalette
import com.containerpro.ui.theme.SuccessGreen
import com.containerpro.ui.theme.WarningAmber

// ── Feature card (colored background, square) ─────────────
@Composable
fun FeatureCard(
    title   : String,
    sub     : String,
    icon    : ImageVector,
    palette : CardPalette,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
    badge   : @Composable (() -> Unit)? = null,
) {
    Surface(
        onClick   = onClick,
        modifier  = modifier
            .heightIn(min = 128.dp)
            .border(1.dp, palette.border, RoundedCornerShape(24.dp)),
        shape     = RoundedCornerShape(24.dp),
        color     = palette.bg,
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = palette.title, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                color      = palette.title,
                fontWeight = FontWeight.Bold,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
            )
            Text(
                text     = sub,
                style    = MaterialTheme.typography.labelSmall,
                color    = palette.sub,
                modifier = Modifier.padding(top = 3.dp),
            )
            if (badge != null) {
                Spacer(Modifier.height(6.dp))
                badge()
            }
        }
    }
}

// ── Wide card (horizontal layout) ─────────────────────────
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
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
        shape     = RoundedCornerShape(20.dp),
        color     = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text     = sub,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Status dot pill ───────────────────────────────────────
@Composable
fun StatusPill(label: String, active: Boolean) {
    val bg = if (active) SuccessGreen.copy(alpha = 0.18f) else WarningAmber.copy(alpha = 0.15f)
    val fg = if (active) SuccessGreen else WarningAmber
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(fg))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = fg)
    }
}

// ── Section header ────────────────────────────────────────
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text          = text.uppercase(),
        style         = MaterialTheme.typography.labelSmall,
        fontWeight    = FontWeight.Bold,
        color         = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.9.sp,
        modifier      = modifier.padding(bottom = 10.dp),
    )
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
            Text(
                value,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// ── Theme chip row ────────────────────────────────────────
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
                    color   = if (active) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (active) 2.dp else 0.dp,
                ) {
                    Row(
                        modifier           = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment  = Alignment.CenterVertically,
                    ) {
                        Text(emoji, fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (active) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
