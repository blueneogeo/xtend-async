package nl.kii.async.test.stream

import nl.kii.promise.Promise
import nl.kii.promise.Task
import nl.kii.stream.Stream
import nl.kii.stream.SubStream
import nl.kii.stream.message.Closed
import nl.kii.stream.message.Error
import nl.kii.stream.message.Value
import org.junit.Test

import static extension nl.kii.async.test.AsyncJUnitExtensions.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.util.JUnitExtensions.*
import org.junit.Ignore

class TestStream {

	@Test
	def void testStream() {
		// we want to fill in these variables
		val values = newLinkedList
		val error = new Promise
		val closed = new Task
		
		// create a stream and listen for the events
		val stream = new Stream<Integer>
		stream.push(1)
		stream.onChange [ entry |
			switch entry {
				Value<Integer, Integer>: { values.add(entry.value) }
				Error<Integer, Integer>: { error.set(entry.error) }
				Closed<Integer, Integer>: closed.complete
			}
		]
		
		stream.next
		values <=> #[1]
		
		stream.next // next before push should also work
		stream.push(2)
		values <=> #[1, 2]
		
		// push an error and check if it was received
		stream.error(new Exception('help'))
		stream.next
		error.map[message] <=> 'help'
		
		// close the stream and check if it was received and closed
		stream.next
		stream.close
		closed <=> true
		stream.open <=> false
	}

	@Ignore	
	@Test
	def void testSubstream() {
		val stream = new Stream<Integer>
		val sub = new SubStream(stream)
		stream.push(1)
		stream.push(2)
		stream.on [
			each [ sub.push($0, $1) ]
			error [ sub.error($0, $1) ]
			closed [ sub.close ]
		]
		sub.when [
			next [
				println('next!') 
				stream.next
			]
			close [ stream.close ]
		]
		sub.on [
			each [ println($1) sub.next ]
			closed [ println('done') ]
		]
		sub.next
	}

}
