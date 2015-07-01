package nl.kii.async.processors;

import com.google.common.base.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import nl.kii.async.annotation.Async;
import org.eclipse.xtend.lib.macro.AbstractMethodProcessor;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.CompilationStrategy;
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableTypeParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.Type;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.declaration.Visibility;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class AsyncProcessor extends AbstractMethodProcessor {
  public boolean isPromiseType(final TypeReference type) {
    boolean _or = false;
    boolean _or_1 = false;
    String _simpleName = type.getSimpleName();
    boolean _startsWith = _simpleName.startsWith("IPromise");
    if (_startsWith) {
      _or_1 = true;
    } else {
      String _simpleName_1 = type.getSimpleName();
      boolean _startsWith_1 = _simpleName_1.startsWith("Promise");
      _or_1 = _startsWith_1;
    }
    if (_or_1) {
      _or = true;
    } else {
      String _simpleName_2 = type.getSimpleName();
      boolean _startsWith_2 = _simpleName_2.startsWith("Task");
      _or = _startsWith_2;
    }
    return _or;
  }
  
  @Override
  public void doTransform(final MutableMethodDeclaration method, @Extension final TransformationContext context) {
    Iterable<? extends MutableParameterDeclaration> _parameters = method.getParameters();
    final Function1<MutableParameterDeclaration, Boolean> _function = new Function1<MutableParameterDeclaration, Boolean>() {
      @Override
      public Boolean apply(final MutableParameterDeclaration it) {
        TypeReference _type = it.getType();
        return Boolean.valueOf(AsyncProcessor.this.isPromiseType(_type));
      }
    };
    Iterable<? extends MutableParameterDeclaration> _filter = IterableExtensions.filter(_parameters, _function);
    final MutableParameterDeclaration promiseParameter = IterableExtensions.head(_filter);
    boolean _equals = Objects.equal(promiseParameter, null);
    if (_equals) {
      boolean _and = false;
      TypeReference _returnType = method.getReturnType();
      boolean _isInferred = _returnType.isInferred();
      boolean _not = (!_isInferred);
      if (!_not) {
        _and = false;
      } else {
        TypeReference _returnType_1 = method.getReturnType();
        boolean _isPromiseType = this.isPromiseType(_returnType_1);
        boolean _not_1 = (!_isPromiseType);
        _and = _not_1;
      }
      if (_and) {
        context.addError(method, "Methods annotated with @Async must either return a Task or Promise, or pass a Task or Promise in their parameters.");
      }
      return;
    }
    MutableTypeDeclaration _declaringType = method.getDeclaringType();
    String _simpleName = method.getSimpleName();
    final Procedure1<MutableMethodDeclaration> _function_1 = new Procedure1<MutableMethodDeclaration>() {
      @Override
      public void apply(final MutableMethodDeclaration it) {
        context.setPrimarySourceElement(it, method);
        Iterable<? extends MutableTypeParameterDeclaration> _typeParameters = method.getTypeParameters();
        boolean _isEmpty = false;
        if (_typeParameters!=null) {
          _isEmpty=IterableExtensions.isEmpty(_typeParameters);
        }
        boolean _not = (!_isEmpty);
        if (_not) {
          context.addError(method, "Currently type parameters are not supported for @Async, because Xtend Active Annotation do not fully support typed parameters");
        }
        Iterable<? extends MutableTypeParameterDeclaration> _typeParameters_1 = method.getTypeParameters();
        for (final MutableTypeParameterDeclaration typeParameter : _typeParameters_1) {
          String _simpleName = typeParameter.getSimpleName();
          Iterable<? extends TypeReference> _upperBounds = typeParameter.getUpperBounds();
          it.addTypeParameter(_simpleName, ((TypeReference[])Conversions.unwrapArray(_upperBounds, TypeReference.class)));
        }
        boolean _isStatic = method.isStatic();
        it.setStatic(_isStatic);
        String _docComment = method.getDocComment();
        it.setDocComment(_docComment);
        Visibility _visibility = method.getVisibility();
        it.setVisibility(_visibility);
        boolean _isSynchronized = method.isSynchronized();
        it.setSynchronized(_isSynchronized);
        Iterable<? extends TypeReference> _exceptions = method.getExceptions();
        it.setExceptions(((TypeReference[])Conversions.unwrapArray(_exceptions, TypeReference.class)));
        boolean _isVarArgs = method.isVarArgs();
        it.setVarArgs(_isVarArgs);
        final AtomicReference<MutableParameterDeclaration> promise = new AtomicReference<MutableParameterDeclaration>();
        Iterable<? extends MutableParameterDeclaration> _parameters = method.getParameters();
        for (final MutableParameterDeclaration parameter : _parameters) {
          boolean _or = false;
          TypeReference _type = parameter.getType();
          String _simpleName_1 = _type.getSimpleName();
          boolean _startsWith = _simpleName_1.startsWith("Promise");
          if (_startsWith) {
            _or = true;
          } else {
            TypeReference _type_1 = parameter.getType();
            String _simpleName_2 = _type_1.getSimpleName();
            boolean _startsWith_1 = _simpleName_2.startsWith("Task");
            _or = _startsWith_1;
          }
          if (_or) {
            promise.set(parameter);
          } else {
            String _simpleName_3 = parameter.getSimpleName();
            TypeReference _type_2 = parameter.getType();
            it.addParameter(_simpleName_3, _type_2);
          }
        }
        MutableParameterDeclaration _get = promise.get();
        boolean _equals = Objects.equal(_get, null);
        if (_equals) {
          context.addError(method, "Methods annotated with @Async must pass the Promise or Task to return in its parameters.");
        } else {
          MutableParameterDeclaration _get_1 = promise.get();
          TypeReference _type_3 = _get_1.getType();
          it.setReturnType(_type_3);
          final CompilationStrategy _function = new CompilationStrategy() {
            @Override
            public CharSequence compile(final CompilationStrategy.CompilationContext it) {
              StringConcatenation _builder = new StringConcatenation();
              _builder.append("final ");
              MutableParameterDeclaration _get = promise.get();
              TypeReference _type = _get.getType();
              String _simpleName = _type.getSimpleName();
              _builder.append(_simpleName, "");
              _builder.append(" ");
              MutableParameterDeclaration _get_1 = promise.get();
              String _simpleName_1 = _get_1.getSimpleName();
              _builder.append(_simpleName_1, "");
              _builder.append(" = new ");
              MutableParameterDeclaration _get_2 = promise.get();
              TypeReference _type_1 = _get_2.getType();
              String _simpleName_2 = _type_1.getSimpleName();
              _builder.append(_simpleName_2, "");
              _builder.append("();");
              _builder.newLineIfNotEmpty();
              _builder.append("try {");
              _builder.newLine();
              _builder.append("\t");
              String _simpleName_3 = method.getSimpleName();
              _builder.append(_simpleName_3, "\t");
              _builder.append("(");
              {
                Iterable<? extends MutableParameterDeclaration> _parameters = method.getParameters();
                boolean _hasElements = false;
                for(final MutableParameterDeclaration parameter : _parameters) {
                  if (!_hasElements) {
                    _hasElements = true;
                  } else {
                    _builder.appendImmediate(",", "\t");
                  }
                  String _simpleName_4 = parameter.getSimpleName();
                  _builder.append(_simpleName_4, "\t");
                }
              }
              _builder.append(");");
              _builder.newLineIfNotEmpty();
              _builder.append("} catch(Throwable t) {");
              _builder.newLine();
              _builder.append("\t");
              MutableParameterDeclaration _get_3 = promise.get();
              String _simpleName_5 = _get_3.getSimpleName();
              _builder.append(_simpleName_5, "\t");
              _builder.append(".error(t);");
              _builder.newLineIfNotEmpty();
              _builder.append("} finally {");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("return ");
              MutableParameterDeclaration _get_4 = promise.get();
              String _simpleName_6 = _get_4.getSimpleName();
              _builder.append(_simpleName_6, "\t");
              _builder.append(";");
              _builder.newLineIfNotEmpty();
              _builder.append("}");
              _builder.newLine();
              return _builder;
            }
          };
          it.setBody(_function);
        }
      }
    };
    _declaringType.addMethod(_simpleName, _function_1);
    TypeReference _newTypeReference = context.newTypeReference(Async.class);
    Type _type = _newTypeReference.getType();
    final AnnotationReference async = method.findAnnotation(_type);
    boolean _booleanValue = false;
    if (async!=null) {
      _booleanValue=async.getBooleanValue("value");
    }
    boolean _not_2 = (!_booleanValue);
    if (_not_2) {
      return;
    }
    MutableTypeDeclaration _declaringType_1 = method.getDeclaringType();
    String _simpleName_1 = method.getSimpleName();
    final Procedure1<MutableMethodDeclaration> _function_2 = new Procedure1<MutableMethodDeclaration>() {
      @Override
      public void apply(final MutableMethodDeclaration it) {
        context.setPrimarySourceElement(it, method);
        Iterable<? extends MutableTypeParameterDeclaration> _typeParameters = method.getTypeParameters();
        for (final MutableTypeParameterDeclaration typeParameter : _typeParameters) {
          String _simpleName = typeParameter.getSimpleName();
          Iterable<? extends TypeReference> _upperBounds = typeParameter.getUpperBounds();
          it.addTypeParameter(_simpleName, ((TypeReference[])Conversions.unwrapArray(_upperBounds, TypeReference.class)));
        }
        boolean _isStatic = method.isStatic();
        it.setStatic(_isStatic);
        String _docComment = method.getDocComment();
        it.setDocComment(_docComment);
        Visibility _visibility = method.getVisibility();
        it.setVisibility(_visibility);
        boolean _isSynchronized = method.isSynchronized();
        it.setSynchronized(_isSynchronized);
        Iterable<? extends TypeReference> _exceptions = method.getExceptions();
        it.setExceptions(((TypeReference[])Conversions.unwrapArray(_exceptions, TypeReference.class)));
        boolean _isVarArgs = method.isVarArgs();
        it.setVarArgs(_isVarArgs);
        TypeReference _newTypeReference = context.newTypeReference(Executor.class);
        it.addParameter("executor", _newTypeReference);
        final AtomicReference<MutableParameterDeclaration> promise = new AtomicReference<MutableParameterDeclaration>();
        Iterable<? extends MutableParameterDeclaration> _parameters = method.getParameters();
        for (final MutableParameterDeclaration parameter : _parameters) {
          boolean _or = false;
          TypeReference _type = parameter.getType();
          String _simpleName_1 = _type.getSimpleName();
          boolean _startsWith = _simpleName_1.startsWith("Promise");
          if (_startsWith) {
            _or = true;
          } else {
            TypeReference _type_1 = parameter.getType();
            String _simpleName_2 = _type_1.getSimpleName();
            boolean _startsWith_1 = _simpleName_2.startsWith("Task");
            _or = _startsWith_1;
          }
          if (_or) {
            promise.set(parameter);
          } else {
            String _simpleName_3 = parameter.getSimpleName();
            TypeReference _type_2 = parameter.getType();
            it.addParameter(_simpleName_3, _type_2);
          }
        }
        MutableParameterDeclaration _get = promise.get();
        boolean _equals = Objects.equal(_get, null);
        if (_equals) {
          context.addError(method, "Methods annotated with @Async must pass the Promise or Task to return in its parameters.");
        } else {
          MutableParameterDeclaration _get_1 = promise.get();
          TypeReference _type_3 = _get_1.getType();
          it.setReturnType(_type_3);
          final CompilationStrategy _function = new CompilationStrategy() {
            @Override
            public CharSequence compile(final CompilationStrategy.CompilationContext it) {
              StringConcatenation _builder = new StringConcatenation();
              _builder.append("final ");
              MutableParameterDeclaration _get = promise.get();
              TypeReference _type = _get.getType();
              String _simpleName = _type.getSimpleName();
              _builder.append(_simpleName, "");
              _builder.append(" ");
              MutableParameterDeclaration _get_1 = promise.get();
              String _simpleName_1 = _get_1.getSimpleName();
              _builder.append(_simpleName_1, "");
              _builder.append(" = new ");
              MutableParameterDeclaration _get_2 = promise.get();
              TypeReference _type_1 = _get_2.getType();
              String _simpleName_2 = _type_1.getSimpleName();
              _builder.append(_simpleName_2, "");
              _builder.append("();");
              _builder.newLineIfNotEmpty();
              _builder.append("final Runnable toRun = new Runnable() {");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("public void run() {");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("try {");
              _builder.newLine();
              _builder.append("\t\t\t");
              String _simpleName_3 = method.getSimpleName();
              _builder.append(_simpleName_3, "\t\t\t");
              _builder.append("(");
              {
                Iterable<? extends MutableParameterDeclaration> _parameters = method.getParameters();
                boolean _hasElements = false;
                for(final MutableParameterDeclaration parameter : _parameters) {
                  if (!_hasElements) {
                    _hasElements = true;
                  } else {
                    _builder.appendImmediate(",", "\t\t\t");
                  }
                  String _simpleName_4 = parameter.getSimpleName();
                  _builder.append(_simpleName_4, "\t\t\t");
                }
              }
              _builder.append(");");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("} catch(Throwable t) {");
              _builder.newLine();
              _builder.append("\t\t\t");
              MutableParameterDeclaration _get_3 = promise.get();
              String _simpleName_5 = _get_3.getSimpleName();
              _builder.append(_simpleName_5, "\t\t\t");
              _builder.append(".error(t);");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("}");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("}");
              _builder.newLine();
              _builder.append("};");
              _builder.newLine();
              _builder.append("executor.execute(toRun);");
              _builder.newLine();
              _builder.append("return ");
              MutableParameterDeclaration _get_4 = promise.get();
              String _simpleName_6 = _get_4.getSimpleName();
              _builder.append(_simpleName_6, "");
              _builder.append(";");
              _builder.newLineIfNotEmpty();
              return _builder;
            }
          };
          it.setBody(_function);
        }
      }
    };
    _declaringType_1.addMethod(_simpleName_1, _function_2);
  }
}
