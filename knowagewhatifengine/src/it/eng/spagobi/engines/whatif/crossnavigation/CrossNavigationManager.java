/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.whatif.crossnavigation;



import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import java.text.MessageFormat;
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

public class CrossNavigationManager {
	public static transient Logger logger = Logger.getLogger(CrossNavigationManager.class);
	public static String buildCrossNavigationUrl(int targetIndex,SpagoBICellWrapper cellWrapper,WhatIfEngineInstance ei) throws Exception{
		logger.debug("IN");
		SpagoBIPivotModel modelWrapper = ei.getSpagoBIPivotModel();
		Target target = modelWrapper.getCrossNavigation().getTargets().get(targetIndex);
		List<TargetParameter> parameters = target.getParameters();
		StringBuffer buffer = new StringBuffer("parent.execCrossNavigation(window.name, '" 
				+ StringEscapeUtils.escapeJavaScript(target.documentLabel) + "', '");
		 if (!parameters.isEmpty()) {
		    	for (int i = 0; i < parameters.size(); i++) {
		    		TargetParameter aParameter = parameters.get(i);
			    	String parameterName = aParameter.name;
			    	String parameterValue = getParameterValue(aParameter, ei, cellWrapper);
			    	if (parameterValue != null) {
			    		buffer.append(StringEscapeUtils.escapeJavaScript(parameterName + "=" + parameterValue + "&"));
			    	}
		    	}
		    }
		 
	    	if (buffer.charAt(buffer.length() - 1) == '&') {
	    		buffer.deleteCharAt(buffer.length() - 1);
	    	}
	    	if (target.customizedView != null) {
	    		buffer.append("', '" + StringEscapeUtils.escapeJavaScript(target.customizedView) + "'");
	    	} else {
	    		buffer.append("', ''");
	    	}
	    	
	    	if(target.titleCross!=null && target.targetCross!=null && target.targetCross.equalsIgnoreCase("tab")){
	    		buffer.append(",'"+target.titleCross+"','tab'");
			}else if(target.titleCross!=null){
				buffer.append(",'"+target.titleCross+"'");
			}
	    	
	    	buffer.append(");");
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
		logger.debug("Looking for dimension " + dimensionName + ", hierarchy " + hierarchyName + ", level " + levelName + ", property " + propertyName + " ...");
		Hierarchy hierarchy = getHierarchy(ei.getSpagoBIPivotModel().getCube(), ei.getModelConfig(),dimensionName);
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
			logger.debug("Member level matches input level name " + levelName + "!!");
			String uniqueName = member.getUniqueName();
			toReturn = uniqueName.substring(uniqueName.lastIndexOf("].[") + 3, uniqueName.lastIndexOf("]"));
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
		if(property != null){
			try {
				toReturn = member.getPropertyValue(property).toString();
			} catch (OlapException e) {
				logger.error("Error getting the property " + propertyName + " from the cube ", e);
				throw new SpagoBIEngineRuntimeException("Error getting the property " + propertyName +" ", e);		
			}			  
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	public static Hierarchy getHierarchy(Cube cube, ModelConfig modelConfig, String dimensionName) {
		List<Dimension> dimensions = cube.getDimensions();
		Dimension result = null;
		for (java.util.Iterator<Dimension> iterator = dimensions.iterator(); iterator.hasNext();) {
			Dimension dimension = iterator.next();
			if (dimension.getUniqueName().equals("["+dimensionName+"]")) {
				result = dimension;
			}
		}
		if (result == null) {
			logger.error("Could not find dimension");
			throw new SpagoBIEngineRuntimeException("Could not find dimension");
		}
		logger.debug("Found dimension " + result.getUniqueName());

		// get Hierarchy Used by dimension 
		NamedList<Hierarchy> hierarchies = result.getHierarchies();
		Hierarchy hierarchy = null;
		if (hierarchies == null || hierarchies.size() == 0) {
			logger.error("Could not find hierarchies for dimension");
			throw new SpagoBIEngineRuntimeException("Could not find hierarchies for dimension");
		}
		else if (hierarchies.size() == 1) {
			hierarchy = hierarchies.get(0);
		}
		else {
			String hierarchyUsed = modelConfig.getDimensionHierarchyMap().get(dimensionName);
			hierarchy = hierarchies.get(hierarchyUsed);
		}
		return hierarchy;
	}

	
	public static String buildClickableUrl(Member member, List<TargetClickable> targetsClickable) {
		logger.debug("IN");
		Level level = member.getLevel();
		String url = new String();
		for (TargetClickable tc : targetsClickable) {
			if(tc.getUniqueName().equalsIgnoreCase(level.getUniqueName())){
				String targetDocumentParameters = "";
				for(Map.Entry<String,String> entry : tc.getParametersList().entrySet()) {
					  String key = entry.getKey();
					  String value = entry.getValue();
					  targetDocumentParameters += key + "=" + value + "&";
				}
				url = "javascript:parent.execCrossNavigation(window.name, ''" + tc.getTargetDocument() + "'', ''" + targetDocumentParameters + "''";
				if(tc.getTitle()!=null && tc.getTarget()!=null && tc.getTarget().equalsIgnoreCase("tab")){
					url +=",null,''"+tc.getTitle()+"'',''tab''";
				}else if(tc.getTitle()!=null){
					url +=",null,''"+tc.getTitle()+"''";
				}
				url +=");";
				//int lastIndex=member.getUniqueName().lastIndexOf("].[");
				//String newName=member.getUniqueName().substring(lastIndex+3, member.getUniqueName().length()-1);
				String newName = getLevelValue(member, tc.getUniqueName());
				Object[] args = new Object[] {newName};
				url = MessageFormat.format(url, args);
				return url;
			}else{
				url = null;
			}
		}
		return url;	
	}
	

}
