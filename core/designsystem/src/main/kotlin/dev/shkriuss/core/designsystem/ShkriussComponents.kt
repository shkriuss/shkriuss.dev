package dev.shkriuss.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Calm hairline-bordered surface card avoiding drop-shadow clutter.
 */
@Composable
fun ShkriussCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = LocalShkriussSpacing.current
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
            content = content,
        )
    }
}

/**
 * Accessible section heading marked with TalkBack heading semantics.
 */
@Composable
fun ShkriussSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.semantics { heading() },
    )
}

/**
 * Subtle high-contrast pill used to communicate offline or encryption state.
 */
@Composable
fun ShkriussStatusBadge(
    label: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    val containerColor = if (accent) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = if (accent) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

/**
 * Spacious empty state component with a guaranteed >= 48dp primary action button.
 */
@Composable
fun ShkriussEmptyState(
    title: String,
    body: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalShkriussSpacing.current
    ShkriussCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        Button(
            onClick = onActionClick,
            modifier = Modifier.sizeIn(minWidth = spacing.minTouchTarget, minHeight = spacing.minTouchTarget),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(text = actionLabel)
        }
    }
}

/**
 * Reference adaptive screen exercising `ShkriussTheme` on compact phones and expanded tablets.
 * Rendered by Roborazzi on every PR so visual changes can be reviewed directly in Vanadium.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShkriussDesignShowcase(
    isExpandedWidth: Boolean,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalShkriussSpacing.current
    val horizontalPadding = if (isExpandedWidth) {
        spacing.screenHorizontalExpanded
    } else {
        spacing.screenHorizontalCompact
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = spacing.sectionGap),
            verticalArrangement = Arrangement.spacedBy(spacing.sectionGap),
        ) {
            // Calm suite header
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(
                    text = stringResource(R.string.shkriuss_showcase_suite_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.shkriuss_showcase_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(spacing.xxs))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    ShkriussStatusBadge(
                        label = stringResource(R.string.shkriuss_showcase_badge_encrypted),
                        accent = true,
                    )
                    ShkriussStatusBadge(
                        label = stringResource(R.string.shkriuss_showcase_badge_offline),
                        accent = false,
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (isExpandedWidth) {
                // Two-pane tablet layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sectionGap),
                ) {
                    Column(
                        modifier = Modifier.weight(1.1f),
                        verticalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        ShkriussSectionHeader(title = stringResource(R.string.shkriuss_showcase_section_overview))
                        ShowcaseOverviewCards()
                    }
                    Column(
                        modifier = Modifier.weight(0.9f),
                        verticalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        ShkriussSectionHeader(title = stringResource(R.string.shkriuss_showcase_section_actions))
                        ShowcaseActionControls()
                        ShkriussEmptyState(
                            title = stringResource(R.string.shkriuss_showcase_empty_title),
                            body = stringResource(R.string.shkriuss_showcase_empty_body),
                            actionLabel = stringResource(R.string.shkriuss_showcase_empty_action),
                            onActionClick = {},
                        )
                    }
                }
            } else {
                // Single-pane compact phone layout
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    ShkriussSectionHeader(title = stringResource(R.string.shkriuss_showcase_section_overview))
                    ShowcaseOverviewCards()
                }
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    ShkriussSectionHeader(title = stringResource(R.string.shkriuss_showcase_section_actions))
                    ShowcaseActionControls()
                    ShkriussEmptyState(
                        title = stringResource(R.string.shkriuss_showcase_empty_title),
                        body = stringResource(R.string.shkriuss_showcase_empty_body),
                        actionLabel = stringResource(R.string.shkriuss_showcase_empty_action),
                        onActionClick = {},
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowcaseOverviewCards() {
    ShkriussCard {
        Text(
            text = stringResource(R.string.shkriuss_showcase_card_notes_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.shkriuss_showcase_card_notes_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    ShkriussCard {
        Text(
            text = stringResource(R.string.shkriuss_showcase_card_backup_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.shkriuss_showcase_card_backup_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShowcaseActionControls() {
    val spacing = LocalShkriussSpacing.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Button(
            onClick = {},
            modifier = Modifier.sizeIn(minWidth = spacing.minTouchTarget, minHeight = spacing.minTouchTarget),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(text = stringResource(R.string.shkriuss_showcase_action_primary))
        }
        FilledTonalButton(
            onClick = {},
            modifier = Modifier.sizeIn(minWidth = spacing.minTouchTarget, minHeight = spacing.minTouchTarget),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(text = stringResource(R.string.shkriuss_showcase_action_secondary))
        }
    }
}
