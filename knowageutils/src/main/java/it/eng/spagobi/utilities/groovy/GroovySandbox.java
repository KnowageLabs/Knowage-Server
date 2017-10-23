package it.eng.spagobi.utilities.groovy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Blob;
import java.sql.NClob;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
import groovy.lang.GroovyShell;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.tools.dataset.bo.DataSetVariable;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This class permits to run Groovy scripts in a safe mode allowing only a strictly set of classes.
 *
 * @author fabrizio
 *
 */
public class GroovySandbox {

	private final static Class<?>[] CLASSES_WHITELIST = new Class[] { Date.class, List.class, ArrayList.class, Vector.class, Collection.class, HashSet.class,
			HashMap.class, Set.class, Map.class, Math.class, TreeSet.class, Arrays.class, Collections.class, DateFormat.class, SimpleDateFormat.class,
			DecimalFormat.class, NumberFormat.class, MessageFormat.class, TreeSet.class, TreeMap.class, DataSetVariable.class, java.sql.Date.class, Time.class,
			Timestamp.class, Blob.class, NClob.class, StringBuilder.class, StringBuffer.class, Float.class, Double.class, Long.class, BigDecimal.class,
			String.class, BigInteger.class, Integer.class, Character.class, Byte.class };

	private final static Class<?>[] CONSTANT_TYTPE_CLASSES_WHITELIST = new Class[] { Integer.class, Float.class, Long.class, Double.class, BigDecimal.class,
			Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE, Character.TYPE, Byte.TYPE, String.class, BigInteger.class, Object.class, Character.class,
			Byte.class };

	private final Class<?>[] addedClasses;

	private Map<String, Object> bindings = new HashMap<String, Object>();

	public GroovySandbox() {
		this(new Class<?>[0]);
	}

	/**
	 *
	 * @param addedClasses
	 *            add classes to permitted classes
	 */
	public GroovySandbox(Class<?>[] addedClasses) {
		Helper.checkNotNull(addedClasses, "addedClasses");
		this.addedClasses = addedClasses;
	}

	public Object evaluate(String script) {
		try {
			Helper.checkNotNull(script, "script");
			GroovyShell sandboxShell = getSandboxShell();
			setBindingsOnShell(sandboxShell);
			Object res = sandboxShell.evaluate(script);
			return res;
		} catch (CompilationFailedException | IOException e) {
			throw new SpagoBIRuntimeException("Error evaluating groovy script", e);
		}
	}

	/**
	 * Evalaute the script retrieved from url
	 *
	 * @param url
	 * @return
	 * @throws CompilationFailedException
	 * @throws IOException
	 */
	public Object evaluate(URL url) throws CompilationFailedException, IOException {
		Helper.checkNotNull(url, "url");
		GroovyShell sandboxShell = getSandboxShell();
		setBindingsOnShell(sandboxShell);

		URLConnection conn = url.openConnection();
		BufferedReader br = null;

		Object res;
		try {
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			res = sandboxShell.evaluate(br);
		} finally {
			if (br != null) {
				br.close();
			}

		}
		return res;
	}

	/**
	 * Set the bindings variables
	 *
	 * @param bindings
	 */
	public void setBindings(Map<String, Object> bindings) {
		Helper.checkNotNull(bindings, "bindings");
		// shallow copy
		this.bindings = new HashMap<String, Object>(bindings);
	}

	public Map<String, Object> getBindings() {
		// shallow copy
		return new HashMap<String, Object>(bindings);
	}

	private void setBindingsOnShell(GroovyShell shell) {
		for (String name : bindings.keySet()) {
			shell.setVariable(name, bindings.get(name));
		}
	}

	private CompilerConfiguration getSandboxConfiguration() throws IOException {
		ImportCustomizer imports = new ImportCustomizer();
		SecureASTCustomizer secure = new SecureASTCustomizer();
		secure.setClosuresAllowed(true);
		secure.setMethodDefinitionAllowed(true);
		String[] staticImportsWhitelist = new String[] {};
		secure.setStaticImportsWhitelist(Arrays.asList(staticImportsWhitelist));
		secure.setIndirectImportCheckEnabled(true);

		// add also Object.class
		secure.setImportsWhitelist(getStringClasses("", CLASSES_WHITELIST, addedClasses, new Class[] { Object.class }));

		List<String> staticImportMethods = getStaticImportMethods(CLASSES_WHITELIST, addedClasses);
		addPredefinedMethods(staticImportMethods);
		secure.setStaticImportsWhitelist(staticImportMethods);

		secure.setConstantTypesClassesWhiteList(getClasses(CONSTANT_TYTPE_CLASSES_WHITELIST, CLASSES_WHITELIST, addedClasses));
		// add also Object.class
		secure.setReceiversClassesWhiteList(getClasses(CLASSES_WHITELIST, addedClasses, new Class[] { Object.class }));

		CompilerConfiguration res = new CompilerConfiguration();
		res.addCompilationCustomizers(imports, secure);
		return res;
	}

	@SuppressWarnings("unchecked")
	private void addPredefinedMethods(List<String> staticImportMethods) throws IOException {

		try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(SpagoBIConstants.PREDEFINED_GROOVY_SCRIPT_FILE_NAME)) {
			List<String> lines = IOUtils.readLines(stream, "UTF-8");
			for (String line : lines) {
				if (line.contains("public ")) {
					// found method: es: public String getMultiValueProfileAttribute(String attrName...
					int index = line.indexOf('(');
					assert (index >= 0);
					int first = index;
					while (first - 1 >= 0 && line.charAt(first - 1) != ' ') {
						first--;
					}
					String name = line.substring(first, index);
					staticImportMethods.add("java.lang.Object." + name);
				}
			}

		}

	}

	private static List<String> getStaticImportMethods(Class<?>[]... classeses) {
		List<String> res = new ArrayList<String>();
		for (Class<?>[] classes : classeses) {
			for (Class<?> clazz : classes) {
				for (Method m : clazz.getMethods()) {
					res.add(clazz.getName() + "." + m.getName());
					res.add("java.lang.Object." + m.getName());
				}
			}
		}
		return res;
	}

	private GroovyShell getSandboxShell() throws IOException {
		return new GroovyShell(getSandboxConfiguration());
	}

	@SuppressWarnings("rawtypes")
	private static List<Class> getClasses(Class[]... az) {
		List<Class> res = new ArrayList<Class>();
		for (Class[] classes : az) {
			for (Class clazz : classes) {
				res.add(clazz);
				res.add(getArrayClass(clazz));
			}
		}
		return res;
	}

	@SuppressWarnings("rawtypes")
	private static List<String> getStringClasses(String add, Class<?>[]... classesWhitelists) {
		List<String> res = new ArrayList<String>();
		for (Class<?>[] classesWhitelist : classesWhitelists) {
			for (Class clazz : classesWhitelist) {
				res.add(clazz.getName() + add);
				res.add(getArrayClass(clazz).getName() + add);
			}
		}
		return res;
	}

	/**
	 * It's necessary for adding also arrays of all permitted classes
	 *
	 * @param clazz
	 * @return
	 */
	private static Class<? extends Object> getArrayClass(Class<?> clazz) {
		return Array.newInstance(clazz, 0).getClass();
	}
}
