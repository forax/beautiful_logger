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
import java.lang.invoke.MethodHandles.Lookup.ClassOption;
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
      //FIXME
      //try {
        //Class<?> reflectionSupplier = UNSAFE.defineAnonymousClass(LoggerImpl.class, Base64.getDecoder().decode(s), null);
        //supplier = (Supplier<?>) reflectionSupplier.getConstructor().newInstance();
      //} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      //  throw new AssertionError(e);
      //}
      supplier = () -> { throw new UnsupportedOperationException("not supported"); };
      GET_CALLER_CLASS = supplier;
    }

    static Class<?> getCallerClass() {
      return (Class<?>) GET_CALLER_CLASS.get();
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
  
  private static final ConcurrentHashMap<String, LoggerConfigImpl> CONFIG =
      new ConcurrentHashMap<>();
  
  static LoggerConfigImpl configFrom(LoggerConfigKind kind, String name) {
    return CONFIG.computeIfAbsent(kind.key(name), __ ->  new LoggerConfigImpl());
  }

  static final Object UNSAFE;
  static {
    Object unsafe;
    try {
      // jdk.unsupported module can b not available
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");

      try {
        Field field = unsafeClass.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = field.get(null);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new AssertionError(e);
      }
    } catch (ClassNotFoundException e) {
      unsafe = null;
    }
    UNSAFE = unsafe;
  }

  private static byte[] loggerFactoryBytecode() {
    String data = "yv66vgAAADQAWwEAJ2NvbS9naXRodWIvZm9yYXgvYmVhdXRpZnVsbG9nZ2VyL0xvZ2dlcgcAAQEALGNvbS9naXRodWIvZm9yYXgvYmVhdXRpZnVsbG9nZ2VyL0xvZ2dlciRTdHViBwADAQAQamF2YS9sYW5nL09iamVjdAcABQEAAm1oAQAfTGphdmEvbGFuZy9pbnZva2UvTWV0aG9kSGFuZGxlOwEABjxpbml0PgEAIihMamF2YS9sYW5nL2ludm9rZS9NZXRob2RIYW5kbGU7KVYBAAMoKVYMAAkACwoABgAMDAAHAAgJAAQADgEABmNyZWF0ZQEASihMamF2YS9sYW5nL2ludm9rZS9NZXRob2RIYW5kbGU7KUxjb20vZ2l0aHViL2ZvcmF4L2JlYXV0aWZ1bGxvZ2dlci9Mb2dnZXI7DAAJAAoKAAQAEgEABWVycm9yAQAqKExqYXZhL2xhbmcvU3RyaW5nO0xqYXZhL2xhbmcvVGhyb3dhYmxlOylWAQAkTGphdmEvbGFuZy9pbnZva2UvTGFtYmRhRm9ybSRIaWRkZW47AQAjTGpkay9pbnRlcm5hbC92bS9hbm5vdGF0aW9uL0hpZGRlbjsBAChMamRrL2ludGVybmFsL3ZtL2Fubm90YXRpb24vRm9yY2VJbmxpbmU7AQAtY29tL2dpdGh1Yi9mb3JheC9iZWF1dGlmdWxsb2dnZXIvTG9nZ2VyJExldmVsBwAZAQAFRVJST1IBAC9MY29tL2dpdGh1Yi9mb3JheC9iZWF1dGlmdWxsb2dnZXIvTG9nZ2VyJExldmVsOwwAGwAcCQAaAB0BACtjb20vZ2l0aHViL2ZvcmF4L2JlYXV0aWZ1bGxvZ2dlci9Mb2dnZXJJbXBsBwAfAQAETk9ORQEAEkxqYXZhL2xhbmcvT2JqZWN0OwwAIQAiCQAgACMBAB1qYXZhL2xhbmcvaW52b2tlL01ldGhvZEhhbmRsZQcAJQEAC2ludm9rZUV4YWN0AQChKExjb20vZ2l0aHViL2ZvcmF4L2JlYXV0aWZ1bGxvZ2dlci9Mb2dnZXIkTGV2ZWw7TGphdmEvbGFuZy9UaHJvd2FibGU7TGphdmEvbGFuZy9PYmplY3Q7TGphdmEvbGFuZy9PYmplY3Q7TGphdmEvbGFuZy9PYmplY3Q7TGphdmEvbGFuZy9PYmplY3Q7TGphdmEvbGFuZy9PYmplY3Q7KVYMACcAKAoAJgApAQAgKExqYXZhL3V0aWwvZnVuY3Rpb24vU3VwcGxpZXI7KVYBADQoTGphdmEvdXRpbC9mdW5jdGlvbi9TdXBwbGllcjxMamF2YS9sYW5nL1N0cmluZzs+OylWAQAyKExqYXZhL3V0aWwvZnVuY3Rpb24vRnVuY3Rpb247TGphdmEvbGFuZy9PYmplY3Q7KVYBAFE8VDpMamF2YS9sYW5nL09iamVjdDs+KExqYXZhL3V0aWwvZnVuY3Rpb24vRnVuY3Rpb248LVRUO0xqYXZhL2xhbmcvU3RyaW5nOz47VFQ7KVYBACQoTGphdmEvdXRpbC9mdW5jdGlvbi9JbnRGdW5jdGlvbjtJKVYBADgoTGphdmEvdXRpbC9mdW5jdGlvbi9JbnRGdW5jdGlvbjxMamF2YS9sYW5nL1N0cmluZzs+O0kpVgEAEWphdmEvbGFuZy9JbnRlZ2VyBwAxAQAHdmFsdWVPZgEAFihJKUxqYXZhL2xhbmcvSW50ZWdlcjsMADMANAoAMgA1AQAlKExqYXZhL3V0aWwvZnVuY3Rpb24vTG9uZ0Z1bmN0aW9uO0opVgEAOShMamF2YS91dGlsL2Z1bmN0aW9uL0xvbmdGdW5jdGlvbjxMamF2YS9sYW5nL1N0cmluZzs+O0opVgEADmphdmEvbGFuZy9Mb25nBwA5AQATKEopTGphdmEvbGFuZy9Mb25nOwwAMwA7CgA6ADwBACcoTGphdmEvdXRpbC9mdW5jdGlvbi9Eb3VibGVGdW5jdGlvbjtEKVYBADsoTGphdmEvdXRpbC9mdW5jdGlvbi9Eb3VibGVGdW5jdGlvbjxMamF2YS9sYW5nL1N0cmluZzs+O0QpVgEAEGphdmEvbGFuZy9Eb3VibGUHAEABABUoRClMamF2YS9sYW5nL0RvdWJsZTsMADMAQgoAQQBDAQBGKExqYXZhL3V0aWwvZnVuY3Rpb24vQmlGdW5jdGlvbjtMamF2YS9sYW5nL09iamVjdDtMamF2YS9sYW5nL09iamVjdDspVgEAbjxUOkxqYXZhL2xhbmcvT2JqZWN0O1U6TGphdmEvbGFuZy9PYmplY3Q7PihMamF2YS91dGlsL2Z1bmN0aW9uL0JpRnVuY3Rpb248LVRUOy1UVTtMamF2YS9sYW5nL1N0cmluZzs+O1RUO1RVOylWAQAHd2FybmluZwEAB1dBUk5JTkcMAEgAHAkAGgBJAQAEaW5mbwEABElORk8MAEwAHAkAGgBNAQAFZGVidWcBAAVERUJVRwwAUAAcCQAaAFEBAAV0cmFjZQEABVRSQUNFDABUABwJABoAVQEAA2xvZwEABENvZGUBABlSdW50aW1lVmlzaWJsZUFubm90YXRpb25zAQAJU2lnbmF0dXJlACAABAAGAAEAAgABABIABwAIAAAAJgACAAkACgABAFgAAAAWAAIAAgAAAAoqtwANKiu1AA+xAAAAAAAJABAAEQABAFgAAAAVAAMAAQAAAAm7AARZKrcAE7AAAAAAAAEAFAAVAAIAWAAAACUACAADAAAAGSq0AA+yAB4sK7IAJLIAJLIAJLIAJLYAKrEAAAAAAFkAAAAOAAMAFgAAABcAAAAYAAAAAQAUACsAAwBYAAAAJQAIAAIAAAAZKrQAD7IAHgErsgAksgAksgAksgAktgAqsQAAAAAAWgAAAAIALABZAAAADgADABYAAAAXAAAAGAAAAAEAFAAtAAMAWAAAACMACAADAAAAFyq0AA+yAB4BKyyyACSyACSyACS2ACqxAAAAAABaAAAAAgAuAFkAAAAOAAMAFgAAABcAAAAYAAAAAQAUAC8AAwBYAAAAJgAIAAMAAAAaKrQAD7IAHgErHLgANrIAJLIAJLIAJLYAKrEAAAAAAFoAAAACADAAWQAAAA4AAwAWAAAAFwAAABgAAAABABQANwADAFgAAAAmAAgABAAAABoqtAAPsgAeASsguAA9sgAksgAksgAktgAqsQAAAAAAWgAAAAIAOABZAAAADgADABYAAAAXAAAAGAAAAAEAFAA+AAMAWAAAACYACAAEAAAAGiq0AA+yAB4BKyi4AESyACSyACSyACS2ACqxAAAAAABaAAAAAgA/AFkAAAAOAAMAFgAAABcAAAAYAAAAAQAUAEUAAwBYAAAAIQAIAAQAAAAVKrQAD7IAHgErLC2yACSyACS2ACqxAAAAAABaAAAAAgBGAFkAAAAOAAMAFgAAABcAAAAYAAAAAQBHABUAAgBYAAAAJQAIAAMAAAAZKrQAD7IASiwrsgAksgAksgAksgAktgAqsQAAAAAAWQAAAA4AAwAWAAAAFwAAABgAAAABAEcAKwADAFgAAAAlAAgAAgAAABkqtAAPsgBKASuyACSyACSyACSyACS2ACqxAAAAAABaAAAAAgAsAFkAAAAOAAMAFgAAABcAAAAYAAAAAQBHAC0AAwBYAAAAIwAIAAMAAAAXKrQAD7IASgErLLIAJLIAJLIAJLYAKrEAAAAAAFoAAAACAC4AWQAAAA4AAwAWAAAAFwAAABgAAAABAEcALwADAFgAAAAmAAgAAwAAABoqtAAPsgBKASscuAA2sgAksgAksgAktgAqsQAAAAAAWgAAAAIAMABZAAAADgADABYAAAAXAAAAGAAAAAEARwA3AAMAWAAAACYACAAEAAAAGiq0AA+yAEoBKyC4AD2yACSyACSyACS2ACqxAAAAAABaAAAAAgA4AFkAAAAOAAMAFgAAABcAAAAYAAAAAQBHAD4AAwBYAAAAJgAIAAQAAAAaKrQAD7IASgErKLgARLIAJLIAJLIAJLYAKrEAAAAAAFoAAAACAD8AWQAAAA4AAwAWAAAAFwAAABgAAAABAEcARQADAFgAAAAhAAgABAAAABUqtAAPsgBKASssLbIAJLIAJLYAKrEAAAAAAFoAAAACAEYAWQAAAA4AAwAWAAAAFwAAABgAAAABAEsAFQACAFgAAAAlAAgAAwAAABkqtAAPsgBOLCuyACSyACSyACSyACS2ACqxAAAAAABZAAAADgADABYAAAAXAAAAGAAAAAEASwArAAMAWAAAACUACAACAAAAGSq0AA+yAE4BK7IAJLIAJLIAJLIAJLYAKrEAAAAAAFoAAAACACwAWQAAAA4AAwAWAAAAFwAAABgAAAABAEsALQADAFgAAAAjAAgAAwAAABcqtAAPsgBOASsssgAksgAksgAktgAqsQAAAAAAWgAAAAIALgBZAAAADgADABYAAAAXAAAAGAAAAAEASwAvAAMAWAAAACYACAADAAAAGiq0AA+yAE4BKxy4ADayACSyACSyACS2ACqxAAAAAABaAAAAAgAwAFkAAAAOAAMAFgAAABcAAAAYAAAAAQBLADcAAwBYAAAAJgAIAAQAAAAaKrQAD7IATgErILgAPbIAJLIAJLIAJLYAKrEAAAAAAFoAAAACADgAWQAAAA4AAwAWAAAAFwAAABgAAAABAEsAPgADAFgAAAAmAAgABAAAABoqtAAPsgBOASsouABEsgAksgAksgAktgAqsQAAAAAAWgAAAAIAPwBZAAAADgADABYAAAAXAAAAGAAAAAEASwBFAAMAWAAAACEACAAEAAAAFSq0AA+yAE4BKywtsgAksgAktgAqsQAAAAAAWgAAAAIARgBZAAAADgADABYAAAAXAAAAGAAAAAEATwAVAAIAWAAAACUACAADAAAAGSq0AA+yAFIsK7IAJLIAJLIAJLIAJLYAKrEAAAAAAFkAAAAOAAMAFgAAABcAAAAYAAAAAQBPACsAAwBYAAAAJQAIAAIAAAAZKrQAD7IAUgErsgAksgAksgAksgAktgAqsQAAAAAAWgAAAAIALABZAAAADgADABYAAAAXAAAAGAAAAAEATwAtAAMAWAAAACMACAADAAAAFyq0AA+yAFIBKyyyACSyACSyACS2ACqxAAAAAABaAAAAAgAuAFkAAAAOAAMAFgAAABcAAAAYAAAAAQBPAC8AAwBYAAAAJgAIAAMAAAAaKrQAD7IAUgErHLgANrIAJLIAJLIAJLYAKrEAAAAAAFoAAAACADAAWQAAAA4AAwAWAAAAFwAAABgAAAABAE8ANwADAFgAAAAmAAgABAAAABoqtAAPsgBSASsguAA9sgAksgAksgAktgAqsQAAAAAAWgAAAAIAOABZAAAADgADABYAAAAXAAAAGAAAAAEATwA+AAMAWAAAACYACAAEAAAAGiq0AA+yAFIBKyi4AESyACSyACSyACS2ACqxAAAAAABaAAAAAgA/AFkAAAAOAAMAFgAAABcAAAAYAAAAAQBPAEUAAwBYAAAAIQAIAAQAAAAVKrQAD7IAUgErLC2yACSyACS2ACqxAAAAAABaAAAAAgBGAFkAAAAOAAMAFgAAABcAAAAYAAAAAQBTABUAAgBYAAAAJQAIAAMAAAAZKrQAD7IAViwrsgAksgAksgAksgAktgAqsQAAAAAAWQAAAA4AAwAWAAAAFwAAABgAAAABAFMAKwADAFgAAAAlAAgAAgAAABkqtAAPsgBWASuyACSyACSyACSyACS2ACqxAAAAAABaAAAAAgAsAFkAAAAOAAMAFgAAABcAAAAYAAAAAQBTAC0AAwBYAAAAIwAIAAMAAAAXKrQAD7IAVgErLLIAJLIAJLIAJLYAKrEAAAAAAFoAAAACAC4AWQAAAA4AAwAWAAAAFwAAABgAAAABAFMALwADAFgAAAAmAAgAAwAAABoqtAAPsgBWASscuAA2sgAksgAksgAktgAqsQAAAAAAWgAAAAIAMABZAAAADgADABYAAAAXAAAAGAAAAAEAUwA3AAMAWAAAACYACAAEAAAAGiq0AA+yAFYBKyC4AD2yACSyACSyACS2ACqxAAAAAABaAAAAAgA4AFkAAAAOAAMAFgAAABcAAAAYAAAAAQBTAD4AAwBYAAAAJgAIAAQAAAAaKrQAD7IAVgErKLgARLIAJLIAJLIAJLYAKrEAAAAAAFoAAAACAD8AWQAAAA4AAwAWAAAAFwAAABgAAAABAFMARQADAFgAAAAhAAgABAAAABUqtAAPsgBWASssLbIAJLIAJLYAKrEAAAAAAFoAAAACAEYAWQAAAA4AAwAWAAAAFwAAABgAAAABAFcAKAACAFgAAAAfAAgACAAAABMqtAAPKywtGQQZBRkGGQe2ACqxAAAAAABZAAAADgADABYAAAAXAAAAGAAAAAA=";
    return Base64.getDecoder().decode(data);
  }

  private static MethodHandle defineAnonymousClass(Class<?> unsafeClass) {
    Lookup lookup = MethodHandles.lookup();
    try {
      return lookup.findStatic(unsafeClass, "defineAnonymousClass", methodType(Class.class, Class.class, byte[].class, Object[].class));
    } catch (NoSuchMethodException e) {
      return null;  // not found
    }catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  private static MethodHandle defineLoggerFactoryUsingUnsafe(MethodHandle defineAnonymousClass) {
    Class<?> stubClass;
    try {
      stubClass = (Class<?>) defineAnonymousClass.invoke(UNSAFE, null, loggerFactoryBytecode(), null);
    } catch (Throwable e) {
      throw new AssertionError(e);
    }

    Lookup lookup = MethodHandles.lookup();
    try {
      return lookup.findStatic(stubClass, "create", methodType(Logger.class, MethodHandle.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  private static MethodHandle defineLoggerFactoryUsingHiddenClass() {
    Lookup lookup = MethodHandles.lookup();
    Lookup loggerClassLookup;
    try {
      loggerClassLookup = lookup.defineHiddenClass(loggerFactoryBytecode(), true, ClassOption.NESTMATE, ClassOption.STRONG);
    } catch (IllegalAccessException | IllegalStateException e) {
      throw new AssertionError(e);
    }
    try {
      return lookup.findStatic(loggerClassLookup.lookupClass(), "create", methodType(Logger.class, MethodHandle.class));
    } catch (NoSuchMethodException | IllegalAccessException | IllegalStateException e) {
      throw new AssertionError(e);
    }
  }

  private static final MethodHandle LOGGER_FACTORY;
  static {
    MethodHandle loggerFactory;
    MethodHandle defineAnonymousClass;
    if (UNSAFE != null && (defineAnonymousClass = defineAnonymousClass(UNSAFE.getClass())) != null) {
      loggerFactory = defineLoggerFactoryUsingUnsafe(defineAnonymousClass);
    } else {
      loggerFactory = defineLoggerFactoryUsingHiddenClass();
    }
    LOGGER_FACTORY = loggerFactory;
  }

  /*
  static Class<?> loggerClass(Class<?> hostClass, Object[] patches) {
    return UNSAFE.defineAnonymousClass(hostClass, LOGGER_BYTECODE, patches);
  }

  static MethodHandle createFactory(Lookup lookup, Class<?> loggerClass) {
    try {
      return lookup.findStatic(loggerClass, "create", methodType(Logger.class, MethodHandle.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }*/

  static Logger createLogger(MethodHandle mh) {
    try {
      return (Logger) LOGGER_FACTORY.invokeExact(mh);
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
          methodType(void.class, System.Logger.class, String.class, Level.class, Throwable.class),
          0, 2, 1, 3);
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
          0, 2, 1, 3);
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
      return switch (level) {
        case ERROR -> ch.qos.logback.classic.Level.ERROR;
        case WARNING -> ch.qos.logback.classic.Level.WARN;
        case INFO -> ch.qos.logback.classic.Level.INFO;
        case DEBUG -> ch.qos.logback.classic.Level.DEBUG;
        case TRACE -> ch.qos.logback.classic.Level.TRACE;
        default -> throw newIllegalStateException();
      };
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
          0, 2, 1, 3);
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
}
