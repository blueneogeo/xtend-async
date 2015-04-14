package nl.kii.promise.internal;

import java.util.concurrent.atomic.AtomicReference;

import nl.kii.promise.IPromise;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Error;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

public abstract class FixedBasePromise<R, T> extends BasePromise<R, T> {

	/**
	 * If the promise recieved or recieves an error, onError is called with the
	 * throwable. Removes the error from the chain, so the returned promise no
	 * longer receives the error.
	 * 
	 * FIX: this method should return a subpromise with the error filtered out,
	 * but it returns this, since there is a generics problem trying to assign
	 * the values.
	 */
	@Override
	public IPromise<R, T> on(final Class<? extends Throwable> errorType, final Procedure2<R, Throwable> errorFn) {
		final SubPromise<R, T> subPromise = new SubPromise<R, T>(this, false);

		this.then(new Procedure2<R, T>() {
			@Override
			public void apply(R from, T value) {
				subPromise.set(from, value);
			}
		});
		
		final AtomicReference<Procedure0> unregisterFn = new AtomicReference<Procedure0>();
		
		final Procedure1<Entry<R, T>> onChange = new Procedure1<Entry<R, T>>() {
			@Override
			public void apply(final Entry<R, T> it) {
				if (it instanceof nl.kii.stream.message.Error) {
					Error<R, T> error = (Error<R, T>)it;
					try {
						unregisterFn.get().apply();
						Class<? extends Throwable> _class = error.error.getClass();
						if (errorType.isAssignableFrom(_class)) {
							errorFn.apply(error.from, error.error);
						} else {
							subPromise.error(error.from, error.error);
						}
					} catch(final Throwable t) {
						subPromise.error(error.from, t);
					}
				}
			}
		};
		unregisterFn.set(this.getPublisher().onChange(onChange));

		this.setHasErrorHandler(true);
		
		if(getEntry() != null) {
			this.getPublisher().apply(getEntry());
		}
		
		return subPromise;
	}

}
