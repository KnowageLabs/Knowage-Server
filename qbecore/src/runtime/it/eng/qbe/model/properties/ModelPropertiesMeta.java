/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.properties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ModelPropertiesMeta {
	
	public final static ModelPropertyMeta[] globalProperties = new ModelPropertyMeta[]{
		new ModelPropertyMeta("recursiveFiltering", false, true, "enabled")
	};
	
	public final static ModelPropertyMeta[] entityProperties = new ModelPropertyMeta[]{
		new ModelPropertyMeta("visible", false, true, "true"),
		new ModelPropertyMeta("type", false, true, "dimension"),
		new ModelPropertyMeta("position", false, true, "" + Integer.MAX_VALUE)
	};
	
	public final static ModelPropertyMeta[] fieldProperties = new ModelPropertyMeta[]{
		new ModelPropertyMeta("visible", false, true, "true"),
		new ModelPropertyMeta("type", false, true, "attribute"),
		new ModelPropertyMeta("position", false, true, "" + Integer.MAX_VALUE),
		new ModelPropertyMeta("format", false, true, null)
	};
	
	static Map<String, ModelPropertyMeta> globalPropertiesMap;
	static Map<String, ModelPropertyMeta> entityPropertiesMap;
	static Map<String, ModelPropertyMeta> fieldPropertiesMap;
	
	static {
		globalPropertiesMap = new HashMap<String, ModelPropertyMeta>();
		entityPropertiesMap = new HashMap<String, ModelPropertyMeta>();
		fieldPropertiesMap = new HashMap<String, ModelPropertyMeta>();
		
		for(int i = 0; i < globalProperties.length; i++) globalPropertiesMap.put(globalProperties[i].getName(), globalProperties[i]);
		for(int i = 0; i < entityProperties.length; i++) entityPropertiesMap.put(entityProperties[i].getName(), entityProperties[i]);
		for(int i = 0; i < fieldProperties.length; i++) fieldPropertiesMap.put(fieldProperties[i].getName(), fieldProperties[i]);
	}
	
	public ModelPropertyMeta getGlobalProperty(String name) {return globalPropertiesMap.get(name);}
	public ModelPropertyMeta getEntityProperties(String name) {return entityPropertiesMap.get(name);}
	public ModelPropertyMeta getFieldProperties(String name) {return fieldPropertiesMap.get(name);}
}
