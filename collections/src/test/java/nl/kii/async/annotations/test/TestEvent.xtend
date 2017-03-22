package nl.kii.async.annotations.test

import org.eclipse.xtend.lib.annotations.Data
import org.junit.Test

import static extension nl.kii.async.stream.StreamExtensions.*

class TestEvent {
	
	@Test
	def void testEvent() {
	
		val publisher = new Publisher
		
		publisher.onNewArticle [ println('first listener: ' + name) ]
		publisher.onNewArticle [ println('second listener: ' + name) ]
		publisher.newArticleStream.effect [ println('stream listener: ' + name) ].start
	
		publisher.checkForNews
	}
	
}

@Data
class Article {
	
	String name
	
}
