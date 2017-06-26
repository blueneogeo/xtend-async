package nl.kii.async.annotations.test

import nl.kii.async.event.Event
import org.eclipse.xtend.lib.annotations.Data
import org.junit.Test

class TestEventInterfaces {
	
	@Test
	def void testEvent() {
	
		val PublisherInterface publisher = new Publisher
		
		publisher.onNewArticle [ println('first listener got article ' + getTitle) ]
		publisher.onNewArticle [ println('second listener got article: ' + getTitle) ]
		val stopHello = publisher.onPublishingComplete [ println('hello!') ]
		publisher.newArticleStream
		publisher.checkForNews
		
		stopHello.apply
		
		publisher.checkForNews
	}
	
}

/**
 * You can also define events in your interface. This allows you to create interfaces that not only
 * define methods (pull) but also events (push) as a contract.
 * <p> 
 * Adding an event annotation forces subclasses to implement your event listening methods (onEventName and streamEventName).
 */
interface PublisherInterface {
	
	@Event Article newArticle
	@Event Void publishingComplete
	
	def void checkForNews()
	
}

/**
 * This class implements the interface. The easiest way is to simply define the event here the same way.
 */
class Publisher implements PublisherInterface {
	
	@Event Article newArticle
	@Event Void publishingComplete
	
	override checkForNews() {
		newArticle(new Article('Hello world!'))
		newArticle(new Article('This is world news.'))
		publishingComplete // may call without parameters, is of type void
	}
	
}

@Data
class Article {
	
	String title
	
}
