package io.github.sergiobe31.vistazo.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDefaults
import io.github.sergiobe31.vistazo.R
import io.github.sergiobe31.vistazo.VistazoApp
import io.github.sergiobe31.vistazo.data.TodayStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Widget de escritorio: número grande de desbloqueos de hoy y vistazos debajo.
 * Room es la fuente de verdad; se actualiza tras cada evento (updateAll desde
 * el receiver) y con updatePeriodMillis como red de seguridad (mínimo del sistema).
 */
class CounterWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as VistazoApp).repository
        val titleToday = context.getString(R.string.home_today)
        val unlocksLabel = context.getString(R.string.home_unlocks_label)
        val vistazosSuffix = context.getString(R.string.widget_vistazos_suffix)
        provideContent {
            val today by repository.observeToday()
                .collectAsState(initial = TodayStats(0, 0))
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(paper)
                        .cornerRadius(16.dp)
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = titleToday,
                        style = TextDefaults.defaultTextStyle.copy(
                            fontSize = 13.sp,
                            color = inkVariant,
                        ),
                    )
                    Text(
                        text = "${today.unlocks}",
                        style = TextDefaults.defaultTextStyle.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        ),
                    )
                    Text(
                        text = "$unlocksLabel · ${today.vistazos} $vistazosSuffix",
                        style = TextDefaults.defaultTextStyle.copy(
                            fontSize = 13.sp,
                            color = inkVariant,
                        ),
                    )
                }
            }
        }
    }

    companion object {
        // Glance no soporta fuentes custom: jerarquía y paleta vía ColorProvider.
        private val accent = ColorProvider(Color(0xFFB45309), Color(0xFFE8A33D))
        private val paper = ColorProvider(Color(0xFFFAF3D9), Color(0xFF241D12))
        private val inkVariant = ColorProvider(Color(0xFF7A6A4F), Color(0xFFA89878))
        private val scope = CoroutineScope(Dispatchers.Default)

        /** Llamar tras persistir cada evento para refrescar los widgets. */
        fun updateAll(context: Context) {
            scope.launch {
                GlanceAppWidgetManager(context).getGlanceIds(CounterWidget::class.java)
                    .forEach { CounterWidget().update(context, it) }
            }
        }
    }
}
