package nl.kii.async.annotations.test

import nl.kii.async.event.Event

class Publisher {
	
	@Event Article newArticle
	
	def checkForNews() {
		newArticle(new Article('Hello world!'))
		newArticle(new Article('This is world news.'))
	}
	
}
