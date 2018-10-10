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

package it.eng.spagobi.engines.whatif.crossnavigation;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Property;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

public class CrossNavigationManager {
	public static transient Logger logger = Logger.getLogger(CrossNavigationManager.class);

	public static String buildCrossNavigationUrl(SpagoBICellWrapper cellWrapper, WhatIfEngineInstance ei) throws Exception {
		logger.debug("IN");
		SpagoBIPivotModel modelWrapper = ei.getSpagoBIPivotModel();
		List<TargetParameter> parameters = modelWrapper.getCrossNavigation().getParameters();

		StringBuffer buffer = new StringBuffer("parent.execExternalCrossNavigation({");
		if (!parameters.isEmpty()) {
			for (int i = 0; i < parameters.size(); i++) {
				TargetParameter aParameter = parameters.get(i);
				if (!aParameter.isAbsolute()) {// absolute will be managed from external cross navigation
					String parameterName = aParameter.name;
					String parameterValue = getParameterValue(aParameter, ei, cellWrapper);
					if (parameterValue != null && !parameterValue.equals("")) {
						parameterValue = "'" + StringEscapeUtils.escapeJavaScript(parameterValue) + "'";
					} else {
						parameterValue = "''";
					}
					if (parameterValue != null) {
						buffer.append(parameterName + ":" + parameterValue + ",");
					}
				}
			}
		}

		if (buffer.charAt(buffer.length() - 1) == ',') {
			buffer.deleteCharAt(buffer.length() - 1);
		}

		buffer.append("});");
		String toReturn = buffer.toString();
		logger.debug("OUT: returning [" + toReturn + "]");
		return toReturn;

	}

	private static String getParameterValue(TargetParameter parameter, WhatIfEngineInstance ei, SpagoBICellWrapper cell) {
		if (parameter.isAbsolute()) {
			return parameter.getValue();
		}
		String value = null;
		String dimensionName = parameter.getDimension();
		String hierarchyName = parameter.getHierarchy();
		String levelName = parameter.getLevel();
		String propertyName = parameter.getProperty();
		logger.debug(
				"Looking for dimension " + dimensionName + ", hierarchy " + hierarchyName + ", level " + levelName + ", property " + propertyName + " ...");
		Hierarchy hierarchy = getHierarchy(ei.getSpagoBIPivotModel().getCube(), ei.getModelConfig(), dimensionName, hierarchyName);
		Member member = cell.getContextMember(hierarchy);
		logger.debug("Considering context member " + member.getUniqueName());
		logger.debug("Member hierarchy is " + hierarchy.getUniqueName());
		if (hierarchy.getUniqueName().equals(hierarchyName)) {
			if (propertyName == null || propertyName.trim().equals("")) {
				value = getLevelValue(member, levelName);
			} else {
				value = getMemberPropertyValue(member, propertyName);
			}
		}
		return value;
	}

	private static String getLevelValue(Member member, String levelName) {
		logger.debug("IN: Member is " + member.getUniqueName() + ", levelName is " + levelName);
		String toReturn = null;
		Level level = member.getLevel();
		logger.debug("Member level is " + level.getUniqueName());
		if (level.getUniqueName().equals(levelName)) {
			level.getName();
			logger.debug("Member level matches input level name " + levelName + "!!");
			toReturn = member.getName();
		} else {
			logger.debug("Member level does NOT match input level name " + levelName + "!!");
			// look for parent member at parent level
			Member parent = member.getParentMember();
			if (parent == null) {
				return null;
			} else {
				return getLevelValue(parent, levelName);
			}
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	private static String getMemberPropertyValue(Member member, String propertyName) {
		logger.debug("IN: Member is " + member.getUniqueName() + ", propertyName is " + propertyName);
		String toReturn = null;
		Property property = member.getProperties().get(propertyName);
		if (property != null) {
			try {
				toReturn = member.getPropertyValue(property).toString();
			} catch (OlapException e) {
				logger.error("Error getting the property " + propertyName + " from the cube ", e);
				throw new SpagoBIEngineRuntimeException("Error getting the property " + propertyName + " ", e);
			}
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}

	public static Hierarchy getHierarchy(Cube cube, ModelConfig modelConfig, String dimensionName, String hierarchyName) {
		List<Dimension> dimensions = cube.getDimensions();
		Dimension result = null;
		for (java.util.Iterator<Dimension> iterator = dimensions.iterator(); iterator.hasNext();) {
			Dimension dimension = iterator.next();
			if (dimension.getUniqueName().equals("[" + dimensionName + "]")) {
				result = dimension;
				break;
			}
		}
		if (result == null) {
			logger.error("Could not find dimension" + dimensionName);
			throw new SpagoBIEngineRuntimeException("Could not find dimension" + dimensionName);
		}
		logger.debug("Found dimension " + result.getUniqueName());

		// get Hierarchy Used by dimension
		NamedList<Hierarchy> hierarchies = result.getHierarchies();
		for (Iterator iterator = hierarchies.iterator(); iterator.hasNext();) {
			Hierarchy aHierarchy = (Hierarchy) iterator.next();
			if (aHierarchy.getUniqueName().equals(hierarchyName)) {
				return aHierarchy;
			}
		}

		logger.error("Could not find hierarchy" + hierarchyName + " in dimension " + dimensionName);
		throw new SpagoBIEngineRuntimeException("Could not find hierarchy" + hierarchyName + " in dimension " + dimensionName);

	}

	public static String buildClickableUrl(Member member, List<TargetClickable> targetsClickable) {
		logger.debug("IN");
		Level level = member.getLevel();
		String url = new String();
		for (TargetClickable tc : targetsClickable) {
			if (tc.getUniqueName().equalsIgnoreCase(level.getUniqueName())) {
				StringBuffer targetDocumentParameters = new StringBuffer();
				for (Map.Entry<String, String> entry : tc.getParametersList().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					targetDocumentParameters.append(key);
					targetDocumentParameters.append(":");
					targetDocumentParameters.append(value);
					targetDocumentParameters.append(",");
				}

				if (targetDocumentParameters.length() > 0) {
					int index = targetDocumentParameters.lastIndexOf(",");
					targetDocumentParameters.setLength(index);
				}

				String documentParametersUnformatted = (targetDocumentParameters.toString());

				logger.debug("Composing cross nav url. Parameters unformatted = " + documentParametersUnformatted);
				String newName = getLevelValue(member, tc.getUniqueName());
				newName = "'" + StringEscapeUtils.escapeJavaScript(newName) + "'";
				Object[] args = new Object[] { newName };
				url = MessageFormat.format(documentParametersUnformatted, args);
				logger.debug("Composing cross nav url. Parameters formatted = " + url);

				url = "parent.execExternalCrossNavigation({" + (url) + "});";

				return url;
			} else {
				url = null;
			}
		}
		return url;
	}

}
