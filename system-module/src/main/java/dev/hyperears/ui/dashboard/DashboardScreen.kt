package dev.hyperears.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import dev.hyperears.R
import dev.hyperears.ui.resolve
import dev.hyperears.ui.components.HyperEarsPage
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onRefresh: () -> Unit,
) {
    HyperEarsPage(title = stringResource(R.string.app_name)) { pagePadding, scrollBehavior ->
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(pagePadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 0.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "runtime") {
                RuntimeCard(uiState, onRefresh)
            }
            item(key = "session-header") {
                SectionHeader(
                    title = stringResource(R.string.dashboard_device_sessions),
                    count = uiState.sessions.size,
                )
            }
            if (uiState.deviceCards.isEmpty()) {
                item(key = "empty-sessions") {
                    EmptySessionsCard()
                }
            } else {
                items(
                    items = uiState.deviceCards,
                    key = { session -> "${session.address}:${session.adapterId}" },
                ) { session ->
                    DeviceSessionCard(session)
                }
            }
        }
    }
}

@Composable
private fun RuntimeCard(
    uiState: DashboardUiState,
    onRefresh: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.dashboard_runtime_status),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(onClick = onRefresh) { Text(stringResource(R.string.action_sync)) }
            }
            RuntimeProcessRow(
                label = stringResource(R.string.dashboard_bluetooth_hook),
                status = if (uiState.runtimeResponsive) {
                    stringResource(
                        R.string.dashboard_responded_at,
                        uiState.lastUpdatedAtMillis?.let(::formatTime) ?: "—",
                    )
                } else {
                    stringResource(R.string.dashboard_not_responding)
                },
                online = uiState.runtimeResponsive,
            )
            Spacer(Modifier.height(12.dp))
            RuntimeProcessRow(
                label = stringResource(R.string.dashboard_milink_hook),
                status = if (uiState.miLinkProcesses.isEmpty()) {
                    stringResource(R.string.dashboard_not_responding)
                } else {
                    pluralStringResource(
                        R.plurals.dashboard_processes_responding,
                        uiState.miLinkProcesses.size,
                        uiState.miLinkProcesses.size,
                    )
                },
                online = uiState.miLinkProcesses.isNotEmpty(),
            )
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryMetric(
                    stringResource(R.string.dashboard_state_received),
                    uiState.miLinkObservedCount,
                    Modifier.weight(1f),
                )
                SummaryMetric(
                    stringResource(R.string.dashboard_identity_queries),
                    uiState.identityQueriedCount,
                    Modifier.weight(1f),
                )
                SummaryMetric(
                    stringResource(R.string.dashboard_capability_queries),
                    uiState.capabilitiesQueriedCount,
                    Modifier.weight(1f),
                )
                SummaryMetric(
                    stringResource(R.string.dashboard_active_sessions),
                    uiState.sessions.size,
                    Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RuntimeProcessRow(
    label: String,
    status: String,
    online: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusDot(
            color = if (online) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptySessionsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.dashboard_no_sessions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.dashboard_no_sessions_detail),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DeviceSessionCard(
    session: DeviceSessionUiModel,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.deviceName.resolve(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.dashboard_adapter,
                            session.adapterName.resolve(),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.dashboard_adapter_id, session.adapterId),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.dashboard_bluetooth_address,
                            session.address,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.size(12.dp))
                PhasePill(session.phase)
            }

            AdapterFacts(session)

            Text(
                text = stringResource(R.string.dashboard_session_status),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SessionStatusList(session.headsetLifecycle)

            Text(
                text = stringResource(R.string.dashboard_milink_processing),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LifecycleStrip(session.miLinkLifecycle)

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            MetricStrip(session.metrics)
        }
    }
}

@Composable
private fun AdapterFacts(session: DeviceSessionUiModel) {
    if (!session.adapterResolved) {
        Text(
            text = session.adapterSummary.resolve(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = session.adapterSummary.resolve(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(
                R.string.dashboard_control,
                session.controlSummary.resolve(),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SessionStatusList(
    stages: List<DeviceLinkStage>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stages.forEachIndexed { index, stage ->
            SessionStatusRow(stage)
            if (index != stages.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 20.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun MetricStrip(metrics: List<DeviceMetric>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        metrics.forEach { metric ->
            CompactMetric(
                metric.label.resolve(),
                metric.value.resolve(),
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SessionStatusRow(
    stage: DeviceLinkStage,
) {
    val color = when (stage.status) {
        DeviceLinkStatus.READY -> MaterialTheme.colorScheme.primary
        DeviceLinkStatus.ACTIVE -> MaterialTheme.colorScheme.tertiary
        DeviceLinkStatus.INACTIVE -> MaterialTheme.colorScheme.outline
        DeviceLinkStatus.ERROR -> MaterialTheme.colorScheme.error
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusDot(color)
        Text(
            text = stage.label.resolve(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stage.value.resolve(),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LifecycleStrip(stages: List<DeviceLifecycleStage>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        stages.forEach { stage ->
            val color = when {
                stage.complete -> MaterialTheme.colorScheme.primary
                stage.active -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.outline
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                color = if (stage.complete || stage.active) {
                    color.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    StatusDot(color)
                    Text(
                        text = stage.label.resolve(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Text(
                        text = stage.value.resolve(),
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        fontWeight = if (stage.complete || stage.active) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun PhasePill(phase: DevicePhase) {
    val color = when (phase) {
        DevicePhase.SYSTEM_DISCONNECTED,
        DevicePhase.PROTOCOL_REJECTED,
        DevicePhase.TRANSPORT_DORMANT,
        -> MaterialTheme.colorScheme.error
        DevicePhase.TRANSPORT_CONNECTING,
        DevicePhase.TRANSPORT_RECOVERING,
        DevicePhase.PROTOCOL_CONFIRMING,
        -> MaterialTheme.colorScheme.secondary
        DevicePhase.WAITING_FOR_MILINK,
        DevicePhase.EXTERNAL_CONTROL_APP,
        -> MaterialTheme.colorScheme.tertiary
        DevicePhase.STATE_ACCEPTED,
        DevicePhase.IDENTITY_QUERIED,
        DevicePhase.CAPABILITIES_QUERIED,
        -> MaterialTheme.colorScheme.primary
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color,
    ) {
        Text(
            text = stringResource(phase.labelRes),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape),
    )
}

private fun formatTime(timestamp: Long): String =
    DateFormat.getTimeInstance(DateFormat.MEDIUM).format(Date(timestamp))
