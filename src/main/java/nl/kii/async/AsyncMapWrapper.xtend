package nl.kii.async

import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import nl.kii.promise.Task

import static extension nl.kii.promise.PromiseExtensions.*

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
	
	override get(K... keys) {
		keys.map[ it->map.get(it) ].toMap.promise
	}
	
	// copied from xtend-tools/IterableExtensions.toMap
	private static def <K, V> Map<K, V> toMap(Iterable<Pair<K, V>> pairs) {
		val map = newHashMap
		if(pairs != null) pairs.forEach[map.put(key, value)]
		map
	}
	
}