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

internal val defaultLoadingStateProvider = DefaultLoadingStateProvider()

internal class DefaultLoadingStateProvider: LoadingStateProvider {
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
 * @see LoadingStateProvider
 */
val LocalLoadingState = compositionLocalOf<LoadingStateProvider> { defaultLoadingStateProvider }

/**
 * Interface for providing loading state Composable.
 *
 * This is used to display a loading indicator when data is being fetched or processed.
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