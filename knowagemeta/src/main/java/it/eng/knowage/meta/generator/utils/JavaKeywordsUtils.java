/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.meta.generator.utils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class JavaKeywordsUtils {
	private static final char JAVA_RESERVEDWORD_PREFIX = '_';
	private static final char[] PKG_SEPARATORS = { '.', ':' };
	
	 /**
	  * List of java reserved words.  Must be kept sorted in ascending order.
	  */
	 private static final String JAVA_RESERVED_WORDS[] =
	 {
		 "abstract",   "assert",     "boolean",      "break",        "byte",
	     "case",       "catch",      "char",         "class",        "const",
	     "continue",   "default",    "do",           "double",       "else",
	     "enum",       "extends",    "false",        "final",        "finally",
	     "float",      "for",        "goto",         "if",           "implements",
	     "import",     "instanceof", "int",          "interface",    "long",
	     "native",     "new",        "null",         "package",      "private",
	     "protected",  "public",     "return",       "short",        "static",
	     "strictfp",   "super",      "switch",       "synchronized", "this",
	     "throw",      "throws",     "transient",    "true",         "try",
	     "void",       "volatile",   "while"
	 };


	 /**
	  * List of java.lang classes (1.5 JDK).
	  */
	 private final static Set<String> JAVA_LANG_NAMES = new HashSet<String>(Arrays.asList(
			 new String[]
	         {
				// Interfaces
				"Appendable", "CharSequence", "Cloneable", "Comparable",
	            "Iterable",   "Readable",     "Runnable",

	            // Classes
	            "Boolean",         "Byte",                   "Character",          "Class",
	            "ClassLoader",     "Compiler",               "Double",             "Enum",
	            "Float",           "InheritableThreadLocal", "Integer",            "Long",
	            "Math",            "Number",                 "Object",             "Package",
	            "Process",         "ProcessBuilder",         "Runtime",            "RuntimePermission",
	            "SecurityManager", "Short",                  "StackTraceElement",  "StrictMath",
	            "String",          "StringBuffer",           "StringBuilder",      "System",
	            "Thread",          "ThreadGroup",            "ThreadLocal",        "Throwable",
	            "Void",

	            // Exceptions
	            "ArithmeticException",             "ArrayIndexOutOfBoundsException", "ArrayStoreException",
	            "ClassCastException",              "ClassNotFoundException",         "CloneNotSupportedException",
	            "EnumConstantNotPresentException", "Exception",                      "IllegalAccessException",
	            "IllegalArgumentException",        "IllegalMonitorStateException",   "IllegalStateException",
	            "IllegalThreadStateException",     "IndexOutOfBoundsException",      "InstantiationException",
	            "InterruptedException",            "NegativeArraySizeException",     "NoSuchFieldException",
	            "NoSuchMethodException",           "NullPointerException",           "NumberFormatException",
	            "RuntimeException",                "SecurityException",              "StringIndexOutOfBoundsException",
	            "TypeNotPresentException",         "UnsupportedOperationException",

	            // Errors
	            "AbstractMethodError",  "AssertionError",               "ClassCircularityError",
	            "ClassFormatError",     "Error",                        "ExceptionInInitializerError",
	            "IllegalAccessError",   "IncompatibleClassChangeError", "InstantiationError",
	            "InternalError",        "LinkageError",                 "NoClassDefFoundError",
	            "NoSuchFieldError",     "NoSuchMethodError",            "OutOfMemoryError",
	            "StackOverflowError",   "ThreadDeath",                  "UnknownError",
	            "UnsatisfiedLinkError", "UnsupportedClassVersionError", "VerifyError",
	            "VirtualMachineError",

	            // Annotation types
	            "Deprecated", "Override", "SuppressWarnings"
	          }
	));
	 
	/**
	 * Is the value of checkString a java reserved word?
	 * 
	 * @param checkString
	 *            String value to check.
	 * @return true if checkString exactly matches a java reserved word
	 */
	public static boolean isJavaReservedWord(String checkString) {
		return Arrays.binarySearch(JAVA_RESERVED_WORDS, checkString) >= 0;
	}

	/**
	 * Transform an invalid java identifier into a valid one. Any invalid
	 * characters are replaced with '_'s.
	 * 
	 * @param id
	 *            The invalid java identifier.
	 * @return The transformed java identifier.
	 */
	public static String transformInvalidJavaIdentifier(String id) {
		if (id == null)
			throw new IllegalArgumentException("id cannot be null");

		final int len = id.length();
		if (len == 0)
			return "_";

		//
		// begin the transform
		//

		StringBuilder transformed = new StringBuilder(id);
		if (isJavaReservedWord(id) || JAVA_LANG_NAMES.contains(id)) {
			transformed.insert(0, JAVA_RESERVEDWORD_PREFIX);
		}

		if (!Character.isJavaIdentifierStart(transformed.charAt(0))) {
			transformed.insert(0, JAVA_RESERVEDWORD_PREFIX);
		}

		for (int i = 1; i < transformed.length(); i++) {
			if (!Character.isJavaIdentifierPart(transformed.charAt(i))) {
				transformed.replace(i, i + 1, "" + JAVA_RESERVEDWORD_PREFIX);
			}
		}

		return transformed.toString();
	}

	/**
	 * Check the identifier to see if it is a valid Java identifier.
	 * 
	 * @param id
	 *            The Java identifier to check.
	 * @return true if identifier is valid.
	 */
	public static boolean isValidJavaIdentifier(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id cannot be null");
		}

		final int len = id.length();
		if (len == 0) {
			return false;
		}

		if (isJavaReservedWord(id) || JAVA_LANG_NAMES.contains(id)) {
			return false;
		}

		if (!Character.isJavaIdentifierStart(id.charAt(0))) {
			return false;
		}

		for (int i = 1; i < len; i++) {
			if (!Character.isJavaIdentifierPart(id.charAt(i)))
				return false;
		}

		return true;
	}

	/**
	 * Convert a java package name to a file directory name.
	 * 
	 * @param packageName
	 *            Package name to convert.
	 * @return The converted package name, empty string if packageName was null.
	 */
	public static String packageNameToDirectoryName(String packageName) {
		String dir;

		if (packageName != null) {
			for (char pkgSeparator : PKG_SEPARATORS) {
				packageName = packageName.replace(pkgSeparator,
						File.separatorChar);
			}
		}

		dir = packageName;
		return (dir == null) ? "" : dir + File.separatorChar;
	}
	
	/**
	 * Converts a name to a Java class name (<em>first letter
	 * not capitalized</em>)
	 * 
	 * @param name the name to convert
	 * @return the generated java variable name
	 */
	public static String transformToJavaClassName(String name) {
		String className;
		className = StringUtils.initUpper(name);
		className = transformInvalidJavaIdentifier(className);
		if(className.charAt(0) == JAVA_RESERVEDWORD_PREFIX) className = "Class" + className;
		return className;
	}
	
	/**
	 * Converts a name to a Java property name (<em>first letter
	 * not capitalized</em>)
	 * 
	 * @param name the name to convert
	 * @return the generated java variable name
	 */
	public static String transformToJavaPropertyName(String name) {
		String propertyName;
		propertyName = transformInvalidJavaIdentifier(name);
		return propertyName;
	}
}
