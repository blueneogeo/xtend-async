package nl.kii.async

import java.util.Date
import nl.kii.async.annotation.Atomic
import nl.kii.promise.IPromise
import nl.kii.promise.Promise
import nl.kii.util.Period

import static extension nl.kii.promise.PromiseExtensions.*
import static extension nl.kii.util.DateExtensions.*

/**
 * A cached value, that is retained for at least the passed retaintime.
 * This class lets you cache an asynchronous call.
 */
class AsyncCached<T> {

	val Period retainTime
	val =>Promise<T> fetchFn
	
	@Atomic T data
	@Atomic Date lastFetched

	new(Period retainTime, =>Promise<T> fetchFn) {
		this.retainTime = retainTime
		this.fetchFn = fetchFn
	}
	
	def IPromise<?, T> get() {
		if(data == null || lastFetched == null || now - lastFetched > retainTime) {
			fetchFn.apply.effect [ value |
					data = value
					lastFetched = now
				]
		} else promise(data)
	}
	
	def void clear() {
		lastFetched = null
		data = null
	}
	
}
