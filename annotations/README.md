# XTEND-ASYNC-ANNOTATIONS

Annotations for streams and promises from the xtend-async project.
See https://github.com/blueneogeo/xtend-async for more information.

## Async Functions

The @Async annotation lets you mark methods as asynchronous:

	@Async def loadWebpage(String url, Promise<String> result) {
		... code that loads webpage, and calls result.set(webpage)
	}
	
You can then call this async function like before:

	loadWebpage('http://cnn.com')
		.then [ println(it) ]

The @Async annotation creates a loadWebpage(String url) function, creates the promise, calls your function, then returns the promise. It also catches any exceptions and reports them to the promise, and makes sure the promise is always returned.

@Async also works with a Task, for example:

	@Async def updateUser(User user, Task update) {
		someService.updateUser(user) [|	task.complete ]
	}
	
	updateUser(john).then [ println('done!') ]
