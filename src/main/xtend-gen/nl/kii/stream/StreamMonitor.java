package nl.kii.stream;

import com.google.common.base.Objects;
import java.util.List;
import nl.kii.stream.StreamStats;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;

@SuppressWarnings("all")
public class StreamMonitor {
  private List<Pair<String, StreamStats>> chain = CollectionLiterals.<Pair<String, StreamStats>>newLinkedList();
  
  public boolean add(final String name, final StreamStats stats) {
    Pair<String, StreamStats> _mappedTo = Pair.<String, StreamStats>of(name, stats);
    return this.chain.add(_mappedTo);
  }
  
  public StreamStats stats(final String name) {
    final Function1<Pair<String, StreamStats>, Boolean> _function = new Function1<Pair<String, StreamStats>, Boolean>() {
      public Boolean apply(final Pair<String, StreamStats> it) {
        String _key = it.getKey();
        return Boolean.valueOf(Objects.equal(_key, name));
      }
    };
    Pair<String, StreamStats> _findFirst = IterableExtensions.<Pair<String, StreamStats>>findFirst(this.chain, _function);
    StreamStats _value = null;
    if (_findFirst!=null) {
      _value=_findFirst.getValue();
    }
    return _value;
  }
  
  public StreamStats stats(final int position) {
    StreamStats _xblockexpression = null;
    {
      int _size = this.chain.size();
      boolean _lessThan = (_size < position);
      if (_lessThan) {
        return null;
      }
      Pair<String, StreamStats> _get = this.chain.get((position - 1));
      StreamStats _value = null;
      if (_get!=null) {
        _value=_get.getValue();
      }
      _xblockexpression = _value;
    }
    return _xblockexpression;
  }
  
  public List<Pair<String, StreamStats>> getChain() {
    return this.chain;
  }
  
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    {
      for(final Pair<String, StreamStats> pair : this.chain) {
        _builder.append("--- ");
        String _key = pair.getKey();
        _builder.append(_key, "");
        _builder.append(" --------------");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        StreamStats _value = pair.getValue();
        _builder.append(_value, "\t");
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder.toString();
  }
}
