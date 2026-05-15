package com.boxcontairner.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Anotación para previsualizar en Modo Claro y Modo Oscuro simultáneamente.
 */
@Preview(
    name = "Light Mode",
    group = "Themes",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    group = "Themes",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
annotation class ThemePreviews

/**
 * Anotación para previsualizar en diferentes dispositivos (Phone y Tablet).
 */
@Preview(
    name = "Phone",
    device = "spec:width=411dp,height=891dp",
    showSystemUi = true,
    group = "Devices"
)
@Preview(
    name = "Tablet",
    device = "spec:width=1280dp,height=800dp",
    showSystemUi = true,
    group = "Devices"
)
annotation class DevicePreviews
