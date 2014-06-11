package nl.kii.act

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ActorExtensions {

	static var ExecutorService defaultActorExecutor = null

	def static synchronized getDefaultExecutorService() {
		if(defaultActorExecutor == null)
			defaultActorExecutor = Executors.newCachedThreadPool
		defaultActorExecutor
	}

//	def static <T> Actor<T> actor((T, =>void)=>void actFn) {
//		actor(defaultActorExecutor, actFn)
//	}
//
//	def static <T> Actor<T> actor((T)=>void actFn) {
//		actor(defaultActorExecutor, actFn)
//	}
	
	def static <T> Actor<T> actor((T, =>void)=>void actFn) {
		new Actor<T>() {
			override act(T input, ()=>void done) {
				actFn.apply(input, done)
			}
		}
	}
	
	def static <T> Actor<T> actor((T)=>void actFn) {
		new Actor<T>() {
			override act(T input, ()=>void done) {
				actFn.apply(input)
				done.apply
			}
		}
	}

	def static <T> >> (T value, Actor<T> actor) {
		actor.apply(value)
	}
		
	def static <T> << (Actor<T> actor, T value) {
		actor.apply(value)
	}

}
