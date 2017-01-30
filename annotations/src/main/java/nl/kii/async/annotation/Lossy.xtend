package nl.kii.async.annotation

import java.lang.annotation.Target

/** Streams annotated with lossy are not guaranteed to return everything you put in.  */
@Target(METHOD)
annotation Lossy {
	
}
