package nl.kii.async

import java.util.Iterator
import nl.kii.promise.Promise
import nl.kii.promise.Task

/**
 * An asynchronous version of a Java Collection.
 * <p>
 * This means that all operations are non-blocking, and instead of returning void and values,
 * they return Tasks and Promises. These can be listened to for the result of the operation,
 * or to catch any thrown exceptions.
 * <p>
 * Async collections are especially useful representing networked operations, since it allows
 * for slower operations to not block the code and to have a mechanism to catch exceptions. 
 */
interface AsyncCollection<T> {
	
	def Task add(T value)
	
	def Task remove(T value)
	
	def Task clear()

	def Promise<Boolean> isEmpty()

	def Promise<Integer> size()
	
	def Promise<Iterator<T>> iterator()
	
}