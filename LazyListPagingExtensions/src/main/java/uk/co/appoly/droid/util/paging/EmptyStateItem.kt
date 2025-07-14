package uk.co.appoly.droid.util.paging

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.appoly.droid.ui.paging.LocalEmptyState

/**
 * Adds an empty state item to the list.
 *
 * Creates an empty state message using the [LocalEmptyState] composition local provider.
 * The empty state is animated when added to the list.
 *
 * @param key A unique key for the empty state item
 * @param emptyText A composable function that provides the text to display when the list is empty
 * @param contentPadding Padding values to apply around the empty state item
 */
inline fun LazyListScope.emptyStateItem(
	key: Any,
	crossinline emptyText: @Composable () -> String,
	contentPadding: PaddingValues = PaddingValues(0.dp)
) {
	item(
		key = key,
		contentType = key
	) {
		LocalEmptyState.current.EmptyStateText(
			modifier = Modifier
				.animateItem()
				.fillMaxWidth(),
			text = emptyText(),
			contentPadding = contentPadding
		)
	}
}