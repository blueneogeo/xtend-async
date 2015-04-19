package nl.kii.promise.internal;

import java.util.concurrent.atomic.AtomicReference;

import nl.kii.promise.IPromise;
import nl.kii.stream.message.Entry;
import nl.kii.stream.message.Error;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

public abstract class FixedBasePromise<I, O> extends BasePromise<I, O> {

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
	public IPromise<I, O> on(final Class<? extends Throwable> errorType, final boolean swallow, final Procedure2<I, Throwable> errorFn) {
		final SubPromise<I, O> subPromise = new SubPromise<I, O>(this, false);

		this.then(new Procedure2<I, O>() {
			@Override
			public void apply(I from, O value) {
				subPromise.set(from, value);
			}
		});
		
		final AtomicReference<Procedure0> unregisterFn = new AtomicReference<Procedure0>();
		
		final Procedure1<Entry<I, O>> onChange = new Procedure1<Entry<I, O>>() {
			@Override
			public void apply(final Entry<I, O> it) {
				if (it instanceof Error) {
					Error<I, O> error = (Error<I, O>)it;
					try {
						unregisterFn.get().apply();
						
						Class<? extends Throwable> errorCls = error.error.getClass();
						Class<? extends Throwable> causeCls = (error.error.getCause() != null) ? error.error.getCause().getClass() : null;

						if(errorCls != null && errorType.isAssignableFrom(errorCls)) {
							errorFn.apply(error.from, error.error);
							if(!swallow) {
								subPromise.error(error.from, error.error);
							}
						} else if(causeCls != null && errorType.isAssignableFrom(causeCls)) {
							errorFn.apply(error.from, error.error);
							if(!swallow) {
								subPromise.error(error.from, error.error);
							}
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
