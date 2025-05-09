package uk.co.appoly.droid.ui.paging

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

internal val defaultErrorStateProvider = DefaultErrorStateProvider()

internal class DefaultErrorStateProvider: ErrorStateProvider {
	@Composable
	override fun ErrorState(
		modifier: Modifier,
		text: @Composable () -> Unit,
		onRetry: (() -> Unit)?
	) {
		Column(
			modifier = modifier
				.then(
					Modifier
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
 * @see ErrorStateProvider
 */
val LocalErrorState = compositionLocalOf<ErrorStateProvider> { defaultErrorStateProvider }

/**
 * Local composition for providing error state Composable.
 *
 * This is used to display an error message when there is an issue with data fetching or processing.
 */
interface ErrorStateProvider {
	/**
	 * Composable function to display an error state.
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
		onRetry: (() -> Unit)?
	) = ErrorState(
		modifier = modifier,
		text = { Text(text = text) },
		onRetry = onRetry
	)

	/**
	 * Composable function to display an error state.
	 *
	 * @param modifier Modifier to be applied to the error state.
	 * @param text Composable function to display the error message.
	 * @param onRetry Optional callback function to be invoked when the retry button is clicked.
	 */
	@Composable
	abstract fun ErrorState(
		modifier: Modifier,
		text: @Composable () -> Unit,
		onRetry: (() -> Unit)?
	)
}