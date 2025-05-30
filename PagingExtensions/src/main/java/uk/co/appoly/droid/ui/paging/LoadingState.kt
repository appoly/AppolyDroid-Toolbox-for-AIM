package uk.co.appoly.droid.ui.paging

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Default implementation of [LoadingStateProvider] used when no custom provider is specified.
 *
 * This provider creates a simple loading state UI with a centered CircularProgressIndicator
 * and consistent vertical padding (8dp).
 */
internal val defaultLoadingStateProvider = DefaultLoadingStateProvider()

/**
 * Default implementation of [LoadingStateProvider].
 *
 * Used as the default provider for the [LocalLoadingState] composition local.
 */
internal class DefaultLoadingStateProvider: LoadingStateProvider {
	/**
	 * Creates a default loading state UI with a centered circular progress indicator.
	 *
	 * @param modifier Modifier to be applied to the container
	 */
	@Composable
	override fun LoadingState(modifier: Modifier) {
		Column(
			modifier = modifier,
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			CircularProgressIndicator(
				modifier = Modifier.padding(vertical = 8.dp)
			)
		}
	}
}

/**
 * Local composition for providing loading state Composable.
 *
 * This is used to display a loading indicator when data is being fetched or processed.
 * It provides a default implementation that can be overridden by the user.
 *
 * Example usage:
 * ```kotlin
 * CompositionLocalProvider(
 *     LocalLoadingState provides MyCustomLoadingStateProvider()
 * ) {
 *     // Content that will use your custom loading state provider
 *     MyPagingList()
 * }
 * ```
 *
 * @see LoadingStateProvider
 */
val LocalLoadingState = compositionLocalOf<LoadingStateProvider> { defaultLoadingStateProvider }

/**
 * Interface for providing loading state Composable.
 *
 * This is used to display a loading indicator when data is being fetched or processed.
 *
 * Implement this interface to provide custom loading state UI in your application.
 * Then provide your implementation through the [LocalLoadingState] composition local.
 */
interface LoadingStateProvider {
	/**
	 * Composable function to display a loading state.
	 *
	 * @param modifier Modifier to be applied to the loading state.
	 */
	@Composable
	fun LoadingState(modifier: Modifier)
}
