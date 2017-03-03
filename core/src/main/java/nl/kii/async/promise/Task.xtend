package nl.kii.async.promise

class Task extends Input<Void> {
	
	override void complete() {
		set(null)
	}
	
}
