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
package it.eng.spagobi.utilities.scripting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.groovy.GroovySandbox;

public class SpagoBIScriptManager {

	private static Logger logger = Logger.getLogger(SpagoBIScriptManager.class);

	public Object runScript(String script, String language) {
		return runScript(script, language, null, null);
	}

	public Object runScript(String script, String language, Map<String, Object> bindings) {
		return runScript(script, language, bindings, null);
	}

	public Object runScript(String script, String language, Map<String, Object> bindings, List imports) {

		Object results;

		logger.debug("IN");

		results = null;
		try {

			if (imports != null) {
				StringBuffer importsBuffer = new StringBuffer();
				for (Object importedScriptReference : imports) {
					if (importedScriptReference instanceof File) {
						importsBuffer.append(this.getImportedScript((File) importedScriptReference) + "\n");
					} else if (importedScriptReference instanceof URL) {
						importsBuffer.append(this.getImportedScript((URL) importedScriptReference) + "\n");
					} else {
						logger.warn("Impossible to resolve import reference of type [" + importedScriptReference.getClass().getName() + "]");
					}

				}
				script = importsBuffer.toString() + script;
			}

			if (isGroovy(language)) {
				return evaluateGroovy(script, bindings);
			}

			final ScriptEngine scriptEngine = getScriptEngine(language);

			if (scriptEngine == null) {
				throw new RuntimeException("No engine available to execute scripts of type [" + language + "]");
			} else {
				logger.debug("Found engine [" + scriptEngine.NAME + "]");
			}

			if (bindings != null) {
				Bindings scriptBindings = new SimpleBindings(bindings);

				scriptBindings.forEach((k, v) -> {
					scriptEngine.put(k, v);
				});
			}

			PermissionCollection pc = new Permissions(); // This means no permissions at all
			CodeSource codeSource = scriptEngine.getClass().getProtectionDomain().getCodeSource();
			ProtectionDomain protectionDomain = new ProtectionDomain(codeSource, pc);
			ProtectionDomain[] context = new ProtectionDomain[] { protectionDomain };
			AccessControlContext accessControlContext = new AccessControlContext(context);

			final String _script = script;

			results = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

				@Override
				public Object run() throws ScriptException {
					return scriptEngine.eval(_script);
				}
			}, accessControlContext);
			
			// the keyword "query" is used because it is useful in datasets when changing the query entered 
			// this is a commonly used variable name.
			if(scriptEngine.get("query") != null) {			
				results = scriptEngine.get("query");
			}
		} catch (Throwable t) {
			logger.error("Error while executing Javascript:\n" + script, t);
			throw new SpagoBIRuntimeException("An unexpected error occured while executing script", t);
		} finally {
			logger.debug("OUT");
		}

		return results;
	}

	private static Object evaluateGroovy(String script, Map<String, Object> bindings) throws IOException {
		logger.debug("Initializating Groovy Sandbox...");
		GroovySandbox gs = new GroovySandbox();
		gs.setBindings(bindings);
		LogMF.debug(logger, "Evaluating script:\n{0}", script);
		return gs.evaluate(script);
	}

	private static boolean isGroovy(String language) {
		return "".equals(language) || "groovy".equalsIgnoreCase(language);
	}

	public boolean isEngineSupported(String name) {
		return getScriptEngine(name) != null;
	}

	private ScriptEngine getScriptEngine(String name) {
		ScriptEngine scriptEngine;
		scriptEngine = null;
		scriptEngine = getScriptEngineByLanguage(name);
		if (scriptEngine == null) {
			scriptEngine = getScriptEngineByName(name);
		}
		return scriptEngine;
	}

	private ScriptEngine getScriptEngineByLanguage(String language) {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		List<ScriptEngineFactory> scriptEngineFactories = scriptEngineManager.getEngineFactories();

		for (ScriptEngineFactory scriptEngineFactory : scriptEngineFactories) {
			if (scriptEngineFactory.getLanguageName().equals(language)) {
				return scriptEngineFactory.getScriptEngine();
			}
		}
		return null;
	}

	private ScriptEngine getScriptEngineByName(String name) {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		List<ScriptEngineFactory> scriptEngineFactories = scriptEngineManager.getEngineFactories();

		for (ScriptEngineFactory scriptEngineFactory : scriptEngineFactories) {
			if (scriptEngineFactory.getNames().contains(name)) {
				return scriptEngineFactory.getScriptEngine();
			}
		}
		return null;
	}

	/**
	 * @return A list containing the names of all scripting languages supported
	 */
	public Set<String> getSupportedLanguages() {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		List<ScriptEngineFactory> scriptEngineFactories = scriptEngineManager.getEngineFactories();

		Set<String> languages = new HashSet<String>();
		for (ScriptEngineFactory scriptEngineFactory : scriptEngineFactories) {
			languages.add(scriptEngineFactory.getLanguageName());
		}
		return languages;
	}

	/**
	 * @return A list containing the short names of all scripting engines supported. An engie can have multiple names so in general the number of engine names
	 *         is greather than the number of actual engines registered into the platform
	 */
	public Set<String> getSuportedEngineNames() {
		ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
		List<ScriptEngineFactory> scriptEngineFactories = scriptEngineManager.getEngineFactories();

		Set<String> engineNames = new HashSet<String>();
		for (ScriptEngineFactory scriptEngineFactory : scriptEngineFactories) {
			engineNames.addAll(scriptEngineFactory.getNames());
		}
		return engineNames;
	}

	// load predefined script file
	// if(predefinedJsScriptFileName==null || predefinedJsScriptFileName.equals("")){
	// predefinedJsScriptFileName = SingletonConfig.getInstance().getConfigValue("SCRIPT_LANGUAGE.javascript.predefinedScriptFile");
	// }
	// InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(predefinedJsScriptFileName);

	private String getImportedScript(File scriptFile) {
		String importedScript;
		InputStream is;

		importedScript = null;
		is = null;
		try {
			logger.debug("Importing script from file [" + scriptFile + "]");
			is = new FileInputStream(scriptFile);
			importedScript = getImportedScript(is);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while importing script from file [" + scriptFile + "]", t);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException t) {
					logger.warn("Impossible to close inpust stream associated to file [" + scriptFile + "]", t);
				}
			}
		}

		return importedScript;
	}

	private String getImportedScript(URL url) {
		String importedScript;
		InputStream is;

		importedScript = null;
		is = null;
		try {
			logger.debug("Importing script from url [" + url + "]");
			is = url.openStream();
			importedScript = getImportedScript(is);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while importing script from file [" + url + "]", t);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException t) {
					logger.warn("Impossible to close inpust stream associated to file [" + url + "]", t);
				}
			}
		}

		return importedScript;
	}

	private String getImportedScript(InputStream is) {
		String importedScript;

		importedScript = null;

		try {
			StringBuffer buffer = new StringBuffer();
			int arrayLength = 1024;
			byte[] bufferbyte = new byte[arrayLength];
			char[] bufferchar = new char[arrayLength];
			int len;
			while ((len = is.read(bufferbyte)) >= 0) {
				for (int i = 0; i < arrayLength; i++) {
					bufferchar[i] = (char) bufferbyte[i];
				}
				buffer.append(bufferchar, 0, len);
			}
			importedScript = buffer.toString();
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while importing script from stream", t);
		}

		return importedScript;
	}

	public void printInfo() {
		ScriptEngineManager mgr = new ScriptEngineManager();
		List<ScriptEngineFactory> factories = mgr.getEngineFactories();

		for (ScriptEngineFactory factory : factories) {
			logger.debug("ScriptEngineFactory Info");
			String engName = factory.getEngineName();
			String engVersion = factory.getEngineVersion();
			String langName = factory.getLanguageName();
			String langVersion = factory.getLanguageVersion();
			System.out.printf("\tScript Engine: %s (%s)\n", engName, engVersion);
			List<String> engNames = factory.getNames();
			for (String name : engNames) {
				System.out.printf("\tEngine Alias: %s\n", name);
			}
			System.out.printf("\tLanguage: %s (%s)\n", langName, langVersion);
		}
	}
}
