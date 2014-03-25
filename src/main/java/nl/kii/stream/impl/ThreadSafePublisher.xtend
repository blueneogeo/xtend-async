package nl.kii.stream.impl

import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import nl.kii.stream.Publisher
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

class ThreadSafePublisher<T> implements Publisher<T> {
	
	var public isPublishing = true
	Map<Procedure1<T>, String> listeners
	
	def protected newListenersMap() {
		new ConcurrentHashMap<Procedure1<T>, String>
	}
	
	override onChange(Procedure1<T> listener) {
		if(listeners == null) listeners = newListenersMap
		listeners.put(listener, '')
	}
	
	override apply(T change) {
		if(listeners == null || !isPublishing) return;
		for(listener : listeners.keySet)
			listener.apply(change)
	}
	
}
