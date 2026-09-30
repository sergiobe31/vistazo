package io.github.sergiobe31.vistazo.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.sergiobe31.vistazo.R
import io.github.sergiobe31.vistazo.ui.HomeViewModel.Destination
import java.util.Locale
import kotlin.math.roundToInt

/** Todos los ajustes de la app en una sola pantalla. */
@Composable
fun SettingsScreen(viewModel: HomeViewModel) {
    val reportEnabled by viewModel.reportEnabled.collectAsStateWithLifecycle()
    val reportTime by viewModel.reportTime.collectAsStateWithLifecycle()
    val thresholdSeconds by viewModel.thresholdSeconds.collectAsStateWithLifecycle()
    val statusBarEnabled by viewModel.statusBarEnabled.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.history_back),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { viewModel.navigateTo(Destination.Home) },
            )

            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(40.dp))

            // --- Reporte diario ---
            SectionLabel(stringResource(R.string.settings_section_report))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_report_toggle),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.settings_report_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(16.dp))
                NotificationPermissionSwitch(
                    checked = reportEnabled,
                    onCheckedChange = viewModel::setReportEnabled,
                )
            }
            if (reportEnabled) {
                Spacer(Modifier.height(12.dp))
                ReportTimeRow(
                    hour = reportTime.first,
                    minute = reportTime.second,
                    onSave = viewModel::setReportTime,
                )
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // --- Umbral de vistazo ---
            Spacer(Modifier.height(24.dp))
            SectionLabel(stringResource(R.string.settings_section_vistazo))
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.settings_threshold_value, thresholdSeconds),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Slider(
                value = thresholdSeconds.toFloat(),
                onValueChange = { seconds ->
                    viewModel.setVistazoThreshold((seconds / STEP_SECONDS).roundToInt() * STEP_SECONDS)
                },
                valueRange = MIN_THRESHOLD_SECONDS.toFloat()..MAX_THRESHOLD_SECONDS.toFloat(),
                steps = (MAX_THRESHOLD_SECONDS - MIN_THRESHOLD_SECONDS) / STEP_SECONDS - 1,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )
            Text(
                text = stringResource(R.string.settings_threshold_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(32.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // --- Barra de estado ---
            Spacer(Modifier.height(24.dp))
            SectionLabel(stringResource(R.string.settings_section_status_bar))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.status_bar_toggle),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.status_bar_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(16.dp))
                NotificationPermissionSwitch(
                    checked = statusBarEnabled,
                    onCheckedChange = viewModel::setStatusBarEnabled,
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Fila de hora del reporte: muestra el valor y, al tocarla, se despliega un
 * editor inline compacto (dos campos numéricos HH / MM). Sin diálogos.
 */
@Composable
private fun ReportTimeRow(hour: Int, minute: Int, onSave: (Int, Int) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var hourText by remember { mutableStateOf("") }
    var minuteText by remember { mutableStateOf("") }

    if (!editing) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    hourText = "%02d".format(hour)
                    minuteText = "%02d".format(minute)
                    editing = true
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_report_time),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = formatTime(hour, minute),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        return
    }

    val hourValue = hourText.toIntOrNull()
    val minuteValue = minuteText.toIntOrNull()
    val hourComplete = hourText.length == 2
    val minuteComplete = minuteText.length == 2
    val hourError = hourComplete && (hourValue == null || hourValue !in 0..23)
    val minuteError = minuteComplete && (minuteValue == null || minuteValue !in 0..59)
    val canSave = hourValue != null && hourValue in 0..23 &&
        minuteValue != null && minuteValue in 0..59

    Row(verticalAlignment = Alignment.CenterVertically) {
        TimeField(
            value = hourText,
            onValueChange = { hourText = it.filter(Char::isDigit).take(2) },
            label = stringResource(R.string.settings_hour_label),
            isError = hourError,
        )
        Text(
            text = ":",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        TimeField(
            value = minuteText,
            onValueChange = { minuteText = it.filter(Char::isDigit).take(2) },
            label = stringResource(R.string.settings_minute_label),
            isError = minuteError,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.settings_time_save),
            style = MaterialTheme.typography.labelSmall,
            color = if (canSave) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.clickable(enabled = canSave) {
                onSave(hourValue!!, minuteValue!!)
                editing = false
            },
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.settings_time_cancel),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable { editing = false },
        )
    }
    if (hourError || minuteError) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.settings_time_error),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary, // error discreto en ámbar, sin rojos
        )
    }
}

@Composable
private fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(fontFamily = MaterialTheme.typography.bodyLarge.fontFamily),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier.width(72.dp),
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Switch que pide POST_NOTIFICATIONS en runtime (Android 13+) al activarse,
 * igual que el patrón que ya existía para la barra de estado.
 */
@Composable
private fun NotificationPermissionSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) onCheckedChange(true) }

    Switch(
        checked = checked,
        onCheckedChange = { enabled ->
            if (enabled && Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onCheckedChange(enabled)
            }
        },
        colors = SwitchDefaults.colors(
            checkedTrackColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

private fun formatTime(hour: Int, minute: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

private const val MIN_THRESHOLD_SECONDS = 15
private const val MAX_THRESHOLD_SECONDS = 300
private const val STEP_SECONDS = 15
