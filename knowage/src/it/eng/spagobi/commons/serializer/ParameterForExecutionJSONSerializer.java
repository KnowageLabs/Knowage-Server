/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.analiticalmodel.execution.service.GetParametersForExecutionAction;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class ParameterForExecutionJSONSerializer implements Serializer {

	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject result = null;

		if (!(o instanceof GetParametersForExecutionAction.ParameterForExecution)) {
			throw new SerializationException("ParameterForExecutionJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}

		try {
			GetParametersForExecutionAction.ParameterForExecution parameter = (GetParametersForExecutionAction.ParameterForExecution) o;
			result = new JSONObject();
			result.put("id", parameter.getId());
			MessageBuilder msgBuild = new MessageBuilder();

			// String label=msgBuild.getUserMessage(parameter.getLabel(),null,
			// locale);
			String label = parameter.getLabel();
			label = msgBuild.getI18nMessage(locale, label);

			result.put("label", label);
			result.put("type", parameter.getParType());
			result.put("selectionType", parameter.getSelectionType());
			result.put("allowInternalNodeSelection", parameter.getPar().getModalityValue().getLovProvider().contains("<LOVTYPE>treeinner</LOVTYPE>"));
			result.put("enableMaximizer", parameter.isEnableMaximizer());
			result.put("typeCode", parameter.getTypeCode());
			result.put("mandatory", parameter.isMandatory());
			result.put("colspan", parameter.getColspan());
			result.put("thickPerc", parameter.getThickPerc());
			result.put("multivalue", parameter.isMultivalue());
			result.put("visible", parameter.isVisible());
			result.put("valuesCount", parameter.getValuesCount());
			result.put("valueSelection", parameter.getValueSelection());
			result.put("selectedLayer", parameter.getSelectedLayer());
			result.put("selectedLayerProp", parameter.getSelectedLayerProp());
			
			if (parameter.getValuesCount() == 1) {
				result.put("value", parameter.getValue());
			}

			if (parameter.getObjParameterIds() != null) {
				JSONArray objParameterIds = new JSONArray();
				for (Iterator iterator = parameter.getObjParameterIds().iterator(); iterator.hasNext();) {
					Integer id = (Integer) iterator.next();
					objParameterIds.put(id);
				}
				result.put("objParameterIds", objParameterIds);
			}

			JSONArray dependencies = new JSONArray();
			Iterator it = parameter.getDependencies().keySet().iterator();
			while (it.hasNext()) {
				String paramUrlName = (String) it.next();
				JSONObject dependency = new JSONObject();
				dependency.put("urlName", paramUrlName);
				dependency.put("hasDataDependency", false);
				dependency.put("hasVisualDependency", false);
				JSONArray visualDependencyConditions = new JSONArray();
				dependency.put("visualDependencyConditions", visualDependencyConditions);

				List<GetParametersForExecutionAction.ParameterForExecution.ParameterDependency> parameterDependencies;
				parameterDependencies = parameter.getDependencies().get(paramUrlName);

				for (int i = 0; i < parameterDependencies.size(); i++) {
					Object pd = parameterDependencies.get(i);
					if (pd instanceof GetParametersForExecutionAction.ParameterForExecution.DataDependency) {
						dependency.put("hasDataDependency", true);
					} else if (pd instanceof GetParametersForExecutionAction.ParameterForExecution.VisualDependency) {
						ObjParview visualCondition = ((GetParametersForExecutionAction.ParameterForExecution.VisualDependency) pd).condition;
						dependency.put("hasVisualDependency", true);
						JSONObject visualDependencyCondition = new JSONObject();
						visualDependencyCondition.put("operation", visualCondition.getOperation());
						visualDependencyCondition.put("value", visualCondition.getCompareValue());
						String viewLabel = visualCondition.getViewLabel();
						viewLabel = msgBuild.getI18nMessage(locale, viewLabel);
						visualDependencyCondition.put("label", viewLabel);
						visualDependencyConditions.put(visualDependencyCondition);
					}
				}

				dependencies.put(dependency);
			}
			result.put("dependencies", dependencies);
			result.put("parameterUseId", parameter.getParameterUseId());

			JSONArray defaultValues = new JSONArray();
			DefaultValuesList defaults = parameter.getDefaultValues();
			Iterator<DefaultValue> defaultsIt = defaults.iterator();
			while (defaultsIt.hasNext()) {
				DefaultValue aDefault = defaultsIt.next();
				JSONObject aDefaultJSON = new JSONObject();
				aDefaultJSON.put("value", aDefault.getValue());
				aDefaultJSON.put("description", aDefault.getDescription());
				defaultValues.put(aDefaultJSON);
			}
			result.put("defaultValues", defaultValues);
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}

		return result;
	}

}
