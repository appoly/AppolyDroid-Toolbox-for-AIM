package uk.co.appoly.droid.util.paging

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.LoadState.Error
import uk.co.appoly.droid.ui.paging.LocalErrorState

/**
 * Adds an error state item to the list.
 *
 * Creates an error message with a retry option using the [LocalErrorState] composition local provider.
 * The error state is animated when added to the list.
 *
 * @param key A unique key prefix for the error item
 * @param error The [LoadState.Error] containing error details
 * @param contentPadding Padding values to apply around the error state item
 * @param errorText A composable function that converts the error to a display string
 * @param retry A callback function that is invoked when the user chooses to retry the operation
 */
inline fun LazyListScope.errorStateItem(
	key: Any,
	error: LoadState.Error,
	crossinline errorText: @Composable (LoadState.Error) -> String,
	crossinline retry: () -> Unit,
	contentPadding: PaddingValues = PaddingValues(0.dp)
) {
	item(
		key = "${key}_Paging_error",
		contentType = "${key}_Paging_error"
	) {
		LocalErrorState.current.ErrorState(
			modifier = Modifier
				.animateItem()
				.fillMaxWidth(),
			text = errorText(error),
			onRetry = {
				retry()
			},
			contentPadding = contentPadding
		)
	}
}