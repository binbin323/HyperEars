package dev.hyperears.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class Resource(
        @param:StringRes val id: Int,
        val arguments: List<UiText> = emptyList(),
    ) : UiText

    data class Dynamic(val value: String) : UiText

    data class Joined(
        val values: List<UiText>,
        val separator: String = " / ",
    ) : UiText
}

fun uiText(@StringRes id: Int, vararg arguments: UiText): UiText =
    UiText.Resource(id, arguments.toList())

@Composable
fun UiText.resolve(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Joined -> values.map { it.resolve() }.joinToString(separator)
    is UiText.Resource -> stringResource(
        id = id,
        formatArgs = arguments.map { it.resolve() }.toTypedArray(),
    )
}
