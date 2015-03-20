/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.jpa;

import it.eng.qbe.datasource.IPersistenceManager;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration.Column;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class JPAPersistenceManager implements IPersistenceManager {

	private JPADataSource dataSource;

	public static transient Logger logger = Logger.getLogger(JPAPersistenceManager.class);

	public JPAPersistenceManager(JPADataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	public JPADataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(JPADataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getKeyColumn(JSONObject aRecord, RegistryConfiguration registryConf) {
		String toReturn = null;

		logger.debug("IN");
		EntityManager entityManager = null;
		try {
			Assert.assertNotNull(aRecord, "Input parameter [record] cannot be null");
			Assert.assertNotNull(aRecord, "Input parameter [registryConf] cannot be null");

			logger.debug("New record: " + aRecord.toString(3));
			logger.debug("Target entity: " + registryConf.getEntity());

			entityManager = dataSource.getEntityManager();
			Assert.assertNotNull(entityManager, "entityManager cannot be null");

			EntityType targetEntity = getTargetEntity(registryConf, entityManager);
			String keyAttributeName = getKeyAttributeName(targetEntity);
			logger.debug("Key attribute name is equal to " + keyAttributeName);

			toReturn = keyAttributeName;

		} catch (Throwable t) {
			logger.error(t);
			throw new SpagoBIRuntimeException("Error searching for key column", t);
		} finally {
			if (entityManager != null) {
				if (entityManager.isOpen()) {
					entityManager.close();
				}
			}
		}

		logger.debug("OUT");
		return toReturn;
	}

	private synchronized Integer getPKValue(EntityType targetEntity, String keyColumn, EntityManager entityManager) {
		logger.debug("IN");
		Integer toReturn = 0;
		String name = targetEntity.getName();

		logger.debug("SELECT max(p." + keyColumn + ") as c FROM " + targetEntity.getName() + " p");
		// System.out.println("SELECT max(p."+keyColumn+") as c FROM "+targetEntity.getName()+" p");
		Query maxQuery = entityManager.createQuery("SELECT max(p." + keyColumn + ") as c FROM " + targetEntity.getName() + " p");

		Object result = maxQuery.getSingleResult();

		if (result != null) {
			toReturn = Integer.valueOf(result.toString());
			toReturn++;
		}

		logger.debug("New PK is " + toReturn);
		logger.debug("OUT");
		return toReturn;
	}

	private synchronized Integer getPKValueFromTemplateTable(String tableName, String keyColumn, EntityManager entityManager) {
		logger.debug("IN");
		Integer toReturn = 0;

		Query maxQuery = entityManager.createQuery("SELECT max(p." + keyColumn + ") as c FROM " + tableName + " p");

		Object result = maxQuery.getSingleResult();

		if (result != null) {
			toReturn = Integer.valueOf(result.toString());
			toReturn++;
		}

		logger.debug("New PK taken from table " + tableName + " is " + toReturn);
		logger.debug("OUT");
		return toReturn;
	}

	public Integer insertRecord(JSONObject aRecord, RegistryConfiguration registryConf, boolean autoLoadPK, String tableForPkMax, String columnForPkMax) {

		EntityTransaction entityTransaction = null;
		Integer toReturn = null;

		logger.debug("IN");
		EntityManager entityManager = null;
		try {
			Assert.assertNotNull(aRecord, "Input parameter [record] cannot be null");
			Assert.assertNotNull(aRecord, "Input parameter [registryConf] cannot be null");

			logger.debug("New record: " + aRecord.toString(3));
			logger.debug("Target entity: " + registryConf.getEntity());

			entityManager = dataSource.getEntityManager();
			Assert.assertNotNull(entityManager, "entityManager cannot be null");

			entityTransaction = entityManager.getTransaction();

			EntityType targetEntity = getTargetEntity(registryConf, entityManager);
			String keyAttributeName = getKeyAttributeName(targetEntity);
			logger.debug("Key attribute name is equal to " + keyAttributeName);
			// targetEntity.getI

			// if(autoLoadPK == true){
			// //remove key attribute
			// aRecord.remove(keyAttributeName);
			// }

			Iterator it = aRecord.keys();

			Object newObj = null;
			Class classToCreate = targetEntity.getJavaType();

			newObj = classToCreate.newInstance();
			logger.debug("Key column class is equal to [" + newObj.getClass().getName() + "]");

			while (it.hasNext()) {
				String attributeName = (String) it.next();
				logger.debug("Processing column [" + attributeName + "] ...");

				if (keyAttributeName.equals(attributeName)) {
					logger.debug("Skip column [" + attributeName + "] because it is the key of the table");
					continue;
				}
				Column column = registryConf.getColumnConfiguration(attributeName);
				List columnDepends = new ArrayList();
				if (column.getDependences() != null && !"".equals(column.getDependences())) {
					String[] dependences = column.getDependences().split(",");
					for (int i = 0; i < dependences.length; i++) {
						// get dependences informations
						Column dependenceColumn = getDependenceColumns(registryConf.getColumns(), dependences[i]);
						if (dependenceColumn != null)
							columnDepends.add(dependenceColumn);
					}
				}

				if (column.getSubEntity() != null) {
					logger.debug("Column [" + attributeName + "] is a foreign key");
					if (aRecord.get(attributeName) != null && !aRecord.get(attributeName).equals("")) {
						logger.debug("search foreign reference for value " + aRecord.get(attributeName));
						manageForeignKey(targetEntity, column, newObj, attributeName, aRecord, columnDepends, entityManager);
					} else {
						// no value in column, insert null
						logger.debug("No value for " + attributeName + ": keep it null");
					}

				} else {
					logger.debug("Column [" + attributeName + "] is a normal column");
					manageProperty(targetEntity, newObj, attributeName, aRecord);
				}
			}

			// calculate PK
			if (true || autoLoadPK == false) {

				Integer pkValue = null;
				// check if an alternative table and column has been specified
				// to retrieve PK
				String keyColumn = getKeyColumn(aRecord, registryConf);
				if (tableForPkMax != null && columnForPkMax != null) {
					logger.debug("Retrieve PK as max+1 from table: " + tableForPkMax + " / column: " + columnForPkMax);
					pkValue = getPKValueFromTemplateTable(tableForPkMax, columnForPkMax, entityManager);
					setKeyProperty(targetEntity, newObj, keyColumn, pkValue);
				} else {
					logger.debug("calculate max value +1 for key column " + keyColumn + " in table " + targetEntity.getName());
					pkValue = getPKValue(targetEntity, keyColumn, entityManager);
					setKeyProperty(targetEntity, newObj, keyColumn, pkValue);
				}

				if (pkValue == null) {
					logger.error("could not retrieve pk ");
					throw new Exception("could not retrieve pk for table " + targetEntity.getName());
				}

				toReturn = pkValue;
			}

			if (!entityTransaction.isActive()) {
				entityTransaction.begin();
			}

			entityManager.persist(newObj);
			entityManager.flush();
			entityTransaction.commit();

		} catch (Throwable t) {
			if (entityTransaction != null && entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			logger.error(t);
			throw new SpagoBIRuntimeException("Error saving entity", t);
		} finally {
			if (entityManager != null) {
				if (entityManager.isOpen()) {
					entityManager.close();
				}
			}
			logger.debug("OUT");
		}
		return toReturn;
	}

	public void updateRecord(JSONObject aRecord, RegistryConfiguration registryConf) {

		EntityTransaction entityTransaction = null;

		logger.debug("IN");
		EntityManager entityManager = null;
		try {
			Assert.assertNotNull(aRecord, "Input parameter [record] cannot be null");
			Assert.assertNotNull(aRecord, "Input parameter [registryConf] cannot be null");

			logger.debug("New record: " + aRecord.toString(3));
			logger.debug("Target entity: " + registryConf.getEntity());

			entityManager = dataSource.getEntityManager();
			Assert.assertNotNull(entityManager, "entityManager cannot be null");

			entityTransaction = entityManager.getTransaction();

			EntityType targetEntity = getTargetEntity(registryConf, entityManager);
			String keyAttributeName = getKeyAttributeName(targetEntity);
			logger.debug("Key attribute name is equal to " + keyAttributeName);

			Iterator it = aRecord.keys();

			Object keyColumnValue = aRecord.get(keyAttributeName);
			logger.debug("Key of new record is equal to " + keyColumnValue);
			logger.debug("Key column java type equal to [" + targetEntity.getJavaType() + "]");
			Attribute a = targetEntity.getAttribute(keyAttributeName);
			Object obj = entityManager.find(targetEntity.getJavaType(), this.convertValue(keyColumnValue, a));
			logger.debug("Key column class is equal to [" + obj.getClass().getName() + "]");

			while (it.hasNext()) {
				String attributeName = (String) it.next();
				logger.debug("Processing column [" + attributeName + "] ...");

				if (keyAttributeName.equals(attributeName)) {
					logger.debug("Skip column [" + attributeName + "] because it is the key of the table");
					continue;
				}
				Column column = registryConf.getColumnConfiguration(attributeName);
				List columnDepends = new ArrayList();
				if (column.getDependences() != null && !"".equals(column.getDependences())) {
					String[] dependences = column.getDependences().split(",");
					for (int i = 0; i < dependences.length; i++) {
						// get dependences informations
						Column dependenceColumns = getDependenceColumns(registryConf.getColumns(), dependences[i]);
						if (dependenceColumns != null)
							columnDepends.add(dependenceColumns);
					}
				}

				// if column is info column do not update
				if (!column.isInfoColumn()) {
					if (column.getSubEntity() != null) {
						logger.debug("Column [" + attributeName + "] is a foreign key");
						manageForeignKey(targetEntity, column, obj, attributeName, aRecord, columnDepends, entityManager);
					} else {
						logger.debug("Column [" + attributeName + "] is a normal column");
						manageProperty(targetEntity, obj, attributeName, aRecord);
					}
				}
			}

			if (!entityTransaction.isActive()) {
				entityTransaction.begin();
			}

			entityManager.persist(obj);
			entityManager.flush();
			entityTransaction.commit();

		} catch (Throwable t) {
			if (entityTransaction != null && entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			logger.error(t);
			throw new SpagoBIRuntimeException("Error saving entity", t);
		} finally {
			if (entityManager != null) {
				if (entityManager.isOpen()) {
					entityManager.close();
				}
			}
			logger.debug("OUT");
		}

	}

	public void deleteRecord(JSONObject aRecord, RegistryConfiguration registryConf) {

		EntityTransaction entityTransaction = null;

		logger.debug("IN");
		EntityManager entityManager = null;
		try {
			Assert.assertNotNull(aRecord, "Input parameter [record] cannot be null");
			Assert.assertNotNull(aRecord, "Input parameter [registryConf] cannot be null");

			logger.debug("Record: " + aRecord.toString(3));
			logger.debug("Target entity: " + registryConf.getEntity());

			entityManager = dataSource.getEntityManager();
			Assert.assertNotNull(entityManager, "entityManager cannot be null");

			entityTransaction = entityManager.getTransaction();

			EntityType targetEntity = getTargetEntity(registryConf, entityManager);
			String keyAttributeName = getKeyAttributeName(targetEntity);
			logger.debug("Key attribute name is equal to " + keyAttributeName);

			Iterator it = aRecord.keys();

			Object keyColumnValue = aRecord.get(keyAttributeName);
			logger.debug("Key of record is equal to " + keyColumnValue);
			logger.debug("Key column java type equal to [" + targetEntity.getJavaType() + "]");
			Attribute a = targetEntity.getAttribute(keyAttributeName);
			Object obj = entityManager.find(targetEntity.getJavaType(), this.convertValue(keyColumnValue, a));
			logger.debug("Key column class is equal to [" + obj.getClass().getName() + "]");

			if (!entityTransaction.isActive()) {
				entityTransaction.begin();
			}

			// String q =
			// "DELETE from "+targetEntity.getName()+" o WHERE o."+keyAttributeName+"="+keyColumnValue.toString();
			String q = "DELETE from " + targetEntity.getName() + " WHERE " + keyAttributeName + "=" + keyColumnValue.toString();
			logger.debug("create Query " + q);
			Query deleteQuery = entityManager.createQuery(q);

			int deleted = deleteQuery.executeUpdate();

			// entityManager.remove(obj);
			// entityManager.flush();
			entityTransaction.commit();

		} catch (Throwable t) {
			if (entityTransaction != null && entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			logger.error(t);
			throw new SpagoBIRuntimeException("Error deleting entity", t);
		} finally {
			if (entityManager != null) {
				if (entityManager.isOpen()) {
					entityManager.close();
				}
			}
			logger.debug("OUT");
		}

	}

	public EntityType getTargetEntity(RegistryConfiguration registryConf, EntityManager entityManager) {

		EntityType targetEntity;

		String targetEntityName = getTargetEntityName(registryConf);

		Metamodel classMetadata = entityManager.getMetamodel();
		Iterator it = classMetadata.getEntities().iterator();

		targetEntity = null;
		while (it.hasNext()) {
			EntityType entity = (EntityType) it.next();
			String jpaEntityName = entity.getName();

			if (entity != null && jpaEntityName.equals(targetEntityName)) {
				targetEntity = entity;
				break;
			}
		}

		return targetEntity;
	}

	public String getKeyAttributeName(EntityType entity) {
		logger.debug("IN : entity = [" + entity + "]");
		String keyName = null;
		for (Object attribute : entity.getAttributes()) {
			if (attribute instanceof SingularAttribute) {
				SingularAttribute s = (SingularAttribute) attribute;
				logger.debug("Attribute: " + s.getName() + " is a singular attribute.");
				if (s.isId()) {
					keyName = s.getName();
					break;
				}
			} else {
				logger.debug("Attribute " + attribute + " is not singular attribute, cannot manage it");
			}
		}
		Assert.assertNotNull(keyName, "Key attribute name was not found!");
		logger.debug("OUT : " + keyName);
		return keyName;
	}

	// case of foreign key
	private void manageForeignKey(EntityType targetEntity, Column c, Object obj, String aKey, JSONObject aRecord, List lstDependences,
			EntityManager entityManager) {

		logger.debug("column " + aKey + " is a FK");

		Attribute a = targetEntity.getAttribute(c.getSubEntity());

		Attribute.PersistentAttributeType type = a.getPersistentAttributeType();
		if (type.equals(PersistentAttributeType.MANY_TO_ONE)) {
			String entityType = a.getJavaType().getName();
			String subKey = a.getName();
			int lastPkgDotSub = entityType.lastIndexOf(".");
			String entityNameNoPkgSub = entityType.substring(lastPkgDotSub + 1);

			try {
				LinkedHashMap filtersForRef = new LinkedHashMap();
				filtersForRef.put(c.getField(), (aRecord.get(aKey)));
				// add dependences if they are
				if (lstDependences != null) {
					for (int i = 0; i < lstDependences.size(); i++) {
						Column tmpDep = (Column) lstDependences.get(i);
						if (!tmpDep.isInfoColumn() && tmpDep.getSubEntity() != null)
							filtersForRef.put(tmpDep.getSubEntity() + "." + tmpDep.getField(), (aRecord.get(tmpDep.getField())));
						else if (!tmpDep.isInfoColumn() && tmpDep.getSubEntity() == null)
							filtersForRef.put(tmpDep.getField(), (aRecord.get(tmpDep.getField())));
					}
				}
				Object referenced = getReferencedObjectJPA(entityManager, entityNameNoPkgSub, filtersForRef);

				Class clas = targetEntity.getJavaType();
				Field f = clas.getDeclaredField(subKey);
				f.setAccessible(true);
				// entityManager.refresh(referenced);
				f.set(obj, referenced);
			} catch (JSONException e) {
				logger.error(e);
				throw new SpagoBIRuntimeException("Property " + c.getSubEntity() + " is not a many-to-one relation", e);
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Error setting Field " + aKey + "", e);
			}
		} else {
			throw new SpagoBIRuntimeException("Property " + c.getSubEntity() + " is not a many-to-one relation");
		}
	}

	private void manageProperty(EntityType targetEntity, Object obj, String aKey, JSONObject aRecord) {

		logger.debug("IN");

		try {
			Attribute a = targetEntity.getAttribute(aKey);
			Class clas = targetEntity.getJavaType();
			Field f = clas.getDeclaredField(aKey);
			f.setAccessible(true);
			Object valueConverted = this.convertValue(aRecord.get(aKey), a);
			f.set(obj, valueConverted);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error setting Field " + aKey + "", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void setKeyProperty(EntityType targetEntity, Object obj, String aKey, Integer value) {

		logger.debug("IN");

		try {
			Attribute a = targetEntity.getAttribute(aKey);
			Class clas = targetEntity.getJavaType();
			Field f = clas.getDeclaredField(aKey);
			f.setAccessible(true);
			Object valueConverted = this.convertValue(value, a);
			f.set(obj, valueConverted);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error setting Field " + aKey + "", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private String getTargetEntityName(RegistryConfiguration registryConf) {
		String entityName = registryConf.getEntity();
		int lastPkgDot = entityName.lastIndexOf(".");
		String entityNameNoPkg = entityName.substring(lastPkgDot + 1);
		return entityNameNoPkg;
	}

	private Object convertValue(Object valueObj, Attribute attribute) {
		if (valueObj == null) {
			return null;
		}
		String value = valueObj.toString();
		Object toReturn = null;

		Class clazz = attribute.getJavaType();
		String clazzName = clazz.getName();
		logger.error("Field type: " + clazzName);

		if (Number.class.isAssignableFrom(clazz)) {
			if (value.equals("NaN") || value.equals("null") || value.equals("")) {
				toReturn = null;
				return toReturn;
			}
			// BigInteger, Integer, Long, Short, Byte
			if (Integer.class.getName().equals(clazzName)) {
				logger.error(">>> Integer");
				toReturn = Integer.parseInt(value);
			} else if (Double.class.getName().equals(clazzName)) {
				logger.error(">>> Double");
				toReturn = new Double(value);
			} else if (BigDecimal.class.getName().equals(clazzName)) {
				logger.error(">>> BigDecimal");
				toReturn = new BigDecimal(value);
			} else if (BigInteger.class.getName().equals(clazzName)) {
				logger.error(">>> BigInteger");
				toReturn = new BigInteger(value);
			} else if (Long.class.getName().equals(clazzName)) {
				logger.error(">>> Long");
				toReturn = new Long(value);
			} else if (Short.class.getName().equals(clazzName)) {
				logger.error(">>> Short");
				toReturn = new Short(value);
			} else if (Byte.class.getName().equals(clazzName)) {
				logger.error(">>> Byte");
				toReturn = new Byte(value);
			} else {
				logger.error(">>> Float");
				toReturn = new Float(value);
			}
		} else if (String.class.isAssignableFrom(clazz)) {
			if (value.equals("")) {
				toReturn = null;
			} else
				toReturn = value;
		} else if (Timestamp.class.isAssignableFrom(clazz)) {
			Date date;
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
			// SimpleDateFormat sdf = new
			// SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
			if (!value.equals("") && !value.contains(":")) {
				value += " 00:00:00";
			}
			try {
				date = sdf.parse(value);
				toReturn = new Timestamp(date.getTime());

			} catch (ParseException e) {
				logger.error("Unparsable timestamp", e);
			}

		} else if (Date.class.isAssignableFrom(clazz)) {
			// TODO manage dates
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

			try {
				toReturn = sdf.parse(value);
			} catch (ParseException e) {
				logger.error("Unparsable date", e);
			}

		} else if (Boolean.class.isAssignableFrom(clazz)) {
			toReturn = Boolean.parseBoolean(value);
		} else {
			toReturn = value;
		}

		return toReturn;
	}

	private Object getReferencedObjectJPA(EntityManager em, String entityType, HashMap whereFields) {
		// the master field element (for exception messages):
		String field = "";
		Object fieldValue = "";

		// Defining query ...
		String query = "select x from " + entityType + " x where ";
		Integer i = 0;
		for (Iterator iterator = whereFields.keySet().iterator(); iterator.hasNext();) {
			String andOperator = (i < whereFields.size() - 1) ? " AND " : "";
			String key = (String) iterator.next();
			Object value = whereFields.get(key);
			if (value != null) {
				query += " x." + key + " = :val__" + i + andOperator;
			}
			if (i == 0) {
				field = key;
				fieldValue = value;
			}
			i++;
		}

		// Setting where clause values...
		i = 0;
		Query tmpQuery = em.createQuery(query);
		for (Iterator iterator = whereFields.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Object value = whereFields.get(key);
			tmpQuery.setParameter("val__" + i, value);
			i++;
		}
		// Getting list of records...
		final List result = tmpQuery.getResultList();

		if (result == null || result.size() == 0) {
			throw new SpagoBIRuntimeException("Record with " + field + " equals to " + fieldValue.toString() + " not found for entity " + entityType);
		}
		if (result.size() > 1) {
			throw new SpagoBIRuntimeException("More than 1 record with " + field + " equals to " + fieldValue.toString() + " in entity " + entityType);
		}

		return result.get(0);
	}

	private Column getDependenceColumns(List columns, String depField) {
		Column toReturn = null;
		for (int i = 0; i < columns.size(); i++) {
			Column c = (Column) columns.get(i);
			if (c.getField().equalsIgnoreCase(depField)) {
				toReturn = c;
				break;
			}
		}
		return toReturn;
	}
}
