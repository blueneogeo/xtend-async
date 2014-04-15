# xtend-streams

WARNING: WIP, not production ready!

Xtend-streams give streams and promises to Xtend. It is inspired by the Java Streams and RXJava, but is specifically built to work well with the Xtend language and Vert.x. It has no dependencies apart from Xtend.

So why was this library built, even though Java8 already has stream support?

- made to be very simple and easy to use
- completely non-blocking (required for Vert.x)
- high performance single threaded model (best for Vert.x)
- tailor-built for Xtend and leveraging the features of the language
- streams and promises are integrated and work with each other (Java 8 has no promises)
- support for batches, separated by finish commands, which allows for more complex and interesting behaviors
- source code is made to be simple and Stream and Promise classes are as simple as possible. All features are added with Xtend extensions. Java 8 streams are not made to be extended. This lets you add your own operators easily, as well as easily debug code
- streams and promises can be hard to debug because they encapsulate errors. Xtend-streams lets you choose: throw errors as they occur, or catch them at the end
- flow control for listeners
