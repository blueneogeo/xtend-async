# XTEND-ASYNC

Xtend-async provides asynchronous streams, promises and functions to Xtend. It can be used for any Java-based project, but is specifically built to work well with the Xtend language. It has no runtime dependencies apart from the small Xtend Java library and Google Guava.

Main features:

- easy to use, simple syntax
- asynchronous, non-blocking and thread-safe 
- integrates the concepts of streaming and promising

Some features are:

- lightweight, with no dependencies besides Xtend and Xtend-tools
- fast, and threadsafe when you tell it to
- streams and promises are integrated and work with each other and use nearly the exact same syntax
- clear source code, the base Stream and Promise classes are as simple as possible. All features are added with Xtend extensions. This lets you add your own operators easily, as well as easily debug code.
- streams are controlled and support back pressure, meaning that you can indicate when a listener is ready to process a next item from a stream
- streams and promises in xtend-stream encapsulate errors thrown in your handlers and propagate them so you can listen for then
- streams and promises keep a reference to the input, letting you for example respond to a request without leaving the stream or promise chain.

## What is a stream

A stream of data is like a list, where the items come in not all at once, but one by one.

## Creating a stream

You can create a stream either using one of the creation shortcuts from the StreamExtensions, or by using a Sink.

To use the StreamExtensions, add the following import:

	import static extension nl.kii.async.stream.StreamExtensions.*

To create a stream from any collection (list, queue, etc), stream the iterator:

	val s = #[1, 2, 3].iterator().stream()

You can also stream any range:

	val s = (1..1000).stream()

You can also create any stream using a Sink as follows:

	val sink = new Sink<Integer> {
		override onNext() { }
		override onClose() { }
	}

or:

	val sink = newSink()

A Sink is a Stream. Sink has a method *push(value)* that allows you to push something into the stream:

	sink.push(12)

You can also push in an error:

	sink.error(new Exception('something went wrong'))

By implementing onNext() and onClose() you can decide what should happen when sink.next and sink.close are called. 
This allows the stream to be *controlled*, meaning that the listener of the stream can control when it gets a new value
from the stream.

For example, to implement an iterator stream:

	val iterator = #[1, 2, 3].iterator()
	val sink = new Sink<Integer> {
		override onNext() {
			if(iterator.hasNext) push(iterator.next()) 
			else complete()
		}
		override onClose() { }
	}

Sink.complete() tells the stream that there will be no more data coming, the set is completed.

## Listening to a Stream using an Observer

Every stream is *Observable*. This means it exposes the method *Stream.observe(observer)*. 

An *Observer* is an interface that lets you respond to a value from the stream, an error from the stream, 
and when the stream completes.

For example, to print all values coming from a stream:

	val stream = (1..3).stream()
	stream.observer = new Observer<Integer> {
		override value(int in, int value) {
			println('got value ' + value)
			stream.next
		}
		override error(int in, Throwable err) {
			println('error: ' + err.message)
			stream.next
		}
		override complete() {
			println('done!')
		}
	}
	stream.next

This will print:

	got value 1
	got value 2
	got value 3
	done!

The .stream() method is an extension method from StreamExtensions that creates a 
controlled sink from a range, much like discussed above. By setting the observer
to this sink (which implements *Stream*) we can then listen to values from the stream.

Notice that we need to perform stream.next to get a next value from the stream, otherwise nothing happens! 
We need to do this to get the first value or error, and again when we recieve a value.

## Listening to a Stream using StreamExtensions

*StreamExtensions* contains a lot of methods that make working with streams easier.
We can do what we did with the observable above using the extensions like this:

	(1..3).stream
		.effect [ println(‘got value’ + it) ]
		.on(Throwable) [ println(‘error: ‘ + message) ]
		.start

The *Stream.effect [ ]* method performs a side effect for each incoming value on the stream.
This is much like Iterable.onEach [ ], but for streams.

The *Stream.on(Throwable) [ ]* method performs a side effect when an error of the passed type
occurs. In this case, we print the error message.

Finally, the *Stream.start()* method does two things. First of all it will perform the stream.next
to start the stream initially. Then for each incoming value, it will also call stream.next. In other
words, the start method starts off the stream and makes sure it keeps asking for the next value after
a value arrives.
