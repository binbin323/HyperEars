package dev.hyperears.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import dev.hyperears.R
import dev.hyperears.integration.EarbudAdapterDescriptor
import dev.hyperears.integration.EarbudAdapterGroup
import dev.hyperears.integration.EarbudAdapterKind
import dev.hyperears.root.RootAction
import dev.hyperears.root.RootActionState
import dev.hyperears.settings.ModuleSettings
import dev.hyperears.ui.components.HyperEarsPage
import dev.hyperears.ui.components.rememberSwitchHaptics

enum class SettingsDestination {
    ADAPTERS,
    DEBUG,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: ModuleSettings,
    adapterGroups: List<EarbudAdapterGroup>,
    rootAvailable: Boolean?,
    rootActionState: RootActionState,
    onSettingsChanged: (ModuleSettings) -> Unit,
    onRunRootAction: (RootAction) -> Unit,
    onOpenDebug: () -> Unit,
) {
    HyperEarsPage(title = stringResource(R.string.settings_title)) { pagePadding, scrollBehavior ->
        var pendingRootAction by remember { mutableStateOf<RootAction?>(null) }
        var showModelPicker by rememberSaveable { mutableStateOf(false) }
        val listState = rememberLazyListState()
        val selectableModels = remember(adapterGroups) {
            adapterGroups.flatMap { group ->
                group.adapters
                    .filter { it.kind == EarbudAdapterKind.MODEL }
                    .map { SelectableAdapterModel(group.id, group.displayName, it) }
            }
        }
        val selectedModelName = selectableModels
            .firstOrNull { it.adapter.id == settings.selectedAdapterId }
            ?.adapter
            ?.localizedDisplayName()
            ?: stringResource(R.string.settings_headset_model_auto)

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
            item(key = "preferences") {
                SettingsGroupCard {
                    NavigationPreference(
                        title = stringResource(R.string.settings_headset_model),
                        detail = selectedModelName,
                        onClick = { showModelPicker = true },
                    )
                    PreferenceDivider()
                    TogglePreference(
                        title = stringResource(R.string.settings_pause_module),
                        detail = stringResource(R.string.settings_pause_module_detail),
                        checked = settings.modulePaused,
                        onCheckedChange = {
                            onSettingsChanged(settings.copy(modulePaused = it))
                        },
                    )
                    PreferenceDivider()
                    TogglePreference(
                        title = stringResource(R.string.settings_open_vendor_settings),
                        detail = stringResource(R.string.settings_open_vendor_settings_detail),
                        checked = settings.preferVendorControlApp,
                        onCheckedChange = {
                            onSettingsChanged(settings.copy(preferVendorControlApp = it))
                        },
                    )
                    PreferenceDivider()
                    TogglePreference(
                        title = stringResource(R.string.settings_runtime_yield),
                        detail = stringResource(R.string.settings_runtime_yield_detail),
                        checked = settings.yieldToVendorControlApp,
                        onCheckedChange = {
                            onSettingsChanged(settings.copy(yieldToVendorControlApp = it))
                        },
                    )
                    PreferenceDivider()
                    NavigationPreference(
                        title = stringResource(R.string.settings_debug),
                        detail = stringResource(R.string.settings_debug_detail),
                        onClick = onOpenDebug,
                    )
                }
            }
            item(key = "quick-actions") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (rootAvailable != true) {
                        Text(
                            text = if (rootAvailable == false) {
                                stringResource(R.string.settings_root_required)
                            } else {
                                stringResource(R.string.settings_root_checking)
                            },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    SettingsGroupCard {
                        RootAction.entries.forEachIndexed { index, action ->
                            ActionPreference(
                                title = stringResource(action.titleRes),
                                detail = stringResource(action.detailRes),
                                actionLabel = stringResource(R.string.action_execute),
                                available = rootAvailable == true,
                                running = rootActionState is RootActionState.Running &&
                                    rootActionState.action == action,
                                onClick = { pendingRootAction = action },
                            )
                            if (index != RootAction.entries.lastIndex) {
                                PreferenceDivider()
                            }
                        }
                    }
                }
            }
        }

        pendingRootAction?.let { action ->
            AlertDialog(
                onDismissRequest = { pendingRootAction = null },
                title = { Text(stringResource(action.titleRes)) },
                text = { Text(stringResource(action.detailRes)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            pendingRootAction = null
                            onRunRootAction(action)
                        },
                    ) {
                        Text(stringResource(R.string.action_execute))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingRootAction = null }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                },
            )
        }

        if (showModelPicker) {
            HeadsetModelPickerDialog(
                models = selectableModels,
                selectedAdapterId = settings.selectedAdapterId,
                onSelected = { adapterId ->
                    val disabled = if (adapterId == null) {
                        settings.disabledAdapterIds
                    } else {
                        settings.disabledAdapterIds - adapterId
                    }
                    onSettingsChanged(
                        settings.copy(
                            selectedAdapterId = adapterId,
                            disabledAdapterIds = disabled,
                        ),
                    )
                    showModelPicker = false
                },
                onDismiss = { showModelPicker = false },
            )
        }
    }
}

private data class SelectableAdapterModel(
    val groupId: String,
    val groupName: String,
    val adapter: EarbudAdapterDescriptor,
)

@Composable
private fun HeadsetModelPickerDialog(
    models: List<SelectableAdapterModel>,
    selectedAdapterId: String?,
    onSelected: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_headset_model)) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
            ) {
                item(key = "automatic") {
                    ModelPickerRow(
                        title = stringResource(R.string.settings_headset_model_auto),
                        detail = stringResource(R.string.settings_headset_model_auto_detail),
                        selected = selectedAdapterId == null,
                        onClick = { onSelected(null) },
                    )
                }
                items(
                    items = models,
                    key = { it.adapter.id },
                ) { model ->
                    ModelPickerRow(
                        title = model.adapter.localizedDisplayName(),
                        detail = localizedAdapterGroupName(model.groupId, model.groupName),
                        selected = selectedAdapterId == model.adapter.id,
                        onClick = { onSelected(model.adapter.id) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun ModelPickerRow(
    title: String,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(detail) },
        leadingContent = {
            RadioButton(
                selected = selected,
                onClick = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugSettingsScreen(
    settings: ModuleSettings,
    rootAvailable: Boolean?,
    onSettingsChanged: (ModuleSettings) -> Unit,
    onExportLogs: () -> Unit,
    onOpenAdapters: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    HyperEarsPage(
        title = stringResource(R.string.settings_debug),
        onNavigateBack = onNavigateBack,
    ) { pagePadding, scrollBehavior ->
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(pagePadding)
                .navigationBarsPadding()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "debug-preferences") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (rootAvailable != true) {
                        Text(
                            text = if (rootAvailable == false) {
                                stringResource(R.string.settings_debug_export_root_required)
                            } else {
                                stringResource(R.string.settings_root_checking)
                            },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    SettingsGroupCard {
                        NavigationPreference(
                            title = stringResource(R.string.settings_adapters),
                            detail = stringResource(R.string.settings_adapters_detail),
                            onClick = onOpenAdapters,
                        )
                        PreferenceDivider()
                        TogglePreference(
                            title = stringResource(R.string.settings_detailed_logging),
                            detail = stringResource(R.string.settings_detailed_logging_detail),
                            checked = settings.diagnosticLogging,
                            onCheckedChange = {
                                onSettingsChanged(settings.copy(diagnosticLogging = it))
                            },
                        )
                        PreferenceDivider()
                        ActionPreference(
                            title = stringResource(R.string.settings_export_logs),
                            detail = stringResource(R.string.settings_export_logs_detail),
                            actionLabel = stringResource(R.string.action_export),
                            available = rootAvailable == true,
                            running = false,
                            onClick = onExportLogs,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdapterSettingsScreen(
    groups: List<EarbudAdapterGroup>,
    settings: ModuleSettings,
    onSettingsChanged: (ModuleSettings) -> Unit,
    onNavigateBack: () -> Unit,
) {
    HyperEarsPage(
        title = stringResource(R.string.settings_adapters),
        onNavigateBack = onNavigateBack,
    ) { pagePadding, scrollBehavior ->
        val listState = rememberLazyListState()
        var expandedGroupId by rememberSaveable { mutableStateOf<String?>(null) }
        val rows = remember(groups, expandedGroupId) {
            buildAdapterRows(groups, expandedGroupId)
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(pagePadding)
                .navigationBarsPadding()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 16.dp,
            ),
        ) {
            items(
                items = rows,
                key = AdapterListRow::key,
                contentType = AdapterListRow::contentType,
            ) { row ->
                when (row) {
                    is AdapterListRow.GroupHeader -> {
                        val group = row.group
                        val enabledCount = group.adapters.count { adapter ->
                            adapter.id !in settings.disabledAdapterIds
                        }
                        AdapterListSurface(
                            position = if (row.expanded) {
                                AdapterListPosition.TOP
                            } else {
                                AdapterListPosition.SINGLE
                            },
                        ) {
                            AdapterGroupHeader(
                                title = localizedAdapterGroupName(group.id, group.displayName),
                                enabledCount = enabledCount,
                                totalCount = group.adapters.size,
                                expanded = row.expanded,
                                enabled = enabledCount > 0,
                                onEnabledChange = { enabled ->
                                    val disabled = if (enabled) {
                                        settings.disabledAdapterIds - row.adapterIds
                                    } else {
                                        settings.disabledAdapterIds + row.adapterIds
                                    }
                                    onSettingsChanged(
                                        settings.copy(
                                            disabledAdapterIds = disabled,
                                            selectedAdapterId = settings.selectedAdapterId
                                                ?.takeUnless(disabled::contains),
                                        ),
                                    )
                                },
                                onClick = {
                                    expandedGroupId = group.id.takeUnless { row.expanded }
                                },
                            )
                        }
                    }

                    is AdapterListRow.SectionHeader -> {
                        AdapterListSurface(position = AdapterListPosition.MIDDLE) {
                            PreferenceDivider()
                            Text(
                                text = stringResource(row.kind.sectionTitleRes),
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 12.dp,
                                    bottom = 4.dp,
                                ),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    is AdapterListRow.AdapterToggle -> {
                        AdapterListSurface(
                            position = if (row.endsGroup) {
                                AdapterListPosition.BOTTOM
                            } else {
                                AdapterListPosition.MIDDLE
                            },
                        ) {
                            if (row.showTopDivider) PreferenceDivider()
                            TogglePreference(
                                title = row.adapter.localizedDisplayName(),
                                detail = row.adapter.id,
                                checked = row.adapter.id !in settings.disabledAdapterIds,
                                onCheckedChange = { enabled ->
                                    val disabled = if (enabled) {
                                        settings.disabledAdapterIds - row.adapter.id
                                    } else {
                                        settings.disabledAdapterIds + row.adapter.id
                                    }
                                    onSettingsChanged(
                                        settings.copy(
                                            disabledAdapterIds = disabled,
                                            selectedAdapterId = settings.selectedAdapterId
                                                ?.takeUnless(disabled::contains),
                                        ),
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private sealed interface AdapterListRow {
    val key: String
    val contentType: String

    data class GroupHeader(
        val group: EarbudAdapterGroup,
        val expanded: Boolean,
        val adapterIds: Set<String>,
    ) : AdapterListRow {
        override val key: String = "group:${group.id}"
        override val contentType: String = "group"
    }

    data class SectionHeader(
        val groupId: String,
        val kind: EarbudAdapterKind,
    ) : AdapterListRow {
        override val key: String = "section:$groupId:${kind.name}"
        override val contentType: String = "section"
    }

    data class AdapterToggle(
        val adapter: EarbudAdapterDescriptor,
        val showTopDivider: Boolean,
        val endsGroup: Boolean,
    ) : AdapterListRow {
        override val key: String = "adapter:${adapter.id}"
        override val contentType: String = "adapter"
    }
}

private fun buildAdapterRows(
    groups: List<EarbudAdapterGroup>,
    expandedGroupId: String?,
): List<AdapterListRow> = buildList {
    groups.forEach { group ->
        val expanded = group.id == expandedGroupId
        add(
            AdapterListRow.GroupHeader(
                group = group,
                expanded = expanded,
                adapterIds = group.adapters.mapTo(linkedSetOf(), EarbudAdapterDescriptor::id),
            ),
        )
        if (!expanded) return@forEach

        val sections = EarbudAdapterKind.entries.mapNotNull { kind ->
            group.adapters.filter { it.kind == kind }
                .takeIf(List<EarbudAdapterDescriptor>::isNotEmpty)
                ?.let { kind to it }
        }
        sections.forEachIndexed { sectionIndex, (kind, adapters) ->
            add(AdapterListRow.SectionHeader(group.id, kind))
            adapters.forEachIndexed { adapterIndex, adapter ->
                add(
                    AdapterListRow.AdapterToggle(
                        adapter = adapter,
                        showTopDivider = adapterIndex > 0,
                        endsGroup = sectionIndex == sections.lastIndex &&
                            adapterIndex == adapters.lastIndex,
                    ),
                )
            }
        }
    }
}

@Composable
private fun localizedAdapterGroupName(groupId: String, fallback: String): String {
    val resource = when (groupId) {
        "vivo" -> R.string.brand_vivo
        "oppo" -> R.string.brand_oppo
        "starring" -> R.string.brand_starring
        "bose" -> R.string.brand_bose
        "edifier" -> R.string.brand_edifier
        "rose" -> R.string.brand_roseselsa
        "nicehck" -> R.string.brand_nicehck
        "moondrop" -> R.string.brand_moondrop
        "honor" -> R.string.brand_honor
        "sony" -> R.string.brand_sony
        "qcy" -> R.string.brand_qcy
        "standard" -> R.string.brand_generic
        else -> return fallback
    }
    return stringResource(resource)
}

@Composable
private fun EarbudAdapterDescriptor.localizedDisplayName(): String = when (id) {
    "edifier-evo-pro" -> stringResource(R.string.model_edifier_evo)
    "honor-x5spro" -> stringResource(R.string.model_honor_x5s_pro)
    else -> displayName
}

private enum class AdapterListPosition {
    SINGLE,
    TOP,
    MIDDLE,
    BOTTOM,
}

@Composable
private fun AdapterListSurface(
    position: AdapterListPosition,
    content: @Composable () -> Unit,
) {
    val shape = when (position) {
        AdapterListPosition.SINGLE -> RoundedCornerShape(24.dp)
        AdapterListPosition.TOP -> RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
        )

        AdapterListPosition.MIDDLE -> RoundedCornerShape(0.dp)
        AdapterListPosition.BOTTOM -> RoundedCornerShape(
            bottomStart = 24.dp,
            bottomEnd = 24.dp,
        )
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (position.endsGroup) 12.dp else 0.dp),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        content = content,
    )
}

private val AdapterListPosition.endsGroup: Boolean
    get() = this == AdapterListPosition.SINGLE || this == AdapterListPosition.BOTTOM

private val EarbudAdapterKind.sectionTitleRes: Int
    get() = when (this) {
        EarbudAdapterKind.MODEL -> R.string.adapter_section_models
        EarbudAdapterKind.FAMILY -> R.string.adapter_section_family
        EarbudAdapterKind.STANDARD -> R.string.adapter_section_standard
    }

@Composable
private fun AdapterGroupHeader(
    title: String,
    enabledCount: Int,
    totalCount: Int,
    expanded: Boolean,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val haptics = rememberSwitchHaptics()
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        supportingContent = {
            Text(
                text = pluralStringResource(
                    R.plurals.adapter_enabled_count,
                    enabledCount,
                    enabledCount,
                    totalCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = enabled,
                    onCheckedChange = { updated ->
                        haptics.perform(updated)
                        onEnabledChange(updated)
                    },
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(
                        if (expanded) {
                            R.string.content_description_collapse
                        } else {
                            R.string.content_description_expand
                        },
                    ),
                    modifier = Modifier.rotate(if (expanded) 90f else 0f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun SettingsGroupCard(
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column { content() }
    }
}

@Composable
private fun PreferenceDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun TogglePreference(
    title: String,
    detail: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val haptics = rememberSwitchHaptics()
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        },
        supportingContent = {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = { updated ->
                    haptics.perform(updated)
                    onCheckedChange(updated)
                },
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
    )
}

@Composable
private fun NavigationPreference(
    title: String,
    detail: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        },
        supportingContent = {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun ActionPreference(
    title: String,
    detail: String,
    actionLabel: String,
    available: Boolean,
    running: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (available) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )
        },
        supportingContent = {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
            )
        },
        trailingContent = {
            Button(
                onClick = onClick,
                enabled = available && !running,
            ) {
                Text(
                    if (running) stringResource(R.string.action_executing) else actionLabel,
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
    )
}
