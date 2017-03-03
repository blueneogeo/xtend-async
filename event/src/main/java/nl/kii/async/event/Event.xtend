package nl.kii.async.event

import org.eclipse.xtend.lib.macro.Active
import java.lang.annotation.Target
import org.eclipse.xtend.lib.macro.AbstractFieldProcessor
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import nl.kii.async.publish.BasicPublisher
import nl.kii.async.publish.Publisher
import org.eclipse.xtend.lib.macro.declaration.Visibility
import nl.kii.async.stream.Stream
import nl.kii.async.stream.StreamExtensions
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1
import nl.kii.async.annotation.Hot
import nl.kii.async.annotation.Uncontrolled

/**
 * Add an Event listener to a class. This allows you to listen to events from this class
 * with multiple listeners, and publish messages to all listeners.
 * <p>
 * The annotation performs the following steps:
 * <ol>
 * <li>it adds a protected method with the name of the field, that lets you pass an item of the type of the field,
 * this method posts an event to all listeners
 * <li>it adds a public method on + [name of the field], that lets other classes register a listener for these events
 * <li>it adds a public method [name of the field] + stream, that lets you listen for events as a hot uncontrolled stream
 * <li>it creates a protected publisher that keeps track of all listening streams
 * <li>it removes the actual field, since it is only meant as instructive to the annotation
 * </ol>
 * <p>
 * 
 * Example:
 * <p>
 * <pre>
 * class RSSFeed {
 * 
 *    \@Event Article newArticle
 * 
 *    def someMethod() {
 *       ...
 *       // we want to publish some new article to all listeners
 *       article = loadedFeed.articles.head
 *       newArticle(article)
 *       ...
 *    }
 * 
 * }
 * 
 * class RSSFeedResponder {
 * 
 *    def someOtherMethod() {
 *       ...
 *       // listen for articles coming in with a closure
 *       rssFeed.onNewArticle [
 *           
 *       ]
 * 
 * 		 // you can also get the direct stream, by not passing a closure:
 * 		rssFeed.newArticleStream
 *        .effect [ println('got article' + it) ]
 *        .start
 *       ...
 *    }
 * 
 * }
 * </pre>
 */
@Active(EventProcessor)
@Target(FIELD)
annotation Event {
}

class EventProcessor extends AbstractFieldProcessor {
	
	/** Process a field annotated with @Event */
	override doTransform(MutableFieldDeclaration field, extension TransformationContext context) {
		
		val cls = field.declaringType
		
		val publisherFieldName = '__' + field.simpleName + 'EventPublisher'
		
		// add a publisher of the type of the field
		cls.addField(publisherFieldName) [
			primarySourceElement = field
			type = Publisher.newTypeReference(field.type)
			visibility = Visibility.PROTECTED
			transient = true
		]
		
		// add a method for publishing the event, with the name of the field, and as a parameter the type of the field
		cls.addMethod(field.simpleName) [
			primarySourceElement = field
			val fieldParameterName = field.type.simpleName.toFirstLower
			addParameter(fieldParameterName, field.type)
			body = '''
				if(«publisherFieldName» == null) return;
				«publisherFieldName».publish(«fieldParameterName»);
			'''
		]
		
		// add a method for listening to the method as a stream. It lazily initialises the event publisher.
		val streamMethodName = field.simpleName + 'Stream'
		
		cls.addMethod(streamMethodName) [
			primarySourceElement = field
			addAnnotation(Hot.newAnnotationReference)
			addAnnotation(Uncontrolled.newAnnotationReference)
			returnType = Stream.newTypeReference(field.type, field.type)
			body = '''
				if(«publisherFieldName» == null) {
					«publisherFieldName» = new «BasicPublisher.newTypeReference(field.type)»();
					«publisherFieldName».start();
				} 
				return «publisherFieldName».subscribe();
			'''
		]

		// add a method for listening to the method with a handler. Wraps the stream method.
		cls.addMethod('on' + field.simpleName.toFirstUpper) [
			primarySourceElement = field
			val handlerParameterName = field.simpleName + 'Handler' 
			addParameter(handlerParameterName, Procedure1.newTypeReference(field.type))
			body = '''
				«StreamExtensions».start(«StreamExtensions».effect(«streamMethodName»(), «handlerParameterName»));
			'''
		]

		// remove the annotated field, since it is only instructional for creating the event publisher and methods
		field.remove
		
	}
	
}
