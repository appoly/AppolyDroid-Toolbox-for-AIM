/**
 * LazyListPagingExtensions for Jetpack Compose
 *
 * This module provides extension functions for the LazyListScope to work with Paging 3 library's LazyPagingItems.
 * It offers convenient utilities to handle common paging scenarios including:
 *
 * - Displaying paging items with proper key and content type handling
 * - Managing loading states (prepend, append, refresh)
 * - Handling error states with retry functionality
 * - Showing empty state when no items are available
 *
 * The extensions are designed to work with composition locals defined in the UiState module
 * (LocalLoadingState, LocalErrorState, LocalEmptyState) for consistent UI presentation.
 */
package uk.co.appoly.droid.util.paging

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import uk.co.appoly.droid.ui.paging.LocalEmptyState
import uk.co.appoly.droid.ui.paging.LocalErrorState
import uk.co.appoly.droid.ui.paging.LocalLoadingState

/**
 * Adds a list of items from a [LazyPagingItems] object.
 *
 * @param lazyPagingItems The [LazyPagingItems] object to use as the data source
 * @param key a factory of stable and unique keys representing the item. Using the same key
 * for multiple items in the list is not allowed. Type of the key should be saveable
 * via Bundle on Android. If null is passed the position in the list will represent the key.
 * When you specify the key the scroll position will be maintained based on the key, which
 * means if you add/remove items before the current visible item the item with the given key
 * will be kept as the first visible one. This can be overridden by calling
 * 'requestScrollToItem' on the 'LazyListState'.
 * @param contentType a factory of the content types for the item. The item compositions of
 * the same type could be reused more efficiently. Note that null is a valid type and items of such
 * type will be considered compatible.
 * @param itemContent the content displayed by a single item
 * @param placeholderItemContent the content displayed by a single placeholder item
 *
 * @see LazyListScope.items
 */
inline fun <T : Any> LazyListScope.lazyPagingItems(
	lazyPagingItems: LazyPagingItems<T>,
	noinline key: ((item: T) -> Any)? = null,
	noinline contentType: (item: T) -> Any? = { null },
	crossinline placeholderItemContent: @Composable (LazyItemScope.() -> Unit) = {},
	crossinline itemContent: @Composable (LazyItemScope.(item: T) -> Unit)
) = items(
	count = lazyPagingItems.itemCount,
	key = if (key != null) lazyPagingItems.itemKey { key(it) } else null,
	contentType = lazyPagingItems.itemContentType { contentType(it) }
) {
	val item = lazyPagingItems[it]
	if (item != null) {
		itemContent(item)
	} else {
		placeholderItemContent()
	}
}

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

/**
 * Item and loading state management for [LazyPagingItems] within a [LazyListScope].
 *
 * This function will automatically handle the loading, error, empty states and the items.
 *
 * This function uses [LazyListScope.lazyPagingItems], passing the [itemKey], [itemContentType], [itemPlaceholderContent] and [itemContent] to it.
 *
 * @param lazyPagingItems The [LazyPagingItems] object to use as the data source.
 * @param usingPlaceholders If using Placeholders, then the [prependLoadingContent] and [appendLoadingContent] will not be displayed.
 * @param prependLoadingContent The content displayed when the prepend is loading, this defaults to a [loadingStateItem].
 * @param appendLoadingContent The content displayed when the append or refresh is loading, this defaults to a [loadingStateItem].
 * @param refreshLoadingContent The content displayed when the refresh is loading, this defaults to null.
 * @param errorText The text displayed when an error occurs.
 * @param emptyText The text displayed when the list is empty and not loading.
 * @param retry The retry action to perform when an error occurs.
 * @param itemKey The key for the item, this should be unique for each item.
 * @param itemContentType The content type for the item, this should be unique for each item.
 * @param itemPlaceholderContent The content displayed by a single placeholder item.
 * @param itemContent The content displayed by a single item.
 * @param statesContentPadding The padding to apply around the loading and error states, this defaults to 0.dp.
 *
 * @see LazyListScope.lazyPagingItems
 */
inline fun <T : Any> LazyListScope.lazyPagingItemsWithStates(
	lazyPagingItems: LazyPagingItems<T>,
	usingPlaceholders: Boolean = false,
	crossinline prependLoadingContent: LazyListScope.(PaddingValues) -> Unit = { paddingValues ->
		loadingStateItem(key = "paging_prepend_loading", contentPadding = paddingValues)
	},
	crossinline appendLoadingContent: LazyListScope.(PaddingValues) -> Unit = { paddingValues ->
		loadingStateItem(key = "paging_append_loading" , contentPadding = paddingValues)
	},
	noinline refreshLoadingContent: (LazyListScope.(PaddingValues) -> Unit)? = null,
	noinline emptyText: (@Composable () -> String)?,
	crossinline errorText: @Composable (LoadState.Error) -> String,
	crossinline retry: () -> Unit = { lazyPagingItems.retry() },
	noinline itemKey: ((item: T) -> Any)? = null,
	noinline itemContentType: (item: T) -> Any? = { null },
	crossinline itemPlaceholderContent: @Composable (LazyItemScope.() -> Unit) = {},
	crossinline itemContent: @Composable (LazyItemScope.(item: T) -> Unit),
	statesContentPadding: PaddingValues = PaddingValues(0.dp)
) = lazyPagingItemsWithStates(
	lazyPagingItems = lazyPagingItems,
	usingPlaceholders = usingPlaceholders,
	prependLoadingContent = prependLoadingContent,
	appendLoadingContent = appendLoadingContent,
	refreshLoadingContent = refreshLoadingContent,
	errorContent = { key, error, paddingValues ->
		errorStateItem(
			key = "${key}_Paging_error",
			error = error,
			errorText = errorText,
			retry = retry,
			contentPadding = paddingValues
		)
	},
	emptyContent = if (emptyText != null) {
		{
			emptyStateItem(
				key = "paging_empty",
				emptyText = emptyText,
				contentPadding = statesContentPadding
			)
		}
	} else null,
	itemKey = itemKey,
	itemContentType = itemContentType,
	itemPlaceholderContent = itemPlaceholderContent,
	itemContent = itemContent,
	statesContentPadding = statesContentPadding
)

/**
 * Item and loading state management for [LazyPagingItems] within a [LazyListScope].
 *
 * This function will automatically handle the loading, error, empty states and the items.
 *
 * @param lazyPagingItems The [LazyPagingItems] object to use as the data source.
 * @param usingPlaceholders If using Placeholders, then the [prependLoadingContent] and [appendLoadingContent] will not be displayed.
 * @param prependLoadingContent The content displayed when the prepend is loading, this defaults to a [loadingStateItem].
 * @param appendLoadingContent The content displayed when the append or refresh is loading, this defaults to a [loadingStateItem].
 * @param refreshLoadingContent The content displayed when the refresh is loading, this defaults to null.
 * @param errorText The text displayed when an error occurs.
 * @param emptyText The text displayed when the list is empty and not loading.
 * @param retry The retry action to perform when an error occurs.
 * @param itemsContent The content displayed for the items.
 * @param statesContentPadding The padding to apply around the loading and error states, this defaults to 0.dp.
 */
inline fun <T : Any> LazyListScope.lazyPagingItemsWithStates(
	lazyPagingItems: LazyPagingItems<T>,
	usingPlaceholders: Boolean = false,
	crossinline prependLoadingContent: LazyListScope.(PaddingValues) -> Unit = { paddingValues ->
		loadingStateItem(key = "paging_prepend_loading", contentPadding = paddingValues)
	},
	crossinline appendLoadingContent: LazyListScope.(PaddingValues) -> Unit = { paddingValues ->
		loadingStateItem(key = "paging_append_loading", contentPadding = paddingValues)
	},
	noinline refreshLoadingContent: (LazyListScope.(PaddingValues) -> Unit)? = null,
	crossinline emptyText: @Composable () -> String,
	crossinline errorText: @Composable (LoadState.Error) -> String,
	crossinline retry: () -> Unit = { lazyPagingItems.retry() },
	crossinline itemsContent: LazyListScope.(lazyPagingItems: LazyPagingItems<T>) -> Unit,
	statesContentPadding: PaddingValues = PaddingValues(0.dp)
) = lazyPagingItemsWithStates(
	lazyPagingItems = lazyPagingItems,
	usingPlaceholders = usingPlaceholders,
	prependLoadingContent = prependLoadingContent,
	appendLoadingContent = appendLoadingContent,
	refreshLoadingContent = refreshLoadingContent,
	errorContent = { key, error, paddingValues ->
		errorStateItem(
			key = "${key}_Paging_error",
			error = error,
			errorText = errorText,
			retry = retry,
			contentPadding = paddingValues
		)
	},
	emptyContent = {
		emptyStateItem(
			key = "paging_empty",
			emptyText = emptyText,
			contentPadding = statesContentPadding
		)
	},
	itemsContent = itemsContent,
	statesContentPadding = statesContentPadding
)

/**
 * Item and loading state management for [LazyPagingItems] within a [LazyListScope].
 *
 * This function will automatically handle the loading, error, empty states and the items.
 *
 * This function uses [LazyListScope.lazyPagingItems], passing the [itemKey], [itemContentType], [itemPlaceholderContent] and [itemContent] to it.
 *
 * @param lazyPagingItems The [LazyPagingItems] object to use as the data source.
 * @param usingPlaceholders If using Placeholders, then the [prependLoadingContent] and [appendLoadingContent] will not be displayed.
 * @param prependLoadingContent The content displayed when the prepend is loading, this defaults to a [loadingStateItem].
 * @param appendLoadingContent The content displayed when the append or refresh is loading, this defaults to a [loadingStateItem].
 * @param refreshLoadingContent The content displayed when the refresh is loading, this defaults to null.
 * @param errorContent The content displayed when an error occurs, this provides the key and the error, the should be used for
 * the [item][LazyListScope.item] key as there could show multiple errors for the prepend, append and refresh states.
 * @param emptyContent The content displayed when the list is empty and not loading.
 * @param itemKey The key for the item, this should be unique for each item.
 * @param itemContentType The content type for the item, this should be unique for each item.
 * @param itemPlaceholderContent The content displayed by a single placeholder item.
 * @param itemContent The content displayed by a single item.
 * @param statesContentPadding The padding to apply around the loading and error states, this defaults to 0.dp.
 *
 * @see LazyListScope.lazyPagingItems
 */
inline fun <T : Any> LazyListScope.lazyPagingItemsWithStates(
	lazyPagingItems: LazyPagingItems<T>,
	usingPlaceholders: Boolean = false,
	crossinline prependLoadingContent: LazyListScope.(PaddingValues) -> Unit = { paddingValues ->
		loadingStateItem("paging_prepend_loading", contentPadding = paddingValues)
	},
	crossinline appendLoadingContent: LazyListScope.(PaddingValues) -> Unit = { paddingValues ->
		loadingStateItem("paging_append_loading", contentPadding = paddingValues)
	},
	noinline refreshLoadingContent: (LazyListScope.(PaddingValues) -> Unit)? = null,
	crossinline errorContent: LazyListScope.(key: PagingErrorType, error: LoadState.Error, PaddingValues) -> Unit,
	noinline emptyContent: (LazyListScope.(PaddingValues) -> Unit)?,
	noinline itemKey: ((item: T) -> Any)? = null,
	noinline itemContentType: (item: T) -> Any? = { null },
	crossinline itemPlaceholderContent: @Composable (LazyItemScope.() -> Unit) = {},
	crossinline itemContent: @Composable (LazyItemScope.(item: T) -> Unit),
	statesContentPadding: PaddingValues = PaddingValues(0.dp)
) = lazyPagingItemsWithStates(
	lazyPagingItems = lazyPagingItems,
	usingPlaceholders = usingPlaceholders,
	prependLoadingContent = prependLoadingContent,
	appendLoadingContent = appendLoadingContent,
	refreshLoadingContent = refreshLoadingContent,
	errorContent = errorContent,
	emptyContent = emptyContent,
	itemsContent = {
		lazyPagingItems(
			lazyPagingItems = lazyPagingItems,
			key = itemKey,
			contentType = itemContentType,
			placeholderItemContent = itemPlaceholderContent,
			itemContent = itemContent
		)
	},
	statesContentPadding = statesContentPadding
)

/**
 * Item and loading state management for [LazyPagingItems] within a [LazyListScope].
 *
 * This function will automatically handle the loading, error, empty states and the items.
 *
 * @param lazyPagingItems The [LazyPagingItems] object to use as the data source.
 * @param errorContent The content displayed when an error occurs, this provides the key and the error, the should be used for
 * the [item][LazyListScope.item] key as there could show multiple errors for the prepend, append and refresh states.
 * @param emptyContent The content displayed when the list is empty and not loading.
 * @param itemsContent The content displayed for the items.
 * @param prependLoadingContent The content displayed when the prepend is loading, this defaults to a [LoadingState].
 * @param appendLoadingContent The content displayed when the append or refresh is loading, this defaults to a [LoadingState].
 * @param refreshLoadingContent The content displayed when the refresh is loading, this defaults to null.
 * @param usingPlaceholders If using Placeholders, then the [prependLoadingContent] and [appendLoadingContent] will not be displayed.
 * @param statesContentPadding The padding to apply around the loading and error states, this defaults to 0.dp.
 */
inline fun <T : Any> LazyListScope.lazyPagingItemsWithStates(
	lazyPagingItems: LazyPagingItems<T>,
	crossinline errorContent: LazyListScope.(key: PagingErrorType, error: LoadState.Error, PaddingValues) -> Unit,
	noinline emptyContent: (LazyListScope.(PaddingValues) -> Unit)?,
	crossinline itemsContent: LazyListScope.(lazyPagingItems: LazyPagingItems<T>) -> Unit,
	crossinline prependLoadingContent: LazyListScope.(PaddingValues) -> Unit = { paddingValues ->
		loadingStateItem("paging_prepend_loading", contentPadding = paddingValues)
	},
	crossinline appendLoadingContent: LazyListScope.(PaddingValues) -> Unit = { paddingValues ->
		loadingStateItem("paging_append_loading", contentPadding = paddingValues)
	},
	noinline refreshLoadingContent: (LazyListScope.(PaddingValues) -> Unit)? = null,
	usingPlaceholders: Boolean = false,
	statesContentPadding: PaddingValues = PaddingValues(0.dp),
) {
	val prependState = lazyPagingItems.loadState.prepend
	val appendState = lazyPagingItems.loadState.append
	val refreshState = lazyPagingItems.loadState.refresh
	val loading = (listOf(
		prependState,
		appendState,
		refreshState
	).firstOrNull { it.isLoading() } as LoadState.Loading?) != null
	if (!loading && refreshState.isError()) {
		errorContent(PagingErrorType.REFRESH, refreshState, statesContentPadding)
	} else {
		if (!usingPlaceholders && prependState.isLoading()) {
			prependLoadingContent(statesContentPadding)
		} else if (prependState.isError()) {
			errorContent(PagingErrorType.PREPEND, prependState, statesContentPadding)
		}
		if (refreshLoadingContent != null && refreshState.isLoading()) {
			refreshLoadingContent(statesContentPadding)
		}
		if (emptyContent != null && lazyPagingItems.itemCount == 0 && !loading) {
			emptyContent(statesContentPadding)
		} else {
			itemsContent(lazyPagingItems)
		}
		if (!usingPlaceholders && appendState.isLoading()) {
			appendLoadingContent(statesContentPadding)
		} else if (appendState.isError()) {
			errorContent(PagingErrorType.APPEND, appendState, statesContentPadding)
		}
	}
}
