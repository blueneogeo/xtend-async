package nl.kii.stream
import static extension com.google.common.io.ByteStreams.*
import com.google.common.io.ByteProcessor
import com.google.common.io.Files
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.List
import nl.kii.async.annotation.Async
import nl.kii.promise.Task

import static extension nl.kii.stream.StreamExtensions.*

class IOStreamExtensions {
	
	/** stream a standard Java inputstream. closing the stream closes the inputstream. */
	def static Stream<List<Byte>> stream(InputStream stream) {
		val newStream = new Stream<List<Byte>>
		stream.readBytes(new ByteProcessor {
			
			override getResult() { newStream.finish null }
			
			override processBytes(byte[] buf, int off, int len) throws IOException {
				if(!newStream.isOpen) return false
				newStream.push(buf)
				true
			}
			
		})
		newStream.monitor [
			skip [ stream.close]
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

	def static Stream<String> toText(Stream<List<Byte>> stream) {
		stream.toText('UTF-8')
	}
	
	def static Stream<String> toText(Stream<List<Byte>> stream, String encoding) {
		stream
			.map [ new String(it, encoding).split('\n').toList ]
			.separate
			=> [ stream.operation = 'toText(encoding=' +  encoding + ')' ]
	}
	
	def static Stream<List<Byte>> toBytes(Stream<String> stream) {
		stream.toBytes('UTF-8')
	}

	def static Stream<List<Byte>> toBytes(Stream<String> stream, String encoding) {
		stream
			.map [ (it + '\n').getBytes(encoding) as List<Byte> ]
			=> [ stream.operation = 'toBytes(encoding=' +  encoding + ')' ]
	}

	/** write a buffered bytestream to an standard java outputstream */
	@Async def static void writeTo(Stream<List<Byte>> stream, OutputStream out, Task task) {
		stream.on [
			closed [ out.close task.complete ]
			finish [ 
				if(it == 0) out.close task.complete 
				stream.next
			]
			error [ 
				task.error(it)
				stream.close
				true
			]
			each [
				out.write(it)
				stream.next
			]
		]
		stream.operation = 'writeTo'
		stream.next
	}

	/** write a buffered bytestream to a file */
	@Async def static void writeTo(Stream<List<Byte>> stream, File file, Task task) {
		val sink = Files.asByteSink(file)
		val out = sink.openBufferedStream
		stream.writeTo(out, task)
		stream.operation = 'writeTo(file=' + file.absolutePath + ')'
	}
	
	
}