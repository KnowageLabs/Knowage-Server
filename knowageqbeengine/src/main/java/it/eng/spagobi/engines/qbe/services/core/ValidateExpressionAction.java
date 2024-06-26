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
package it.eng.spagobi.engines.qbe.services.core;

import it.eng.qbe.script.groovy.GroovyScriptAPI;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.groovy.GroovySandbox;
import it.eng.spagobi.utilities.service.JSONAcknowledge;
import it.eng.spagobi.utilities.service.JSONFailure;
import it.eng.spagobi.utilities.service.JSONResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class ValidateExpressionAction extends AbstractQbeEngineAction {

	public static final String EXPRESSION = "expression";
	public static final String FIELDS = "fields";

	public static final String SERVICE_NAME = "VALIDATE_EXPRESSION_ACTION";

	@Override
	public String getActionName() {
		return SERVICE_NAME;
	}

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(ValidateExpressionAction.class);

	@Override
	public void service(SourceBean request, SourceBean response) {

		String expression;
		JSONArray fieldsJSON;

		// filed in context (mapped by unique name and by alias)
		Set availableDMFields;
		Set availableQFields;

		// unresolved field reference errors stack
		List uresolvedReferenceErrors;
		List items;
		Pattern seedPattern;
		Matcher seedMatcher;

		// bindings
		Map attributes;
		Map parameters;
		Map qFields;
		Map dmFields;

		ScriptEngineManager scriptManager;
		ScriptEngine groovyScriptEngine;

		logger.debug("IN");

		try {
			super.service(request, response);

			expression = getAttributeAsString(EXPRESSION);
			logger.debug("Parameter [" + EXPRESSION + "] is equals to [" + expression + "]");
			Assert.assertNotNull(expression, "Parameter [" + EXPRESSION + "] cannot be null in oder to execute " + this.getActionName() + " service");

			fieldsJSON = getAttributeAsJSONArray(FIELDS);
			logger.debug("Parameter [" + FIELDS + "] is equals to [" + fieldsJSON + "]");
			Assert.assertNotNull(fieldsJSON, "Parameter [" + FIELDS + "] cannot be null in oder to execute " + this.getActionName() + " service");

			availableQFields = new HashSet();
			availableDMFields = new HashSet();
			for (int i = 0; i < fieldsJSON.length(); i++) {
				JSONObject field = fieldsJSON.getJSONObject(i);
				availableDMFields.add(field.getString("uniqueName"));
				availableQFields.add(field.getString("alias"));
			}

			attributes = new HashMap();
			UserProfile profile = (UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE);
			Iterator it = profile.getUserAttributeNames().iterator();
			while (it.hasNext()) {
				String attributeName = (String) it.next();
				Object attributeValue = profile.getUserAttribute(attributeName);
				attributes.put(attributeName, attributeValue);
			}

			parameters = this.getEnv();

			// check for unresolved reference first
			uresolvedReferenceErrors = new ArrayList();

			seedPattern = Pattern.compile("'[^']*'");

			// ... in fields
			items = new ArrayList();
			items.addAll(extractItems("fields", expression));
			items.addAll(extractItems("qFields", expression));
			items.addAll(extractItems("dmFields", expression));
			qFields = new HashMap();
			dmFields = new HashMap();

			for (int i = 0; i < items.size(); i++) {
				String item = (String) items.get(i);
				seedMatcher = seedPattern.matcher(item);
				seedMatcher.find();
				String seed = seedMatcher.group().substring(1, seedMatcher.group().length() - 1);
				if (!availableQFields.contains(seed) && !availableDMFields.contains(seed)) {
					uresolvedReferenceErrors.add("Impossible to resolve reference to filed: " + item);
				}
				if (item.trim().startsWith("dmFields")) {
					dmFields.put(seed, 1000);
				} else {
					qFields.put(seed, 1000);
				}
			}

			// ... in attributes
			items = new ArrayList();
			items.addAll(extractItems("attributes", expression));
			for (int i = 0; i < items.size(); i++) {
				String item = (String) items.get(i);
				seedMatcher = seedPattern.matcher(item);
				seedMatcher.find();
				String seed = seedMatcher.group().substring(1, seedMatcher.group().length() - 1);
				if (!attributes.containsKey(seed)) {
					uresolvedReferenceErrors.add("Impossible to resolve reference to attribute: " + item);
				}
			}

			// ... in parameters
			items = new ArrayList();
			items.addAll(extractItems("parameters", expression));
			for (int i = 0; i < items.size(); i++) {
				String item = (String) items.get(i);
				seedMatcher = seedPattern.matcher(item);
				seedMatcher.find();
				String seed = seedMatcher.group().substring(1, seedMatcher.group().length() - 1);
				if (!parameters.containsKey(seed)) {
					uresolvedReferenceErrors.add("Impossible to resolve reference to parameter: " + item);
				}
			}

			JSONResponse jsonResponse = null;
			if (uresolvedReferenceErrors.size() > 0) {
				SpagoBIEngineServiceException validationException;
				String msg = "Unresolved reference error: ";
				for (int i = 0; i < uresolvedReferenceErrors.size(); i++) {
					String error = (String) uresolvedReferenceErrors.get(i);
					msg += "\n" + error + "; ";
				}
				validationException = new SpagoBIEngineServiceException(getActionName(), msg);
				jsonResponse = new JSONFailure(validationException);
			} else {

				Map<String, Object> bindings = new HashMap<String, Object>();

				// bindings ...
				bindings.put("attributes", attributes);
				bindings.put("parameters", parameters);
				bindings.put("qFields", qFields);
				bindings.put("dmFields", dmFields);
				bindings.put("fields", qFields);
				bindings.put("api", new GroovyScriptAPI());

				Object calculatedValue = null;
				try {
					GroovySandbox gs = new GroovySandbox(new Class[] { GroovyScriptAPI.class });
					gs.setBindings(bindings);
					calculatedValue = gs.evaluate(expression);
					jsonResponse = new JSONAcknowledge();
				} catch (Exception e) {
					SpagoBIEngineServiceException validationException;
					Throwable t = e;
					String msg = t.getMessage();
					while ((msg = t.getMessage()) == null && t.getCause() != null)
						t = t.getCause();
					if (msg == null)
						msg = e.toString();
					// msg = "Syntatic error at line:" + e.getLineNumber() + ", column:" + e.getColumnNumber() + ". Error details: " + msg;
					validationException = new SpagoBIEngineServiceException(getActionName(), msg, e);
					jsonResponse = new JSONFailure(validationException);
					// logger.error("validation error", e);
				}
			}

			try {
				writeBackToClient(jsonResponse);
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}

		} catch (Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}

	}

	private List extractItems(String itemGroupName, String expression) {
		List items;
		Pattern itemPattern;
		Matcher itemMatcher;

		items = new ArrayList();

		itemPattern = Pattern.compile(itemGroupName + "\\['[^']+'\\]");
		itemMatcher = itemPattern.matcher(expression);
		while (itemMatcher.find()) {
			items.add(itemMatcher.group());
		}
		return items;
	}
}
