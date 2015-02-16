package nl.kii.stream;

import com.google.common.base.Objects;
import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import nl.kii.promise.PromiseExtensions;
import nl.kii.promise.Task;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.SubStream;
import nl.kii.stream.internal.StreamEventResponder;
import nl.kii.stream.internal.StreamResponder;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

@SuppressWarnings("all")
public class StreamIOExtensions {
  /**
   * stream a standard Java inputstream. closing the stream closes the inputstream.
   */
  public static Stream<List<Byte>> stream(final InputStream stream) {
    try {
      Stream<List<Byte>> _xblockexpression = null;
      {
        final Stream<List<Byte>> newStream = new Stream<List<Byte>>();
        ByteStreams.<Object>readBytes(stream, new ByteProcessor() {
          public Object getResult() {
            Object _xblockexpression = null;
            {
              newStream.finish();
              _xblockexpression = null;
            }
            return _xblockexpression;
          }
          
          public boolean processBytes(final byte[] buf, final int off, final int len) throws IOException {
            boolean _xblockexpression = false;
            {
              boolean _isOpen = newStream.isOpen();
              boolean _not = (!_isOpen);
              if (_not) {
                return false;
              }
              newStream.push(((List<Byte>)Conversions.doWrapArray(buf)));
              _xblockexpression = true;
            }
            return _xblockexpression;
          }
        });
        final Procedure1<StreamEventResponder> _function = new Procedure1<StreamEventResponder>() {
          public void apply(final StreamEventResponder it) {
            final Procedure1<Void> _function = new Procedure1<Void>() {
              public void apply(final Void it) {
                try {
                  stream.close();
                } catch (Throwable _e) {
                  throw Exceptions.sneakyThrow(_e);
                }
              }
            };
            it.skip(_function);
            final Procedure1<Void> _function_1 = new Procedure1<Void>() {
              public void apply(final Void it) {
                try {
                  stream.close();
                } catch (Throwable _e) {
                  throw Exceptions.sneakyThrow(_e);
                }
              }
            };
            it.close(_function_1);
          }
        };
        StreamExtensions.<List<Byte>, List<Byte>>when(newStream, _function);
        _xblockexpression = newStream;
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * stream a file as byte blocks. closing the stream closes the file.
   */
  public static Stream<List<Byte>> stream(final File file) {
    try {
      Stream<List<Byte>> _xblockexpression = null;
      {
        final ByteSource source = Files.asByteSource(file);
        BufferedInputStream _openBufferedStream = source.openBufferedStream();
        _xblockexpression = StreamIOExtensions.stream(_openBufferedStream);
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public static <I extends Object> SubStream<I, String> toText(final IStream<I, List<Byte>> stream) {
    return StreamIOExtensions.<I>toText(stream, "UTF-8");
  }
  
  public static <I extends Object> SubStream<I, String> toText(final IStream<I, List<Byte>> stream, final String encoding) {
    final Function1<List<Byte>, List<String>> _function = new Function1<List<Byte>, List<String>>() {
      public List<String> apply(final List<Byte> it) {
        try {
          String _string = new String(((byte[])Conversions.unwrapArray(it, byte.class)), encoding);
          String[] _split = _string.split("\n");
          return IterableExtensions.<String>toList(((Iterable<String>)Conversions.doWrapArray(_split)));
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    SubStream<I, List<String>> _map = StreamExtensions.<I, List<Byte>, List<String>>map(stream, _function);
    SubStream<I, String> _separate = StreamExtensions.<I, String>separate(_map);
    final Procedure1<SubStream<I, String>> _function_1 = new Procedure1<SubStream<I, String>>() {
      public void apply(final SubStream<I, String> it) {
        stream.setOperation((("toText(encoding=" + encoding) + ")"));
      }
    };
    return ObjectExtensions.<SubStream<I, String>>operator_doubleArrow(_separate, _function_1);
  }
  
  public static <I extends Object> SubStream<I, List<Byte>> toBytes(final IStream<I, String> stream) {
    return StreamIOExtensions.<I>toBytes(stream, "UTF-8");
  }
  
  public static <I extends Object> SubStream<I, List<Byte>> toBytes(final IStream<I, String> stream, final String encoding) {
    final Function1<String, List<Byte>> _function = new Function1<String, List<Byte>>() {
      public List<Byte> apply(final String it) {
        try {
          byte[] _bytes = (it + "\n").getBytes(encoding);
          return ((List<Byte>) Conversions.doWrapArray(_bytes));
        } catch (Throwable _e) {
          throw Exceptions.sneakyThrow(_e);
        }
      }
    };
    SubStream<I, List<Byte>> _map = StreamExtensions.<I, String, List<Byte>>map(stream, _function);
    final Procedure1<SubStream<I, List<Byte>>> _function_1 = new Procedure1<SubStream<I, List<Byte>>>() {
      public void apply(final SubStream<I, List<Byte>> it) {
        stream.setOperation((("toBytes(encoding=" + encoding) + ")"));
      }
    };
    return ObjectExtensions.<SubStream<I, List<Byte>>>operator_doubleArrow(_map, _function_1);
  }
  
  /**
   * write a buffered bytestream to an standard java outputstream
   */
  public static <I extends Object> Task writeTo(final IStream<I, List<Byte>> stream, final OutputStream out) {
    Task _xblockexpression = null;
    {
      final Task task = new Task();
      final Procedure1<StreamResponder<I, List<Byte>>> _function = new Procedure1<StreamResponder<I, List<Byte>>>() {
        public void apply(final StreamResponder<I, List<Byte>> it) {
          final Procedure1<Void> _function = new Procedure1<Void>() {
            public void apply(final Void it) {
              try {
                out.close();
                task.complete();
              } catch (Throwable _e) {
                throw Exceptions.sneakyThrow(_e);
              }
            }
          };
          it.closed(_function);
          final Procedure2<I, Integer> _function_1 = new Procedure2<I, Integer>() {
            public void apply(final I $0, final Integer $1) {
              try {
                boolean _equals = Objects.equal(it, Integer.valueOf(0));
                if (_equals) {
                  out.close();
                }
                task.complete();
                stream.next();
              } catch (Throwable _e) {
                throw Exceptions.sneakyThrow(_e);
              }
            }
          };
          it.finish(_function_1);
          final Procedure2<I, Throwable> _function_2 = new Procedure2<I, Throwable>() {
            public void apply(final I $0, final Throwable $1) {
              task.error($1);
              stream.close();
            }
          };
          it.error(_function_2);
          final Procedure2<I, List<Byte>> _function_3 = new Procedure2<I, List<Byte>>() {
            public void apply(final I $0, final List<Byte> $1) {
              try {
                out.write(((byte[])Conversions.unwrapArray($1, byte.class)));
                stream.next();
              } catch (Throwable _e) {
                throw Exceptions.sneakyThrow(_e);
              }
            }
          };
          it.each(_function_3);
        }
      };
      StreamExtensions.<I, List<Byte>>on(stream, _function);
      stream.setOperation("writeTo");
      stream.next();
      _xblockexpression = task;
    }
    return _xblockexpression;
  }
  
  /**
   * write a buffered bytestream to a file
   */
  public static <I extends Object> Task writeTo(final IStream<I, List<Byte>> stream, final File file) {
    try {
      Task _xblockexpression = null;
      {
        final Task task = new Task();
        final ByteSink sink = Files.asByteSink(file);
        final BufferedOutputStream out = sink.openBufferedStream();
        Task _writeTo = StreamIOExtensions.<I>writeTo(stream, out);
        PromiseExtensions.<Boolean, Boolean, Boolean>pipe(_writeTo, task);
        String _absolutePath = file.getAbsolutePath();
        String _plus = ("writeTo(file=" + _absolutePath);
        String _plus_1 = (_plus + ")");
        stream.setOperation(_plus_1);
        _xblockexpression = task;
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
