package nl.kii.stream.test;

import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.StreamStats;
import nl.kii.stream.SubStream;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerRange;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

@SuppressWarnings("all")
public class TestStreamMonitor {
  @Test
  public void testSimpleMonitoring() {
    final StreamStats stats = new StreamStats();
    IntegerRange _upTo = new IntegerRange(1, 10);
    Stream<Integer> _stream = StreamExtensions.<Integer>stream(_upTo);
    final Function1<Integer, Integer> _function = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf(((it).intValue() % 3));
      }
    };
    SubStream<Integer, Integer> _map = StreamExtensions.<Integer, Integer, Integer>map(_stream, _function);
    final Function1<Integer, Integer> _function_1 = new Function1<Integer, Integer>() {
      @Override
      public Integer apply(final Integer it) {
        return Integer.valueOf((1 / (it).intValue()));
      }
    };
    SubStream<Integer, Integer> _map_1 = StreamExtensions.<Integer, Integer, Integer>map(_map, _function_1);
    IStream<Integer, Integer> _monitor = StreamExtensions.<Integer, Integer>monitor(_map_1, stats);
    final Procedure1<Throwable> _function_2 = new Procedure1<Throwable>() {
      @Override
      public void apply(final Throwable it) {
        InputOutput.<Throwable>println(it);
      }
    };
    SubStream<Integer, Integer> _onError = StreamExtensions.<Integer, Integer>onError(_monitor, _function_2);
    final Procedure1<Integer> _function_3 = new Procedure1<Integer>() {
      @Override
      public void apply(final Integer it) {
        InputOutput.<Integer>println(it);
      }
    };
    StreamExtensions.<Integer, Integer>onEach(_onError, _function_3);
    InputOutput.<StreamStats>println(stats);
  }
}
