package uk.co.appoly.droid.ui.paging

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uk.co.appoly.droid.pagingextensions.R

/**
 * Default implementation of [ErrorStateProvider] used when no custom provider is specified.
 *
 * This provider creates a simple error state UI with:
 * - A rounded container with the surface container color background
 * - Text styled with the bodyMedium typography and semibold weight
 * - A retry button (if a retry callback is provided)
 * - Consistent padding (16dp) around the content
 */
internal val defaultErrorStateProvider = DefaultErrorStateProvider()

/**
 * Default implementation of [ErrorStateProvider].
 *
 * Used as the default provider for the [LocalErrorState] composition local.
 */
internal class DefaultErrorStateProvider: ErrorStateProvider {
	/**
	 * Creates a default error state UI with a custom text composable.
	 *
	 * @param modifier Modifier to be applied to the container
	 * @param text Composable function to display the error message
	 * @param onRetry Optional callback to be invoked when the retry button is clicked
	 */
	@Composable
	override fun ErrorState(
		modifier: Modifier,
		text: @Composable () -> Unit,
		onRetry: (() -> Unit)?,
		contentPadding: PaddingValues
	) {
		Column(
			modifier = modifier
				.then(
					Modifier
						.padding(contentPadding)
						.background(
							color = MaterialTheme.colorScheme.surfaceContainer,
							RoundedCornerShape(10.dp)
						)
						.padding(16.dp)
				),
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			val mergedTextStyle = LocalTextStyle.current.merge(
				MaterialTheme.typography.bodyMedium.copy(
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.SemiBold
				)
			)
			CompositionLocalProvider(LocalTextStyle provides mergedTextStyle) {
				text()
			}
			if (onRetry != null) {
				Button(
					modifier = Modifier.padding(top = 16.dp),
					onClick = onRetry
				) {
					Text(text = stringResource(R.string.retry))
				}
			}
		}
	}
}

/**
 * Local composition for providing error state Composable.
 *
 * This is used to display an error message when there is an issue with data fetching or processing.
 * It provides a default implementation that can be overridden by the user.
 *
 * Example usage:
 * ```kotlin
 * CompositionLocalProvider(
 *     LocalErrorState provides MyCustomErrorStateProvider()
 * ) {
 *     // Content that will use your custom error state provider
 *     MyPagingList()
 * }
 * ```
 *
 * @see ErrorStateProvider
 */
val LocalErrorState = compositionLocalOf<ErrorStateProvider> { defaultErrorStateProvider }

/**
 * Interface for providing error state Composable UI.
 *
 * This is used to display an error message when there is an issue with data fetching or processing.
 *
 * Implement this interface to provide custom error state UI in your application.
 * Then provide your implementation through the [LocalErrorState] composition local.
 */
interface ErrorStateProvider {
	/**
	 * Composable function to display an error state with a simple string message.
	 *
	 * By default this calls [ErrorState] with a [Text] Composable
	 *
	 * @param modifier Modifier to be applied to the error state.
	 * @param text The text to be displayed in the error state.
	 * @param onRetry Optional callback function to be invoked when the retry button is clicked.
	 */
	@Composable
	fun ErrorState(
		modifier: Modifier,
		text: String,
		onRetry: (() -> Unit)?,
		contentPadding: PaddingValues //= PaddingValues(0.dp)
	) = ErrorState(
		modifier = modifier,
		text = { Text(text = text) },
		onRetry = onRetry,
		contentPadding = contentPadding
	)

	/**
	 * Composable function to display an error state with a custom composable for the error message.
	 *
	 * @param modifier Modifier to be applied to the error state.
	 * @param text Composable function to display the error message.
	 * @param onRetry Optional callback function to be invoked when the retry button is clicked.
	 */
	@Composable
	fun ErrorState(
		modifier: Modifier,
		text: @Composable () -> Unit,
		onRetry: (() -> Unit)?,
		contentPadding: PaddingValues = PaddingValues(0.dp)
	)
}
