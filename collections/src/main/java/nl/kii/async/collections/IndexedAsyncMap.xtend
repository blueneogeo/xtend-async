package nl.kii.async.collections

import java.util.List
import java.util.Map
import nl.kii.async.promise.Promise
import nl.kii.async.promise.Task

/**
 * An AsyncMap that lets you query more than one index / table.
 * It also supports generating new keys for indexes.
 */
interface IndexedAsyncMap<I, K, V> {

	/** Perform a query and promise a map of keys and values. 
	 * All parameters are optional and may be null.
	 */
	def Promise<?, Map<K, V>> query(I index, String query, K startKey, K endKey, Integer skip, Integer limit, boolean descending)

	/** 
	 * Perform a query and promise a list of keys whose values match the query. 
	 * All parameters are optional and may be null.
	 */
	def Promise<?, List<K>> queryKeys(I index, String query, K startKey, K endKey, Integer skip, Integer limit)

	def Promise<?, String> add(I index, V value)
	
	def Task put(I index, String key, V value)
	
	def Promise<?, V> get(I index, String key)
	
	def Promise<?, Map<K, V>> get(I index, List<K> keys)
	
	def Task remove(I index, K key)
	
	/** Generate a new key for the given index. */
	def Promise<?, K> newKey(I index)
	
}
