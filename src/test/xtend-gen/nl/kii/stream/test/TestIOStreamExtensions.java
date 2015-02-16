package nl.kii.stream.test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import nl.kii.promise.SubTask;
import nl.kii.promise.Task;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.StreamIOExtensions;
import nl.kii.stream.SubStream;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

@SuppressWarnings("all")
public class TestIOStreamExtensions {
  @Test
  public void testFileStreaming() {
    final File file = new File("gradle.properties");
    Stream<List<Byte>> _stream = StreamIOExtensions.stream(file);
    SubStream<List<Byte>, String> _text = StreamIOExtensions.<List<Byte>>toText(_stream);
    final Function1<String, String> _function = new Function1<String, String>() {
      public String apply(final String it) {
        return ("- " + it);
      }
    };
    SubStream<List<Byte>, String> _map = StreamExtensions.<List<Byte>, String, String>map(_text, _function);
    final Procedure1<String> _function_1 = new Procedure1<String>() {
      public void apply(final String it) {
        InputOutput.<String>println(it);
      }
    };
    SubTask<List<Byte>> _onEach = StreamExtensions.<List<Byte>, String>onEach(_map, _function_1);
    final Procedure1<Boolean> _function_2 = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        InputOutput.<String>println("finish");
      }
    };
    _onEach.then(_function_2);
  }
  
  @Test
  public void testStreamToFileAndFileCopy() {
    final List<String> data = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("Hello,", "This is some text", "Please make this into a nice file!"));
    Stream<String> _stream = StreamExtensions.<String>stream(data);
    SubStream<String, List<Byte>> _bytes = StreamIOExtensions.<String>toBytes(_stream);
    File _file = new File("test.txt");
    StreamIOExtensions.<String>writeTo(_bytes, _file);
    final File source = new File("test.txt");
    final File destination = new File("text2.txt");
    Stream<List<Byte>> _stream_1 = StreamIOExtensions.stream(source);
    Task _writeTo = StreamIOExtensions.<List<Byte>>writeTo(_stream_1, destination);
    final Procedure1<Boolean> _function = new Procedure1<Boolean>() {
      public void apply(final Boolean it) {
        source.delete();
        destination.delete();
      }
    };
    _writeTo.then(_function);
  }
}
