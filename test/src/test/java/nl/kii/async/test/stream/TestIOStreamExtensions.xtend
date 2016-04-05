package nl.kii.async.test.stream

import java.io.File
import org.junit.Test

import static extension nl.kii.stream.StreamExtensions.*
import static extension nl.kii.stream.StreamIOExtensions.*
import org.junit.Ignore

class TestIOStreamExtensions {
	
	
	// TEST FILE STREAMING ////////////////////////////////////////////////////
	
	@Ignore // FIX: terrible test
	@Test
	def void testFileStreaming() {
		val file = new File('../gradle.properties')
		file.stream
			.toText
			.map [ '- ' + it ]
			.effect [ println(it) ]
			
			.then [ println('finish') ]
	}
	
	@Ignore // FIX: terrible test
	@Test
	def void testStreamToFileAndFileCopy() {
		val data = #[
			'Hello,',
			'This is some text',
			'Please make this into a nice file!'
		]
		stream(data).toBytes.writeTo(new File('test.txt'))

		val source = new File('test.txt')
		val destination = new File('text2.txt')
		source.stream.writeTo(destination).then [
			source.delete
			destination.delete
		]
	}

}
