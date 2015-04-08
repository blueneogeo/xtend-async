package nl.kii.async.test

import nl.kii.promise.IPromise
import nl.kii.promise.SubPromise
import org.junit.Test

import static extension nl.kii.async.ExecutorExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*

class FancyAsyncTests {
	
	@Test
	def void testFancyClosures() {
		
//		complete
//			.map [ 'hello world' ]
//			.when([ String it | contains('hello') ],
//				[ complete ],
//				[ error('test').asTask ]
//			)
//			.then [ println(it) ]

		val x = complete
			.map [ 'hello world' ]
			.if_(true,
				[ it ],
				[ error(String, 'test') ]
			)
			.future
			.get
		println(x)
		
//		val y = complete
//			.map [ 'hi' ]
//			.if_ [ it == 'hi' ]
//				.then [ println('hi') ]
// 				.else_ [ println('boo') ]
	}

	def static <I, O, R, P extends IPromise<I, O>, P2 extends IPromise<?, R>> if_(P promise, (O)=>boolean testFn, (P)=>P2 thenFn) {
		
			
	}
	
	def static <I, O, R, P extends IPromise<I, O>> 
		Pair<P, Boolean> if_(P promise, (O)=>boolean testFn) {
		
	}

	def static <I, O, R, P extends IPromise<I, O>, P2 extends IPromise<?, R>> 
		if_(P promise, (O)=>boolean testFn, (P)=>P2 thenFn, (P)=>P2 elseFn) {

		val newPromise = new SubPromise<I, R>(promise)
		promise
			.onError [ i, e | newPromise.error(i, e) ]
			.then [ i, o |
				try {
					val result = 
						if(testFn.apply(o)) thenFn.apply(promise) 
						else elseFn.apply(promise)
					result
						.onError [ newPromise.error(it) ]
						.then [ r | newPromise.set(i, r) ]
				} catch(Throwable e) {
					newPromise.error(e)
				}
			]
		newPromise => [ operation = 'when' ]
	}

	def static <I, O, R, P extends IPromise<I, O>, P2 extends IPromise<?, R>> 
		if_(P promise, boolean condition, (P)=>P2 thenFn, (P)=>P2 elseFn) {
		promise.if_([condition], thenFn, elseFn)
	}
	
}
