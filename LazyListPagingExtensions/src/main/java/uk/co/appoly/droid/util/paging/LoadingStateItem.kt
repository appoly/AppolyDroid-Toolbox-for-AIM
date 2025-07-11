package uk.co.appoly.droid.util.paging

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.appoly.droid.ui.paging.LocalLoadingState

/**
 * Adds a loading state item to the list.
 *
 * Creates a loading indicator using the [LocalLoadingState] composition local provider.
 * The loading indicator is animated when added to the list.
 *
 * @param key A unique key for the loading item to help with efficient list updates
 * @param contentPadding Padding values to apply around the loading state item
 */
fun LazyListScope.loadingStateItem(
	key: Any,
	contentPadding: PaddingValues = PaddingValues(0.dp)
) {
	item(
		key = key,
		contentType = key
	) {
		LocalLoadingState.current.LoadingState(
			modifier = Modifier
				.animateItem()
				.fillMaxWidth(),
			contentPadding = contentPadding
		)
	}
}