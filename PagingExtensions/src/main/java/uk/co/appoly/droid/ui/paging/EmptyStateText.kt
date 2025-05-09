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
 * Local composition for providing empty state text.
 *
 * This is used to display a message when there is no data available in a list or grid.
 * It provides a default implementation that can be overridden by the user.
 *
 * @see EmptyStateTextProvider
 */
val LocalEmptyState = compositionLocalOf<EmptyStateTextProvider> {
	object : EmptyStateTextProvider {
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
}

/**
 * Interface for providing empty state text Composable.
 *
 * This is used to display a message when there is no data available in a list or grid.
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
		modifier: Modifier = Modifier,
		text: String
	)
}

