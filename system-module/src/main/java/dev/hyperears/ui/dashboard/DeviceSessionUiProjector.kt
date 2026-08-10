package dev.hyperears.ui.dashboard

import dev.hyperears.R
import dev.hyperears.integration.AdapterResolution
import dev.hyperears.integration.AdapterSnapshot
import dev.hyperears.integration.BatteryReading
import dev.hyperears.integration.BatterySource
import dev.hyperears.integration.ControlOwnership
import dev.hyperears.integration.HeadsetFormFactor
import dev.hyperears.integration.NoiseMode
import dev.hyperears.integration.PrivateTransportState
import dev.hyperears.integration.ProtocolHandshakeState
import dev.hyperears.integration.SystemProfileState
import dev.hyperears.integration.TransportKind
import dev.hyperears.ui.UiText
import dev.hyperears.ui.uiText

/** Complete, adapter-agnostic data required to render one dashboard card. */
data class DeviceSessionUiModel(
    val deviceName: UiText,
    val address: String,
    val adapterName: UiText,
    val adapterId: String,
    val adapterSummary: UiText,
    val controlSummary: UiText,
    val adapterResolved: Boolean,
    val phase: DevicePhase,
    val headsetLifecycle: List<DeviceLinkStage>,
    val miLinkLifecycle: List<DeviceLifecycleStage>,
    val metrics: List<DeviceMetric>,
)

data class DeviceLinkStage(
    val label: UiText,
    val value: UiText,
    val status: DeviceLinkStatus,
)

enum class DeviceLinkStatus {
    READY,
    ACTIVE,
    INACTIVE,
    ERROR,
}

data class DeviceMetric(
    val label: UiText,
    val value: UiText,
)

/** Projects runtime state into resource-backed presentation data without resolving a locale. */
object DeviceSessionUiProjector {
    fun project(session: DeviceSessionSnapshot): DeviceSessionUiModel {
        val state = session.state
        val adapter = state.adapter
        return DeviceSessionUiModel(
            deviceName = state.deviceName?.let(UiText::Dynamic)
                ?: uiText(R.string.dashboard_unnamed_headset),
            address = state.address ?: "—",
            adapterName = adapter?.localizedDisplayName()
                ?: uiText(R.string.dashboard_unresolved),
            adapterId = adapter?.id ?: "—",
            adapterSummary = adapter?.adapterSummary()
                ?: uiText(R.string.dashboard_no_adapter_snapshot),
            controlSummary = adapter?.controlSummary()
                ?: uiText(R.string.dashboard_capabilities_unknown),
            adapterResolved = adapter != null,
            phase = session.phase,
            headsetLifecycle = headsetLifecycle(session),
            miLinkLifecycle = session.miLinkLifecycle,
            metrics = metrics(session, adapter),
        )
    }

    private fun headsetLifecycle(session: DeviceSessionSnapshot): List<DeviceLinkStage> = buildList {
        val state = session.state
        add(
            DeviceLinkStage(
                label = uiText(R.string.stage_system_connection),
                value = state.lifecycle.systemProfile.displayName(),
                status = if (state.lifecycle.systemProfile == SystemProfileState.CONNECTED) {
                    DeviceLinkStatus.READY
                } else {
                    DeviceLinkStatus.ERROR
                },
            ),
        )
        add(
            DeviceLinkStage(
                label = uiText(R.string.stage_control),
                value = UiText.Dynamic(
                    state.lifecycle.externalControlApp?.displayName ?: "HyperEars",
                ),
                status = if (state.lifecycle.controlOwnership == ControlOwnership.EXTERNAL_APP) {
                    DeviceLinkStatus.ACTIVE
                } else {
                    DeviceLinkStatus.READY
                },
            ),
        )
        add(
            DeviceLinkStage(
                label = uiText(R.string.stage_private_channel),
                value = state.lifecycle.privateTransport.displayName(),
                status = when (state.lifecycle.privateTransport) {
                    PrivateTransportState.NOT_REQUIRED,
                    PrivateTransportState.CONNECTED,
                    -> DeviceLinkStatus.READY
                    PrivateTransportState.CONNECTING,
                    PrivateTransportState.RECOVERING,
                    -> DeviceLinkStatus.ACTIVE
                    PrivateTransportState.IDLE -> DeviceLinkStatus.INACTIVE
                    PrivateTransportState.DORMANT -> DeviceLinkStatus.ERROR
                },
            ),
        )
        add(
            DeviceLinkStage(
                label = uiText(R.string.stage_protocol),
                value = state.lifecycle.protocolHandshake.displayName(),
                status = when (state.lifecycle.protocolHandshake) {
                    ProtocolHandshakeState.NOT_REQUIRED,
                    ProtocolHandshakeState.CONFIRMED,
                    -> DeviceLinkStatus.READY
                    ProtocolHandshakeState.PENDING -> DeviceLinkStatus.ACTIVE
                    ProtocolHandshakeState.REJECTED -> DeviceLinkStatus.ERROR
                },
            ),
        )
    }

    private fun metrics(
        session: DeviceSessionSnapshot,
        adapter: AdapterSnapshot?,
    ): List<DeviceMetric> = buildList {
        val battery = session.state.battery
        if (adapter?.formFactor == HeadsetFormFactor.HEADPHONES || battery.overall.available) {
            val aggregate = battery.overall.takeIf(BatteryReading::available)
                ?: battery.left.takeIf(BatteryReading::available)
                ?: battery.right.takeIf(BatteryReading::available)
                ?: battery.case.takeIf(BatteryReading::available)
                ?: battery.overall
            add(DeviceMetric(uiText(R.string.metric_device), aggregate.displayValue()))
        } else {
            add(DeviceMetric(uiText(R.string.metric_left_earbud), battery.left.displayValue()))
            add(DeviceMetric(uiText(R.string.metric_right_earbud), battery.right.displayValue()))
            add(DeviceMetric(uiText(R.string.metric_charging_case), battery.case.displayValue()))
        }
        add(
            DeviceMetric(
                label = uiText(R.string.metric_mode),
                value = if (adapter?.capabilities?.noiseControl == false) {
                    uiText(R.string.value_not_supported)
                } else {
                    session.state.noiseMode.displayName()
                },
            ),
        )
    }

    private fun AdapterSnapshot.adapterSummary(): UiText = uiText(
        R.string.adapter_summary,
        resolution.displayName(),
        formFactor.displayName(),
        batterySource.displayName(),
        transportSummary(),
    )

    private fun AdapterSnapshot.transportSummary(): UiText {
        if (!privateProtocolRequired) return UiText.Dynamic("A2DP/HFP")
        val transports = transportKinds.map { UiText.Dynamic(it.displayName) }.distinct()
        return if (transports.isEmpty()) {
            uiText(R.string.value_not_declared)
        } else {
            UiText.Joined(transports)
        }
    }

    private fun AdapterSnapshot.controlSummary(): UiText {
        val modeLabels = supportedNoiseModes.map(NoiseMode::displayName)
        return when {
            modeLabels.isNotEmpty() -> UiText.Joined(modeLabels)
            capabilities.audioHandoff -> uiText(R.string.control_audio_handoff_only)
            else -> uiText(R.string.value_none)
        }
    }
}

private fun AdapterSnapshot.localizedDisplayName(): UiText = when (id) {
    "edifier-evo-pro" -> uiText(R.string.model_edifier_evo)
    "honor-x5spro" -> uiText(R.string.model_honor_x5s_pro)
    else -> UiText.Dynamic(displayName)
}

private fun AdapterResolution.displayName(): UiText = uiText(
    when (this) {
        AdapterResolution.STANDARD -> R.string.value_resolution_standard
        AdapterResolution.EXACT_MATCH -> R.string.value_resolution_exact
        AdapterResolution.FAMILY_MATCH -> R.string.value_resolution_family
        AdapterResolution.PROTOCOL_CONFIRMED -> R.string.value_resolution_protocol
    },
)

private fun SystemProfileState.displayName(): UiText = uiText(
    if (this == SystemProfileState.CONNECTED) R.string.value_connected
    else R.string.value_not_connected,
)

private fun PrivateTransportState.displayName(): UiText = uiText(
    when (this) {
        PrivateTransportState.NOT_REQUIRED -> R.string.value_not_required
        PrivateTransportState.IDLE -> R.string.value_idle
        PrivateTransportState.CONNECTING -> R.string.value_connecting
        PrivateTransportState.CONNECTED -> R.string.value_connected
        PrivateTransportState.RECOVERING -> R.string.value_recovering
        PrivateTransportState.DORMANT -> R.string.value_dormant
    },
)

private fun ProtocolHandshakeState.displayName(): UiText = uiText(
    when (this) {
        ProtocolHandshakeState.NOT_REQUIRED -> R.string.value_not_required
        ProtocolHandshakeState.PENDING -> R.string.value_pending
        ProtocolHandshakeState.CONFIRMED -> R.string.value_confirmed
        ProtocolHandshakeState.REJECTED -> R.string.value_rejected
    },
)

private fun BatteryReading.displayValue(): UiText = UiText.Dynamic(
    percent?.let { value -> if (charging) "$value%+" else "$value%" } ?: "—",
)

private fun NoiseMode?.displayName(): UiText = when (this) {
    NoiseMode.ANC -> uiText(R.string.value_anc)
    NoiseMode.OFF -> uiText(R.string.value_off)
    NoiseMode.TRANSPARENCY -> uiText(R.string.value_transparency)
    NoiseMode.WIND -> uiText(R.string.value_wind)
    null -> UiText.Dynamic("—")
}

private fun HeadsetFormFactor.displayName(): UiText = when (this) {
    HeadsetFormFactor.TWS -> UiText.Dynamic("TWS")
    HeadsetFormFactor.HEADPHONES -> uiText(R.string.value_headphones)
}

private fun BatterySource.displayName(): UiText = uiText(
    when (this) {
        BatterySource.NONE -> R.string.value_battery_unavailable
        BatterySource.SYSTEM_AGGREGATE -> R.string.value_battery_android
        BatterySource.PRIVATE_PROTOCOL -> R.string.value_battery_private
    },
)

private val TransportKind.displayName: String
    get() = when (this) {
        TransportKind.RFCOMM -> "RFCOMM"
        TransportKind.GATT -> "GATT"
        TransportKind.L2CAP -> "L2CAP"
    }
