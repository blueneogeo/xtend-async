package nl.kii.promise

/** Abstraction of a timer, which is a task that can be canceled.  */
abstract class Timer extends Task {
	
	def void cancel()

}

//interface Cancellable {
//    def void cancel()
//}