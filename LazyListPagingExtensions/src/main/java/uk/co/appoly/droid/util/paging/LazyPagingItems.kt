package uk.co.appoly.droid.util.paging

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey

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
	crossinline placeholderItemContent: @Composable LazyItemScope.() -> Unit = {},
	crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
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
 * Adds a list of items from a [LazyPagingItems] object with indexes.
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
 * @param itemContent the content displayed by a single item, this provides the index and the item.
 * @param placeholderItemContent the content displayed by a single placeholder item, this provides the index.
 *
 * @see LazyListScope.items
 */
inline fun <T : Any> LazyListScope.lazyPagingItemsIndexed(
	lazyPagingItems: LazyPagingItems<T>,
	noinline key: ((index: Int, item: T) -> Any)? = null,
	noinline contentType: (index: Int, item: T) -> Any? = { _,_-> null },
	crossinline placeholderItemContent: @Composable LazyItemScope.(index: Int) -> Unit = {},
	crossinline itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit
) = items(
	count = lazyPagingItems.itemCount,
	key = if (key != null) {
		{ i-> lazyPagingItems.itemKey { key(i, it) }.invoke(i) }
	} else null,
	contentType = { i -> lazyPagingItems.itemContentType { contentType(i, it) } },
	itemContent = { index ->
		val item = lazyPagingItems[index]
		if (item != null) {
			itemContent(index, item)
		} else {
			placeholderItemContent(index)
		}
	}
)