package nl.kii.stream.test;

import java.util.concurrent.ConcurrentHashMap;
import nl.kii.stream.Countdown;
import nl.kii.stream.Gatherer;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

@SuppressWarnings("all")
public class TestCollector {
  @Test
  public void testCountDown() {
    final Countdown countdown = new Countdown();
    final Procedure1<? super Boolean> c1 = countdown.await();
    final Procedure1<? super Boolean> c2 = countdown.await();
    final Procedure1<? super Boolean> c3 = countdown.await();
    Stream<Pair<String,Boolean>> _stream = countdown.stream();
    final Procedure1<Void> _function = new Procedure1<Void>() {
      public void apply(final Void it) {
        Boolean _isSuccess = countdown.isSuccess();
        String _plus = ("countdown done. success:" + _isSuccess);
        InputOutput.<String>println(_plus);
      }
    };
    Stream<Pair<String,Boolean>> _onFinish = StreamExtensions.<Pair<String,Boolean>>onFinish(_stream, _function);
    final Procedure1<Pair<String,Boolean>> _function_1 = new Procedure1<Pair<String,Boolean>>() {
      public void apply(final Pair<String,Boolean> it) {
        InputOutput.<String>println("counting...");
      }
    };
    StreamExtensions.<Pair<String,Boolean>>onEach(_onFinish, _function_1);
    c2.apply(Boolean.valueOf(true));
    c1.apply(Boolean.valueOf(true));
    c3.apply(Boolean.valueOf(true));
  }
  
  @Test
  public void testGatherer() {
    final Gatherer<String> collector = new Gatherer<String>();
    final Procedure1<? super String> cuser = collector.await("user");
    final Procedure1<? super String> cname = collector.await("name");
    final Procedure1<? super String> cage = collector.await("age");
    Stream<Pair<String,String>> _stream = collector.stream();
    final Procedure1<Void> _function = new Procedure1<Void>() {
      public void apply(final Void it) {
        final ConcurrentHashMap<String,String> it_1 = collector.result();
        String _get = it_1.get("user");
        String _plus = ("found user " + _get);
        InputOutput.<String>println(_plus);
        String _get_1 = it_1.get("name");
        String _plus_1 = ("found name " + _get_1);
        InputOutput.<String>println(_plus_1);
        String _get_2 = it_1.get("age");
        String _plus_2 = ("found age " + _get_2);
        InputOutput.<String>println(_plus_2);
      }
    };
    Stream<Pair<String,String>> _onFinish = StreamExtensions.<Pair<String,String>>onFinish(_stream, _function);
    final Procedure1<Pair<String,String>> _function_1 = new Procedure1<Pair<String,String>>() {
      public void apply(final Pair<String,String> it) {
        String _key = it.getKey();
        String _plus = ("got " + _key);
        String _plus_1 = (_plus + " has value ");
        String _value = it.getValue();
        String _plus_2 = (_plus_1 + _value);
        InputOutput.<String>println(_plus_2);
      }
    };
    StreamExtensions.<Pair<String,String>>onEach(_onFinish, _function_1);
    cage.apply("12");
    cname.apply("John");
    cuser.apply("Christian");
  }
}
