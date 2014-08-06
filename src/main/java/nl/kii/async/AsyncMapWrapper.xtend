package nl.kii.async
import static extension nl.kii.promise.PromiseExtensions.*
import java.util.Map
import nl.kii.promise.Task

/** 
 * Converts a normal Map into an AsyncMap
 */
class AsyncMapWrapper<K, V> implements AsyncMap<K, V> {
	
	val Map<K, V> map
	
	new(Map<K, V> map) {
		this.map = map
	}
	
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
	
	override clear() {
		map.clear
		new Task().complete
	}
	
	override isEmpty() {
		map.isEmpty.promise
	}
	
}