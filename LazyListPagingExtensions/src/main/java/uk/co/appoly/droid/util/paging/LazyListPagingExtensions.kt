package uk.co.appoly.droid.util.paging

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

fun LazyListScope.loadingStateItem(
	key: Any
) {
	item(
		key = key,
		contentType = key
	) {
		LocalLoadingState.current.LoadingState(
			modifier = Modifier
				.fillMaxWidth()
				.animateItem()
		)
	}
}

inline fun LazyListScope.errorStateItem(
	key: Any,
	error: LoadState.Error,
	crossinline errorText: @Composable (LoadState.Error) -> String,
	crossinline retry: () -> Unit
) {
	item(
		key = "${key}_Paging_error",
		contentType = "${key}_Paging_error"
	) {
		LocalErrorState.current.ErrorState(
			modifier = Modifier
				.fillMaxWidth()
				.animateItem(),
			text = errorText(error),
			onRetry = {
				retry()
			}
		)
	}
}

inline fun LazyListScope.emptyStateItem(
	key: Any,
	crossinline emptyText: @Composable () -> String
) {
	item(
		key = key,
		contentType = key
	) {
		LocalEmptyState.current.EmptyStateText(
			modifier = Modifier
				.fillMaxWidth()
				.animateItem(),
			text = emptyText()
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
 * @param prependLoadingContent The content displayed when the prepend is loading, this defaults to a [LoadingState].
 * @param appendLoadingContent The content displayed when the append or refresh is loading, this defaults to a [LoadingState].
 * @param refreshLoadingContent The content displayed when the refresh is loading, this defaults to null.
 * @param errorText The text displayed when an error occurs.
 * @param emptyText The text displayed when the list is empty and not loading.
 * @param retry The retry action to perform when an error occurs.
 * @param itemKey The key for the item, this should be unique for each item.
 * @param itemContentType The content type for the item, this should be unique for each item.
 * @param itemPlaceholderContent The content displayed by a single placeholder item.
 * @param itemContent The content displayed by a single item.
 *
 * @see LazyListScope.lazyPagingItems
 */
inline fun <T : Any> LazyListScope.lazyPagingItemsWithStates(
	lazyPagingItems: LazyPagingItems<T>,
	usingPlaceholders: Boolean = false,
	crossinline prependLoadingContent: LazyListScope.() -> Unit = {
		loadingStateItem("paging_prepend_loading")
	},
	crossinline appendLoadingContent: LazyListScope.() -> Unit = {
		loadingStateItem("paging_append_loading")
	},
	noinline refreshLoadingContent: (LazyListScope.() -> Unit)? = null,
	noinline emptyText: (@Composable () -> String)?,
	crossinline errorText: @Composable (LoadState.Error) -> String,
	crossinline retry: () -> Unit = { lazyPagingItems.retry() },
	noinline itemKey: ((item: T) -> Any)? = null,
	noinline itemContentType: (item: T) -> Any? = { null },
	crossinline itemPlaceholderContent: @Composable (LazyItemScope.() -> Unit) = {},
	crossinline itemContent: @Composable (LazyItemScope.(item: T) -> Unit)
) = lazyPagingItemsWithStates(
	lazyPagingItems = lazyPagingItems,
	usingPlaceholders = usingPlaceholders,
	prependLoadingContent = prependLoadingContent,
	appendLoadingContent = appendLoadingContent,
	refreshLoadingContent = refreshLoadingContent,
	errorContent = { key, error ->
		errorStateItem("${key}_Paging_error", error, errorText, retry)
	},
	emptyContent = if (emptyText != null) {
		{
			emptyStateItem("paging_empty", emptyText)
		}
	} else null,
	itemKey = itemKey,
	itemContentType = itemContentType,
	itemPlaceholderContent = itemPlaceholderContent,
	itemContent = itemContent
)

/**
 * Item and loading state management for [LazyPagingItems] within a [LazyListScope].
 *
 * This function will automatically handle the loading, error, empty states and the items.
 *
 * @param lazyPagingItems The [LazyPagingItems] object to use as the data source.
 * @param usingPlaceholders If using Placeholders, then the [prependLoadingContent] and [appendLoadingContent] will not be displayed.
 * @param prependLoadingContent The content displayed when the prepend is loading, this defaults to a [LoadingState].
 * @param appendLoadingContent The content displayed when the append or refresh is loading, this defaults to a [LoadingState].
 * @param refreshLoadingContent The content displayed when the refresh is loading, this defaults to null.
 * @param errorText The text displayed when an error occurs.
 * @param emptyText The text displayed when the list is empty and not loading.
 * @param retry The retry action to perform when an error occurs.
 * @param itemsContent The content displayed for the items.
 */
inline fun <T : Any> LazyListScope.lazyPagingItemsWithStates(
	lazyPagingItems: LazyPagingItems<T>,
	usingPlaceholders: Boolean = false,
	crossinline prependLoadingContent: LazyListScope.() -> Unit = {
		loadingStateItem("paging_prepend_loading")
	},
	crossinline appendLoadingContent: LazyListScope.() -> Unit = {
		loadingStateItem("paging_append_loading")
	},
	noinline refreshLoadingContent: (LazyListScope.() -> Unit)? = null,
	crossinline emptyText: @Composable () -> String,
	crossinline errorText: @Composable (LoadState.Error) -> String,
	crossinline retry: () -> Unit = { lazyPagingItems.retry() },
	crossinline itemsContent: LazyListScope.(lazyPagingItems: LazyPagingItems<T>) -> Unit
) = lazyPagingItemsWithStates(
	lazyPagingItems = lazyPagingItems,
	usingPlaceholders = usingPlaceholders,
	prependLoadingContent = prependLoadingContent,
	appendLoadingContent = appendLoadingContent,
	refreshLoadingContent = refreshLoadingContent,
	errorContent = { key, error ->
		errorStateItem("${key}_Paging_error", error, errorText, retry)
	},
	emptyContent = {
		emptyStateItem("paging_empty", emptyText)
	},
	itemsContent = itemsContent
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
 * @param prependLoadingContent The content displayed when the prepend is loading, this defaults to a [LoadingState].
 * @param appendLoadingContent The content displayed when the append or refresh is loading, this defaults to a [LoadingState].
 * @param refreshLoadingContent The content displayed when the refresh is loading, this defaults to null.
 * @param errorContent The content displayed when an error occurs, this provides the key and the error, the should be used for
 * the [item][LazyListScope.item] key as there could show multiple errors for the prepend, append and refresh states.
 * @param emptyContent The content displayed when the list is empty and not loading.
 * @param itemKey The key for the item, this should be unique for each item.
 * @param itemContentType The content type for the item, this should be unique for each item.
 * @param itemPlaceholderContent The content displayed by a single placeholder item.
 * @param itemContent The content displayed by a single item.
 *
 * @see LazyListScope.lazyPagingItems
 */
inline fun <T : Any> LazyListScope.lazyPagingItemsWithStates(
	lazyPagingItems: LazyPagingItems<T>,
	usingPlaceholders: Boolean = false,
	crossinline prependLoadingContent: LazyListScope.() -> Unit = {
		loadingStateItem("paging_prepend_loading")
	},
	crossinline appendLoadingContent: LazyListScope.() -> Unit = {
		loadingStateItem("paging_append_loading")
	},
	noinline refreshLoadingContent: (LazyListScope.() -> Unit)? = null,
	crossinline errorContent: LazyListScope.(key: PagingErrorType, error: LoadState.Error) -> Unit,
	noinline emptyContent: (LazyListScope.() -> Unit)?,
	noinline itemKey: ((item: T) -> Any)? = null,
	noinline itemContentType: (item: T) -> Any? = { null },
	crossinline itemPlaceholderContent: @Composable (LazyItemScope.() -> Unit) = {},
	crossinline itemContent: @Composable (LazyItemScope.(item: T) -> Unit)
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
	}
)

/**
 * Item and loading state management for [LazyPagingItems] within a [LazyListScope].
 *
 * This function will automatically handle the loading, error, empty states and the items.
 *
 * @param lazyPagingItems The [LazyPagingItems] object to use as the data source.
 * @param usingPlaceholders If using Placeholders, then the [prependLoadingContent] and [appendLoadingContent] will not be displayed.
 * @param prependLoadingContent The content displayed when the prepend is loading, this defaults to a [LoadingState].
 * @param appendLoadingContent The content displayed when the append or refresh is loading, this defaults to a [LoadingState].
 * @param refreshLoadingContent The content displayed when the refresh is loading, this defaults to null.
 * @param errorContent The content displayed when an error occurs, this provides the key and the error, the should be used for
 * the [item][LazyListScope.item] key as there could show multiple errors for the prepend, append and refresh states.
 * @param emptyContent The content displayed when the list is empty and not loading.
 * @param itemsContent The content displayed for the items.
 */
inline fun <T : Any> LazyListScope.lazyPagingItemsWithStates(
	lazyPagingItems: LazyPagingItems<T>,
	usingPlaceholders: Boolean = false,
	crossinline prependLoadingContent: LazyListScope.() -> Unit = {
		loadingStateItem("paging_prepend_loading")
	},
	crossinline appendLoadingContent: LazyListScope.() -> Unit = {
		loadingStateItem("paging_append_loading")
	},
	noinline refreshLoadingContent: (LazyListScope.() -> Unit)? = null,
	crossinline errorContent: LazyListScope.(key: PagingErrorType, error: LoadState.Error) -> Unit,
	noinline emptyContent: (LazyListScope.() -> Unit)?,
	crossinline itemsContent: LazyListScope.(lazyPagingItems: LazyPagingItems<T>) -> Unit
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
		errorContent(PagingErrorType.REFRESH, refreshState)
	} else {
		if (!usingPlaceholders && prependState.isLoading()) {
			prependLoadingContent()
		} else if (prependState.isError()) {
			errorContent(PagingErrorType.PREPEND, prependState)
		}
		if (refreshLoadingContent != null && refreshState.isLoading()) {
			refreshLoadingContent()
		}
		if (emptyContent != null && lazyPagingItems.itemCount == 0 && !loading) {
			emptyContent()
		} else {
			itemsContent(lazyPagingItems)
		}
		if (!usingPlaceholders && appendState.isLoading()) {
			appendLoadingContent()
		} else if (appendState.isError()) {
			errorContent(PagingErrorType.APPEND, appendState)
		}
	}
}