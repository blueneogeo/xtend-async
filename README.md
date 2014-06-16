# XTEND-STREAMS

Xtend-streams give streams and promises to Xtend. It is inspired by the Java 8 Streams and RXJava, but is specifically built to work well with the Xtend language and Vert.x. It has no runtime dependencies apart from Xtend.

So why was this library built, even though Java8 already has stream support?

- completely non blocking
- optimized for asynchronous programming
- integration between promises and streams

Some features are:

- made to be easy to use, simple syntax
- lightweight, with no dependencies besides Xtend
- streams and promises are integrated and work with each other and use nearly the exact same syntax
- support for RX-like batches, which is useful for aggregation.
- clear source code, the base Stream and Promise classes are as simple as possible. All features are added with Xtend extensions. This lets you add your own operators easily, as well as easily debug code.
- flow control for listeners, meaning that you can indicate when a listener is ready to process a next item from a stream
- internally uses thread-borrowing actor that allows asynchronous coding without requiring a new thread or thread pool 
- streams and promises can be hard to debug because they encapsulate errors. xtend-streams lets you choose: throw errors as they occur, or catch them at the end

# QUICK EXAMPLES

## Normal Stream Processing

	// non-blocking collections
	#[1, 2, 3].stream
		.map [ it * 2 ]
		.collect
		.then [ assertEquals(#[2, 4, 6], it) ]

	// streaming the numbers on demand, not all in memory
	(1..1_000_000).stream
		.filter [ it % 2 == 0 ] // only even numbers
		.count
		.then [ assertEquals(500_000, it) ]

	// streaming after creation of the stream
	val s = int.stream
	s
		.onFinish [ println('done!') ]
		.onEach [ println(it) ]
	s << 1 << 2 << finish // prints 1 2 done!

	// cutting off a stream
	(1..1_000_000_000).stream
		.until [ it > 1000 ]
		.count
		.then [ assertEquals(1000, it) ]

	// flow control
	val s = #['John', 'Mary'].stream
	s.onEach [ name, stream |
		// ask for the next only after the user is saved
		saveUserAsync(name) [ stream.next ]
	]

## Async Processing

	def loadWebpageInBackground(URL url) {
		val loaded = new Promise<Webpage>
		someHttpService.loadAsync(url) [ page | loaded.apply(page) 	]
		return loaded
	}

	val stream = new Stream<URL>
	stream
		.map [ loadWebpageInBackground ]
		.resolve(2) // max two async processes in parallel
		.onEach [ webpage | println(webpage.content) ]

	stream << new URL('http://www.cnn.com') << new URL('http://www.theverge.com') << .. etc. << finish

The loadWebpageInBackground method returns a promise of a webpage for a given url. The stream code then uses that method to set up a pipeline that allows you to push in URLs, which get mapped to a stream of webpage promises, which then get resolved into a stream of webpages, which in turn get printed.

## Non-blocking Aggregation

Instead of having aggregation such as stream.count and stream.collect block the thread, these streams use finish markers to indicate the end of a stream of data.

	val stream = int.stream
	stream.collect.then [ println('got list: ' + it) ]

	stream << 1 << 2 << 3 << finish

This will print: 

	got list [1, 2, 3]

## Stream Segmentation

You can easily split a stream into multiple blocks for aggregation using the .split method:

	#[1, 2, 3, 4, 5].stream
		.split [ it % 2 == 0 ]
		.collect
		.onEach [ println(it) ]

This will print:

	[1, 2]
	[3, 4]
	[5]

# PROMISE USAGE

Promises are a bit like Futures, they represent a promise of a value in the future. This allows you to think of that value as if you already have it, and describe what should happen to it when it arrives. Promises come into their own with asynchronous programming.

## Importing the Extensions

Importing the promise extensions:

	import static extension nl.kii.promise.PromiseExtensions.*

## Creating a Promise

Creating a promise, telling what to do when it is fulfilled, and then fulfilling the promise:

	val p = new Promise<Integer>
	p.then [ println('got value ' + it) ]
	p.set(10)

The same, but using the extensions for nicer syntax:

	val p = int.promise
	p.then [ println('got value ' + it ]
	p << 10

## Mapping

You can transform a promise into another promise using a mapping:

	val p = 4.promise
	val p2 = p.map [ it+1 ]
	p2.then [ println(it) ] // prints 5

## Handling Errors

If the handler has an error, you can catch it using .onError:

	val p = 0.promise
	p.onError [ println('got exception ' + it) ]
	p.then [ println(1/it) ] // throws /0 exception
  
A nice feature of handling errors this way is that they are wrapped for you, so you can have a single place to handle them.

	val p = 0.promise
	val p2 = p.map[1/it] // throws the exception
	val p3 = p2.map[it + 1]
	p3.onError [ println('got error: ' + message) ]
  p3.then [ println('this will not get printed') ]

In the above code, the mapping throws the error, but that error is passed down the chain up to where you listen for it.

# STREAM USAGE

Streams are like queues that you can listen to. You can push values in, and listen for these incoming values. Like with Promises, you can use operations on streams to transform them. The usage of a stream is almost identical to a promise.

## Importing the Extensions

Importing the stream extensions:

	import static extension nl.kii.stream.StreamExtensions.*

## Creating a Stream

Creating a stream, telling how to respond to it, and passing some values to it:

	val s = new Stream<Integer>
	p.forEach [ println('got value ' + it) ]
	s.push(1)
	s.push(2)
	s.push(3)

This will print:

	got value 1
	got value 2
	got value 3

The same, but using the extensions for nicer syntax:

	val s = int.stream
	s.each [ println('got value ' + it ]
	s << 1 << 2 << 3

The syntax for handling incoming items is the same as iterating through Lists. The difference is that with streams, the list never has to end. At any time you can push a new item in, and the handler will be called again.

You can also create a stream from a list:

	val s = #[1, 2, 3].stream

Or from any Iterable in fact:

	(1..1_000_000).stream
		.onFinish [ println('done!') ]
		.onEach [ println(it) ]

Note that we are actually not dumping a million numbers into the stream and then processing it. What actually happens is that each number goes down the stream once, and then onEach asks for the next number, etc.

## Mapping

You can transform a stream into another stream using a mapping, just like you would with Lists:

	#[1, 2, 3].stream
		.map [ it+1 ]
		.onEach [ println(it) ] // prints 2, 3 and 4

## Filtering

Sometimes you only want items to pass that match some criterium.  You can use filter for this, just like you would with Lists:

	#[1, 2, 3].stream
		.filter [ it < 3 ]
		.onEach [ print(it) ] // prints 1 and 2

## Handling Errors

If the handler has an error, you can catch it using .onError:

	#[1, 0, 2].stream
		.onError [ println('got exception ' + it) ]
		.onEach [ println(1/it) ] // throws /0 exception
  
A nice feature of handling errors this way is that they are wrapped for you, so you can have a single place to handle them. This works for both streams and promises.

	0.promise
		.map[1/it] // throws the exception, 1/0
		.map[it + 1] // some code to demonstrate the exception is propagated
		.onError [ println('got error: ' + message) ]
		.then [ println('this will not get printed') ]

In the above code, the mapping throws the error, but that error is passed down the chain up to where you listen for it.

## Alternative Syntax

You can also listen to a stream like this:

	(1..10).stream.on [
		each [ println(it) ]
		error [ println('got error: ' + it) ]
		finish [ println('we are done!') ]
	]

The result of .on[] is a subscription, which allows you to close the stream.

## Observing a stream with multiple listeners

A stream can only be listened to by a single listener. This keeps flow control predictable and the streams light. However you can wrap a stream into an Observable<T> by calling stream.observe. You can then listen with multiple listeners:

	val s = int.stream
	val observable = s.observe
	observable.onChange [ println('first listener got value ' + it) ]
	observable.onChange [ println('second listener got value ' + it) ]
	s << 1 << 2 << 3 // will trigger both listeners above for each value

The Observable.onChange method returns a closure that you can call to stop listening:

	val stop = observable.onChange [ ... ]
	...
	stop.apply // stops the listener from responding

# COMBINING STREAMS AND PROMISES

## @Async Functions

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

The nice thing about promise functions is that they allow you to  reuse asynchronous code. Since this is a common pattern, there is an @Async Active Annotation that helps you write this code. For example:

	@Async def loadWebpage(String url, Promise<String> result) {
		... code that loads webpage, and calls result.set(webpage)
	}

This syntax makes it clear on the first line that this is an asynchronous function, and it makes sure the promise is returned at the end.

You can then call this async function like before:

	loadWebpage('http://cnn.com')
		.then [ println(it) ]

The @Async annotation creates a loadWebpage(String url) function, creates the promise, calls your function, then returns the promise. It also catches any exceptions and reports them to the promise, and makes sure the promise is always returned.

## Using Promise Functions in Streams

If you want to load a whole bunch of URL's, you can create a stream of URL's, and then process these with the same promise function:

	val urls = String.stream
	urls
		.map [ loadWebpage ] // results in a Stream<Promise<String>
		.resolve // results in a Stream<String>
		.then [ println(it) ] // so then we can print it
	urls << 'http://cnn.com' << 'http://yahoo.com'

The mapping first takes the url and applies it to loadWebpage, which is a function that returns a Promise<String>. We then have a Stream<Promise<String>>. You can then use the resolve function to resolve the promises so you get a Stream<String>, a stream of actual values.

# BATCHES AND AGGREGATION

## Entry

Streams and promises actually do not process and pass just values, they process Entry<T>'s. An entry can be:

- a Value<T>, listen to by using .onEach [ ]
- a Finish<T>, listen to by using .onFinish [ ]
- or an Error<T>, listen to by using .onError [ ]

Values and errors you've seen, onFinish is new. You use a finish to indicate the end of the values that have been passed so far (much like RXJava's complete). This is necessary if you want to pass batches of data through a stream. Consider finish the separator between these batches.

## Counting

Quite often, you will want to take a stream of values, and do some aggregation on them, such as counting them.

The stream extensions have some of these reductions built-in, and you can easily add your own as well. For example, to count the amount of values coming down the stream:

	val s = char.stream
	s.count.then [ println('counted ' + it + ' chars') ]
	s << 'a' << 'x' << 'c' << finish

Note that counts takes a lambda instead of directly returning a value. This is because counting may not be finished when .count is called, new values may still arrive.

So when does count know that it is finished? Here the finish command comes in. The count function uses the finish to know it has come to the end of the batch.

## Other Aggregations

Other supported aggregation functions are: 

- avg : gives the average of all numbers in a batch
- sum: sum of all numbers in a batch
- collect: create a list of a batch
- anyMatch: true when any entry matches a criteria
- allMatch: true only when all entries match a criterium
- reduce: custom aggregation

# EXTENDING XTEND-STREAM

The following part describes how Streams use flow control internally. This is useful to know if you want to write your own extensions. I recommend you use Promises in most cases, since that gives you automatic flow control.

In order not to clutter the namespace, extensions that use the stream flow control functions must be packaged in package nl.kii.stream.

I recommend you have a look at the PromiseExtensions and StreamExtensions source code as an example.

## FLOW CONTROL

Xtend streams let you control how fast listeners get new data. This is useful when you have heavy asynchronous processes. If you had no way to queue the data coming in, these would be overwhelmed.

Consider the following situation:

	def Promise<Boolean> emailUser(int userId) { ... }
	(1..10000).stream.onEach [ emailUser ]

The second line would call emailUser, which is an asynchronous functions which returns immediately. So, if a thread pool is being used by the async function, 10000 thread processes are started in parallel!

### Using Stream.next

In order to tell a stream that you want to control it, you can use a different handler version:

	(1..10000).stream.onEach [ it, subscription |
		emailUser.then [ subscription.next ]
	]

Here, only a single user will be emailed at the same time. This is because the two-parameter version of forEach does not automatically start streaming everything. Instead, it passes you the stream as well, and only passes you the first entry from the stream. It then stops, until you call stream.next.

### Using Stream.resolve

A nice feature of the Stream.resolve function discussed earlier is, that it calls next for you. So instead of the code above, you could also write:

	(1..10000).stream.map[emailUser].resolve.then [ ... ]

You can also indicate that you want asynchronous concurrency by passing how many concurrent processes you want to allow:

	(1..10000).stream.map[emailUser].resolve(3).then [ ... ]

### Using Stream.skip   

Sometimes a stream can be very large, but you might only need a few items from a batch. You can call stream.skip to tell the stream that it can skip processing the rest of the batch. (it will start again when the next batch arrives).

For example:

	val s = int.stream << 1 << 2 << 3 << finish << 1 << 5 << finish
	s.onEach [ it, subscription |
		if(it > 2) subscription.skip else print(it)
		stream.next
	]

The above code will print 121. It will first stream 1 and 2, then skip to the finish at 3, then print 1 and skip again at 5.

StreamExtensions.until [] uses this mechanism and is usually easier than implementing your own:

	// only print the first 10
	(1..10000).stream.until[it > 10].onEach[println(it)]

### Alternative Syntax

Just like the .on [] syntax earlier, you can do a similar thing for flow control:

	(1..1000).stream.onAsync [ s |
		s.each [ println(it); s.next ]
		s.error [ println('got error ' + it); s.next ]
		s.finish [ println('we are done!'); s.next ]
	]

As you see here we must call s.next on s (which is an AsyncSubscription) in order to get a next value from the stream.
