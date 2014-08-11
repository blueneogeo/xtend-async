package nl.kii.async

import nl.kii.promise.Task
import nl.kii.promise.Promise

/**
 * An asynchronous version of a Java Map.
 * <p>
 * This means that all operations are non-blocking, and instead of returning void and values,
 * they return Tasks and Promises. These can be listened to for the result of the operation,
 * or to catch any thrown exceptions.
 * <p>
 * Async maps are especially useful representing networked operations, since it allows
 * for slower operations to not block the code and to have a mechanism to catch exceptions. 
 */
interface AsyncMap<K, V> {
	
	def Task put(K key, V value)
	
	def Promise<V> get(K key)
	
	def Task remove(K key)
	
}
