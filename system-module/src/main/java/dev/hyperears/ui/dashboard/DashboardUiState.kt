package dev.hyperears.ui.dashboard

import dev.hyperears.bridge.BridgeReceipt
import dev.hyperears.bridge.BridgeStage
import dev.hyperears.integration.EarbudState
import dev.hyperears.integration.ControlOwnership
import dev.hyperears.integration.PrivateTransportState
import dev.hyperears.integration.ProtocolHandshakeState
import dev.hyperears.integration.SystemProfileState
import dev.hyperears.R
import dev.hyperears.ui.UiText
import dev.hyperears.ui.uiText
import java.util.Locale

data class DeviceSessionSnapshot(
    val state: EarbudState,
    val sessionToken: String,
    val bridgeReceipts: Set<BridgeReceipt> = emptySet(),
) {
    val bridgeObserved: Boolean
        get() = observed(BridgeStage.STATE_ACCEPTED)

    val identityQueried: Boolean
        get() = observed(BridgeStage.IDENTITY_QUERIED)

    val capabilitiesQueried: Boolean
        get() = observed(BridgeStage.CAPABILITIES_QUERIED)

    val runtimeNotified: Boolean
        get() = observed(BridgeStage.RUNTIME_NOTIFIED)

    val phase: DevicePhase
        get() = when {
            state.lifecycle.systemProfile == SystemProfileState.DISCONNECTED ->
                DevicePhase.SYSTEM_DISCONNECTED
            state.lifecycle.controlOwnership == ControlOwnership.EXTERNAL_APP ->
                DevicePhase.EXTERNAL_CONTROL_APP
            state.lifecycle.privateTransport == PrivateTransportState.CONNECTING ->
                DevicePhase.TRANSPORT_CONNECTING
            state.lifecycle.privateTransport == PrivateTransportState.RECOVERING ->
                DevicePhase.TRANSPORT_RECOVERING
            state.lifecycle.privateTransport == PrivateTransportState.DORMANT ->
                DevicePhase.TRANSPORT_DORMANT
            state.lifecycle.protocolHandshake == ProtocolHandshakeState.PENDING ->
                DevicePhase.PROTOCOL_CONFIRMING
            state.lifecycle.protocolHandshake == ProtocolHandshakeState.REJECTED ->
                DevicePhase.PROTOCOL_REJECTED
            !bridgeObserved -> DevicePhase.WAITING_FOR_MILINK
            capabilitiesQueried -> DevicePhase.CAPABILITIES_QUERIED
            identityQueried -> DevicePhase.IDENTITY_QUERIED
            else -> DevicePhase.STATE_ACCEPTED
        }

    val miLinkLifecycle: List<DeviceLifecycleStage>
        get() = listOf(
            DeviceLifecycleStage(
                label = uiText(R.string.stage_state_received),
                value = uiText(
                    if (bridgeObserved) R.string.stage_received else R.string.stage_not_observed,
                ),
                complete = bridgeObserved,
                active = state.connected && !bridgeObserved,
            ),
            DeviceLifecycleStage(
                label = uiText(R.string.stage_identity_query),
                value = uiText(
                    if (identityQueried) R.string.stage_called else R.string.stage_not_observed,
                ),
                complete = identityQueried,
                active = bridgeObserved && !identityQueried,
            ),
            DeviceLifecycleStage(
                label = uiText(R.string.stage_card_capabilities),
                value = uiText(
                    if (capabilitiesQueried) R.string.stage_called else R.string.stage_not_observed,
                ),
                complete = capabilitiesQueried,
                active = identityQueried && !capabilitiesQueried,
            ),
            DeviceLifecycleStage(
                label = uiText(R.string.stage_state_notification),
                value = uiText(
                    if (runtimeNotified) R.string.stage_triggered else R.string.stage_not_observed,
                ),
                complete = runtimeNotified,
                active = false,
            ),
        )

    private fun observed(stage: BridgeStage): Boolean =
        bridgeReceipts.any {
            it.sessionToken == sessionToken &&
                it.stage == stage &&
                (stage != BridgeStage.STATE_ACCEPTED || it.revision == state.revision)
        }
}

data class DeviceSessionCollection(
    val sessions: Map<String, DeviceSessionSnapshot> = emptyMap(),
    val pendingBridgeReceipts: Map<String, Set<BridgeReceipt>> = emptyMap(),
)

data class DashboardUiState(
    val sessions: List<DeviceSessionSnapshot> = emptyList(),
    val runtimeResponsive: Boolean = false,
    val miLinkProcesses: Set<String> = emptySet(),
    val lastUpdatedAtMillis: Long? = null,
) {
    val deviceCards: List<DeviceSessionUiModel> by lazy(LazyThreadSafetyMode.NONE) {
        sessions.map(DeviceSessionUiProjector::project)
    }

    val connectedCount: Int
        get() = sessions.count { it.state.connected }

    val handshakeCount: Int
        get() = sessions.count { it.state.handshakeAccepted }

    val miLinkObservedCount: Int
        get() = sessions.count { it.bridgeObserved }

    val identityQueriedCount: Int
        get() = sessions.count { it.identityQueried }

    val capabilitiesQueriedCount: Int
        get() = sessions.count { it.capabilitiesQueried }
}

enum class DevicePhase(val labelRes: Int) {
    SYSTEM_DISCONNECTED(R.string.phase_system_disconnected),
    EXTERNAL_CONTROL_APP(R.string.phase_external_control_app),
    TRANSPORT_CONNECTING(R.string.phase_transport_connecting),
    TRANSPORT_RECOVERING(R.string.phase_transport_recovering),
    TRANSPORT_DORMANT(R.string.phase_transport_dormant),
    PROTOCOL_CONFIRMING(R.string.phase_protocol_confirming),
    PROTOCOL_REJECTED(R.string.phase_protocol_rejected),
    WAITING_FOR_MILINK(R.string.phase_waiting_for_milink),
    STATE_ACCEPTED(R.string.phase_state_accepted),
    IDENTITY_QUERIED(R.string.phase_identity_queried),
    CAPABILITIES_QUERIED(R.string.phase_capabilities_queried),
}

data class DeviceLifecycleStage(
    val label: UiText,
    val value: UiText,
    val complete: Boolean,
    val active: Boolean,
)

object DeviceSessionReducer {
    fun reduce(
        previous: DeviceSessionCollection,
        state: EarbudState,
        sessionToken: String,
    ): DeviceSessionCollection {
        val address = state.address?.takeIf(String::isNotBlank) ?: return previous
        val key = normalizeAddress(address)
        if (!state.sessionActive) {
            return previous.copy(
                sessions = previous.sessions - key,
                pendingBridgeReceipts = previous.pendingBridgeReceipts - key,
            )
        }

        val receipts = buildSet {
            addAll(previous.sessions[key]?.bridgeReceipts.orEmpty())
            addAll(previous.pendingBridgeReceipts[key].orEmpty())
        }.filterTo(mutableSetOf()) {
            it.sessionToken == sessionToken &&
                (
                    it.stage != BridgeStage.STATE_ACCEPTED ||
                        it.revision == state.revision
                )
        }
        return previous.copy(
            sessions = previous.sessions + (
                key to DeviceSessionSnapshot(
                    state = state,
                    sessionToken = sessionToken,
                    bridgeReceipts = receipts,
                )
            ),
            pendingBridgeReceipts = previous.pendingBridgeReceipts - key,
        )
    }

    fun acceptBridgeReceipt(
        previous: DeviceSessionCollection,
        receipt: BridgeReceipt,
    ): DeviceSessionCollection {
        val key = normalizeAddress(receipt.address)
        val session = previous.sessions[key]
        if (session == null) {
            return previous.copy(
                pendingBridgeReceipts = previous.pendingBridgeReceipts + (
                    key to (previous.pendingBridgeReceipts[key].orEmpty() + receipt)
                ),
            )
        }
        if (receipt.sessionToken != session.sessionToken) return previous
        if (receipt.stage == BridgeStage.STATE_ACCEPTED &&
            receipt.revision < session.state.revision
        ) {
            return previous
        }
        if (receipt.stage == BridgeStage.STATE_ACCEPTED &&
            receipt.revision > session.state.revision
        ) {
            return previous.copy(
                pendingBridgeReceipts = previous.pendingBridgeReceipts + (
                    key to (previous.pendingBridgeReceipts[key].orEmpty() + receipt)
                ),
            )
        }
        return previous.copy(
            sessions = previous.sessions + (
                key to session.copy(
                    bridgeReceipts = session.bridgeReceipts + receipt,
                )
            ),
        )
    }

    private fun normalizeAddress(address: String): String =
        address.uppercase(Locale.ROOT)
}
