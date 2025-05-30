package uk.co.appoly.droid.ui.paging

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Default implementation of [EmptyStateTextProvider] used when no custom provider is specified.
 *
 * This provider creates a simple empty state text UI with:
 * - A rounded container with the surface container color background
 * - Text styled with the bodyMedium typography and semibold weight
 * - Consistent padding (16dp) around the content
 */
internal val defaultEmptyStateTextProvider = DefaultEmptyStateTextProvider()

/**
 * Default implementation of [EmptyStateTextProvider].
 *
 * Used as the default provider for the [LocalEmptyState] composition local.
 */
internal class DefaultEmptyStateTextProvider: EmptyStateTextProvider {
	/**
	 * Creates a default empty state text UI.
	 *
	 * @param modifier Modifier to be applied to the container
	 * @param text The text to display in the empty state
	 */
	@Composable
	override fun EmptyStateText(
		modifier: Modifier,
		text: String
	) {
		Row(
			modifier = modifier
				.then(
					Modifier
						.background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp))
						.padding(16.dp)
				),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center
		) {
			val mergedTextStyle = LocalTextStyle.current.merge(
				MaterialTheme.typography.bodyMedium.copy(
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.SemiBold
				)
			)
			CompositionLocalProvider(LocalTextStyle provides mergedTextStyle) {
				Text(text = text)
			}
		}
	}
}

/**
 * Local composition for providing empty state text.
 *
 * This is used to display a message when there is no data available in a list or grid.
 * It provides a default implementation that can be overridden by the user.
 *
 * Example usage:
 * ```kotlin
 * CompositionLocalProvider(
 *     LocalEmptyState provides MyCustomEmptyStateProvider()
 * ) {
 *     // Content that will use your custom empty state provider
 *     MyPagingList()
 * }
 * ```
 *
 * @see EmptyStateTextProvider
 */
val LocalEmptyState = compositionLocalOf<EmptyStateTextProvider> { defaultEmptyStateTextProvider }

/**
 * Interface for providing empty state text Composable.
 *
 * This is used to display a message when there is no data available in a list or grid.
 *
 * Implement this interface to provide custom empty state UI in your application.
 * Then provide your implementation through the [LocalEmptyState] composition local.
 */
interface EmptyStateTextProvider {
	/**
	 * Composable function to display empty state text.
	 *
	 * @param modifier Modifier to be applied to the Composable.
	 * @param text The text to be displayed in the empty state.
	 */
	@Composable
	fun EmptyStateText(
		modifier: Modifier,
		text: String
	)
}
