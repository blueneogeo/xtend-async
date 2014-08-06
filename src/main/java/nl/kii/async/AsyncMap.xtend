package nl.kii.async

import nl.kii.promise.Task
import nl.kii.promise.Promise

interface AsyncMap<K, V> {
	
	def Task put(K key, V value)
	
	def Promise<V> get(K key)
	
	def Task remove(K key)
	
	def Task clear()
	
	def Promise<Boolean> isEmpty()
	
}
