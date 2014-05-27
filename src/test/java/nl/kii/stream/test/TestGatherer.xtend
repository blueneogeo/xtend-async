package nl.kii.stream.test

//import nl.kii.stream.Countdown
//import nl.kii.stream.Gatherer

import java.util.concurrent.atomic.AtomicInteger
import nl.kii.stream.Next
import nl.kii.stream.Promise
import nl.kii.stream.Value
import org.junit.Test

import static nl.kii.stream.PromiseExtensions.*

import static extension nl.kii.stream.StreamExtensions.*
import nl.kii.stream.Stream

class TestCollector {
	
	@Test
	def void testStreaming() {
		val s = Integer.stream
		val s2 = s.map [ it * 2 ]
		run [|
			for(i : 0..999) {
				s.apply(new Value(1))
			}
		]
		run [|
			for(i : 1000..1999) {
				s.apply(new Value(2))
			}
		]
		run [|
			for(i : 2000..2999) {
				s.apply(new Value(3))
			}
		]
		val sum = new AtomicInteger
		s2.listener = [ it, next, skip, close |
			println('got : ' + it)
			switch it {
				Value<Integer>: sum.addAndGet(value) 
			}
			next.apply
		]
		s2.perform(new Next)
		Thread.sleep(500)
		println(sum.get)
		println(s.queue)
	}
	
	def <T> onEach(Stream<T> stream, (T)=>void handler) {
		stream.listener = [ it, next, skip, close |
			switch it {
				Value<T>: { 
					handler.apply(value)
					next.apply
				}
			}
		]
	}
	
	@Test
	def void testResolving() {
		val s = #['a', 'b', 'c', 'd', 'e'].stream
		s
			.map [
				println('pushing ' + it)
				it
			]
			.map [ doSomethingAsync ]
			.resolve(2)
			.collect
			.forEach [
				println('got: ' + it)
			]
			//.onEach [ println('got: ' + it)	]
		 s << 'f' << 'g' << finish << 'h' << finish
		//s << 'd' << 'e'
//		s << 'a' << 'b' << 'c'
		Thread.sleep(3000)
	}
	
	def Promise<String> doSomethingAsync(String x) {
		async [|
			for(i : 1..5) {
				// println(x + i)
				System.out.print('')
			}
			x
		]
	}
	
//	@Test
//	def void testCountDown() {
//		val countdown = new Countdown
//		val c1 = countdown.await
//		val c2 = countdown.await
//		val c3 = countdown.await
//		countdown.stream
//			.onFinish [ println('countdown done. success:' + countdown.success) ]
//			.onEach [ println('counting...') ]
//		c2.apply(true)
//		c1.apply(true)
//		c3.apply(true)
//	}
//	
//	@Test
//	def void testGatherer() {
//		val collector = new Gatherer<String>
//		val cuser = collector.await('user')
//		val cname = collector.await('name')
//		val cage = collector.await('age')
//		
//		collector.stream
//			.onFinish [
//				val it = collector.result
//				println('found user ' + get('user'))
//				println('found name ' + get('name'))
//				println('found age ' + get('age'))
//			]
//			.onEach [ println('got ' + it.key + ' has value ' + it.value)]
//
//		cage.apply('12')
//		cname.apply('John')
//		cuser.apply('Christian')
//	}
	
}