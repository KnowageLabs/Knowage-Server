/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.hibernate;

import it.eng.qbe.datasource.IPersistenceManager;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration.Column;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.property.Setter;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.Type;
import org.json.JSONObject;

public class HibernatePersistenceManager implements IPersistenceManager {
	
	public HibernatePersistenceManager(HibernateDataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	private HibernateDataSource dataSource;
	
	public static transient Logger logger = Logger.getLogger(HibernatePersistenceManager.class);
	
	public HibernateDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(HibernateDataSource dataSource) {
		this.dataSource = dataSource;
	}

	
	
	public void updateRecord(JSONObject aRecord,
			RegistryConfiguration registryConf) {
		
		SessionFactory sf = dataSource.getHibernateSessionFactory();
		Configuration cfg = dataSource.getHibernateConfiguration();
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = sf.openSession();
			tx = aSession.beginTransaction();
			String entityName = registryConf.getEntity();
			PersistentClass classMapping = cfg.getClassMapping(entityName);
			ClassMetadata classMetadata = sf.getClassMetadata(entityName);
			String keyName = classMetadata.getIdentifierPropertyName();
			Object key = aRecord.get(keyName);
			Property propertyId = classMapping.getProperty(keyName);

			//casts the id to the appropriate java type
			Object keyConverted = this.convertValue(key, propertyId);
			
			
			Object obj = aSession.load(entityName, (Serializable) keyConverted);
			Iterator it = aRecord.keys();
			while (it.hasNext()) {
				String aKey = (String) it.next();
				if (keyName.equals(aKey)) {
					continue;
				}
				Column c = registryConf.getColumnConfiguration(aKey);
				if (c.getSubEntity() != null) {
					// case of foreign key
					Property property = classMapping.getProperty(c.getSubEntity());
					Type propertyType = property.getType();	
					if (propertyType instanceof ManyToOneType) { 
				 		ManyToOneType manyToOnePropertyType = (ManyToOneType) propertyType; 
				 		String entityType = manyToOnePropertyType.getAssociatedEntityName();
						Object referenced = getReferencedObject(aSession, entityType, c.getField(), aRecord.get(aKey));
						Setter setter = property.getSetter(obj.getClass());
						setter.getMethod().invoke(obj, referenced);
					} else {
						throw new SpagoBIRuntimeException("Property " + c.getSubEntity() + " is not a many-to-one relation");
					}
				} else {
					// case of property
					Property property = classMapping.getProperty(aKey);
					Setter setter = property.getSetter(obj.getClass());
					Object valueObj = aRecord.get(aKey);
					if(valueObj != null && !valueObj.equals("")){
						Object valueConverted = this.convertValue(valueObj, property);
						setter.getMethod().invoke(obj, valueConverted);
					}

				}

			}
			aSession.saveOrUpdate(obj);
			tx.commit();
			
		} catch (Exception e) {

			if ( tx != null ) {
				tx.rollback();
			}
			throw new RuntimeException(e);
		} finally {
			if ( aSession != null ) {
				if ( aSession.isOpen() ) aSession.close();
			}
		}
	}

	private Object convertValue(Object valueObj, Property property) {
		if (valueObj == null) {
			return null;
		}
		String value = valueObj.toString();
		Object toReturn = null;
		Type type = property.getType();
		Class clazz = type.getReturnedClass();
		
		if( Number.class.isAssignableFrom(clazz) ) {
			//BigInteger, Integer, Long, Short, Byte
			if(value.equals("NaN") || value.equals("null")){
				toReturn = null;
				return toReturn;
			}
			if (Integer.class.isAssignableFrom(clazz)) {
				toReturn = Integer.parseInt(value);
			} else if (BigInteger.class.isAssignableFrom(clazz)) {
				toReturn = new BigInteger(value);
			} else if (BigDecimal.class.isAssignableFrom(clazz)) {
				toReturn = new BigDecimal(value);
			} else if (Long.class.isAssignableFrom(clazz)) {
				toReturn = new Long(value);
			} else if (Short.class.isAssignableFrom(clazz)) {
				toReturn = new Short(value);
			} else if (Byte.class.isAssignableFrom(clazz)) {
				toReturn = new Byte(value);
			} else {
				toReturn = new Float(value);
			}
		} else if( String.class.isAssignableFrom(clazz) ) {
			toReturn = value;
		} else if( Timestamp.class.isAssignableFrom(clazz) ) {
			// TODO manage dates
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
			try {
				toReturn = sdf.parse(value);
			} catch (ParseException e) {
				logger.error("Unparsable timestamp", e);
			}

		} else if( Date.class.isAssignableFrom(clazz) ) {
			// TODO manage dates
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			
			try {
				toReturn = sdf.parse(value);
			} catch (ParseException e) {
				logger.error("Unparsable date", e);
			}

		} else if( Boolean.class.isAssignableFrom(clazz) ) {
			toReturn = Boolean.parseBoolean(value);
		} else {
			toReturn = value;
		}
		
		return toReturn;
	}

	private Object getReferencedObject(Session aSession, String entityType,
			String field, Object value) {
		Query query = aSession.createQuery(" from " + entityType + " where " + field + " = ?");
		query.setParameter(0, value);
		List result = query.list();
		if (result == null || result.size() == 0) {
			throw new SpagoBIRuntimeException("Record with " + field + " equals to " + value.toString() + " not found for entity " + entityType);
		}
		if (result.size() > 1) {
			throw new SpagoBIRuntimeException("More than 1 record with " + field + " equals to " + value.toString() + " in entity " + entityType);
		}
		return result.get(0);
	}

	public Integer insertRecord(JSONObject aRecord,
			RegistryConfiguration registryConf, boolean autoLoadPK, String tableForPkMax, String columnForPkMax) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteRecord(JSONObject aRecord,
			RegistryConfiguration registryConf) {
		// TODO Auto-generated method stub
		
	}

	public String getKeyColumn(JSONObject aRecord,
			RegistryConfiguration registryConf) {
		// TODO Auto-generated method stub
		return null;
	}

}
