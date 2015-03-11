package nl.kii.stream.internal;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

/**
 * An exception that occurred in a stream handler, somewhere in the chain of stream operations.
 * <p>
 * Since stream processing is a bunch of messages going up and down the chain, stream errors
 * can be notoriously hard to debug, since the stacktrace becomes a huge mess in which
 * somewhere there is the actual cause. Because of this, all Stream operations store their
 * name on the operation field of the stream, and when caught, the current value being processed
 * and the operation of the stream are passed into the StreamException. When displayed, this
 * exception will try to show you the operation that was being performed, the value that was
 * being processed, and the root cause and location of that root cause, like this:
 * <p>
 * <pre>
 * nl.kii.stream.StreamException: Stream.observe gave error "ack!" for value "3"
 * at nl.kii.stream.test.TestStreamObserving$1.onValue(TestStreamObserving.java:23)
 * </pre>
 */
@SuppressWarnings("all")
public class StreamException extends Exception {
  private final static int valueWrapSize = 10;
  
  private final static int traceSize = 1;
  
  private final static int maxValueStringLength = 500;
  
  public final String operation;
  
  public final Object value;
  
  public StreamException(final String operation, final Object value, final Throwable cause) {
    super(cause);
    this.operation = operation;
    this.value = value;
  }
  
  public static Object getMessage(final StreamException e) {
    Object _xifexpression = null;
    boolean _and = false;
    Throwable _cause = e.getCause();
    boolean _notEquals = (!Objects.equal(_cause, null));
    if (!_notEquals) {
      _and = false;
    } else {
      Throwable _cause_1 = e.getCause();
      _and = (_cause_1 instanceof StreamException);
    }
    if (_and) {
      _xifexpression = null;
    }
    return _xifexpression;
  }
  
  @Override
  public String getMessage() {
    String _xblockexpression = null;
    {
      Throwable _cause = this.getCause();
      final Throwable root = Throwables.getRootCause(_cause);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Stream.");
      _builder.append(this.operation, "");
      {
        boolean _and = false;
        boolean _notEquals = (!Objects.equal(root, null));
        if (!_notEquals) {
          _and = false;
        } else {
          String _message = root.getMessage();
          boolean _notEquals_1 = (!Objects.equal(_message, null));
          _and = _notEquals_1;
        }
        if (_and) {
          _builder.append(" gave error \"");
          String _message_1 = root.getMessage();
          _builder.append(_message_1, "");
        }
      }
      _builder.append("\"");
      {
        boolean _and_1 = false;
        boolean _notEquals_2 = (!Objects.equal(this.value, null));
        if (!_notEquals_2) {
          _and_1 = false;
        } else {
          String _string = this.value.toString();
          int _length = _string.length();
          boolean _lessThan = (_length < StreamException.valueWrapSize);
          _and_1 = _lessThan;
        }
        if (_and_1) {
          _builder.append(" for value: \"");
          _builder.append(this.value, "");
          _builder.append("\"");
        }
      }
      _builder.newLineIfNotEmpty();
      {
        boolean _and_2 = false;
        boolean _notEquals_3 = (!Objects.equal(this.value, null));
        if (!_notEquals_3) {
          _and_2 = false;
        } else {
          String _string_1 = this.value.toString();
          int _length_1 = _string_1.length();
          boolean _greaterEqualsThan = (_length_1 >= StreamException.valueWrapSize);
          _and_2 = _greaterEqualsThan;
        }
        if (_and_2) {
          _builder.append("For value: { ");
          String _string_2 = this.value.toString();
          String _limit = StreamException.limit(_string_2, StreamException.maxValueStringLength);
          _builder.append(_limit, "");
          _builder.append(" }");
        }
      }
      _builder.newLineIfNotEmpty();
      {
        Throwable _cause_1 = this.getCause();
        boolean _notEquals_4 = (!Objects.equal(_cause_1, null));
        if (_notEquals_4) {
          {
            StackTraceElement[] _stackTrace = root.getStackTrace();
            Iterable<StackTraceElement> _take = IterableExtensions.<StackTraceElement>take(((Iterable<StackTraceElement>)Conversions.doWrapArray(_stackTrace)), StreamException.traceSize);
            for(final StackTraceElement e : _take) {
              _builder.append("at ");
              String _className = e.getClassName();
              _builder.append(_className, "");
              _builder.append(".");
              String _methodName = e.getMethodName();
              _builder.append(_methodName, "");
              _builder.append("(");
              String _fileName = e.getFileName();
              _builder.append(_fileName, "");
              _builder.append(":");
              int _lineNumber = e.getLineNumber();
              _builder.append(_lineNumber, "");
              _builder.append(")");
              _builder.newLineIfNotEmpty();
            }
          }
        }
      }
      _xblockexpression = _builder.toString();
    }
    return _xblockexpression;
  }
  
  public static String limit(final String string, final int maxLength) {
    String _xblockexpression = null;
    {
      boolean _or = false;
      boolean _equals = Objects.equal(string, null);
      if (_equals) {
        _or = true;
      } else {
        int _length = string.length();
        boolean _lessEqualsThan = (_length <= maxLength);
        _or = _lessEqualsThan;
      }
      if (_or) {
        return string;
      }
      String _substring = string.substring(0, maxLength);
      String _plus = (_substring + "... (");
      int _length_1 = string.length();
      int _minus = (_length_1 - maxLength);
      String _plus_1 = (_plus + Integer.valueOf(_minus));
      _xblockexpression = (_plus_1 + " more chars)");
    }
    return _xblockexpression;
  }
}
