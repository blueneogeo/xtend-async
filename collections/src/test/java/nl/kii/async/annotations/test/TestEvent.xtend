package nl.kii.async.annotations.test

import nl.kii.async.event.Event
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

class Publisher {
	
	@Event Article newArticle
	
	def checkForNews() {
		newArticle(new Article('Hello world!'))
		newArticle(new Article('This is world news.'))
	}
	
}

@Data
class Article {
	
	String name
	
}
