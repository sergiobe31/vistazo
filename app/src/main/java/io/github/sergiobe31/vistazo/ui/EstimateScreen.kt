package io.github.sergiobe31.vistazo.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.sergiobe31.vistazo.R
import io.github.sergiobe31.vistazo.ui.HomeViewModel.Destination
import io.github.sergiobe31.vistazo.ui.HomeViewModel.EstimateUi

/**
 * "¿Cuántas crees?": estimación vs realidad. Sin juicios ni colores de
 * alarma: la brecha habla sola. Si hoy ya hay estimación, se revela directo
 * (es el caso de llegar tocando la notificación del reporte).
 */
@Composable
fun EstimateScreen(viewModel: HomeViewModel) {
    val ui by viewModel.estimateUi.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.history_back),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { viewModel.navigateTo(Destination.Home) },
            )

            Spacer(Modifier.height(64.dp))

            Crossfade(targetState = ui, animationSpec = tween(200), label = "estimate") { state ->
                when (state) {
                    EstimateUi.Loading -> Unit

                    is EstimateUi.Ask -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.estimate_question),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(40.dp))
                        OutlinedTextField(
                            value = input,
                            onValueChange = { value ->
                                input = value.filter(Char::isDigit).take(4)
                            },
                            label = { Text(stringResource(R.string.estimate_input_label)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(
                                fontFamily = MaterialTheme.typography.displayMedium.fontFamily,
                                fontWeight = MaterialTheme.typography.displayMedium.fontWeight,
                                fontSize = 40.sp,
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(32.dp))
                        Text(
                            text = stringResource(R.string.estimate_confirm),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (input.isEmpty()) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.clickable(enabled = input.isNotEmpty()) {
                                input.toIntOrNull()?.let(viewModel::submitEstimate)
                            },
                        )
                    }

                    is EstimateUi.Revealed -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.estimate_believed, state.estimate),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.estimate_actual, state.today.unlocks),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.estimate_vistazos, state.today.vistazos),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
