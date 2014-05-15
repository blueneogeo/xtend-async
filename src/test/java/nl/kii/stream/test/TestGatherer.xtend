//package nl.kii.stream.test
//
//import nl.kii.stream.Countdown
//import nl.kii.stream.Gatherer
//import nl.kii.stream.Promise
//import org.junit.Test
//
//import static nl.kii.stream.PromiseExtensions.*
//
//import static extension nl.kii.stream.StreamExtensions.*
//
//class TestCollector {
//	
//	@Test
//	def void testResolving() {
//		val s = #['a', 'b', 'c'].stream
//		s
//			.map [
//				println('pushing ' + it)
//				it
//			]
//			.map [ doSomethingAsync ]
//			.resolve(2)
//			.onFinish [	println('FINISHED') ]
//			.onEach [ 
//				println('RESULT: ' + it)
//			]
////			.collect
////			.onEach [ println('got: ' + it)	]
//		s << 'd' << 'e' << 'f' << 'g' << finish << 'h' << finish
//		Thread.sleep(3000)
//	}
//	
//	def Promise<String> doSomethingAsync(String x) {
//		resolve [|
//			for(i : 1..5) {
//				// println(x + i)
//				System.out.println('                       ')
//			}
//			x
//		]
//	}
//	
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
//	
//}