package nl.kii.async.promise

class Task extends Input<Boolean> {
	
	override void complete() {
		set(true)
	}
	
}
