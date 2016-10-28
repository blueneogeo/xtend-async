package nl.kii.async.promise

import co.paralleluniverse.fibers.Suspendable

@Suspendable
class Task extends Input<Void> {
	
	override void complete() {
		set(null)
	}
	
}
