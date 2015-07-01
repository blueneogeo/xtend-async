package nl.kii.async.processors;

import com.google.common.base.Objects;
import com.google.common.util.concurrent.AtomicDouble;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.xtend.lib.macro.AbstractFieldProcessor;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtend.lib.macro.declaration.CompilationStrategy;
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.declaration.Visibility;
import org.eclipse.xtend.lib.macro.expression.Expression;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;

@SuppressWarnings("all")
public class AtomicProcessor extends AbstractFieldProcessor {
  @Override
  public void doTransform(final MutableFieldDeclaration field, @Extension final TransformationContext context) {
    final TypeReference BOOLEAN = context.newTypeReference(Boolean.class);
    final TypeReference INTEGER = context.newTypeReference(Integer.class);
    final TypeReference LONG = context.newTypeReference(Long.class);
    final TypeReference DOUBLE = context.newTypeReference(Double.class);
    TypeReference _switchResult = null;
    TypeReference _type = field.getType();
    String _simpleName = _type.getSimpleName();
    switch (_simpleName) {
      case "boolean":
      case "Boolean":
        _switchResult = BOOLEAN;
        break;
      case "int":
      case "Integer":
        _switchResult = INTEGER;
        break;
      case "long":
      case "Long":
        _switchResult = LONG;
        break;
      case "float":
      case "Float":
      case "double":
      case "Double":
        _switchResult = DOUBLE;
        break;
      default:
        _switchResult = field.getType();
        break;
    }
    final TypeReference type = _switchResult;
    TypeReference _switchResult_1 = null;
    boolean _matched = false;
    if (!_matched) {
      if (Objects.equal(type, BOOLEAN)) {
        _matched=true;
        _switchResult_1 = context.newTypeReference(AtomicBoolean.class);
      }
    }
    if (!_matched) {
      if (Objects.equal(type, INTEGER)) {
        _matched=true;
        _switchResult_1 = context.newTypeReference(AtomicInteger.class);
      }
    }
    if (!_matched) {
      if (Objects.equal(type, LONG)) {
        _matched=true;
        _switchResult_1 = context.newTypeReference(AtomicLong.class);
      }
    }
    if (!_matched) {
      if (Objects.equal(type, DOUBLE)) {
        _matched=true;
        _switchResult_1 = context.newTypeReference(AtomicDouble.class);
      }
    }
    if (!_matched) {
      _switchResult_1 = context.newTypeReference(AtomicReference.class, type);
    }
    final TypeReference atomicType = _switchResult_1;
    final Visibility methodVisibility = field.getVisibility();
    Visibility _xifexpression = null;
    boolean _equals = Objects.equal(methodVisibility, Visibility.PUBLIC);
    if (_equals) {
      _xifexpression = Visibility.PROTECTED;
    } else {
      _xifexpression = methodVisibility;
    }
    final Visibility atomicMethodsVisibility = _xifexpression;
    final String fieldName = field.getSimpleName();
    field.setType(atomicType);
    field.setVisibility(Visibility.PRIVATE);
    Object _elvis = null;
    Expression _initializer = field.getInitializer();
    if (_initializer != null) {
      _elvis = _initializer;
    } else {
      _elvis = "";
    }
    final Object defaultValue = _elvis;
    final CompilationStrategy _function = new CompilationStrategy() {
      @Override
      public CharSequence compile(final CompilationStrategy.CompilationContext it) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("new ");
        String _simpleName = atomicType.getSimpleName();
        _builder.append(_simpleName, "");
        _builder.append("(");
        _builder.append(defaultValue, "");
        _builder.append(")");
        return _builder;
      }
    };
    field.setInitializer(_function);
    field.setFinal(true);
    String _simpleName_1 = field.getSimpleName();
    String _plus = ("_" + _simpleName_1);
    field.setSimpleName(_plus);
    MutableTypeDeclaration _declaringType = field.getDeclaringType();
    String _firstUpper = StringExtensions.toFirstUpper(fieldName);
    String _plus_1 = ("set" + _firstUpper);
    final Procedure1<MutableMethodDeclaration> _function_1 = new Procedure1<MutableMethodDeclaration>() {
      @Override
      public void apply(final MutableMethodDeclaration it) {
        context.setPrimarySourceElement(it, field);
        it.setVisibility(methodVisibility);
        boolean _isStatic = field.isStatic();
        it.setStatic(_isStatic);
        it.addParameter("value", type);
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            _builder.append("this.");
            String _simpleName = field.getSimpleName();
            _builder.append(_simpleName, "");
            _builder.append(".set(value);");
            _builder.newLineIfNotEmpty();
          }
        };
        it.setBody(_client);
      }
    };
    _declaringType.addMethod(_plus_1, _function_1);
    MutableTypeDeclaration _declaringType_1 = field.getDeclaringType();
    String _firstUpper_1 = StringExtensions.toFirstUpper(fieldName);
    String _plus_2 = ("get" + _firstUpper_1);
    final Procedure1<MutableMethodDeclaration> _function_2 = new Procedure1<MutableMethodDeclaration>() {
      @Override
      public void apply(final MutableMethodDeclaration it) {
        context.setPrimarySourceElement(it, field);
        it.setVisibility(methodVisibility);
        boolean _isStatic = field.isStatic();
        it.setStatic(_isStatic);
        it.setReturnType(type);
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            _builder.append("return this.");
            String _simpleName = field.getSimpleName();
            _builder.append(_simpleName, "");
            _builder.append(".get();");
            _builder.newLineIfNotEmpty();
          }
        };
        it.setBody(_client);
      }
    };
    _declaringType_1.addMethod(_plus_2, _function_2);
    MutableTypeDeclaration _declaringType_2 = field.getDeclaringType();
    String _firstUpper_2 = StringExtensions.toFirstUpper(fieldName);
    String _plus_3 = ("getAndSet" + _firstUpper_2);
    final Procedure1<MutableMethodDeclaration> _function_3 = new Procedure1<MutableMethodDeclaration>() {
      @Override
      public void apply(final MutableMethodDeclaration it) {
        context.setPrimarySourceElement(it, field);
        it.setVisibility(atomicMethodsVisibility);
        boolean _isStatic = field.isStatic();
        it.setStatic(_isStatic);
        it.addParameter("value", type);
        it.setReturnType(type);
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            _builder.append("return this.");
            String _simpleName = field.getSimpleName();
            _builder.append(_simpleName, "");
            _builder.append(".getAndSet(value);");
            _builder.newLineIfNotEmpty();
          }
        };
        it.setBody(_client);
      }
    };
    _declaringType_2.addMethod(_plus_3, _function_3);
    boolean _or = false;
    boolean _equals_1 = Objects.equal(type, INTEGER);
    if (_equals_1) {
      _or = true;
    } else {
      boolean _equals_2 = Objects.equal(type, LONG);
      _or = _equals_2;
    }
    if (_or) {
      MutableTypeDeclaration _declaringType_3 = field.getDeclaringType();
      String _firstUpper_3 = StringExtensions.toFirstUpper(fieldName);
      String _plus_4 = ("inc" + _firstUpper_3);
      final Procedure1<MutableMethodDeclaration> _function_4 = new Procedure1<MutableMethodDeclaration>() {
        @Override
        public void apply(final MutableMethodDeclaration it) {
          context.setPrimarySourceElement(it, field);
          it.setVisibility(atomicMethodsVisibility);
          boolean _isStatic = field.isStatic();
          it.setStatic(_isStatic);
          it.setReturnType(type);
          StringConcatenationClient _client = new StringConcatenationClient() {
            @Override
            protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
              _builder.append("return this.");
              String _simpleName = field.getSimpleName();
              _builder.append(_simpleName, "");
              _builder.append(".incrementAndGet();");
              _builder.newLineIfNotEmpty();
            }
          };
          it.setBody(_client);
        }
      };
      _declaringType_3.addMethod(_plus_4, _function_4);
      MutableTypeDeclaration _declaringType_4 = field.getDeclaringType();
      String _firstUpper_4 = StringExtensions.toFirstUpper(fieldName);
      String _plus_5 = ("dec" + _firstUpper_4);
      final Procedure1<MutableMethodDeclaration> _function_5 = new Procedure1<MutableMethodDeclaration>() {
        @Override
        public void apply(final MutableMethodDeclaration it) {
          context.setPrimarySourceElement(it, field);
          it.setVisibility(atomicMethodsVisibility);
          boolean _isStatic = field.isStatic();
          it.setStatic(_isStatic);
          it.setReturnType(type);
          StringConcatenationClient _client = new StringConcatenationClient() {
            @Override
            protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
              _builder.append("return this.");
              String _simpleName = field.getSimpleName();
              _builder.append(_simpleName, "");
              _builder.append(".decrementAndGet();");
              _builder.newLineIfNotEmpty();
            }
          };
          it.setBody(_client);
        }
      };
      _declaringType_4.addMethod(_plus_5, _function_5);
    }
    boolean _or_1 = false;
    boolean _or_2 = false;
    boolean _equals_3 = Objects.equal(type, INTEGER);
    if (_equals_3) {
      _or_2 = true;
    } else {
      boolean _equals_4 = Objects.equal(type, LONG);
      _or_2 = _equals_4;
    }
    if (_or_2) {
      _or_1 = true;
    } else {
      boolean _equals_5 = Objects.equal(type, DOUBLE);
      _or_1 = _equals_5;
    }
    if (_or_1) {
      MutableTypeDeclaration _declaringType_5 = field.getDeclaringType();
      String _firstUpper_5 = StringExtensions.toFirstUpper(fieldName);
      String _plus_6 = ("inc" + _firstUpper_5);
      final Procedure1<MutableMethodDeclaration> _function_6 = new Procedure1<MutableMethodDeclaration>() {
        @Override
        public void apply(final MutableMethodDeclaration it) {
          context.setPrimarySourceElement(it, field);
          it.addParameter("value", type);
          boolean _isStatic = field.isStatic();
          it.setStatic(_isStatic);
          it.setVisibility(atomicMethodsVisibility);
          it.setReturnType(type);
          StringConcatenationClient _client = new StringConcatenationClient() {
            @Override
            protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
              _builder.append("return this.");
              String _simpleName = field.getSimpleName();
              _builder.append(_simpleName, "");
              _builder.append(".addAndGet(value);");
              _builder.newLineIfNotEmpty();
            }
          };
          it.setBody(_client);
        }
      };
      _declaringType_5.addMethod(_plus_6, _function_6);
    }
  }
}
