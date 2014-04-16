# XTEND-STREAMS

Xtend-streams give streams and promises to Xtend. It is inspired by the Java Streams and RXJava, but is specifically built to work well with the Xtend language and Vert.x. It has no runtime dependencies apart from Xtend.

So why was this library built, even though Java8 already has stream support?

- made to be very simple and easy to use
- completely non-blocking (required for Vert.x)
- tailor-built for Xtend and leveraging the features of the language
- streams and promises are integrated and work with each other and use nearly the exact same syntax
- support for RX-like batches, which is useful for aggregation
- source code is made to be simple and Stream and Promise classes are as simple as possible. All features are added with Xtend extensions. Java 8 streams are not made to be extended. This lets you add your own operators easily, as well as easily debug code
- streams and promises can be hard to debug because they encapsulate errors. Xtend-streams lets you choose: throw errors as they occur, or catch them at the end
- flow control for listeners, meaning that you can indicate when a listener is ready to process a next item from a stream.

# PROMISE USAGE

Promises are a bit like Futures, they represent a promise of a value in the future. This allows you to think of that value as if you already have it, and describe what should happen to it when it arrives. Promises come into their own with asynchronous programming.

## Importing the Extensions

Importing the promise extensions:

	import static extension nl.kii.stream.PromiseExt.*

## Creating a Promise

Creating a promise, passing it a value, and then responding to it:

	val p = new Promise<Integer>
	p.set(10)
	p.then [ println('got value ' + it) ]

The same, but using the extensions for nicer syntax:

	val p = int.promise
	p << 10
	p.then [ println('got value ' + it ]

## Mapping

You can transform a promise into another promise using a mapping:

	val p = 4.promise
	val p2 = p.map [ it+1 ]
	p2.then[println(it)] // prints 5

## Handling Errors

If the handler has an error, you can catch it using .onError:

	val p = 0.promise
	p.onError [ println('got exception ' + it) ]
	p.then [ println(1/it) ] // throws /0 exception
  
A nice feature of handling errors this way is that they are wrapped for you, so you can have a single place to handle them.

	val p = 0.promise
	val p2 = p.map[1/it] // throws the exception
	val p3 = p2.map[it + 1]
	p3.onError[ println('got error: ' + message) ]
  p3.then [ println('this will not get printed') ]

In the above code, the mapping throws the error, but that error is passed down the chain up to where you listen for it.

# STREAM USAGE

Streams are like queues that you can listen to. You can push values in, and listen for these incoming values. Like with Promises, you can use operations on streams to transform them. The usage of a stream is almost identical to a promise.

## Importing the Extensions

Importing the stream extensions:

	import static extension nl.kii.stream.StreamExt.*

## Creating a Stream

Creating a stream, passing it some values, and then responding to them:

	val s = new Stream<Integer>
	s.push(1)
	s.push(2)
	s.push(3)
	p.forEach [ println('got value ' + it) ]

The same, but using the extensions for nicer syntax:

	val s = int.stream
	s << 1 << 2 << 3
	s.forEach [ println('got value ' + it ]

The syntax for handling incoming items is the same as iterating through Lists. The difference is that with streams, the list never has to end. At any time you can push a new item in, and the handler will be called again.

## Mapping

You can transform a stream into another stream using a mapping, just like you would with Lists:

	val s = #[1, 2, 3].stream
	val s2 = s.map [ it+1 ]
	s2.forEach [ print(it) ] // prints 123

## Filtering

Sometimes you only want items to pass that match some criterium.  You can use filter for this, just like you would with Lists:

	val s = #[1, 2, 3].stream
	val s2 = s.filter [ it < 3 ]
	s2.forEach [ print(it) ] // prints 12

## Handling Errors

If the handler has an error, you can catch it using .onError:

	val s = #[1, 0, 2].stream
	s.onError [ println('got exception ' + it) ]
	s.forEach [ println(1/it) ] // throws /0 exception
  
A nice feature of handling errors this way is that they are wrapped for you, so you can have a single place to handle them.

	val p = 0.promise
	val p2 = p.map[1/it] // throws the exception
	val p3 = p2.map[it + 1]
	p3.onError[ println('got error: ' + message) ]
  p3.then [ println('this will not get printed') ]

In the above code, the mapping throws the error, but that error is passed down the chain up to where you listen for it.

# COMBINING STREAMS AND PROMISES

## Promise Functions

The strength of streams comes out best using asynchronous programming. In asynchronous programming, when you call a function, it is executed directly, and this function is executed on another thread or moment. The result from the function is returned later.

A promise is a great way to represent this, using promise functions:

	def Promise<String> loadWebpage(String url) {
		val result = String.promise
		... code that loads webpage, and calls result.set(webpage)
		result
	}

To simply print a webpage, you can then do this:

	loadWebpage('http://cnn.com')
		.then [ println(it) ]

The nice thing about promise functions is that they allow you to  reuse asynchronous code.

## Using Promise Functions in Streams

If you want to load a whole bunch of URL's, you can create a stream of URL's, and then process these with the same promise function:

	val urls = String.stream
	urls
		.async [ loadWebpage ]
		.then [ println(it) ]
	urls << 'http://cnn.com' << 'http://yahoo.com'

The async method you see above lets you map an incoming value to a promise, and then unwraps the promise for you so you can process it like a normal mapping.

# BATCHES AND AGGREGATION

## Entry

Streams and promises actually do not process and pass just values, they process Entry<T>'s. An entry can be:

- a Value<T>, listen to by using .forEach [ ]
- a Finish<T>, listen to by using .onFinish [ ]
- or an Error<T>, listen to by using .onError [ ]

Values and errors you've seen, finish is new. You use a finish to indicate the end of the values that have been passed so far (much like RXJava's complete). This is necessary if you want to pass batches of data through a stream. Consider finish the separator between these batches.

## Counting

Quite often, you will want to take a stream of values, and do some aggregation on them, such as counting them.

The stream extensions have many such reductions built-in, and you can easily add your own as well. For example, to count the amount of values coming down the stream:

	val s = char.stream
	s.count.then [ println('counted ' + it + ' chars') ]
	s << 'a' << 'x' << 'c' << finish

Note that counts takes a lambda instead of directly returning a value. This is because counting may not be finished when .count is called, new values may still arrive.

So when does count know that it is finished? Here the finish command comes in. The count function uses the finish to know it has come to the end of the batch.

## Counting Multiple Batches 

Note that count returns a new Stream<Integer>, and .then on a stream produces a Promise of the first value from that stream. The reason for this is that actually, count counts each batch. You could also do this:

	val s = char.stream
	s.count.forEach [ println('counted ' + it + ' chars') ]
	s << 'a' << 'x' << 'c' << finish << 'v' << 't' << finish

This will produce:

	counted 3 chars
	counted 2 chars

## Other Aggregations

Similar functions are: 

- avg : gives the average of all numbers in a batch
- sum: sum of all numbers in a batch
- collect: create a list of a batch
- anyMatch: true when any entry matches a criteria
- allMatch: true only when all entries match a criterium
- reduce: custom aggregation

# FLOW CONTROL

Xtend streams let you control how fast listeners get new data. This is useful when you have heavy asynchronous processes. If you had no way to queue the data coming in, these would be overwhelmed.

Consider the following situation:

	def Promise<Boolean> emailUser(int userId) { ... }
	(1..10000).stream.forEach [ emailUser ]

The second line would call emailUser, which is an asynchronous functions which returns immediately. So, if a thread pool is being used by the async function, 10000 thread processes are started in parallel!

## Using Stream.next

In order to tell a stream that you want to control it, you can use a different forEach version:

	(1..10000).stream.forEach [ it, stream |
		emailUser.then [ stream.next ]
	]

Here, only a single user will be emailed at the same time. This is because the two-parameter version of forEach does not automatically start streaming everything. Instead, it passes you the stream as well, and only passes you the first entry from the stream. It then stops, until you call stream.next.

## Using Stream.async

A nice feature of the Stream.async function discussed earlier is, that it does this for you. So instead of the code above, you could also write:

	(1..10000).stream.async [ emailUser	].then [ ... ]

You can also indicate that you want asynchronous concurrency by passing how many concurrent processes you want to allow:

	(1..10000).stream.async(3) [ emailUser	].then [ ... ]

## Using Stream.skip

Sometimes a stream can be very large, but you might only need a few items from a batch. You can call stream.skip to tell the stream that it can skip processing the rest of the batch. (it will start again when the next batch arrives).

For example:

	val s = int.stream << 1 << 2 << 3 << finish << 1 << 5 << finish
	s.forEach [ it, stream |
		if(it > 2) stream.skip else print(it)
		stream.next
	]

The above code will print 121. It will first stream 1 and 2, then skip to the finish at 3, then print 1 and skip again at 5.

think about next when using ranges. next should travel upwards as well, so the range can do +1...
