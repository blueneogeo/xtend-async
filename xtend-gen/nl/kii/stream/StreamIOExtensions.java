package nl.kii.stream;

import nl.kii.promise.Task;
import nl.kii.stream.IStream;
import nl.kii.stream.Stream;
import nl.kii.stream.internal.SubStream;

public class StreamIOExtensions {
  /**
   * stream a standard Java inputstream. closing the stream closes the inputstream.
   */
  public static /* Stream<List<Byte>> */Object stream(final /* InputStream */Object stream) {
    throw new Error("Unresolved compilation problems:"
      + "\nList cannot be resolved to a type."
      + "\nIOException cannot be resolved to a type."
      + "\nByteProcessor cannot be resolved."
      + "\nThe method close is undefined for the type StreamIOExtensions"
      + "\n! cannot be resolved."
      + "\nInvalid number of arguments. The method skip(IStream<I, O>, int) is not applicable for the arguments (Object)"
      + "\nByte cannot be resolved to a type."
      + "\nreadBytes cannot be resolved"
      + "\nclose cannot be resolved"
      + "\nclose cannot be resolved");
  }
  
  /**
   * stream a file as byte blocks. closing the stream closes the file.
   */
  public static /* Stream<List<Byte>> */Object stream(final /* File */Object file) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Files is undefined for the type StreamIOExtensions"
      + "\nasByteSource cannot be resolved"
      + "\nopenBufferedStream cannot be resolved"
      + "\nstream cannot be resolved");
  }
  
  public static <I extends java.lang.Object> /* SubStream<I, String> */Object toText(final /* IStream<I, List<Byte>> */Object stream) {
    return StreamIOExtensions.<I>toText(stream, "UTF-8");
  }
  
  public static <I extends java.lang.Object> /* SubStream<I, String> */Object toText(final /* IStream<I, List<Byte>> */Object stream, final /* String */Object encoding) {
    throw new Error("Unresolved compilation problems:"
      + "\nString cannot be resolved."
      + "\nsplit cannot be resolved"
      + "\ntoList cannot be resolved"
      + "\nseparate cannot be resolved"
      + "\n=> cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  public static <I extends java.lang.Object> /* SubStream<I, List<Byte>> */Object toBytes(final /* IStream<I, String> */Object stream) {
    return StreamIOExtensions.<I>toBytes(stream, "UTF-8");
  }
  
  public static <I extends java.lang.Object> /* SubStream<I, List<Byte>> */Object toBytes(final /* IStream<I, String> */Object stream, final /* String */Object encoding) {
    throw new Error("Unresolved compilation problems:"
      + "\nList cannot be resolved to a type."
      + "\nByte cannot be resolved to a type."
      + "\n+ cannot be resolved"
      + "\ngetBytes cannot be resolved"
      + "\n=> cannot be resolved"
      + "\n+ cannot be resolved"
      + "\n+ cannot be resolved");
  }
  
  /**
   * write a buffered bytestream to an standard java outputstream
   */
  public static <I extends java.lang.Object> Task writeTo(final /* IStream<I, List<Byte>> */Object stream, final /* OutputStream */Object out) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method closed is undefined for the type StreamIOExtensions"
      + "\nThe method or field $1 is undefined for the type StreamIOExtensions"
      + "\nThe method each is undefined for the type StreamIOExtensions"
      + "\nThe method or field $1 is undefined for the type StreamIOExtensions"
      + "\nclose cannot be resolved"
      + "\n== cannot be resolved"
      + "\nclose cannot be resolved"
      + "\nwrite cannot be resolved");
  }
  
  /**
   * write a buffered bytestream to a file
   */
  public static <I extends java.lang.Object> Task writeTo(final /* IStream<I, List<Byte>> */Object stream, final /* File */Object file) {
    throw new Error("Unresolved compilation problems:"
      + "\nThe method or field Files is undefined for the type StreamIOExtensions"
      + "\nThe method pipe is undefined for the type StreamIOExtensions"
      + "\nasByteSink cannot be resolved"
      + "\nopenBufferedStream cannot be resolved"
      + "\n+ cannot be resolved"
      + "\nabsolutePath cannot be resolved"
      + "\n+ cannot be resolved");
  }
}
