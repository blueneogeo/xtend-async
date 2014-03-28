package nl.kii.stream.impl

import java.util.HashMap
import java.util.Map
import java.util.WeakHashMap
import nl.kii.stream.Publisher
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

class SimplePublisher<T> implements Publisher<T> {
	
	val public boolean usesWeakReferences
	var public isPublishing = true
	
	new() {	this(false) }
	new(boolean usesWeakReferences) { this.usesWeakReferences = usesWeakReferences }
	
	Map<Procedure1<T>, String> _listeners
	
	override onChange(Procedure1<T> listener) {
		// FIX: use weak references or we get a memory problem...
		// however, currently it seems to let go too soon!
		if(_listeners == null) 
			_listeners = if(usesWeakReferences) 
				new WeakHashMap 
				else new HashMap
		_listeners.put(listener, '')
	}
	
	override apply(T change) {
		if(_listeners == null || !isPublishing) return;
		for(listener : _listeners.keySet)
			listener.apply(change)
	}
	
}
