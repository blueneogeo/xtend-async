package nl.kii.async.annotation

import java.lang.annotation.Target

/** 
 * Methods annotated with Blocking can or will block the thread.
 * Usually this is something you will want to avoid.
 */
@Target(METHOD)
annotation Blocking {
	
}
