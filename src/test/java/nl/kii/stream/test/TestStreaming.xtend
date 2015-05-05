package nl.kii.stream.test

import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import org.junit.Test
import nl.kii.promise.Promise
import nl.kii.async.annotation.Async

class TestStreaming {
	
	@Test
	def void doTest() {
		
		val s = String.stream
		s
			.map [ 'http://' + it ]
			.call [ loadPage ]
			.effect [ println('got ' + it) ]
			.start

		s << 'cnn.com' << 'cnn.com' << 'cnn.com' << 'cnn.com' << 'cnn.com' << 'cnn.com' << 'cnn.com'
	}
	

	@Test
	def void doTest2() {
		
		val s = String.promise
		s
			.map [ 'http://' + it ]
			.call [ loadPage ]
			.call [ loadPage ]
			.call [ loadPage ]
			.call [ loadPage ]
			.call [ loadPage ]
			.call [ loadPage ]
			.call [ loadPage ]
			.then [ println('got ' + it) ]

		s << 'cnn.com'
		
	}
	
	@Async def loadPage(String url, Promise<String> promise) {
//		loader.load(url) [ body |
//			promise.set(body)
//		]
		promise << 'test'
	}
	
	
}