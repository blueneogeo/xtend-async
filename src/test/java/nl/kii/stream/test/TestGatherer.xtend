package nl.kii.stream.test

//import nl.kii.stream.Countdown
//import nl.kii.stream.Gatherer

class TestCollector {
	
	
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