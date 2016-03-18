package nl.kii.act

import nl.kii.async.options.AsyncDefault

class ActorExtensions {

	/** Creates a threadsafe asynchronous actor using the default stream options */
	def static <T> AsyncActor<T> actor((T, =>void)=>void actFn) {
		new NonBlockingAsyncActor<T>(AsyncDefault.options.newActorQueue, AsyncDefault.options.actorMaxCallDepth) {
			override act(T input, ()=>void done) {
				actFn.apply(input, done)
			}
		}
	}
	
	/** Creates a threadsafe blocking actor using the default stream options */
	def static <T> AsyncActor<T> actor((T)=>void actFn) {
		new NonBlockingAsyncActor<T>(AsyncDefault.options.newActorQueue, AsyncDefault.options.actorMaxCallDepth) {
			override act(T input, ()=>void done) {
				actFn.apply(input)
				done.apply
			}
		}
	}

	/** Apply a value to the actor */
	def static <T> >> (T value, AsyncActor<T> actor) {
		actor.apply(value)
	}
		
	/** Apply a value to the actor */
	def static <T> << (AsyncActor<T> actor, T value) {
		actor.apply(value)
	}

}
