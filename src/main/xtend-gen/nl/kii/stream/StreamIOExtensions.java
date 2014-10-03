package nl.kii.stream;

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
import nl.kii.async.annotation.Async;
import nl.kii.promise.Task;
import nl.kii.stream.Stream;
import nl.kii.stream.StreamExtensions;
import nl.kii.stream.StreamHandlerBuilder;
import nl.kii.stream.StreamResponder;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

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
        final Procedure1<StreamResponder> _function = new Procedure1<StreamResponder>() {
          public void apply(final StreamResponder it) {
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
        StreamExtensions.<List<Byte>>monitor(newStream, _function);
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
  
  public static Stream<String> toText(final Stream<List<Byte>> stream) {
    return StreamIOExtensions.toText(stream, "UTF-8");
  }
  
  public static Stream<String> toText(final Stream<List<Byte>> stream, final String encoding) {
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
    Stream<List<String>> _map = StreamExtensions.<List<Byte>, List<String>>map(stream, _function);
    Stream<String> _separate = StreamExtensions.<String>separate(_map);
    final Procedure1<Stream<String>> _function_1 = new Procedure1<Stream<String>>() {
      public void apply(final Stream<String> it) {
        stream.setOperation((("toText(encoding=" + encoding) + ")"));
      }
    };
    return ObjectExtensions.<Stream<String>>operator_doubleArrow(_separate, _function_1);
  }
  
  public static Stream<List<Byte>> toBytes(final Stream<String> stream) {
    return StreamIOExtensions.toBytes(stream, "UTF-8");
  }
  
  public static Stream<List<Byte>> toBytes(final Stream<String> stream, final String encoding) {
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
    Stream<List<Byte>> _map = StreamExtensions.<String, List<Byte>>map(stream, _function);
    final Procedure1<Stream<List<Byte>>> _function_1 = new Procedure1<Stream<List<Byte>>>() {
      public void apply(final Stream<List<Byte>> it) {
        stream.setOperation((("toBytes(encoding=" + encoding) + ")"));
      }
    };
    return ObjectExtensions.<Stream<List<Byte>>>operator_doubleArrow(_map, _function_1);
  }
  
  /**
   * write a buffered bytestream to an standard java outputstream
   */
  @Async
  public static void writeTo(final Stream<List<Byte>> stream, final OutputStream out, final Task task) {
    final Procedure1<StreamHandlerBuilder<List<Byte>, List<Byte>>> _function = new Procedure1<StreamHandlerBuilder<List<Byte>, List<Byte>>>() {
      public void apply(final StreamHandlerBuilder<List<Byte>, List<Byte>> it) {
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
        final Procedure1<Integer> _function_1 = new Procedure1<Integer>() {
          public void apply(final Integer it) {
            try {
              if (((it).intValue() == 0)) {
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
        final Function1<Throwable, Boolean> _function_2 = new Function1<Throwable, Boolean>() {
          public Boolean apply(final Throwable it) {
            boolean _xblockexpression = false;
            {
              task.error(it);
              stream.close();
              _xblockexpression = true;
            }
            return Boolean.valueOf(_xblockexpression);
          }
        };
        it.error(_function_2);
        final Procedure1<List<Byte>> _function_3 = new Procedure1<List<Byte>>() {
          public void apply(final List<Byte> it) {
            try {
              out.write(((byte[])Conversions.unwrapArray(it, byte.class)));
              stream.next();
            } catch (Throwable _e) {
              throw Exceptions.sneakyThrow(_e);
            }
          }
        };
        it.each(_function_3);
      }
    };
    StreamExtensions.<List<Byte>, List<Byte>>on(stream, _function);
    stream.setOperation("writeTo");
    stream.next();
  }
  
  /**
   * write a buffered bytestream to a file
   */
  @Async
  public static void writeTo(final Stream<List<Byte>> stream, final File file, final Task task) {
    try {
      final ByteSink sink = Files.asByteSink(file);
      final BufferedOutputStream out = sink.openBufferedStream();
      StreamIOExtensions.writeTo(stream, out, task);
      String _absolutePath = file.getAbsolutePath();
      String _plus = ("writeTo(file=" + _absolutePath);
      String _plus_1 = (_plus + ")");
      stream.setOperation(_plus_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * write a buffered bytestream to an standard java outputstream
   */
  public static Task writeTo(final Stream<List<Byte>> stream, final OutputStream out) {
    final Task task = new Task();
    try {
    	writeTo(stream,out,task);
    } catch(Throwable t) {
    	task.error(t);
    } finally {
    	return task;
    }
  }
  
  /**
   * write a buffered bytestream to a file
   */
  public static Task writeTo(final Stream<List<Byte>> stream, final File file) {
    final Task task = new Task();
    try {
    	writeTo(stream,file,task);
    } catch(Throwable t) {
    	task.error(t);
    } finally {
    	return task;
    }
  }
}
