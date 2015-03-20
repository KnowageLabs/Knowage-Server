/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.template;


import it.eng.qbe.model.accessmodality.AbstractModelAccessModality;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.ModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.Filter;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;


/**
 * The Class DataMartModelAccessModality.
 * 
 * @author Andrea Gioia
 */
public class QbeXMLModelAccessModality extends AbstractModelAccessModality {
	
	
	SourceBean modalitySB = null;
	
	Map entityAccessModalityMap = null;
	Boolean recursiveFiltering = null;
	

	/** The Constant TAG_MODALITIES. */
	private static final String TAG_MODALITIES = "MODALITIES";
	
	
	/** The Constant TAG_MODALITY. */
	private static final String TAG_MODALITY = "MODALITY";
	
	/** The Constant TAG_ENTITIES. */
	private static final String TAG_ENTITIES = "TABLES";
	
	/** The Constant TAG_ENTITY. */
	private static final String TAG_ENTITY = "TABLE";
	
	/** The Constant TAG_FIELDS. */
	private static final String TAG_FIELDS = "FIELDS";
	
	/** The Constant TAG_FIELD. */
	private static final String TAG_FIELD = "FIELD";
	
	/** The Constant TAG_FILTERS. */
	private static final String TAG_FILTERS = "FILTERS";
	
	/** The Constant TAG_FILTER. */
	private static final String TAG_FILTER = "FILTER";
	
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(QbeXMLModelAccessModality.class);
	
	
	public QbeXMLModelAccessModality() {}
	
	
	
	
	public QbeXMLModelAccessModality(File file) {
		String recursiveFilteringAttr;
		try {			
			modalitySB = SourceBean.fromXMLStream(
					new InputSource(new FileInputStream(file)));
			initRecursiveFiltering(modalitySB);
			entityAccessModalityMap = buildEntityAccessModalityMap(modalitySB);
		} catch (SourceBeanException e) {
			logger.error("Impossible to parse access modality properties from file " + file.toString() 
					+ ": \n" + e.toString());
		} catch (FileNotFoundException e) {
			logger.error("Impossible to load access modality properties from file " + file.toString() 
					+ ": \n" + e.toString());
		}
	}
	
	/**
	 * Instantiates a new data mart model access modality.
	 * 
	 * @param file the file
	 * @param modalityName the modality name
	 */
	public QbeXMLModelAccessModality(File file, String modalityName) {
		try {			
			SourceBean modalitiesSB = SourceBean.fromXMLStream(
					new InputSource(new FileInputStream(file)));
			modalitySB = getModalitySBByName(modalitiesSB, modalityName);
			initRecursiveFiltering(modalitySB);
			entityAccessModalityMap = buildEntityAccessModalityMap(modalitySB);
		} catch (SourceBeanException e) {
			logger.error(
					"Impossible to parse access modality properties from file " + file.toString() 
					+ ": \n" + e.toString());
		} catch (FileNotFoundException e) {
			logger.error( 
					"Impossible to load access modality properties from file " + file.toString() 
					+ ": \n" + e.toString());
		}
	}
	
	/**
	 * Instantiates a new data mart model access modality.
	 * 
	 * @param modalitiesSB the modalities sb
	 * @param modalityName the modality name
	 */
	public QbeXMLModelAccessModality(SourceBean modalitiesSB, String modalityName) {
		modalitySB = getModalitySBByName(modalitiesSB, modalityName);
		initRecursiveFiltering(modalitySB);
		entityAccessModalityMap = buildEntityAccessModalityMap(modalitySB);
	}
	
	/**
	 * Instantiates a new data mart model access modality.
	 * 
	 * @param modalitySB the modality sb
	 */
	public QbeXMLModelAccessModality(SourceBean modalitySB) {
		this.modalitySB = modalitySB;
		initRecursiveFiltering(modalitySB);
		entityAccessModalityMap = buildEntityAccessModalityMap(modalitySB);
	}
	
	
	private Boolean initRecursiveFiltering(SourceBean modalitySB) {
		String recursiveFilteringAttr;
		
		recursiveFilteringAttr = (String)modalitySB.getAttribute(ATTR_RECURSIVE_FILTERING);
		if(!StringUtilities.isEmpty(recursiveFilteringAttr)) {
			logger.debug("Attribute [" + ATTR_RECURSIVE_FILTERING + "] is equal to ["+ recursiveFilteringAttr + "]");
			if("disabled".equalsIgnoreCase(recursiveFilteringAttr)) {
				recursiveFiltering = Boolean.FALSE;
			} else {
				recursiveFiltering = Boolean.TRUE;
			}
			logger.debug("Recursive filtering enabled [" + recursiveFiltering + "]");
		}
		
		return recursiveFiltering;
	}
	
	/**
	 * Gets the modality sb by name.
	 * 
	 * @param modalitiesSB the modalities sb
	 * @param modalityName the modality name
	 * 
	 * @return the modality sb by name
	 */
	private SourceBean getModalitySBByName(SourceBean modalitiesSB, String modalityName) {
		SourceBean modalitySB = null;
		if(modalityName != null) {
			modalitySB = (SourceBean)modalitiesSB.getFilteredSourceBeanAttribute(TAG_MODALITY, "name", modalityName);	
     		
		} else {
			List modalities = modalitySB.getAttributeAsList(TAG_MODALITY);
			if(modalities != null && modalities.size() > 0) {
				modalitySB = (SourceBean)modalities.get(0);
			}
		}
		return modalitySB;
	}
	
	/**
	 * Builds the entity access modality map.
	 * 
	 * @param modalitySB the modality sb
	 * 
	 * @return the map
	 */
	private Map buildEntityAccessModalityMap(SourceBean modalitySB) {
		Map map = new HashMap();
		
		List entities =  modalitySB.getAttributeAsList(TAG_ENTITY);
		SourceBean entitySB;
		EntityAccessModalitty entityAccessModality;
		for(int i = 0; i < entities.size(); i++) {
			entitySB = (SourceBean)entities.get(i);
			String entityName = (String)entitySB.getAttribute("name");
			String accessibleStr = (String)entitySB.getAttribute("accessible");
			boolean accessibleFlg = accessibleStr == null? true: accessibleStr.equalsIgnoreCase("TRUE");
			entityAccessModality = new EntityAccessModalitty(entityName, accessibleFlg);
			
			// get filed accessibility constraints
			List fields = entitySB.getAttributeAsList(TAG_FIELDS + "." + TAG_FIELD);
			SourceBean fieldSB;
			for(int j = 0; j < fields.size(); j++) {
				fieldSB = (SourceBean)fields.get(j);
				String fieldName = (String)fieldSB.getAttribute("name");
				String accesible = (String)fieldSB.getAttribute("accessible");
				if(accesible.equalsIgnoreCase("FALSE")) {
					entityAccessModality.setFieldUnaccessible(fieldName);
				}				
			}
			// get entity filter conditions
			List filters = entitySB.getAttributeAsList(TAG_FILTERS + "." + TAG_FILTER);
			SourceBean filterSB;
			for(int j = 0; j < filters.size(); j++) {
				filterSB = (SourceBean)filters.get(j);
				String filterCondition = filterSB.getCharacters();
				entityAccessModality.addFilterConditions(filterCondition);
								
			}
			map.put(entityAccessModality.getName(), entityAccessModality);
		}
		
		return map;
	}

	
	public boolean isEntityAccessible(IModelEntity entity) {	
		
		// ERROR: accessibility of entities is type based: ignore the role
		/*
		if(entityAccessModalityMap != null && entityAccessModalityMap.containsKey( entity.getUniqueType() )) {
			EntityAccessModalitty entityAccessModalitty = (EntityAccessModalitty)entityAccessModalityMap.get( entity.getUniqueType() );
			return entityAccessModalitty.isAccessible();
		}
		*/
		if(entityAccessModalityMap != null && entityAccessModalityMap.containsKey( entity.getType() )) {
			EntityAccessModalitty entityAccessModalitty = (EntityAccessModalitty)entityAccessModalityMap.get( entity.getType() );
			return entityAccessModalitty.isAccessible();
		}
		return true;
	}
	
	/**
	 * Checks if is field accessible.
	 * 
	 * @param tableName the table name
	 * @param fieldName the field name
	 * 
	 * @return true, if is field accessible
	 */
	public boolean isFieldAccessible( IModelField field ) {		
		if(entityAccessModalityMap != null && entityAccessModalityMap.containsKey( field.getParent().getType() )) {
			EntityAccessModalitty tableAccessModalitty = (EntityAccessModalitty)entityAccessModalityMap.get( field.getParent().getType() );
			return tableAccessModalitty.isFieldAccessible( field.getName() );
		}
		return true;
	}
	

	public List getEntityFilterConditions(String entityName) {
		if(entityAccessModalityMap != null && entityAccessModalityMap.containsKey(entityName)) {
			EntityAccessModalitty entityAccessModalitty = (EntityAccessModalitty)entityAccessModalityMap.get(entityName);
			return entityAccessModalitty.getFilterConditions();
		}
		return new ArrayList();
	}
	
	public Boolean getRecursiveFiltering() {
		return recursiveFiltering;
	}

	public void setRecursiveFiltering(Boolean recursiveFiltering) {
		this.recursiveFiltering = recursiveFiltering;
	}
	
		
	/**
	 * The Class EntityAccessModalitty.
	 */
	public static class EntityAccessModalitty  {
		
		/** The name. */
		String name;
		
		/** The accessible. */
		boolean accessible;
		
		/** The fileds access modality map. */
		Map filedsAccessModalityMap;
		
		/** The filter conditions. */
		List filterConditions;
		
		/**
		 * Instantiates a new entity access modalitty.
		 * 
		 * @param tableName the table name
		 * @param accessible the accessible
		 */
		public EntityAccessModalitty(String tableName, boolean accessible) {
			this.name = tableName;
			this.accessible = accessible;
			filedsAccessModalityMap = new HashMap();
			filterConditions = new ArrayList();
		}
		
		/**
		 * Sets the field unaccessible.
		 * 
		 * @param fieldName the new field unaccessible
		 */
		public void setFieldUnaccessible(String fieldName) {
			filedsAccessModalityMap.put(fieldName, "UNACCESSIBLE");
		}
		
		/**
		 * Checks if is field accessible.
		 * 
		 * @param fieldName the field name
		 * 
		 * @return true, if is field accessible
		 */
		public boolean isFieldAccessible(String fieldName) {
			boolean accessible = !filedsAccessModalityMap.containsKey(fieldName);
			return accessible;
		}

		/**
		 * Gets the name.
		 * 
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * Checks if is accessible.
		 * 
		 * @return true, if is accessible
		 */
		public boolean isAccessible() {
			return accessible;
		}
		
		/**
		 * Adds the filter conditions.
		 * 
		 * @param condition the condition
		 */
		public void addFilterConditions(String condition) {
			Filter filter = new Filter(name, condition);
			filterConditions.add(filter);
		}

		/**
		 * Gets the filter conditions.
		 * 
		 * @return the filter conditions
		 */
		public List getFilterConditions() {
			return filterConditions;
		}
		
		
	}
}
