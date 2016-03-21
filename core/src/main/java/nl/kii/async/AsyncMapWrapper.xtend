package nl.kii.async

import java.util.List
import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import nl.kii.promise.Task

import static nl.kii.promise.PromiseExtensions.*

/** 
 * Converts a normal Map into an AsyncMap
 */
class AsyncMapWrapper<K, V> implements AsyncMap<K, V> {
	
	val Map<K, V> map
	
	/** Create using a new ConcurrentHashMap */
	new() { this(new ConcurrentHashMap) }
	
	/** Create wrapping your own map */
	new(Map<K, V> myMap) { this.map = myMap }
	
	override put(K key, V value) {
		map.put(key, value)
		new Task => [ complete ]
	}
	
	override get(K key) {
		promise(map.get(key))
	}
	
	override remove(K key) {
		map.remove(key)
		new Task => [ complete ]
	}
	
	override get(List<K> keys) {
		val result = keys.map [ it->map.get(it) ].toMap
		promise(result)
	}
	
	// copied from xtend-tools/IterableExtensions.toMap
	private static def <K, V> Map<K, V> toMap(Iterable<Pair<K, V>> pairs) {
		val map = newHashMap
		if(pairs != null) pairs.forEach [ map.put(key, value) ]
		map
	}
	
}