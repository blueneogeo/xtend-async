package nl.kii.stream.annotation

import java.lang.annotation.Target

/** Methods annotated with Hot will return a hot stream 'hot', meaning that the stream will start streaming data immediately. */
@Target(METHOD)
annotation Hot {
	
}
