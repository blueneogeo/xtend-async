package nl.kii.stream.queues

import java.util.Collection
import java.util.NoSuchElementException
import java.util.Queue
import java.util.concurrent.atomic.AtomicReference

/** A thread safe queue of a single item! Used to increase performance */
class ThreadSafeSingleItemQueue<E> implements Queue<E> {
	
	val element = new AtomicReference<E>
	
	override add(E e) {
		if(e == null) throw new NullPointerException('cannot add null to a ' + class.simpleName)
		val result = offer(e)
//		if(!result) throw new IllegalStateException('queue can only hold a single item, cannot add another')
		result
	}
	
	override element() {
		element.get => [ if(it == null) throw new NoSuchElementException('queue is empty') ]
	}
	
	override offer(E e) {
		element.compareAndSet(null, e)
	}
	
	override peek() {
		element.get
	}
	
	override poll() {
		element.getAndSet(null)
	}
	
	override remove() {
		poll
	}
	
	override addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException
	}
	
	override clear() {
		remove
	}
	
	override contains(Object o) {
		element.get == o
	}
	
	override containsAll(Collection<?> c) {
		throw new UnsupportedOperationException
	}
	
	override isEmpty() {
		element.get == null
	}
	
	override iterator() {
		newArrayList(element.get).iterator
	}
	
	override remove(Object o) {
		if(element.get == o) { clear true } else false
	}
	
	override removeAll(Collection<?> c) {
		if(c.contains(element.get)) { 
			clear
			true			
		} else false
	}
	
	override retainAll(Collection<?> c) {
		if(!c.contains(element.get)) { 
			clear
			true			
		} else false
	}
	
	override size() {
		if(empty) 0 else 1
	}
	
	override toArray() {
		if(empty) newArrayList else	newArrayList(element.get)
	}
	
	override <T> toArray(T[] a) {
		 { if(empty) #[] else #[element.get] }.toArray(a)
	}
	
}
