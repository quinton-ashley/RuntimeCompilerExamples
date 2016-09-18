import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

//import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * RuntimeCompilerExample is an example class that uses stackoverflow 
 * user Marco13's RuntimeCompiler utility class to calculate 360 degree
 * positions from user inputed equations given a radius.
 * <br><br>
 * This example is intended for Java programmers who already have an 
 * advanced knowledge of data structures, classes in Java, and Exceptions.
 */
public class RuntimeCompilerExample {
  static double radius;
  static float[] positions;
  static String eqPos;
  static Map<String, Method> methods = new LinkedHashMap<String, Method>();
  static RuntimeCompiler r = new RuntimeCompiler();

  public static void main(String[] args) {
    System.out.println(

      "This program demonstrates how to use \n"+
      "stackoverflow user Marco13's RuntimeCompiler \n"+
      "class to solve user inputted parametric \n"+
      "equations.  His class requires no third \n"+
      "party libraries, but it does need to run \n"+
      "with the JDK. \n\n"+

		  "This is a barebones, text only, pure Java \n"+
		  "example.  Check out my Processing example \n"+
			"that plots parametric equations for a \n"+
			"visual example! \n\n"+

			"Use 'X' for the angle and 'R' for radius. \n"+
			"Type Math functions in lowercase letters. \n"+
			"Type 'Q' to quit. \n\n"+

			"Example of equation input: \n"+
			"$ cos(X) * R"

    );

    radius = 100.0;
    createPositions("cos(X)*R");
    printPositions(positions);

    System.out.println("\nExample of radius input: \n$ R = 50");
    radius = 50;
    createPositions(eqPos);
    printPositions(positions);
    System.out.println("");

    Scanner input = new Scanner(System.in);
    String temp;
    System.out.print("$ ");
    while(!(temp = input.nextLine()).equals("Q")) {
      if (temp.startsWith("R =")) {
        radius = Double.parseDouble( temp.replaceAll("R =", "").trim() );
        createPositions(eqPos);
      } else {
        createPositions( (temp.equals(" ") || temp.equals(null))?"cos(X)*R":temp );
      }
      printPositions(positions);
      System.out.print("$ ");
    }
    System.out.println("Bye!");
    input.close();
  }

  public static void printPositions(float[] positions) {
    for (int i = 0; i < positions.length; i++) {
      System.out.print(positions[i]+", ");
    }
    System.out.println("");
  }

  public static void createPositions(String equation) {
    // checks to see if an equation was entered
    if (equation != null && equation.equals(" ")) {
      equation = null;
    }

    // if not the default equation is used
    String eq = (equation == null)?"cos(X)*R":equation;

    // equation is converted into a class name
    String className = equationToClassName(eq);

    // if the class is not found in the methods Map...
    if (methods.get(className) == null) {
      // ...then it will be put in the Map, unless the equation is invalid
      methods.putIfAbsent(className, calculator(className, equationToJava(eq)));
      if (methods.get(className) == null) {
        System.out.println("invalid postion equation "+eq);
        return;
      }
      System.out.println("class "+className+" created");
    } else { // ...else the class is already stored in the methods Map
      System.out.println(className+" already stored in methods Map!");
    }
    try { // invoke the method with radius
      positions = ((float[]) methods.get(className).invoke(null, (Double) radius));
      // if the positions are successfully created the equation is saved
      // for calls that just change the radius
      eqPos = equation;
    } catch (Exception e) {
      System.out.println("fatal error creating positions");
      System.exit(0);
    }
  }

  private static String equationToJava(String equation) {
    return equation.replaceAll("cos", "Math.cos")
        .replaceAll("sin", "Math.sin")
        .replaceAll("tan", "Math.tan")
        .replaceAll("acos", "Math.acos")
        .replaceAll("asin", "Math.asin")
        .replaceAll("atan", "Math.atan")
        .replaceAll("abs", "Math.abs")
        .replaceAll("log", "Math.log")
        .replaceAll("log10", "Math.log10")
        .replaceAll("sqrt", "Math.sqrt")
        .replaceAll("cbrt", "Math.cbrt")
        .replaceAll("exp", "Math.exp")
        .replaceAll("pow", "Math.pow")
        .replaceAll("deg", "Math.toDegrees")
        .replaceAll("R", "((double) R)")
        .replaceAll("rad", "Math.toRadians")
        .replaceAll("E", "Math.E")
        .replaceAll("PI", "Math.PI")
        .replaceAll("X", "Math.toRadians(X)")
        .replace('\\', ' ') //
        .replace(':', ' ')  //
        .replace('?', ' ')  // these chars are removed to prevent user hacking
        .replace('"', ' ')  //
        .replace(';', ' '); // 
  }

  private static String equationToClassName(String equation) {
    // a unique Java class name is made by consistently replacing arithmetic and 
    // non-number/non-letter characters with reserved capital letters
    return equation.replaceAll("Math.", "")
        .replaceAll(" ", "")
        .replace('.', 'Z')
        .replace('+', 'A')
        .replace('-', 'S')
        .replace('*', 'T')
        .replace('/', 'D')
        .replace('%', 'M')
        .replace('?', 'Q')
        .replace(':', 'C')
        .replace('(', 'F')
        .replace(')', 'B');
  }

  private static Method calculator(String name, String equation) {
    r.addClass(name, 
        "public class " + name + " { \n" + 
            "public static float[] calc(Double R) { \n" + 
            "  float[] positions = new float[360]; \n" + 
            "  for (double X = 0; X < 360; X++) { \n" + 
            "    positions[((int)X)] = (float) ( " + equation + " ); \n" +
            "  } \n" + 
            "  return positions; \n" + 
            "} \n" +
        "} \n");
    r.compile();

    return MethodInvocationUtils.findMethod(
        (Class<?>) r.getCompiledClass(name),
        "calc",
        (Double) radius);
  }
}

/**
 * This class was not authored by Quinton Ashley 
 * but has been edited for use in this application.
 * The unaltered source of this code is at:
 * http://stackoverflow.com/a/30038318/3792062
 * 
 * Utility class for compiling classes whose source code is given as
 * strings, in-memory, at runtime, using the JavaCompiler tools.
 */
class RuntimeCompiler {
  /** The Java Compiler */
  private final JavaCompiler javaCompiler;

  /** The mapping from fully qualified class names to the class data */
  private final Map<String, byte[]> classData;

  /** A class loader that will look up classes in the {@link #classData} */
  private final MapClassLoader mapClassLoader;

  /**
   * The JavaFileManager that will handle the compiled classes, and
   * eventually put them into the {@link #classData}
   */
  private final ClassDataFileManager classDataFileManager;

  /** The compilation units for the next compilation task */
  private final List<JavaFileObject> compilationUnits;

  /**
   * Creates a new RuntimeCompiler
   * 
   * @throws NullPointerException If no JavaCompiler could be obtained.
   * This is the case when the application was not started with a JDK,
   * but only with a JRE. (More specifically: When the JDK tools are 
   * not in the classpath).
   */
  public RuntimeCompiler() {
    this.javaCompiler = ToolProvider.getSystemJavaCompiler();
    if (javaCompiler == null) {
      throw new NullPointerException(
          "No JavaCompiler found. Make sure to run this with "
              + "a JDK, and not only with a JRE");
    }
    this.classData = new LinkedHashMap<String, byte[]>();
    this.mapClassLoader = new MapClassLoader();
    this.classDataFileManager =
        new ClassDataFileManager(
            javaCompiler.getStandardFileManager(null, null, null));
    this.compilationUnits = new ArrayList<JavaFileObject>();
  }

  /**
   * Add a class with the given name and source code to be compiled
   * with the next call to {@link #compile()}
   * 
   * @param className The class name
   * @param code The code of the class
   */
  public void addClass(String className, String code) {
    String javaFileName = className + ".java";
    JavaFileObject javaFileObject =
        new MemoryJavaSourceFileObject(javaFileName, code);
    compilationUnits.add(javaFileObject);
  }

  /**
   * Compile all classes that have been added by calling 
   * {@link #addClass(String, String)}
   * 
   * @return Whether the compilation succeeded
   */
  boolean compile() {
    DiagnosticCollector<JavaFileObject> diagnosticsCollector =
        new DiagnosticCollector<JavaFileObject>();
    CompilationTask task =
        javaCompiler.getTask(null, classDataFileManager,
            diagnosticsCollector, null, null, 
            compilationUnits);
    boolean success = task.call();
    compilationUnits.clear();
    //			for (Diagnostic<?> diagnostic : diagnosticsCollector.getDiagnostics()) {
    //				System.out.println(
    //						diagnostic.getKind() + " : " + 
    //								diagnostic.getMessage(null));
    //				System.out.println(
    //						"Line " + diagnostic.getLineNumber() + 
    //						" of " + diagnostic.getSource());
    //				System.out.println();
    //			}
    return success;
  }

  /**
   * Obtain a class that was previously compiled by adding it with
   * {@link #addClass(String, String)} and calling {@link #compile()}. 
   * 
   * @param className The class name
   * @return The class. Returns <code>null</code> if the compilation failed.
   */
  public Class<?> getCompiledClass(String className) {
    return mapClassLoader.findClass(className);
  }

  /** In-memory representation of a source JavaFileObject */
  private static final class MemoryJavaSourceFileObject extends SimpleJavaFileObject {
    /** The source code of the class */
    private final String code;

    /**
     * Creates a new in-memory representation of a Java file
     * 
     * @param fileName The file name
     * @param code The source code of the file
     */
    private MemoryJavaSourceFileObject(String fileName, String code) {
      super(URI.create("string:///" + fileName), Kind.SOURCE);
      this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
      return code;
    }
  }

  /** A class loader that will look up classes in the {@link #classData} */
  private class MapClassLoader extends ClassLoader {
    @Override
    public Class<?> findClass(String name) {
      try {
        byte[] b = classData.get(name);
        return defineClass(name, b, 0, b.length);
      } catch (Exception e) { }
      return null;
    }
  }

  /**
   * In-memory representation of a class JavaFileObject
   * @author User
   */
  private class MemoryJavaClassFileObject extends SimpleJavaFileObject {
    /** The name of the class represented by the file object */
    private final String className;

    /**
     * Create a new java file object that represents the specified class
     * 
     * @param className THe name of the class
     */
    private MemoryJavaClassFileObject(String className) {
      super(URI.create("string:///" + className + ".class"), 
          Kind.CLASS);
      this.className = className;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
      return new ClassDataOutputStream(className);
    }
  }

  /**
   * A JavaFileManager that manages the compiled classes by passing
   * them to the {@link #classData} map via a ClassDataOutputStream
   */
  private class ClassDataFileManager extends
  ForwardingJavaFileManager<StandardJavaFileManager> {
    /**
     * Create a new file manager that delegates to the given file manager
     * 
     * @param standardJavaFileManager The delegate file manager
     */
    private ClassDataFileManager(
        StandardJavaFileManager standardJavaFileManager) {
      super(standardJavaFileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(final Location location,
        final String className, Kind kind, FileObject sibling)
            throws IOException {
      return new MemoryJavaClassFileObject(className);
    }
  }

  /**
   * An output stream that is used by the ClassDataFileManager
   * to store the compiled classes in the  {@link #classData} map
   */
  private class ClassDataOutputStream extends OutputStream {
    /** The name of the class that the received class data represents */
    private final String className;

    /** The output stream that will receive the class data */
    private final ByteArrayOutputStream baos;

    /**
     * Creates a new output stream that will store the class
     * data for the class with the given name
     * 
     * @param className The class name
     */
    private ClassDataOutputStream(String className) {
      this.className = className;
      this.baos = new ByteArrayOutputStream();
    }

    @Override
    public void write(int b) throws IOException {
      baos.write(b);
    }

    @Override
    public void close() throws IOException {
      classData.put(className, baos.toByteArray());
      super.close();
    }
  }
}

/** Utility methods not directly related to the RuntimeCompiler */
class MethodInvocationUtils {
  /**
   * Utility method to find the first static method in the given
   * class that has the given name and can accept the given 
   * arguments. Returns <code>null</code> if no such method 
   * can be found.
   * 
   * @param c The class
   * @param methodName The name of the method
   * @param args The arguments
   * @return The first matching static method.
   */
  public static Method findMethod(Class<?> c, String methodName, Object ... args) {
    try {
      Method methods[] = c.getDeclaredMethods();
      for (Method m : methods) {
        if (m.getName().equals(methodName) && Modifier.isStatic(m.getModifiers())) {
          Class<?>[] parameterTypes = m.getParameterTypes();
          if (areAssignable(parameterTypes, args)) {
            return m;
          }
        }
      }
    } catch (Exception e) { }
    return null;
  }

  /**
   * Returns whether the given arguments are assignable to the
   * respective types
   * 
   * @param types The types
   * @param args The arguments
   * @return Whether the arguments are assignable
   */
  public static boolean areAssignable(Class<?> types[], Object ...args) {
    if (types.length != args.length) {
      return false;
    }
    for (int i=0; i<types.length; i++) {
      Object arg = args[i];
      Class<?> type = types[i];
      if (arg != null && !type.isAssignableFrom(arg.getClass())) {
        return false;
      }
    }
    return true;
  }
}

// Original example

//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.tools.Diagnostic;
//import javax.tools.DiagnosticCollector;
//import javax.tools.FileObject;
//import javax.tools.ForwardingJavaFileManager;
//import javax.tools.JavaCompiler;
//import javax.tools.JavaCompiler.CompilationTask;
//import javax.tools.JavaFileObject;
//import javax.tools.JavaFileObject.Kind;
//import javax.tools.SimpleJavaFileObject;
//import javax.tools.StandardJavaFileManager;
//import javax.tools.ToolProvider;
//
///**
// * An example showing how to use the RuntimeCompiler utility class
// */
//public class RuntimeCompilerExample
//{
//    public static void main(String[] args) throws Exception
//    {
//        simpleExample();
//        twoClassExample();
//        useLoadedClassExample();
//    }
//
//    /**
//     * Simple example: Shows how to add and compile a class, and then
//     * invoke a static method on the loaded class.
//     */
//    private static void simpleExample()
//    {
//        String classNameA = "ExampleClass";
//        String codeA =
//            "public class ExampleClass {" + "\n" + 
//            "    public static void exampleMethod(String name) {" + "\n" + 
//            "        System.out.println(\"Hello, \"+name);" + "\n" + 
//            "    }" + "\n" + 
//            "}" + "\n";
//
//        RuntimeCompiler r = new RuntimeCompiler();
//        r.addClass(classNameA, codeA);
//        r.compile();
//
//        MethodInvocationUtils.invokeStaticMethod(
//            r.getCompiledClass(classNameA), 
//            "exampleMethod", "exampleParameter");
//    }
//
//    /**
//     * An example showing how to add two classes (where one refers to the 
//     * other), compile them, and invoke a static method on one of them
//     */
//    private static void twoClassExample()
//    {
//        String classNameA = "ExampleClassA";
//        String codeA =
//            "public class ExampleClassA {" + "\n" + 
//            "    public static void exampleMethodA(String name) {" + "\n" + 
//            "        System.out.println(\"Hello, \"+name);" + "\n" + 
//            "    }" + "\n" + 
//            "}" + "\n";
//
//        String classNameB = "ExampleClassB";
//        String codeB =
//            "public class ExampleClassB {" + "\n" + 
//            "    public static void exampleMethodB(String name) {" + "\n" + 
//            "        System.out.println(\"Passing to other class\");" + "\n" + 
//            "        ExampleClassA.exampleMethodA(name);" + "\n" + 
//            "    }" + "\n" + 
//            "}" + "\n";
//
//        RuntimeCompiler r = new RuntimeCompiler();
//        r.addClass(classNameA, codeA);
//        r.addClass(classNameB, codeB);
//        r.compile();
//
//        MethodInvocationUtils.invokeStaticMethod(
//            r.getCompiledClass(classNameB), 
//            "exampleMethodB", "exampleParameter");
//    }
//
//    /**
//     * An example that compiles and loads a class, and then uses an 
//     * instance of this class
//     */
//    private static void useLoadedClassExample() throws Exception
//    {
//        String classNameA = "ExampleComparator";
//        String codeA =
//            "import java.util.Comparator;" + "\n" + 
//            "public class ExampleComparator " + "\n" + 
//            "    implements Comparator<Integer> {" + "\n" + 
//            "    @Override" + "\n" + 
//            "    public int compare(Integer i0, Integer i1) {" + "\n" + 
//            "        System.out.println(i0+\" and \"+i1);" + "\n" + 
//            "        return Integer.compare(i0, i1);" + "\n" + 
//            "    }" + "\n" + 
//            "}" + "\n";
//
//        RuntimeCompiler r = new RuntimeCompiler();
//        r.addClass(classNameA, codeA);
//        r.compile();
//
//        Class<?> c = r.getCompiledClass("ExampleComparator");
//        Comparator<Integer> comparator = (Comparator<Integer>) c.newInstance();
//
//        System.out.println("Sorting...");
//        List<Integer> list = new ArrayList<Integer>(Arrays.asList(3,1,2));
//        Collections.sort(list, comparator);
//        System.out.println("Result: "+list);
//    }
//
//}
//
//
///**
// * Utility class for compiling classes whose source code is given as
// * strings, in-memory, at runtime, using the JavaCompiler tools.
// */
//class RuntimeCompiler
//{
//    /**
//     * The Java Compiler
//     */
//    private final JavaCompiler javaCompiler;
//
//    /**
//     * The mapping from fully qualified class names to the class data
//     */
//    private final Map<String, byte[]> classData;
//
//    /**
//     * A class loader that will look up classes in the {@link #classData}
//     */
//    private final MapClassLoader mapClassLoader;
//
//    /**
//     * The JavaFileManager that will handle the compiled classes, and
//     * eventually put them into the {@link #classData}
//     */
//    private final ClassDataFileManager classDataFileManager;
//
//    /**
//     * The compilation units for the next compilation task
//     */
//    private final List<JavaFileObject> compilationUnits;
//
//
//    /**
//     * Creates a new RuntimeCompiler
//     * 
//     * @throws NullPointerException If no JavaCompiler could be obtained.
//     * This is the case when the application was not started with a JDK,
//     * but only with a JRE. (More specifically: When the JDK tools are 
//     * not in the classpath).
//     */
//    public RuntimeCompiler()
//    {
//        this.javaCompiler = ToolProvider.getSystemJavaCompiler();
//        if (javaCompiler == null)
//        {
//            throw new NullPointerException(
//                "No JavaCompiler found. Make sure to run this with "
//                    + "a JDK, and not only with a JRE");
//        }
//        this.classData = new LinkedHashMap<String, byte[]>();
//        this.mapClassLoader = new MapClassLoader();
//        this.classDataFileManager =
//            new ClassDataFileManager(
//                javaCompiler.getStandardFileManager(null, null, null));
//        this.compilationUnits = new ArrayList<JavaFileObject>();
//    }
//
//    /**
//     * Add a class with the given name and source code to be compiled
//     * with the next call to {@link #compile()}
//     * 
//     * @param className The class name
//     * @param code The code of the class
//     */
//    public void addClass(String className, String code)
//    {
//        String javaFileName = className + ".java";
//        JavaFileObject javaFileObject =
//            new MemoryJavaSourceFileObject(javaFileName, code);
//        compilationUnits.add(javaFileObject);
//    }
//
//    /**
//     * Compile all classes that have been added by calling 
//     * {@link #addClass(String, String)}
//     * 
//     * @return Whether the compilation succeeded
//     */
//    boolean compile()
//    {
//        DiagnosticCollector<JavaFileObject> diagnosticsCollector =
//            new DiagnosticCollector<JavaFileObject>();
//        CompilationTask task =
//            javaCompiler.getTask(null, classDataFileManager,
//                diagnosticsCollector, null, null, 
//                compilationUnits);
//        boolean success = task.call();
//        compilationUnits.clear();
//        for (Diagnostic<?> diagnostic : diagnosticsCollector.getDiagnostics())
//        {
//            System.out.println(
//                diagnostic.getKind() + " : " + 
//                diagnostic.getMessage(null));
//            System.out.println(
//                "Line " + diagnostic.getLineNumber() + 
//                " of " + diagnostic.getSource());
//            System.out.println();
//        }
//        return success;
//    }
//
//
//    /**
//     * Obtain a class that was previously compiled by adding it with
//     * {@link #addClass(String, String)} and calling {@link #compile()}. 
//     * 
//     * @param className The class name
//     * @return The class. Returns <code>null</code> if the compilation failed.
//     */
//    public Class<?> getCompiledClass(String className)
//    {
//        return mapClassLoader.findClass(className);
//    }
//
//    /**
//     * In-memory representation of a source JavaFileObject 
//     */
//    private static final class MemoryJavaSourceFileObject extends
//        SimpleJavaFileObject
//    {
//        /**
//         * The source code of the class
//         */
//        private final String code;
//
//        /**
//         * Creates a new in-memory representation of a Java file
//         * 
//         * @param fileName The file name
//         * @param code The source code of the file
//         */
//        private MemoryJavaSourceFileObject(String fileName, String code)
//        {
//            super(URI.create("string:///" + fileName), Kind.SOURCE);
//            this.code = code;
//        }
//
//        @Override
//        public CharSequence getCharContent(boolean ignoreEncodingErrors)
//            throws IOException
//        {
//            return code;
//        }
//    }
//
//    /**
//     * A class loader that will look up classes in the {@link #classData}
//     */
//    private class MapClassLoader extends ClassLoader
//    {
//        @Override
//        public Class<?> findClass(String name)
//        {
//            byte[] b = classData.get(name);
//            return defineClass(name, b, 0, b.length);
//        }
//    }
//
//    /**
//     * In-memory representation of a class JavaFileObject
//     * @author User
//     *
//     */
//    private class MemoryJavaClassFileObject extends SimpleJavaFileObject
//    {
//        /**
//         * The name of the class represented by the file object
//         */
//        private final String className;
//
//        /**
//         * Create a new java file object that represents the specified class
//         * 
//         * @param className THe name of the class
//         */
//        private MemoryJavaClassFileObject(String className)
//        {
//            super(URI.create("string:///" + className + ".class"), 
//                Kind.CLASS);
//            this.className = className;
//        }
//
//        @Override
//        public OutputStream openOutputStream() throws IOException
//        {
//            return new ClassDataOutputStream(className);
//        }
//    }
//
//
//    /**
//     * A JavaFileManager that manages the compiled classes by passing
//     * them to the {@link #classData} map via a ClassDataOutputStream
//     */
//    private class ClassDataFileManager extends
//        ForwardingJavaFileManager<StandardJavaFileManager>
//    {
//        /**
//         * Create a new file manager that delegates to the given file manager
//         * 
//         * @param standardJavaFileManager The delegate file manager
//         */
//        private ClassDataFileManager(
//            StandardJavaFileManager standardJavaFileManager)
//        {
//            super(standardJavaFileManager);
//        }
//
//        @Override
//        public JavaFileObject getJavaFileForOutput(final Location location,
//            final String className, Kind kind, FileObject sibling)
//            throws IOException
//        {
//            return new MemoryJavaClassFileObject(className);
//        }
//    }
//
//
//    /**
//     * An output stream that is used by the ClassDataFileManager
//     * to store the compiled classes in the  {@link #classData} map
//     */
//    private class ClassDataOutputStream extends OutputStream
//    {
//        /**
//         * The name of the class that the received class data represents
//         */
//        private final String className;
//
//        /**
//         * The output stream that will receive the class data
//         */
//        private final ByteArrayOutputStream baos;
//
//        /**
//         * Creates a new output stream that will store the class
//         * data for the class with the given name
//         * 
//         * @param className The class name
//         */
//        private ClassDataOutputStream(String className)
//        {
//            this.className = className;
//            this.baos = new ByteArrayOutputStream();
//        }
//
//        @Override
//        public void write(int b) throws IOException
//        {
//            baos.write(b);
//        }
//
//        @Override
//        public void close() throws IOException
//        {
//            classData.put(className, baos.toByteArray());
//            super.close();
//        }
//    }
//}
//
///**
// * Utility methods not directly related to the RuntimeCompiler
// */
//class MethodInvocationUtils
//{
//    /**
//     * Utility method to invoke the first static method in the given 
//     * class that can accept the given parameters.
//     *  
//     * @param c The class
//     * @param methodName The method name
//     * @param args The arguments for the method call
//     * @return The return value of the method call
//     * @throws RuntimeException If either the class or a matching method
//     * could not be found
//     */
//    public static Object invokeStaticMethod(
//        Class<?> c, String methodName, Object... args)
//    {
//        Method m = findFirstMatchingStaticMethod(c, methodName, args);
//        if (m == null)
//        {
//            throw new RuntimeException("No matching method found");
//        }
//        try
//        {
//            return m.invoke(null, args);
//        }
//        catch (IllegalAccessException e)
//        {
//            throw new RuntimeException(e);
//        }
//        catch (IllegalArgumentException e)
//        {
//            throw new RuntimeException(e);
//        }
//        catch (InvocationTargetException e)
//        {
//            throw new RuntimeException(e);
//        }
//        catch (SecurityException e)
//        {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * Utility method to find the first static method in the given
//     * class that has the given name and can accept the given 
//     * arguments. Returns <code>null</code> if no such method 
//     * can be found.
//     * 
//     * @param c The class
//     * @param methodName The name of the method 
//     * @param args The arguments
//     * @return The first matching static method.
//     */
//    private static Method findFirstMatchingStaticMethod(
//        Class<?> c, String methodName, Object ... args)
//    {
//        Method methods[] = c.getDeclaredMethods();
//        for (Method m : methods)
//        {
//            if (m.getName().equals(methodName) &&
//                Modifier.isStatic(m.getModifiers()))
//            {
//                Class<?>[] parameterTypes = m.getParameterTypes();
//                if (areAssignable(parameterTypes, args))
//                {
//                    return m;
//                }
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Returns whether the given arguments are assignable to the
//     * respective types
//     * 
//     * @param types The types
//     * @param args The arguments
//     * @return Whether the arguments are assignable
//     */
//    private static boolean areAssignable(Class<?> types[], Object ...args)
//    {
//        if (types.length != args.length)
//        {
//            return false;
//        }
//        for (int i=0; i<types.length; i++)
//        {
//            Object arg = args[i];
//            Class<?> type = types[i];
//            if (arg != null && !type.isAssignableFrom(arg.getClass()))
//            {
//                return false;
//            }
//        }
//        return true;
//    }
//
//}