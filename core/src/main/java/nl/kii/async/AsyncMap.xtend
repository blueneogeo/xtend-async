package nl.kii.async

import java.util.List
import java.util.Map
import nl.kii.promise.IPromise
import nl.kii.promise.Task

/**
 * An asynchronous version of a Java Map.
 * <p>
 * This means that all operations are non-blocking, and instead of returning void and values,
 * they return Tasks and Promises. These can be listened to for the result of the operation,
 * or to catch any thrown exceptions.
 * <p>
 * Async maps are especially useful representing networked operations, since it allows
 * for slower operations to not block the code and to have a mechanism to catch exceptions.
 * <p>
 * The get for a list of keys is added because it allows the remote implementation to optimize.  
 */
interface AsyncMap<K, V> {
	
	def Task put(K key, V value)
	
	def IPromise<K, V> get(K key)
	
	def IPromise<List<K>, Map<K, V>> get(List<K> keys)
	
	def Task remove(K key)
	
}
