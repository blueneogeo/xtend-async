package nl.kii.stream

import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import static extension nl.kii.stream.StreamExtensions.*

class Gatherer<T> extends Promise<Map<String, T>> {
	
	val data = new ConcurrentHashMap<String, T>
	val protected count = new AtomicInteger
	val protected total = new AtomicInteger
		
	def Promise<T> await(String name) {
		val promise = new Promise<T>
		total.incrementAndGet;
		return [ 
			stream.push(name -> it)
			if(count.incrementAndGet == total.get)
				stream.finish
		]		
	}
	
}


/**
 * Gatherer can collect data from various asynchronous sources.
 * <p>
 * You use it by calling the await(name) method, which gives you
 * a Procedure1 in return. The async code elsewhere can then call
 * this procedure with a result when it is ready.
 * <p>
 * AFTER creating all the await functions you need, you can listen
 * for the functions to finish with a result as an Observable.
 * You can both monitor the results coming in, as well as respond
 * to the closing of the stream using listen.onFinish [ ... ]
 * <p>
 * In the meantime, you also can asynchronously check if the 
 * collector has finished using the isFinished method.
 * <p>
 * When the wait functions have finished, you can request the result
 * data of all awaited functions with the result call, which gives
 * you a map<name, value> of all values. This map also gets filled
 * as the data comes in and is a concurrent map.
 * <p>
 * The awaited functions often work great as closures. For example:
 * <p>
 * <pre>
 * val c = new Gatherer<JSON>
 * // perform slow async calls
 * API.loadUser(12, c.await('user'))
 * API.loadRights(45, c.await('rights'))
 * // listen to the results
 * c.stream.onFinish [
 * 		val it = c.result
 * 		println('loaded user: ' + get('user'))
 * 		println('loaded rights: ' + get('rights'))
 * ]
 * </pre>
 */
class Gatherer2<T> implements Streamable<Pair<String, T>> {
	
	val stream = new Stream<Pair<String, T>>
	val protected count = new AtomicInteger
	val protected total = new AtomicInteger
	val data = new ConcurrentHashMap<String, T>
	
	new() { stream.onEach[ data.put(key, value) ]}

	def (T)=>void await(String name) {
		total.incrementAndGet;
		return [ 
			stream.push(name -> it)
			if(count.incrementAndGet == total.get)
				stream.finish
		]
	}
	
	def collect(String name, T value) {
		await(name).apply(value)
	}

	override stream() {
		stream
	}
	
	def result() {
		data
	}

	def isFinished() {
		count.get == total.get
	}
	
	def onFinish((Map<String, T>)=>void closure) {
		stream.onFinish [
			closure.apply(result) 
		]
	}
	
}

/**
 * Countdown is a specialized version of Collector that collects
 * if various functions have been executed successfully. You can
 * call await with a name like a collector or without a name, in
 * which case the name will be a number. The result function takes
 * a true/false based on whether the async code was successful.
 * <p>
 * You can ask if all functions were successful by calling isSuccess
 * <p>
 * Example code:
 * <p>
 * <pre>
 * val c = new Countdown
 * // create waiting functions
 * val signal1 = c.await
 * val signal2 = c.await
 * // finish when both async waiting functions complete
 * c.listen.onFinish [ println('both code functions ran. success: ' + c.success) ]
 * // sometime later, the signal functions are applied
 * [| ... some code .... signal1.apply(true) .... ].scheduleLater
 * [|... some other async code... signal2.apply(true) ...].scheduleLater
 * </pre>
 */
class Countdown extends Gatherer<Boolean> {
	
	def (Boolean)=>void await() {
		super.await(total.get.toString)
	}
	
	def isSuccess() {
		result.values.reduce [a, b| a && b]
	}
	
}

interface Streamable<T> {
	
	def Stream<T> stream()
	
}
