package nl.kii.async
import static extension nl.kii.promise.PromiseExtensions.*
import java.util.Map
import nl.kii.promise.Task
import java.util.concurrent.ConcurrentHashMap

/** 
 * Converts a normal Map into an AsyncMap
 */
class AsyncMemoryMap<K, V> implements AsyncMap<K, V> {
	
	val Map<K, V> map = new ConcurrentHashMap
	
	override put(K key, V value) {
		map.put(key, value)
		new Task().complete
	}
	
	override get(K key) {
		map.get(key).promise
	}
	
	override remove(K key) {
		map.remove(key)
		new Task().complete
	}
	
}