package nl.kii.stream

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

/** 
 * A publisher is simply a procedure whose calls you can observe.
 * In other words, it broadcasts what you put in to its observers.
 */
interface Publisher<T> extends Observable<T>, Procedure1<T> {
	
}
