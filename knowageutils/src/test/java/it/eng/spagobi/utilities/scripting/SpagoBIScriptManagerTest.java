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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.test.AbstractSpagoBITestCase;
import it.eng.spagobi.test.TestDataSetFactory;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIScriptManagerTest extends AbstractSpagoBITestCase {
	
	SpagoBIScriptManager scriptManager;
	
	public void setUp() throws Exception {
		super.setUp();
		scriptManager = new SpagoBIScriptManager();
	}
	
	public void tearDown() throws Exception {
		super.tearDown();
		scriptManager = null;
	}
	
	public void testGetSuportedLanguages() {
		SpagoBIScriptManager scriptManager = new SpagoBIScriptManager();
		scriptManager.printInfo();
		Set<String> languages = scriptManager.getSupportedLanguages();
		assertNotNull(languages);
		assertEquals(3, languages.size());
		assertTrue(languages.contains("groovy"));
		assertTrue(languages.contains("ECMAScript"));
		assertTrue(languages.contains("EmbeddedECMAScript"));
		assertFalse(languages.contains("js"));
		assertFalse(languages.contains("javascript"));
		
		
	}

	public void testGetSuportedEngineNames() {
		SpagoBIScriptManager scriptManager = new SpagoBIScriptManager();
		Set<String> engineNames = scriptManager.getSuportedEngineNames();
		assertNotNull(engineNames);
		assertEquals(11, engineNames.size());
		assertTrue(engineNames.contains("js"));
		assertTrue(engineNames.contains("rhino"));
		assertTrue(engineNames.contains("JavaScript"));
		assertTrue(engineNames.contains("javascript"));
		assertTrue(engineNames.contains("ecmascript"));
		assertTrue(engineNames.contains("ECMAScript"));
		assertTrue(engineNames.contains("embeddedjavascript"));
		assertTrue(engineNames.contains("EmbeddedJavaScript"));
		assertTrue(engineNames.contains("ejs"));
		assertTrue(engineNames.contains("rhino-nonjdk"));
		assertTrue(engineNames.contains("groovy"));
	}
	
	public void testIsEngineSupported() {
		assertTrue(scriptManager.isEngineSupported("groovy"));
		assertTrue(scriptManager.isEngineSupported("EmbeddedECMAScript"));
		assertTrue(scriptManager.isEngineSupported("js"));
		assertTrue(scriptManager.isEngineSupported("rhino"));
		assertTrue(scriptManager.isEngineSupported("JavaScript"));
		assertTrue(scriptManager.isEngineSupported("javascript"));
		assertTrue(scriptManager.isEngineSupported("ecmascript"));
		assertTrue(scriptManager.isEngineSupported("ECMAScript"));
		assertTrue(scriptManager.isEngineSupported("embeddedjavascript"));
		assertTrue(scriptManager.isEngineSupported("EmbeddedJavaScript"));
		assertTrue(scriptManager.isEngineSupported("ejs"));
		assertTrue(scriptManager.isEngineSupported("rhino-nonjdk"));
		assertTrue(scriptManager.isEngineSupported("groovy"));
		
		assertFalse(scriptManager.isEngineSupported("GROOVY"));
		assertFalse(scriptManager.isEngineSupported("JAVASCRIPT"));
	}
	
	public void testJavascriptWithVoidReturnType() {
		
		Object results;
		
		// scripts that dont return values
		results = scriptManager.runScript("var msg = 'ciao mondo';", "javascript", null);
		assertEquals(null, results);
		
		results = scriptManager.runScript("function hello(name) { return 'helo ' + name; }", "javascript", null);
		assertEquals(null, results);
		
	}
	
	public void testJavascriptReturnTypes() {
		Object results;
		
		results = scriptManager.runScript("'ciao mondo';", "javascript", null);
		assertNotNull(results);
		assertEquals(String.class, results.getClass());
		assertEquals("ciao mondo", results);
		
		results = scriptManager.runScript("var msg = 'ciao mondo'; msg;", "javascript", null);
		assertNotNull(results);
		assertEquals(String.class, results.getClass());
		assertEquals("ciao mondo", results);
		
		results = scriptManager.runScript("'ciao ' + 'mondo';", "javascript", null);
		assertNotNull(results);
		assertEquals(String.class, results.getClass());
		assertEquals("ciao mondo", results);
		
		results = scriptManager.runScript("'Uno = ' + 1;", "javascript", null);
		assertNotNull(results);
		assertEquals(String.class, results.getClass());
		assertEquals("Uno = 1", results);
		
		results = scriptManager.runScript("1 + ' = Uno';", "javascript", null);
		assertNotNull(results);
		assertEquals(String.class, results.getClass());
		assertEquals("1 = Uno", results);
	
		results = scriptManager.runScript("5 + 2", "javascript", null);
		assertTrue(results.getClass() == Integer.class || results.getClass() == Double.class );
		
		results = scriptManager.runScript("5.0 + 2", "javascript", null);
		assertTrue(results.getClass() == Integer.class || results.getClass() == Double.class );
		
		
		results = scriptManager.runScript("var a = 5; var b = 2; a + b;", "javascript", null);
		assertEquals(Double.class, results.getClass());
		assertEquals(7D, results);
		
		results = scriptManager.runScript("var a = true; var b = false; a || b;", "javascript", null);
		assertEquals(Boolean.class, results.getClass());
		assertEquals(true, results);
		
		results = scriptManager.runScript("function hello(n){return 'ciao ' + n;}; hello('mondo');", "javascript", null);
		assertEquals(String.class, results.getClass());
		assertEquals("ciao mondo", results);
		
//		results = scriptManager.runScript("var a = new Array(); a;", "javascript", null);
//		assertEquals(Double.class, results.getClass());
//		assertEquals(7D, results);
	}
	
	public void testJavascriptBindings() {
		Object results;
		
		Map<String, Object> bindings = new HashMap<String, Object>();
		Map<String, String> profileAttributes = new HashMap<String, String>();
		profileAttributes.put("name", "andrea");
		profileAttributes.put("surname", "gioia");
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("family", "food");
		parameters.put("brand", "barilla");
		
		bindings.put("attributes", profileAttributes);
		bindings.put("parameters", parameters);
		results = scriptManager.runScript("if(attributes.get('name') == 'andrea') {parameters.get('family');} else {parameters.get('brand');}", "javascript", bindings);
		assertNotNull(results);
		assertEquals(String.class, results.getClass());
		assertEquals("food", results);
	}
	
	public void testGroovySimpleScripts() {		
		Object results;
		
		results = scriptManager.runScript("'ciao mondo'", "groovy", null);
		assertEquals(results.getClass(), String.class);
		assertEquals("ciao mondo", results);
		
		results = scriptManager.runScript("'ciao mondo';", "groovy", null);
		assertEquals("ciao mondo", results);

		results = scriptManager.runScript("5 + 2", "groovy", null);
		assertEquals(Integer.class, results.getClass());
		assertEquals(7, results);
	}
}
