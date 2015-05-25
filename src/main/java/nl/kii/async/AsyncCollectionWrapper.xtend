package nl.kii.async

import java.util.Collection
import nl.kii.promise.Task

import static extension nl.kii.promise.PromiseExtensions.*

/** 
 * Converts a normal Collection into an AsyncCollection.
 */
class AsyncCollectionWrapper<T> implements AsyncCollection<T> {
	
	val Collection<T> collection

	new(Collection<T> collection) {
		this.collection = collection
	}
	
	override add(T value) {
		collection.add(value)
		new Task => [ complete ]
	}
	
	override remove(T value) {
		collection.remove(value)
		new Task => [ complete ]
	}
	
	override clear() {
		collection.clear
		new Task => [ complete ]
	}
	
	override isEmpty() {
		collection.isEmpty.promise
	}
	
	override size() {
		collection.size.promise
	}
	
	override iterator() {
		collection.iterator.promise
	}
	
}