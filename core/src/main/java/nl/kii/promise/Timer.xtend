package nl.kii.promise

import nl.kii.stream.message.Error
import java.time.Period

/** Abstraction of a timer, which is a task that can be canceled.  */
abstract class Timer extends Task {
	
	def void cancel()

}

//interface Cancellable {
//    def void cancel()
//}