package uk.co.appoly.droid.util.paging

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey


/**
 * Adds a list of items from a [LazyPagingItems] object, providing the previous item and next item
 * to the item content lambda.
 *
 * This is useful for scenarios where you need to compare the current item with its neighbors,
 * such as implementing a carousel or a list with contextual actions based on adjacent items.
 *
 * @param lazyPagingItems The [LazyPagingItems] object to use as the data source
 * @param key a factory of stable and unique keys representing the item. Using the same key
 * for multiple items in the list is not allowed. Type of the key should be saveable
 * via Bundle on Android. If null is passed the position in the list will represent the key.
 * When you specify the key the scroll position will be maintained based on the key, which
 * means if you add/remove items before the current visible item the item with the given key
 * will be kept as the first visible one. This can be overridden by calling
 * `requestScrollToItem` on the `LazyListState`.
 * @param contentType a factory of the content types for the item. The item compositions of
 * the same type could be reused more efficiently. Note that null is a valid type and items of such
 * type will be considered compatible.
 * @param placeholderItemContent the content displayed by a single placeholder item
 * @param item LazyListScope content lambda that provides the previous item, the current item, the next item,
 * the item key and the item content type.
 *
 * @see LazyListScope.items
 */
inline fun <T : Any> LazyListScope.lazyPagingItemsWithNeighbours(
	lazyPagingItems: LazyPagingItems<T>,
	noinline key: ((item: T) -> Any)? = null,
	noinline contentType: (item: T) -> Any? = { null },
	crossinline placeholderItemContent: @Composable (LazyItemScope.() -> Unit) = {},
	crossinline item: LazyListScope.(prevItem: T?, item: T, nextItem: T?, itemKey: Any?, itemContentType: Any?) -> Unit
) {
	for (index in 0 until lazyPagingItems.itemCount) {
		val prevItem = if (index > 0) lazyPagingItems.peek(index - 1) else null
		val item = lazyPagingItems[index]
		val nextItem = if (index < lazyPagingItems.itemCount - 1) lazyPagingItems.peek(index + 1) else null
		if (item != null) {
			item(
				prevItem,
				item,
				nextItem,
				if (key != null) lazyPagingItems.itemKey { key(it) }.invoke(index) else null,
				lazyPagingItems.itemContentType { contentType(it) }.invoke(index)
			)
		} else {
			item(
				key = if (key != null) lazyPagingItems.itemKey { key(it) }.invoke(index) else null,
				contentType = lazyPagingItems.itemContentType { contentType(it) }.invoke(index)
			) {
				placeholderItemContent()
			}
		}
	}
}

/**
 * Adds a list of items from a [LazyPagingItems] object with indexes and provides the previous item and next item
 * to the item content lambda.
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
 * @param itemContent the content displayed by a single item, this provides the previous item, the index, the item and the next item.
 * @param placeholderItemContent the content displayed by a single placeholder item, this provides the previous item and the next item.
 *
 * @see LazyListScope.items
 */
inline fun <T : Any> LazyListScope.lazyPagingItemsIndexedWithNeighbours(
	lazyPagingItems: LazyPagingItems<T>,
	noinline key: ((index: Int, item: T) -> Any)? = null,
	noinline contentType: (index: Int, item: T) -> Any? = { _,_-> null },
	crossinline placeholderItemContent: @Composable LazyItemScope.(prevItem: T?, nextItem: T?) -> Unit = { _,_ -> },
	crossinline itemContent: @Composable LazyItemScope.(prevItem: T?, index: Int, item: T, nextItem: T?) -> Unit
) = items(
	count = lazyPagingItems.itemCount,
	key = if (key != null) {
		{ i ->
			lazyPagingItems.itemKey(key = { item -> key(i, item) }).invoke(i)
		}
	} else null,
	contentType = { i -> lazyPagingItems.itemContentType { contentType(i, it) } },
	itemContent = { index ->
		val prevItem = if (index > 0) lazyPagingItems.peek(index - 1) else null
		val item = lazyPagingItems[index]
		val nextItem = if (index < lazyPagingItems.itemCount - 1) lazyPagingItems.peek(index + 1) else null
		if (item != null) {
			itemContent(prevItem, index, item, nextItem)
		} else {
			placeholderItemContent(prevItem, nextItem)
		}
	}
)