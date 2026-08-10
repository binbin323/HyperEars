package dev.hyperears.ui.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.hyperears.BuildConfig
import dev.hyperears.R
import dev.hyperears.ui.components.HyperEarsPage

private data class SupportEntry(
    val nameRes: Int,
    val evidence: EvidenceLevel,
    val battery: BatteryCapability,
    val noiseControlRes: Int,
)

private data class SupportBrand(
    val nameRes: Int,
    val entries: List<SupportEntry>,
)

private enum class EvidenceLevel(val labelRes: Int) {
    VERIFIED(R.string.evidence_verified),
    PUBLIC_IMPLEMENTATION(R.string.evidence_public_implementation),
    REFERENCE_PROTOCOL(R.string.evidence_reference_protocol),
    FAMILY_PROBE(R.string.evidence_family_probe),
    STANDARD_FALLBACK(R.string.evidence_standard_fallback),
}

private enum class BatteryCapability(val labelRes: Int) {
    COMPONENT(R.string.battery_component),
    LEFT_RIGHT(R.string.battery_left_right),
    DEVICE(R.string.battery_device),
    AGGREGATE(R.string.battery_aggregate),
    DEVICE_OR_COMPONENT(R.string.battery_device_or_component),
    DEVICE_OR_AGGREGATE(R.string.battery_device_or_aggregate),
    SYSTEM(R.string.battery_system),
}

private data class ProjectLink(
    val titleRes: Int,
    val detailRes: Int,
    val url: String,
)

private val supportBrands = listOf(
    SupportBrand(
        nameRes = R.string.brand_vivo,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_vivo_air3_pro,
                evidence = EvidenceLevel.VERIFIED,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_vivo_3e,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_vivo_other,
                evidence = EvidenceLevel.FAMILY_PROBE,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
        ),
    ),
    SupportBrand(
        nameRes = R.string.brand_oppo,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_oppo_air2_pro,
                evidence = EvidenceLevel.REFERENCE_PROTOCOL,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_oppo_free_x3_air5,
                evidence = EvidenceLevel.REFERENCE_PROTOCOL,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_oppo_other,
                evidence = EvidenceLevel.FAMILY_PROBE,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
        ),
    ),
    SupportBrand(
        nameRes = R.string.brand_starring,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_starring_ultra,
                evidence = EvidenceLevel.VERIFIED,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_starring_other,
                evidence = EvidenceLevel.STANDARD_FALLBACK,
                battery = BatteryCapability.SYSTEM,
                noiseControlRes = R.string.noise_none,
            ),
        ),
    ),
    SupportBrand(
        nameRes = R.string.brand_bose,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_bose_qc,
                evidence = EvidenceLevel.VERIFIED,
                battery = BatteryCapability.DEVICE,
                noiseControlRes = R.string.noise_anc_transparency_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_bose_qc35,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.DEVICE,
                noiseControlRes = R.string.noise_anc_off_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_bose_700,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.DEVICE,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_bose_qc45_earbuds,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.DEVICE_OR_COMPONENT,
                noiseControlRes = R.string.noise_anc_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_bose_earbuds_ultra,
                evidence = EvidenceLevel.REFERENCE_PROTOCOL,
                battery = BatteryCapability.DEVICE_OR_COMPONENT,
                noiseControlRes = R.string.noise_anc_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_bose_other,
                evidence = EvidenceLevel.FAMILY_PROBE,
                battery = BatteryCapability.DEVICE_OR_COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency_wind_by_model,
            ),
        ),
    ),
    SupportBrand(
        nameRes = R.string.brand_edifier,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_edifier_w860,
                evidence = EvidenceLevel.VERIFIED,
                battery = BatteryCapability.DEVICE,
                noiseControlRes = R.string.noise_anc_off_transparency_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_edifier_evo,
                evidence = EvidenceLevel.VERIFIED,
                battery = BatteryCapability.AGGREGATE,
                noiseControlRes = R.string.noise_anc_off_transparency_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_edifier_w_series,
                evidence = EvidenceLevel.FAMILY_PROBE,
                battery = BatteryCapability.DEVICE,
                noiseControlRes = R.string.noise_anc_off_transparency_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_edifier_other,
                evidence = EvidenceLevel.FAMILY_PROBE,
                battery = BatteryCapability.DEVICE_OR_AGGREGATE,
                noiseControlRes = R.string.noise_anc_off_transparency_wind,
            ),
        ),
    ),
    SupportBrand(
        nameRes = R.string.brand_roseselsa,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_rose_i5,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_rose_furina,
                evidence = EvidenceLevel.VERIFIED,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_rose_budsfeel,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_rose_product_line,
                evidence = EvidenceLevel.FAMILY_PROBE,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_rose_other,
                evidence = EvidenceLevel.STANDARD_FALLBACK,
                battery = BatteryCapability.SYSTEM,
                noiseControlRes = R.string.noise_none,
            ),
        ),
    ),
    SupportBrand(
        nameRes = R.string.brand_nicehck,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_nicehck_orig,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_nicehck_other,
                evidence = EvidenceLevel.STANDARD_FALLBACK,
                battery = BatteryCapability.SYSTEM,
                noiseControlRes = R.string.noise_none,
            ),
        ),
    ),
    SupportBrand(
        nameRes = R.string.brand_moondrop,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_moondrop_robin,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.LEFT_RIGHT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_moondrop_other,
                evidence = EvidenceLevel.STANDARD_FALLBACK,
                battery = BatteryCapability.SYSTEM,
                noiseControlRes = R.string.noise_none,
            ),
        ),
    ),
    SupportBrand(
        nameRes = R.string.brand_honor,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_honor_x5s_pro,
                evidence = EvidenceLevel.VERIFIED,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
        ),
    ),
    SupportBrand(
        nameRes = R.string.brand_qcy,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_qcy_c50s,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_qcy_other,
                evidence = EvidenceLevel.FAMILY_PROBE,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
        ),
    ),
    SupportBrand(
        nameRes = R.string.brand_sony,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_sony_legacy_anc,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.DEVICE_OR_COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency_wind,
            ),
            SupportEntry(
                nameRes = R.string.model_sony_current_anc,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.DEVICE_OR_COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_sony_wfc510,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.COMPONENT,
                noiseControlRes = R.string.noise_off_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_sony_standard_models,
                evidence = EvidenceLevel.PUBLIC_IMPLEMENTATION,
                battery = BatteryCapability.DEVICE_OR_COMPONENT,
                noiseControlRes = R.string.noise_none,
            ),
            SupportEntry(
                nameRes = R.string.model_sony_other_anc,
                evidence = EvidenceLevel.FAMILY_PROBE,
                battery = BatteryCapability.DEVICE_OR_COMPONENT,
                noiseControlRes = R.string.noise_anc_off_transparency,
            ),
            SupportEntry(
                nameRes = R.string.model_sony_other_standard,
                evidence = EvidenceLevel.STANDARD_FALLBACK,
                battery = BatteryCapability.SYSTEM,
                noiseControlRes = R.string.noise_none,
            ),
        ),
    ),
    SupportBrand(
        nameRes = R.string.brand_generic,
        entries = listOf(
            SupportEntry(
                nameRes = R.string.model_generic,
                evidence = EvidenceLevel.STANDARD_FALLBACK,
                battery = BatteryCapability.SYSTEM,
                noiseControlRes = R.string.noise_none,
            ),
        ),
    ),
)

private val projectLinks = listOf(
    ProjectLink(
        titleRes = R.string.project_source_code,
        detailRes = R.string.project_upstream_address,
        url = "https://github.com/silverpoetry/HyperEars",
    ),
    ProjectLink(
        titleRes = R.string.project_branch_source_code,
        detailRes = R.string.project_fork_address,
        url = "https://github.com/binbin323/HyperEars",
    ),
    ProjectLink(
        titleRes = R.string.project_compatibility,
        detailRes = R.string.project_compatibility_detail,
        url = "https://github.com/silverpoetry/HyperEars/blob/main/docs/compatibility.md",
    ),
    ProjectLink(
        titleRes = R.string.project_feedback,
        detailRes = R.string.project_feedback_detail,
        url = "https://github.com/binbin323/HyperEars/issues/new/choose",
    ),
    ProjectLink(
        titleRes = R.string.project_license,
        detailRes = R.string.project_license_detail,
        url = "https://github.com/silverpoetry/HyperEars/blob/main/LICENSE",
    ),
    ProjectLink(
        titleRes = R.string.project_third_party,
        detailRes = R.string.project_third_party_detail,
        url = "https://github.com/silverpoetry/HyperEars/blob/main/THIRD_PARTY_NOTICES.md",
    ),
    ProjectLink(
        titleRes = R.string.project_privacy,
        detailRes = R.string.project_privacy_detail,
        url = "https://github.com/silverpoetry/HyperEars/blob/main/PRIVACY.md",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    HyperEarsPage(title = stringResource(R.string.about_title)) { pagePadding, scrollBehavior ->
        val uriHandler = LocalUriHandler.current
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(pagePadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
        item(key = "header") {
            CenteredContent { modifier ->
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(
                            R.string.about_version,
                            BuildConfig.VERSION_NAME,
                            BuildConfig.VERSION_CODE,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.about_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item(key = "support-introduction") {
            CenteredContent { modifier ->
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.about_support_introduction),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        items(
            items = supportBrands,
            key = SupportBrand::nameRes,
        ) { brand ->
            CenteredContent { modifier ->
                BrandSupportCard(brand = brand, modifier = modifier)
            }
        }
        item(key = "project") {
            CenteredContent { modifier ->
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    ) {
                        Column {
                            projectLinks.forEachIndexed { index, link ->
                                ListItem(
                                    headlineContent = { Text(stringResource(link.titleRes)) },
                                    supportingContent = {
                                        Text(stringResource(link.detailRes))
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    ),
                                    modifier = Modifier.clickable {
                                        runCatching { uriHandler.openUri(link.url) }
                                    },
                                )
                                if (index != projectLinks.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
            item(key = "copyright") {
                CenteredContent { modifier ->
                    Text(
                        text = stringResource(R.string.about_copyright),
                        modifier = modifier,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CenteredContent(content: @Composable (Modifier) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        content(Modifier.fillMaxWidth().widthIn(max = 800.dp))
    }
}

@Composable
private fun BrandSupportCard(
    brand: SupportBrand,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column {
            Text(
                text = stringResource(brand.nameRes),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            brand.entries.forEachIndexed { index, entry ->
                SupportRow(entry)
                if (index != brand.entries.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportRow(entry: SupportEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = stringResource(entry.nameRes),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(entry.evidence.labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = evidenceColor(entry.evidence),
            )
        }
        Text(
            text = stringResource(
                R.string.about_support_detail,
                stringResource(entry.battery.labelRes),
                stringResource(entry.noiseControlRes),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun evidenceColor(evidence: EvidenceLevel) = when (evidence) {
    EvidenceLevel.VERIFIED -> MaterialTheme.colorScheme.primary
    EvidenceLevel.PUBLIC_IMPLEMENTATION -> MaterialTheme.colorScheme.secondary
    EvidenceLevel.REFERENCE_PROTOCOL,
    EvidenceLevel.FAMILY_PROBE,
    -> MaterialTheme.colorScheme.tertiary
    EvidenceLevel.STANDARD_FALLBACK -> MaterialTheme.colorScheme.onSurfaceVariant
}
