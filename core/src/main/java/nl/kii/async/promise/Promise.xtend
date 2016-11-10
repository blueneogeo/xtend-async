package nl.kii.async.promise

import nl.kii.async.observable.Observable

/**
 * A promise is an observable that outputs only a single value.
 * After outputting this value, the deferred has completed its work.
 * <p>
 * A promise has three possible states:
 * <li>pending: no result is yet available
 * <li>fulfilled: the deferred has been resolved with a value
 * <li>rejected: an error occurred
 * <p>
 * A promise is often part of a chain of deferred operations.
 * These operations can transform the data. To allow you to 
 * have access to the input of these transformations, it keeps
 * track of and retains the IN. Normally only the OUT changes.
 */
interface Promise<IN, OUT> extends Observable<IN, OUT> {

	def boolean isPending()
	def boolean isFulfilled()
	def boolean isRejected()
	
}
