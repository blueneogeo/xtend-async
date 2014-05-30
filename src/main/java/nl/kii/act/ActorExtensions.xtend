package nl.kii.act

class ActorExtensions {
	
	def static <T> Actor<T> actor((T, =>void)=>void actFn) {
		new Actor<T> {
			override act(T input, ()=>void done) {
				actFn.apply(input, done)
			}
		}
	}
	
	def static <T> Actor<T> actor((T)=>void actFn) {
		new Actor<T> {
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
