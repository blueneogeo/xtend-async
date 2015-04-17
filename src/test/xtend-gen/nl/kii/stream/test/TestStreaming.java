package nl.kii.stream.test;

import nl.kii.async.annotation.Async;
import nl.kii.promise.IPromise;
import nl.kii.promise.Promise;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.internal.SubPromise;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.SubStream;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStreaming {
  @Test
  public void doTest() {
    final Stream<String> s = StreamExtensions.<String>stream(String.class);
    final Function1<String, String> _function = new Function1<String, String>() {
      @Override
      public String apply(final String it) {
        return ("http://" + it);
      }
    };
    SubStream<String, String> _map = StreamExtensions.<String, String, String>map(s, _function);
    final Function1<String, Promise<String>> _function_1 = new Function1<String, Promise<String>>() {
      @Override
      public Promise<String> apply(final String it) {
        return TestStreaming.this.loadPage(it);
      }
    };
    SubStream<String, String> _call = StreamExtensions.<String, String, String, Promise<String>>call(_map, _function_1);
    final Procedure1<String> _function_2 = new Procedure1<String>() {
      @Override
      public void apply(final String it) {
        InputOutput.<String>println(("got " + it));
      }
    };
    StreamExtensions.<String, String>onEach(_call, _function_2);
    IStream<String, String> _doubleLessThan = StreamExtensions.<String, String>operator_doubleLessThan(s, "cnn.com");
    IStream<String, String> _doubleLessThan_1 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan, "cnn.com");
    IStream<String, String> _doubleLessThan_2 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_1, "cnn.com");
    IStream<String, String> _doubleLessThan_3 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_2, "cnn.com");
    IStream<String, String> _doubleLessThan_4 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_3, "cnn.com");
    IStream<String, String> _doubleLessThan_5 = StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_4, "cnn.com");
    StreamExtensions.<String, String>operator_doubleLessThan(_doubleLessThan_5, "cnn.com");
  }
  
  @Test
  public void doTest2() {
    final Promise<String> s = PromiseExtensions.<String>promise(String.class);
    final Function1<String, String> _function = new Function1<String, String>() {
      @Override
      public String apply(final String it) {
        return ("http://" + it);
      }
    };
    SubPromise<String, String> _map = PromiseExtensions.<String, String, String>map(s, _function);
    final Function1<String, Promise<String>> _function_1 = new Function1<String, Promise<String>>() {
      @Override
      public Promise<String> apply(final String it) {
        return TestStreaming.this.loadPage(it);
      }
    };
    SubPromise<String, String> _call = PromiseExtensions.<String, String, String, Promise<String>>call(_map, _function_1);
    final Function1<String, Promise<String>> _function_2 = new Function1<String, Promise<String>>() {
      @Override
      public Promise<String> apply(final String it) {
        return TestStreaming.this.loadPage(it);
      }
    };
    SubPromise<String, String> _call_1 = PromiseExtensions.<String, String, String, Promise<String>>call(_call, _function_2);
    final Function1<String, Promise<String>> _function_3 = new Function1<String, Promise<String>>() {
      @Override
      public Promise<String> apply(final String it) {
        return TestStreaming.this.loadPage(it);
      }
    };
    SubPromise<String, String> _call_2 = PromiseExtensions.<String, String, String, Promise<String>>call(_call_1, _function_3);
    final Function1<String, Promise<String>> _function_4 = new Function1<String, Promise<String>>() {
      @Override
      public Promise<String> apply(final String it) {
        return TestStreaming.this.loadPage(it);
      }
    };
    SubPromise<String, String> _call_3 = PromiseExtensions.<String, String, String, Promise<String>>call(_call_2, _function_4);
    final Function1<String, Promise<String>> _function_5 = new Function1<String, Promise<String>>() {
      @Override
      public Promise<String> apply(final String it) {
        return TestStreaming.this.loadPage(it);
      }
    };
    SubPromise<String, String> _call_4 = PromiseExtensions.<String, String, String, Promise<String>>call(_call_3, _function_5);
    final Function1<String, Promise<String>> _function_6 = new Function1<String, Promise<String>>() {
      @Override
      public Promise<String> apply(final String it) {
        return TestStreaming.this.loadPage(it);
      }
    };
    SubPromise<String, String> _call_5 = PromiseExtensions.<String, String, String, Promise<String>>call(_call_4, _function_6);
    final Function1<String, Promise<String>> _function_7 = new Function1<String, Promise<String>>() {
      @Override
      public Promise<String> apply(final String it) {
        return TestStreaming.this.loadPage(it);
      }
    };
    SubPromise<String, String> _call_6 = PromiseExtensions.<String, String, String, Promise<String>>call(_call_5, _function_7);
    final Procedure1<String> _function_8 = new Procedure1<String>() {
      @Override
      public void apply(final String it) {
        InputOutput.<String>println(("got " + it));
      }
    };
    _call_6.then(_function_8);
    PromiseExtensions.<String, String>operator_doubleLessThan(s, "cnn.com");
  }
  
  @Async
  public IPromise<String, String> loadPage(final String url, final Promise<String> promise) {
    return PromiseExtensions.<String, String>operator_doubleLessThan(promise, "test");
  }
  
  public Promise<String> loadPage(final String url) {
    final Promise<String> promise = new Promise<String>();
    try {
    	loadPage(url,promise);
    } catch(Throwable t) {
    	promise.error(t);
    } finally {
    	return promise;
    }
  }
}
