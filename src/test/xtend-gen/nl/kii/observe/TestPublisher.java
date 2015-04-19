package nl.kii.observe;

import java.util.Collections;
import java.util.List;
import nl.kii.observe.Publisher;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.internal.SubStream;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class TestPublisher {
  @Test
  public void testPublishAndObserve() {
    final Stream<String> collector = StreamExtensions.<String>stream(String.class);
    final Publisher<String> publisher = new Publisher<String>();
    final Procedure1<String> _function = new Procedure1<String>() {
      @Override
      public void apply(final String it) {
        StreamExtensions.<String, String>operator_doubleGreaterThan(("1:" + it), collector);
      }
    };
    publisher.onChange(_function);
    final Procedure1<String> _function_1 = new Procedure1<String>() {
      @Override
      public void apply(final String it) {
        StreamExtensions.<String, String>operator_doubleGreaterThan(("2:" + it), collector);
      }
    };
    final Procedure0 stop2 = publisher.onChange(_function_1);
    publisher.apply("A");
    collector.finish();
    stop2.apply();
    publisher.apply("B");
    collector.finish();
    collector.finish(1);
    SubStream<String, List<String>> _collect = StreamExtensions.<String, String>collect(collector);
    SubStream<String, List<List<String>>> _collect_1 = StreamExtensions.<String, List<String>>collect(_collect);
    final Procedure1<List<List<String>>> _function_2 = new Procedure1<List<List<String>>>() {
      @Override
      public void apply(final List<List<String>> it) {
        Assert.assertEquals(
          Collections.<List<String>>unmodifiableList(CollectionLiterals.<List<String>>newArrayList(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("1:A", "2:A")), Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("1:B")))), it);
      }
    };
    StreamExtensions.<String, List<List<String>>>then(_collect_1, _function_2);
  }
}
