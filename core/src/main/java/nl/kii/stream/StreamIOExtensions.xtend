package nl.kii.stream

import com.google.common.io.ByteProcessor
import com.google.common.io.Files
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.List
import nl.kii.promise.Task

import static extension com.google.common.io.ByteStreams.*
import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.stream.StreamExtensions.*

class StreamIOExtensions {
	
	/** stream a standard Java inputstream. closing the stream closes the inputstream. */
	def static Stream<List<Byte>> stream(InputStream stream) {
		val newStream = new Stream<List<Byte>>
		stream.readBytes(new ByteProcessor {
			
			override getResult() { newStream.close null }
			
			override processBytes(byte[] buf, int off, int len) throws IOException {
				if(!newStream.isOpen) return false
				newStream.push(buf)
				true
			}
			
		})
		newStream.when [
			close [ stream.close ]
		]
		newStream
	}

	/** stream a file as byte blocks. closing the stream closes the file. */	
	def static Stream<List<Byte>> stream(File file) {
		val source = Files.asByteSource(file)
		source.openBufferedStream.stream
	}
	
	// WRITING TO OUTPUT STREAMS AND FILES ///////////////////////////////////

	def static <I> IStream<I, String> toText(IStream<I, List<Byte>> stream) {
		stream.toText('UTF-8')
	}
	
	def static <I> IStream<I, String> toText(IStream<I, List<Byte>> stream, String encoding) {
		stream
			.map [ new String(it, encoding).split('\n').toList ]
			.separate
			=> [ stream.options.operation = 'toText(encoding=' +  encoding + ')' ]
	}
	
	def static <I> IStream<I, List<Byte>> toBytes(IStream<I, String> stream) {
		stream.toBytes('UTF-8')
	}

	def static <I> IStream<I, List<Byte>> toBytes(IStream<I, String> stream, String encoding) {
		stream
			.map [ (it + '\n').getBytes(encoding) as List<Byte> ]
			=> [ stream.options.operation = 'toBytes(encoding=' +  encoding + ')' ]
	}

	/** write a buffered bytestream to an standard java outputstream */
	def static <I> Task writeTo(IStream<I, List<Byte>> stream, OutputStream out) {
		val task = new Task
		stream.on [
			each [
				out.write($1)
				stream.next
			]
			error [ 
				task.error($1)
				stream.close
			]
			closed [ 
				out.close
				task.complete
			]
		]
		stream.options.operation = 'writeTo'
		stream.next
		task
	}

	/** write a buffered bytestream to a file */
	def static <I> Task writeTo(IStream<I, List<Byte>> stream, File file) {
		val task = new Task
		val sink = Files.asByteSink(file)
		val out = sink.openBufferedStream
		stream.writeTo(out).pipe(task)
		stream.options.operation = 'writeTo(file=' + file.absolutePath + ')'
		task
	}
	
	
}
