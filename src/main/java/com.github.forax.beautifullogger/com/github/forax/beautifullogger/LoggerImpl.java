package com.github.forax.beautifullogger;

import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigFeature.ENABLE_CONF;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigFeature.LEVEL_CONF;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigFeature.LEVEL_OVERRIDE_CONF;
import static com.github.forax.beautifullogger.LoggerImpl.LoggerConfigFeature.LOGFACADEFACTORY_CONF;
import static java.lang.invoke.MethodHandles.dropArguments;
import static java.lang.invoke.MethodHandles.exactInvoker;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.foldArguments;
import static java.lang.invoke.MethodHandles.guardWithTest;
import static java.lang.invoke.MethodHandles.identity;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.permuteArguments;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static java.util.Collections.nCopies;

import java.lang.StackWalker.Option;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.lang.invoke.SwitchPoint;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Supplier;

import com.github.forax.beautifullogger.Logger.Level;
import com.github.forax.beautifullogger.LoggerConfig.ConfigOption;
import com.github.forax.beautifullogger.LoggerConfig.LogFacade;
import com.github.forax.beautifullogger.LoggerConfig.LogFacadeFactory;

import sun.misc.Unsafe;

class LoggerImpl {
  static final boolean IS_JAVA_8;
  static {
    boolean isJava8;
    try {
      Class.forName("java.lang.StackWalker");
      isJava8 = false;
    } catch (@SuppressWarnings("unused") ClassNotFoundException e) {
      isJava8 = true;
    }
    IS_JAVA_8 = isJava8;
  }
  
  static class StackWalkerHolder {
    static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
  }
  
  static class GetCallerHolder {
    private static final Supplier<?> GET_CALLER_CLASS;
    static {
      String s = "yv66vgAAADQAIAcAAgEAM2NvbS9naXRodWIvZm9yYXgvYmVhdXRpZnVsbG9nZ2VyL1JlZmxlY3Rpb25TdXBwbGllcgcABAEAEGphdmEvbGFuZy9PYmplY3QHAAYBABtqYXZhL3V0aWwvZnVuY3Rpb24vU3VwcGxpZXIBAAY8aW5pdD4BAAMoKVYBAARDb2RlCgADAAsMAAcACAEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEABHRoaXMBADVMY29tL2dpdGh1Yi9mb3JheC9iZWF1dGlmdWxsb2dnZXIvUmVmbGVjdGlvblN1cHBsaWVyOwEAA2dldAEAEygpTGphdmEvbGFuZy9DbGFzczsBAAlTaWduYXR1cmUBABYoKUxqYXZhL2xhbmcvQ2xhc3M8Kj47CgAVABcHABYBABZzdW4vcmVmbGVjdC9SZWZsZWN0aW9uDAAYABkBAA5nZXRDYWxsZXJDbGFzcwEAFChJKUxqYXZhL2xhbmcvQ2xhc3M7AQAUKClMamF2YS9sYW5nL09iamVjdDsKAAEAHAwAEAARAQAKU291cmNlRmlsZQEAF1JlZmxlY3Rpb25TdXBwbGllci5qYXZhAQBFTGphdmEvbGFuZy9PYmplY3Q7TGphdmEvdXRpbC9mdW5jdGlvbi9TdXBwbGllcjxMamF2YS9sYW5nL0NsYXNzPCo+Oz47ACEAAQADAAEABQAAAAMAAQAHAAgAAQAJAAAALwABAAEAAAAFKrcACrEAAAACAAwAAAAGAAEAAAAHAA0AAAAMAAEAAAAFAA4ADwAAAAEAEAARAAIAEgAAAAIAEwAJAAAALwABAAEAAAAFCLgAFLAAAAACAAwAAAAGAAEAAAAKAA0AAAAMAAEAAAAFAA4ADwAAEEEAEAAaAAEACQAAACUAAQABAAAABSq2ABuwAAAAAgAMAAAABgABAAAAAQANAAAAAgAAAAIAEgAAAAIAHwAdAAAAAgAe";
      Supplier<?> supplier;
      try {
        Class<?> reflectionSupplier = UNSAFE.defineAnonymousClass(LoggerImpl.class, Base64.getDecoder().decode(s), null);
        supplier = (Supplier<?>) reflectionSupplier.getConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new AssertionError(e);
      }
      GET_CALLER_CLASS = supplier;
    }
    
    static Class<?> getCallerClass() {
      return (Class<?>)GET_CALLER_CLASS.get();
    } 
  }
  
  private static class None {
    None() { /* singleton */ }
    @Override public String toString() { return "NONE"; }
  }
  static final Object NONE = new None();
  
  // used internally by Logger, should not be public
  static final Consumer<ConfigOption> EMPTY_CONSUMER = __ -> { /* empty */ };
  
  private LoggerImpl() {
    throw new AssertionError();
  }
  
  static MethodHandle getLoggingMethodHandle(Class<?> configClass, int maxParameter) {
    return new CS(configClass, maxParameter).dynamicInvoker();
  }
  
  private static class CS extends MutableCallSite {
    private static final MethodType LOG_METHOD_TYPE = methodType(void.class, String.class, Level.class, Throwable.class);
    private static final MethodHandle FALLBACK, NOP;
    private static final MethodHandle[] CHECK_LEVELS;
    static {
      Lookup lookup = lookup();
      try {
        FALLBACK = lookup.findVirtual(CS.class, "fallback", methodType(MethodHandle.class, Level.class,  Throwable.class, Object.class, Object[].class));
        NOP = lookup.findStatic(CS.class, "nop", methodType(void.class));
        
        MethodHandle[] checkLevels = new MethodHandle[Level.LEVELS.length];
        for(int i = 0; i < checkLevels.length; i++) {
          checkLevels[i] = lookup.findStatic(CS.class, "checkLevel".concat(Level.LEVELS[i].name()), methodType(boolean.class, Level.class));
        }
        CHECK_LEVELS = checkLevels;
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
    
    private final int maxParameters;
    private final Class<?> configClass;
    private final MethodHandle fallback;

    CS(Class<?> configClass, int maxParameters) {
      super(methodType(void.class, Level.class, Throwable.class, Object.class).appendParameterTypes(nCopies(maxParameters, Object.class)));
      this.maxParameters = maxParameters;
      this.configClass = configClass;
      MethodHandle fallback = foldArguments(exactInvoker(type()), FALLBACK.bindTo(this).asCollector(Object[].class, maxParameters));
      this.fallback = fallback;
      setTarget(fallback);
    }
    
    private static MethodHandle empty_void(MethodType methodType) {
      if (IS_JAVA_8) {
        return MethodHandles.dropArguments(NOP, 0, methodType.parameterList());  
      }
      return MethodHandles.empty(methodType);
    }
    
    @SuppressWarnings("unused")
    private MethodHandle fallback(Level level, Throwable context, Object messageProvider, Object[] args) {
      Objects.requireNonNull(level, "level is null");
      Objects.requireNonNull(messageProvider, "message provider is null");      
      
      // check configuration flag 'enable'
      LinkedHashSet<SwitchPoint> switchPoints = new LinkedHashSet<>();
      boolean enable = ENABLE_CONF.findValueAndCollectSwitchPoints(configClass, switchPoints).orElse(true);
      
      MethodHandle target;
      MethodHandle empty = empty_void(type());
      if (enable) {
        // get configuration facade factory
        LogFacadeFactory logFacadeFactory = LOGFACADEFACTORY_CONF.findValueAndCollectSwitchPoints(configClass, switchPoints)
            .orElseGet(LogFacadeFactory::defaultFactory);
        LogFacade logFacade = logFacadeFactory.logFacade(configClass);
        MethodHandle logMH = logFacade.getLogMethodHandle();
        if (!logMH.type().equals(LOG_METHOD_TYPE)) {
          throw new WrongMethodTypeException("the print method handle should be typed (String, Level, Throwable)V ".concat(logMH.toString()));
        }
        
        // adjust to the number of parameters (+ the message provider)
        MethodHandle print = dropArguments(logMH, 3, nCopies(1 + maxParameters, Object.class));
        
        // create the message provider call site, we already have the arguments of the first call here,
        // so we can directly call the fallback to avoid an unnecessary round trip 
        MessageProviderCS providerCallSite = new MessageProviderCS(type(), maxParameters, print); 
        providerCallSite.fallback(messageProvider, args);
        target = providerCallSite.getTarget();
        
        // check configuration level, override the underlying logger level if necessary
        Level configLevel = LEVEL_CONF.findValueAndCollectSwitchPoints(configClass, switchPoints).orElse(Level.INFO);
        if (LEVEL_OVERRIDE_CONF.findValueAndCollectSwitchPoints(configClass, switchPoints).orElse(false)) {
          logFacade.overrideLevel(configLevel);
        }
        target = guardWithTest(CHECK_LEVELS[configLevel.ordinal()], target, empty);
        
      } else {
        // if disable, do nothing !
        target = empty;
      }
      
      // avoid recursion (i.e. non progression) if the switch points are invalidated
      // between the time the configuration is read and the time the method handle is installed
      MethodHandle result = target;
      
      // prepend switch points
      for(SwitchPoint switchPoint: switchPoints) {
        target = switchPoint.guardWithTest(target, fallback);
      }
      
      setTarget(target);
      return result;
    }
    
    @SuppressWarnings("unused")
    private static void nop() {
      // does nothing !
    }
    
    // use one method checkLevel* by level to allow JITs to remove those checks 
    
    @SuppressWarnings("unused")
    private static boolean checkLevelTRACE(Level level) {
      return true;
    }
    @SuppressWarnings("unused")
    private static boolean checkLevelDEBUG(Level level) {
      if (level == Level.TRACE) {
        return false;
      }
      return true;
    }
    @SuppressWarnings("unused")
    private static boolean checkLevelINFO(Level level) {
      if (level == Level.TRACE) {
        return false;
      }
      if (level == Level.DEBUG) {
        return false;
      }
      return true;
    }
    @SuppressWarnings("unused")
    private static boolean checkLevelWARNING(Level level) {
      if (level  == Level.ERROR) {
        return true;
      }
      if (level == Level.WARNING) {
        return true;
      }
      return false;
    }
    @SuppressWarnings("unused")
    private static boolean checkLevelERROR(Level level) {
      if (level == Level.ERROR) {
        return true;
      }
      return false;
    }
  }
  
  private static class MessageProviderCS extends MutableCallSite {
    private static final MethodHandle FALLBACK, IS_INSTANCE;
    static {
      Lookup lookup = lookup();
      try {
        FALLBACK = dropArguments(
            lookup.findVirtual(MessageProviderCS.class, "fallback", methodType(MethodHandle.class, Object.class, Object[].class)),
            0, Level.class, Throwable.class);
        IS_INSTANCE = lookup.findVirtual(Class.class, "isInstance", methodType(boolean.class, Object.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
    
    private final int maxParameters;
    private final MethodHandle print;
    
    MessageProviderCS(MethodType type, int maxParameters, MethodHandle print) {
      super(type);
      this.maxParameters = maxParameters;
      this.print = print;
      setTarget(foldArguments(
          exactInvoker(type()),
          insertArguments(FALLBACK, 2, this).asCollector(Object[].class, maxParameters)));
    }
    
    MethodHandle fallback(Object messageProvider, Object[] args) {
      Entry<Class<?>, MethodHandle> pair = findFunctionalInterfaceMH(messageProvider);
      Class<?> providerClass = pair.getKey();
      MethodHandle provider = pair.getValue();

      // check if the provider parameter count and the actual number of arguments match
      int actualSize = findActualSize(args);
      int providerArgumentCount = provider.type().parameterCount() - 1;
      if (actualSize != providerArgumentCount) {
        throw new IllegalArgumentException(new StringBuilder()
            .append("call mismatch, actual argument count ")
            .append(actualSize)
            .append(" for method type ")
            .append(provider.type().dropParameterTypes(0, 1))
            .toString());
      }
      
      // align signature of the provider with the log signature 
      if (providerArgumentCount != maxParameters) {
        provider = dropArguments(provider, provider.type().parameterCount(), nCopies(maxParameters - providerArgumentCount, Object.class));
      }
      provider = provider.asType(genericMethodType(provider.type().parameterCount()).changeReturnType(String.class));
      provider = dropArguments(provider, 0, Level.class, Throwable.class);
      
      // fold !
      MethodHandle target = foldArguments(print, provider);
      
      // create the inlining cache
      MethodHandle guard = guardWithTest(
          dropArguments(IS_INSTANCE.bindTo(providerClass), 0, Level.class, Throwable.class),
          target,
          new MessageProviderCS(type(), maxParameters, print).dynamicInvoker());
      setTarget(guard);
      
      return target;
    }
  }
  
  
  private static final List<Entry<Class<?>, MethodHandle>> MESSAGE_PROVIDERS = Arrays.asList(
      findVirtualMethod(Supplier.class,       "get",   methodType(String.class)),
      findVirtualMethod(IntFunction.class,    "apply", methodType(String.class, int.class)),
      findVirtualMethod(LongFunction.class,   "apply", methodType(String.class, long.class)),
      findVirtualMethod(DoubleFunction.class, "apply", methodType(String.class, double.class)),
      findVirtualMethod(Function.class,       "apply", methodType(String.class, Object.class)),
      findVirtualMethod(BiFunction.class,     "apply", methodType(String.class, Object.class, Object.class)),
      entry(String.class, identity(Object.class).asType(methodType(String.class, Object.class))));
  
  private static Map.Entry<Class<?>, MethodHandle> entry(Class<?> fun, MethodHandle mh) {
    return new SimpleImmutableEntry<>(fun, mh);
  }
  
  private static Entry<Class<?>, MethodHandle> findVirtualMethod(Class<?> fun, String name, MethodType type) {
    MethodHandle mh;
    try {
      mh = publicLookup().findVirtual(fun, name, type.erase());
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new AssertionError(e);
    }
    return entry(fun, mh.asType(type.insertParameterTypes(0, fun)));
  }
  
  static Entry<Class<?>, MethodHandle> findFunctionalInterfaceMH(Object messageProvider) {
    return MESSAGE_PROVIDERS.stream()
        .filter(entry -> entry.getKey().isInstance(messageProvider))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("unknown message provider type ".concat(messageProvider.getClass().getName())));
  }
  
  static int findActualSize(Object[] args) {
    for(int i = args.length; --i >= 0;) {
      if (args[i] != NONE) {
        return i + 1;
      }
    }
    return 0;
  }
  
  static UndeclaredThrowableException rethrow(Throwable e) {
    if (e instanceof RuntimeException) {
      throw (RuntimeException)e;
    }
    if (e instanceof Error) {
      throw (Error)e;
    }
    return new UndeclaredThrowableException(e);
  }
  
  static String packageName(Class<?> type) {
    if (type.isPrimitive()) {
      return "java.lang";
    }
    if (type.isArray()) {
      return packageName(type.getComponentType());
    }
    String name = type.getName();
    int index = name.lastIndexOf('.');
    return index == -1? "": name.substring(0, index);
  }
  
  enum LoggerConfigKind {
    CLASS(Class::getName),
    PACKAGE(type -> IS_JAVA_8? packageName(type): type.getPackageName()),
    MODULE(type -> IS_JAVA_8? null: type.getModule().getName());
    
    private final Function<Class<?>, String> nameExtractor;
    
    private LoggerConfigKind(Function<Class<?>, String> nameExtractor) {
      this.nameExtractor = nameExtractor;
    }
    
    String key(String name) {
      return new StringBuilder().append(name()).append(';').append(name).toString();
    }
    
    // may return null !
    String extractNameFromClass(Class<?> type) {
      return nameExtractor.apply(type);
    }
    
    static final LoggerConfigKind[] VALUES = values();
  }
  
  static class LoggerConfigFeature<T> {
    static final LoggerConfigFeature<Boolean> ENABLE_CONF = new LoggerConfigFeature<>(LoggerConfig::enable);
    static final LoggerConfigFeature<Level> LEVEL_CONF = new LoggerConfigFeature<>(LoggerConfig::level);
    static final LoggerConfigFeature<Boolean> LEVEL_OVERRIDE_CONF = new LoggerConfigFeature<>(LoggerConfig::levelOverride);
    static final LoggerConfigFeature<LogFacadeFactory> LOGFACADEFACTORY_CONF = new LoggerConfigFeature<>(LoggerConfig::logFacadeFactory);
    
    private final Function<LoggerConfigImpl, Optional<T>> extractor;
    
    private LoggerConfigFeature(Function<LoggerConfigImpl, Optional<T>> extractor) {
      this.extractor = extractor;
    }
    
    Optional<T> findValueAndCollectSwitchPoints(Class<?> type, Set<SwitchPoint> switchPoints) {
      for(LoggerConfigKind kind: LoggerConfigKind.VALUES) {
        String name = kind.extractNameFromClass(type);
        if (name == null) {  // unnamed module or no module (Java 8)
          continue;
        }
        LoggerConfigImpl loggerConfig = configFrom(kind, name);
        switchPoints.add(loggerConfig.switchPoint());
        Optional<T> value = extractor.apply(loggerConfig);
        if (value.isPresent()) {
          return value;
        }
      }
      return Optional.empty();
    }
  }
  
  static class LoggerConfigImpl implements LoggerConfig {
    class ConfigOptionImpl implements ConfigOption {
      @Override
      public ConfigOption enable(boolean enable) {
        LoggerConfigImpl.this.enable = enable;
        return this;
      } 
      @Override
      public ConfigOption level(Level level, boolean override) {
        LoggerConfigImpl.this.level = Objects.requireNonNull(level);
        LoggerConfigImpl.this.levelOverride = override;
        return this;
      }
      @Override
      public ConfigOption logFacadeFactory(LogFacadeFactory printFactory) {
        LoggerConfigImpl.this.logEventFactory = Objects.requireNonNull(printFactory);
        return this;
      }
    }
    
    private final Object lock = new Object();
    private SwitchPoint switchPoint;
    
    volatile Boolean enable;                   // nullable
    volatile Level level;                      // nullable
    volatile Boolean levelOverride;            // nullable 
    volatile LogFacadeFactory logEventFactory;  // nullable

    LoggerConfigImpl() {
      synchronized(lock) {
        this.switchPoint = new SwitchPoint();
      }
    }

    @Override
    public Optional<Boolean> enable() {
      return Optional.ofNullable(enable);
    }
    @Override
    public Optional<Level> level() {
      return Optional.ofNullable(level);
    }
    @Override
    public Optional<Boolean> levelOverride() {
      return Optional.ofNullable(levelOverride);
    }
    @Override
    public Optional<LogFacadeFactory> logFacadeFactory() {
      return Optional.ofNullable(logEventFactory);
    }
    
    SwitchPoint switchPoint() {
      synchronized (lock) {
        return switchPoint;  
      }
    }
    
    @Override
    public LoggerConfig update(Consumer<? super ConfigOption> configUpdater) {
      synchronized(lock) {
        configUpdater.accept(new ConfigOptionImpl());
        SwitchPoint.invalidateAll(new SwitchPoint[] { switchPoint });
        switchPoint = new SwitchPoint();
      }
      return this;
    }
  }
  
  private final static ConcurrentHashMap<String, LoggerConfigImpl> CONFIG =
      new ConcurrentHashMap<>();
  
  static LoggerConfigImpl configFrom(LoggerConfigKind kind, String name) {
    return CONFIG.computeIfAbsent(kind.key(name), __ ->  new LoggerConfigImpl());
  }
  
  private static final byte[] LOGGER_BYTECODE;
  static final Unsafe UNSAFE;
  private static final MethodHandle LOGGER_FACTORY;
  static {
    String data = "yv66vgAAADQAWgEAJ2NvbS9naXRodWIvZm9yYXgvYmVhdXRpZnVsbG9nZ2VyL0xvZ2dlcgcAAQEALGNvbS9naXRodWIvZm9yYXgvYmVhdXRpZnVsbG9nZ2VyL0xvZ2dlciRTdHViBwADAQAQamF2YS9sYW5nL09iamVjdAcABQEAAm1oAQAfTGphdmEvbGFuZy9pbnZva2UvTWV0aG9kSGFuZGxlOwEABjxpbml0PgEAIihMamF2YS9sYW5nL2ludm9rZS9NZXRob2RIYW5kbGU7KVYBAAMoKVYMAAkACwoABgAMDAAHAAgJAAQADgEABmNyZWF0ZQEASihMamF2YS9sYW5nL2ludm9rZS9NZXRob2RIYW5kbGU7KUxjb20vZ2l0aHViL2ZvcmF4L2JlYXV0aWZ1bGxvZ2dlci9Mb2dnZXI7DAAJAAoKAAQAEgEABWVycm9yAQAqKExqYXZhL2xhbmcvU3RyaW5nO0xqYXZhL2xhbmcvVGhyb3dhYmxlOylWAQAkTGphdmEvbGFuZy9pbnZva2UvTGFtYmRhRm9ybSRIaWRkZW47AQAoTGpkay9pbnRlcm5hbC92bS9hbm5vdGF0aW9uL0ZvcmNlSW5saW5lOwEALWNvbS9naXRodWIvZm9yYXgvYmVhdXRpZnVsbG9nZ2VyL0xvZ2dlciRMZXZlbAcAGAEABUVSUk9SAQAvTGNvbS9naXRodWIvZm9yYXgvYmVhdXRpZnVsbG9nZ2VyL0xvZ2dlciRMZXZlbDsMABoAGwkAGQAcAQArY29tL2dpdGh1Yi9mb3JheC9iZWF1dGlmdWxsb2dnZXIvTG9nZ2VySW1wbAcAHgEABE5PTkUBABJMamF2YS9sYW5nL09iamVjdDsMACAAIQkAHwAiAQAdamF2YS9sYW5nL2ludm9rZS9NZXRob2RIYW5kbGUHACQBAAtpbnZva2VFeGFjdAEAoShMY29tL2dpdGh1Yi9mb3JheC9iZWF1dGlmdWxsb2dnZXIvTG9nZ2VyJExldmVsO0xqYXZhL2xhbmcvVGhyb3dhYmxlO0xqYXZhL2xhbmcvT2JqZWN0O0xqYXZhL2xhbmcvT2JqZWN0O0xqYXZhL2xhbmcvT2JqZWN0O0xqYXZhL2xhbmcvT2JqZWN0O0xqYXZhL2xhbmcvT2JqZWN0OylWDAAmACcKACUAKAEAIChMamF2YS91dGlsL2Z1bmN0aW9uL1N1cHBsaWVyOylWAQA0KExqYXZhL3V0aWwvZnVuY3Rpb24vU3VwcGxpZXI8TGphdmEvbGFuZy9TdHJpbmc7PjspVgEAMihMamF2YS91dGlsL2Z1bmN0aW9uL0Z1bmN0aW9uO0xqYXZhL2xhbmcvT2JqZWN0OylWAQBRPFQ6TGphdmEvbGFuZy9PYmplY3Q7PihMamF2YS91dGlsL2Z1bmN0aW9uL0Z1bmN0aW9uPC1UVDtMamF2YS9sYW5nL1N0cmluZzs+O1RUOylWAQAkKExqYXZhL3V0aWwvZnVuY3Rpb24vSW50RnVuY3Rpb247SSlWAQA4KExqYXZhL3V0aWwvZnVuY3Rpb24vSW50RnVuY3Rpb248TGphdmEvbGFuZy9TdHJpbmc7PjtJKVYBABFqYXZhL2xhbmcvSW50ZWdlcgcAMAEAB3ZhbHVlT2YBABYoSSlMamF2YS9sYW5nL0ludGVnZXI7DAAyADMKADEANAEAJShMamF2YS91dGlsL2Z1bmN0aW9uL0xvbmdGdW5jdGlvbjtKKVYBADkoTGphdmEvdXRpbC9mdW5jdGlvbi9Mb25nRnVuY3Rpb248TGphdmEvbGFuZy9TdHJpbmc7PjtKKVYBAA5qYXZhL2xhbmcvTG9uZwcAOAEAEyhKKUxqYXZhL2xhbmcvTG9uZzsMADIAOgoAOQA7AQAnKExqYXZhL3V0aWwvZnVuY3Rpb24vRG91YmxlRnVuY3Rpb247RClWAQA7KExqYXZhL3V0aWwvZnVuY3Rpb24vRG91YmxlRnVuY3Rpb248TGphdmEvbGFuZy9TdHJpbmc7PjtEKVYBABBqYXZhL2xhbmcvRG91YmxlBwA/AQAVKEQpTGphdmEvbGFuZy9Eb3VibGU7DAAyAEEKAEAAQgEARihMamF2YS91dGlsL2Z1bmN0aW9uL0JpRnVuY3Rpb247TGphdmEvbGFuZy9PYmplY3Q7TGphdmEvbGFuZy9PYmplY3Q7KVYBAG48VDpMamF2YS9sYW5nL09iamVjdDtVOkxqYXZhL2xhbmcvT2JqZWN0Oz4oTGphdmEvdXRpbC9mdW5jdGlvbi9CaUZ1bmN0aW9uPC1UVDstVFU7TGphdmEvbGFuZy9TdHJpbmc7PjtUVDtUVTspVgEAB3dhcm5pbmcB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";
    LOGGER_BYTECODE = Base64.getDecoder().decode(data);
    
    Lookup lookup = MethodHandles.lookup();
    try {
      Field field = Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      UNSAFE = (Unsafe)field.get(null);
      LOGGER_FACTORY = createFactory(lookup, loggerClass(Logger.class, null));
    } catch (NoSuchFieldException | IllegalAccessException | IllegalStateException e) {
      throw new AssertionError(e);
    }
  }
  
  static Class<?> loggerClass(Class<?> hostClass, Object[] patches) {
    return UNSAFE.defineAnonymousClass(hostClass, LOGGER_BYTECODE, patches);
  }
  
  static MethodHandle createFactory(Lookup lookup, Class<?> loggerClass) {
    try {
      return lookup.findStatic(loggerClass, "create", methodType(Logger.class, MethodHandle.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }
  
  static Logger createLogger(MethodHandle mh) {
    try {
      return (Logger)LOGGER_FACTORY.invokeExact(mh);
    } catch (Throwable e) {
      throw rethrow(e);
    }
  }
  
  static class SystemLoggerFactoryImpl {
    static final MethodHandle SYSTEM_LOGGER;
    static {
      Lookup lookup = MethodHandles.lookup();
      MethodHandle mh, filter;
      try {
        mh = lookup.findVirtual(System.Logger.class, "log",
            methodType(void.class, System.Logger.Level.class, String.class, Throwable.class));
        filter = lookup.findStatic(SystemLoggerFactoryImpl.class, "level",
            methodType(System.Logger.Level.class, Level.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
      mh = filterArguments(mh, 1, filter);
      SYSTEM_LOGGER = permuteArguments(mh,
          MethodType.methodType(void.class, System.Logger.class, String.class, Level.class, Throwable.class),
          new int[] { 0, 2, 1, 3});
    }
    
    @SuppressWarnings("unused")
    private static System.Logger.Level level(Level level) {
      // do not use a switch here, we want this code to be inlined !
      if (level == Level.ERROR) {
        return System.Logger.Level.ERROR;
      }
      if (level == Level.WARNING) {
        return System.Logger.Level.WARNING;
      }
      if (level == Level.INFO) {
        return System.Logger.Level.INFO;
      }
      if (level == Level.DEBUG) {
        return System.Logger.Level.DEBUG;
      }
      if (level == Level.TRACE) {
        return System.Logger.Level.TRACE;
      }
      throw newIllegalStateException();
    }
  }
  
  static class Log4JFactoryImpl implements LogFacade {
    private static final MethodHandle LOG4J_LOGGER;
    static {
      Lookup lookup = MethodHandles.lookup();
      MethodHandle mh, filter;
      try {
        mh = lookup.findVirtual(org.apache.logging.log4j.Logger.class, "log",
            methodType(void.class, org.apache.logging.log4j.Level.class, Object.class, Throwable.class));
        mh = mh.asType(methodType(void.class, org.apache.logging.log4j.Logger.class, org.apache.logging.log4j.Level.class, String.class, Throwable.class));
        filter = lookup.findStatic(Log4JFactoryImpl.class, "level",
            methodType(org.apache.logging.log4j.Level.class, Level.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
      mh = filterArguments(mh, 1, filter);
      LOG4J_LOGGER = permuteArguments(mh,
          methodType(void.class, org.apache.logging.log4j.Logger.class, String.class, Level.class, Throwable.class),
          new int[] { 0, 2, 1, 3});
    }
    
    private static org.apache.logging.log4j.Level level(Level level) {
      // do not use a switch here, we want this code to be inlined !
      if (level == Level.ERROR) {
        return org.apache.logging.log4j.Level.ERROR;
      }
      if (level == Level.WARNING) {
        return org.apache.logging.log4j.Level.WARN;
      }
      if (level == Level.INFO) {
        return org.apache.logging.log4j.Level.INFO;
      }
      if (level == Level.DEBUG) {
        return org.apache.logging.log4j.Level.DEBUG;
      }
      if (level == Level.TRACE) {
        return org.apache.logging.log4j.Level.TRACE;
      }
      throw newIllegalStateException();
    }

    private final org.apache.logging.log4j.Logger logger;
    
    Log4JFactoryImpl(org.apache.logging.log4j.Logger logger) {
      this.logger = logger;
    }

    @Override
    public MethodHandle getLogMethodHandle() {
      return LOG4J_LOGGER.bindTo(logger);
    }
    
    @Override
    public void overrideLevel(Level level) {
      String name = logger.getName();
      try {
        Class<?> configuratorClass = Class.forName("org.apache.logging.log4j.core.config.Configurator");
        Method setLevel = configuratorClass.getMethod("setLevel", String.class, org.apache.logging.log4j.Level.class);
        setLevel.invoke(null, name, level(level));
      } catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        throw new UnsupportedOperationException("can not override the level of Log4J logger " + name, e);
      }
    }
  }
  
  static class SLF4JFactoryImpl {
    static final MethodHandle SLF4J_LOGGER;
    private static final MethodHandle ERROR_MH, WARNING_MH, INFO_MH, DEBUG_MH, TRACE_MH; 
    static {
      Lookup lookup = MethodHandles.lookup();
      ERROR_MH = mh(lookup, "error");
      WARNING_MH = mh(lookup, "warn");
      INFO_MH = mh(lookup, "info");
      DEBUG_MH = mh(lookup, "debug");
      TRACE_MH = mh(lookup, "trace");
      
      MethodHandle level;
      try {
        level = lookup.findStatic(SLF4JFactoryImpl.class, "levelMH",
            methodType(MethodHandle.class, org.slf4j.Logger.class, String.class, Level.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
      
      MethodHandle invoker = exactInvoker(methodType(void.class, org.slf4j.Logger.class, String.class, Throwable.class));
      MethodHandle mh = dropArguments(invoker, 3, Level.class);
      SLF4J_LOGGER = foldArguments(mh, level);
    }
    
    private static MethodHandle mh(Lookup lookup, String name) {
      try {
        return lookup.findVirtual(org.slf4j.Logger.class, name, methodType(void.class, String.class, Throwable.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
    
    @SuppressWarnings("unused")
    private static MethodHandle levelMH(org.slf4j.Logger logger, String message, Level level) {
      // do not use a switch here, we want this code to be inlined !
      if (level == Level.ERROR) {
        return ERROR_MH;
      }
      if (level == Level.WARNING) {
        return WARNING_MH;
      }
      if (level == Level.INFO) {
        return INFO_MH;
      }
      if (level == Level.DEBUG) {
        return DEBUG_MH;
      }
      if (level == Level.TRACE) {
        return TRACE_MH;
      }
      throw newIllegalStateException();
    }
  }
  
  static class LogbackFactoryImpl implements LogFacade {
    private static final MethodHandle LOGBACK_LOGGER;
    private static final MethodHandle ERROR_MH, WARNING_MH, INFO_MH, DEBUG_MH, TRACE_MH; 
    static {
      Lookup lookup = MethodHandles.lookup();
      ERROR_MH = mh(lookup, "error");
      WARNING_MH = mh(lookup, "warn");
      INFO_MH = mh(lookup, "info");
      DEBUG_MH = mh(lookup, "debug");
      TRACE_MH = mh(lookup, "trace");
      
      MethodHandle level;
      try {
        level = lookup.findStatic(LogbackFactoryImpl.class, "levelMH",
            methodType(MethodHandle.class, ch.qos.logback.classic.Logger.class, String.class, Level.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
      
      MethodHandle invoker = exactInvoker(methodType(void.class, ch.qos.logback.classic.Logger.class, String.class, Throwable.class));
      MethodHandle mh = dropArguments(invoker, 3, Level.class);
      LOGBACK_LOGGER = foldArguments(mh, level);
    }
    
    private static MethodHandle mh(Lookup lookup, String name) {
      try {
        return lookup.findVirtual(ch.qos.logback.classic.Logger.class, name, methodType(void.class, String.class, Throwable.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
    
    @SuppressWarnings("unused")
    private static MethodHandle levelMH(ch.qos.logback.classic.Logger logger, String message, Level level) {
      // do not use a switch here, we want this code to be inlined !
      if (level == Level.ERROR) {
        return ERROR_MH;
      }
      if (level == Level.WARNING) {
        return WARNING_MH;
      }
      if (level == Level.INFO) {
        return INFO_MH;
      }
      if (level == Level.DEBUG) {
        return DEBUG_MH;
      }
      if (level == Level.TRACE) {
        return TRACE_MH;
      }
      throw newIllegalStateException();
    }
    
    private final ch.qos.logback.classic.Logger logger;
    
    LogbackFactoryImpl(ch.qos.logback.classic.Logger logger) {
      this.logger = logger;
    }
    
    @Override
    public MethodHandle getLogMethodHandle() {
      return LOGBACK_LOGGER.bindTo(logger);
    }
    
    
    private static ch.qos.logback.classic.Level level(Level level) {
      // not used in the fast path, so a switch is OK here !
      switch(level) {
      case ERROR:
        return ch.qos.logback.classic.Level.ERROR;
      case WARNING:
        return ch.qos.logback.classic.Level.WARN;
      case INFO:
        return ch.qos.logback.classic.Level.INFO;
      case DEBUG:
        return ch.qos.logback.classic.Level.DEBUG;
      case TRACE:
        return ch.qos.logback.classic.Level.TRACE;
       default:
         throw newIllegalStateException();
      }
    }
    
    @Override
    public void overrideLevel(Level level) {
      logger.setLevel(level(level));
    }
  }
  
  static class JULFactoryImpl implements LogFacade {
    private static final MethodHandle JUL_LOGGER;
    static {
      Lookup lookup = MethodHandles.lookup();
      MethodHandle mh, filter;
      try {
        mh = lookup.findVirtual(java.util.logging.Logger.class, "log",
            methodType(void.class, java.util.logging.Level.class, String.class, Throwable.class));
        filter = lookup.findStatic(JULFactoryImpl.class, "level",
            methodType(java.util.logging.Level.class, Level.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
      mh = filterArguments(mh, 1, filter);
      JUL_LOGGER = permuteArguments(mh,
          methodType(void.class, java.util.logging.Logger.class, String.class, Level.class, Throwable.class),
          new int[] { 0, 2, 1, 3});
    }
    
    private static java.util.logging.Level level(Level level) {
      // do not use a switch here, we want this code to be inlined !
      if (level == Level.ERROR) {
        return java.util.logging.Level.SEVERE;
      }
      if (level == Level.WARNING) {
        return java.util.logging.Level.WARNING;
      }
      if (level == Level.INFO) {
        return java.util.logging.Level.INFO;
      }
      if (level == Level.DEBUG) {
        return java.util.logging.Level.FINE;
      }
      if (level == Level.TRACE) {
        return java.util.logging.Level.FINER;
      }
      throw newIllegalStateException();
    }
    
    private final java.util.logging.Logger logger;

    JULFactoryImpl(java.util.logging.Logger logger) {
      this.logger = logger;
    }

    @Override
    public MethodHandle getLogMethodHandle() {
      return LoggerImpl.JULFactoryImpl.JUL_LOGGER.bindTo(logger);
    }
    
    @Override
    public void overrideLevel(Level level) {
      logger.setLevel(level(level));
    }
  }
  
  static IllegalStateException newIllegalStateException() {
    return new IllegalStateException("unknown level");
  }
  
  static class LogServiceImpl {
    private static final ClassValue<Class<?>> SERVICE_IMPLS = new ClassValue<>() {
      @Override
      protected Class<?> computeValue(Class<?> type) {
        return LoggerImpl.loggerClass(type, new Object[] { null, type.getName().replace('.', '/') });
      }
    };
    
    static Object getService(Lookup lookup, Class<?> serviceInterface, Class<?> configClass) {
      Class<?> implClass = SERVICE_IMPLS.get(serviceInterface);
      MethodHandle factory = createFactory(lookup, implClass);
      MethodHandle mh = getLoggingMethodHandle(configClass, 4);
      Object service;
      try {
        service = factory.invoke(mh);
      } catch (Throwable e) {
        throw LoggerImpl.rethrow(e);
      }
      return service;
    }
  }
}
