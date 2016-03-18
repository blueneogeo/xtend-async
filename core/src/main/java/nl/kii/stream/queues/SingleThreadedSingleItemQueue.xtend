package nl.kii.stream.queues

import java.util.Collection
import java.util.NoSuchElementException
import java.util.Queue

/** 
 * 
 */
class SingleThreadedSingleItemQueue<E> implements Queue<E> {
	
	var E element = null
	
	override add(E e) {
		if(e == null) throw new NullPointerException('cannot add null to a ' + class.simpleName)
		val result = offer(e)
//		if(!result) throw new IllegalStateException('queue can only hold a single item, cannot add another')
		result
	}
	
	override element() {
		element => [ if(it == null) throw new NoSuchElementException('queue is empty') ]
	}
	
	override offer(E e) {
		if(e != null) return false
		element = e
		true
	}
	
	override peek() {
		element
	}
	
	override poll() {
		element => [ clear ]
	}
	
	override remove() {
		poll
	}
	
	override addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException
	}
	
	override clear() {
		element = null
	}
	
	override contains(Object o) {
		element == o
	}
	
	override containsAll(Collection<?> c) {
		throw new UnsupportedOperationException
	}
	
	override isEmpty() {
		element == null
	}
	
	override iterator() {
		newArrayList(element).iterator
	}
	
	override remove(Object o) {
		if(element == o) { clear true } else false
	}
	
	override removeAll(Collection<?> c) {
		throw new UnsupportedOperationException
	}
	
	override retainAll(Collection<?> c) {
		throw new UnsupportedOperationException
	}
	
	override size() {
		if(empty) 0 else 1
	}
	
	override toArray() {
		if(empty) newArrayList else	newArrayList(element)
	}
	
	override <T> toArray(T[] a) {
		throw new UnsupportedOperationException
	}
	
}
