package nl.kii.stream

import java.io.Closeable
import java.io.IOException
import java.util.Map
import java.util.Set
import java.util.concurrent.ConcurrentHashMap
import static extension nl.kii.util.SynchronizeExt.*

interface StreamBalancer<T> extends Closeable {
	
	def void start()
	def void stop()
	def StreamBalancer<T> register(Stream<T> stream)
	def StreamBalancer<T> register(Stream<T> stream, (T)=>boolean criterium)
	def StreamBalancer<T> unregister(Stream<T> stream)
	def Set<Stream<T>> getStreams()
	
}

class ControlledBalancer<T> implements StreamBalancer<T> {
	
	val Stream<T> source
	val Map<Stream<T>,(T)=>boolean> streams = new ConcurrentHashMap
	val Map<Stream<T>, Boolean> ready = new ConcurrentHashMap
	
	new(Stream<T> source) {
		this.source = source
	}
	
	override register(Stream<T> stream) {
		register(stream) [ true ]
	}
	
	override register(Stream<T> stream, (T)=>boolean criterium) {
		streams.put(stream, criterium)
		ready.put(stream, false)
		stream.onClose [|
			unregister(stream)
		]
		stream.onReadyForNext [|
			ready.put(stream, true)
		]
		stream.onSkip [|]
		this
		
	}
	
	override unregister(Stream<T> stream) {
		streams.remove(stream)
		ready.remove(stream)
		stream.onClose [|]
		stream.onReadyForNext [|]
		this
	}
	
	override close() throws IOException {
		streams.keySet.forEach [ unregister ]
	}
	
	override getStreams() {
		streams.keySet
	}
	
	override start() {
		source.onNextValue [ value |
			synchronize(value) [
				for(stream : ready.keySet) {
					if(ready.get(stream)) {
						stream.push(value)
						ready.put(stream, false)
						return null
					}
				}
			]
		]
	}
	
	override stop() {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
}
