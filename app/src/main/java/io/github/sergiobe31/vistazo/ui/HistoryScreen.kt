package io.github.sergiobe31.vistazo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.sergiobe31.vistazo.R
import io.github.sergiobe31.vistazo.data.DayStats
import io.github.sergiobe31.vistazo.ui.HomeViewModel.Destination
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Segunda pantalla: histórico. Aquí vivirá el histórico largo en el futuro. */
@Composable
fun HistoryScreen(viewModel: HomeViewModel) {
    val lastDays by viewModel.lastDays.collectAsStateWithLifecycle()

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
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(40.dp))
            WeekBars(lastDays)
        }
    }
}

/** Barras finas de los últimos 7 días; una letra por día, sin más ruido. */
@Composable
private fun WeekBars(days: List<DayStats>) {
    if (days.isEmpty()) return
    val maxCount = days.maxOf { it.unlocks }.coerceAtLeast(1).toFloat()
    val todayStart = days.lastOrNull()?.startOfDayMillis
    Row(
        modifier = Modifier.fillMaxWidth().height(132.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEach { day ->
            val isToday = day.startOfDayMillis == todayStart
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((day.unlocks / maxCount * 96).dp.coerceAtLeast(3.dp))
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .alpha(if (isToday) 1f else 0.35f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = dayLetter(day.startOfDayMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(if (isToday) 1f else 0.6f),
                )
            }
        }
    }
}

private fun dayLetter(startOfDayMillis: Long): String = when (
    LocalDate.ofInstant(Instant.ofEpochMilli(startOfDayMillis), ZoneId.systemDefault()).dayOfWeek
) {
    DayOfWeek.MONDAY -> "L"
    DayOfWeek.TUESDAY -> "M"
    DayOfWeek.WEDNESDAY -> "X"
    DayOfWeek.THURSDAY -> "J"
    DayOfWeek.FRIDAY -> "V"
    DayOfWeek.SATURDAY -> "S"
    DayOfWeek.SUNDAY -> "D"
}
