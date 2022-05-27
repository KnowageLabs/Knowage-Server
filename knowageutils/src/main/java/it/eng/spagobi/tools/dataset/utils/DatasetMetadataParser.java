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
package it.eng.spagobi.tools.dataset.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Functions that convert from IMetadata object to xml rapresentation via sourcebean and viceversa
 *
 * @author gavardi
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */

public class DatasetMetadataParser {

	private static transient Logger logger = Logger.getLogger(DatasetMetadataParser.class);

	// XML tags
	public static final String COLUMNLIST = "COLUMNLIST";
	public static final String COLUMN = "COLUMN";
	public static final String PROPERTY = "PROPERTY";
	public static final String COLUMNS = "COLUMNS";
	public static final String DATASET = "DATASET";
	public static final String META = "META";

	// XML attributes for tag COLUMM
	public static final String NAME = "name";
	public static final String FIELD_TYPE = "fieldType";
	public static final String TYPE = "type";
	public static final String ALIAS = "alias";
	public static final String MULTIVALUE = "multivalue";
	public static final String VERSION = "version";

	public static final String PERSONAL = "personal";
	public static final String MASKED = "masked";
	public static final String DECRIPT = "decript";
	public static final String SUBJECT_ID = "subjectId";

	// XML VALUES FOR PROPERTIES TAG

	public static final String VALUE = "value";
	public static final String NAME_P = "name";

	public final static String CURRENT_VERSION = "1";
	public final static String ATTRIBUTE_VERSION = "version";

	// Previous version of metadataToXml left only for backup
	@Deprecated
	public String metadataToXMLOldVersion(IMetaData dataStoreMetaData) {
		logger.debug("IN");

		SourceBean sb = null;
		try {

			sb = new SourceBean(DatasetMetadataParser.COLUMNLIST);

			for (int i = 0; i < dataStoreMetaData.getFieldCount(); i++) {
				IFieldMetaData fieldMetaData = dataStoreMetaData.getFieldMeta(i);
				String name = fieldMetaData.getName();
				Assert.assertNotNull(name, "Name of the field cannot be null");
				String alias = fieldMetaData.getAlias();
				String type = fieldMetaData.getType().getName();
				boolean multiValue = fieldMetaData.isMultiValue();
				Assert.assertNotNull(type, "Type of the field " + name + " cannot be null");

				FieldType fieldType = fieldMetaData.getFieldType();

				Map properties = fieldMetaData.getProperties();

				SourceBean sbMeta = new SourceBean(DatasetMetadataParser.COLUMN);
				SourceBeanAttribute attN = new SourceBeanAttribute(NAME, name);
				SourceBeanAttribute attT = new SourceBeanAttribute(TYPE, type);
				SourceBeanAttribute attM = new SourceBeanAttribute(MULTIVALUE, multiValue);
				SourceBeanAttribute attA = alias != null ? new SourceBeanAttribute(ALIAS, alias) : null;
				SourceBeanAttribute attF = fieldType != null ? new SourceBeanAttribute(FIELD_TYPE, fieldType.toString()) : null;

				sbMeta.setAttribute(attN);
				sbMeta.setAttribute(attT);
				sbMeta.setAttribute(attM);

				if (attA != null)
					sbMeta.setAttribute(attA);
				if (attF != null)
					sbMeta.setAttribute(attF);
				sb.setAttribute(sbMeta);

				// insert properties
				if (properties != null) {
					insertPropertiesInSourceBean(sbMeta, properties);
				}
			}
		} catch (Exception e) {
			logger.error("Error in building xml from metadata", e);
			return null;
		}

		String xml1 = sb.toXML(false);
		logger.debug("OUT");

		return xml1;
	}

	// This is a new implementation to convert the metadata information to a more general structure
	public String metadataToXML(IMetaData dataStoreMetaData) {
		logger.debug("IN");

		SourceBean sb = null;
		SourceBean sbColumns = null;
		SourceBean sbDataset = null;
		try {

			sb = new SourceBean(DatasetMetadataParser.META);
			sbColumns = new SourceBean(DatasetMetadataParser.COLUMNLIST);
			sbDataset = new SourceBean(DatasetMetadataParser.DATASET);

			// Dataset Metadata
			Map datasetProperties = dataStoreMetaData.getProperties();
			if (datasetProperties != null) {
				insertPropertiesInSourceBean(sbDataset, datasetProperties);
			}

			// Columns Metadata
			for (int i = 0; i < dataStoreMetaData.getFieldCount(); i++) {
				IFieldMetaData fieldMetaData = dataStoreMetaData.getFieldMeta(i);
				String name = fieldMetaData.getName();
				String alias = fieldMetaData.getAlias();
				String type = fieldMetaData.getType().getName();

				boolean multiValue = fieldMetaData.isMultiValue();
				Assert.assertNotNull(type, "Type of the field " + name + " cannot be null");
				FieldType fieldType = fieldMetaData.getFieldType();

				Map properties = fieldMetaData.getProperties();

				SourceBean sbMeta = new SourceBean(DatasetMetadataParser.COLUMN);
				SourceBeanAttribute attN = new SourceBeanAttribute(NAME, name);
				SourceBeanAttribute attT = new SourceBeanAttribute(TYPE, type);
				SourceBeanAttribute attM = new SourceBeanAttribute(MULTIVALUE, multiValue);
				SourceBeanAttribute attF = fieldType != null ? new SourceBeanAttribute(FIELD_TYPE, fieldType.toString()) : null;
				SourceBeanAttribute attA = alias != null ? new SourceBeanAttribute(ALIAS, alias) : null;
				if (attF != null)
					sbMeta.setAttribute(attF);
				if (attA != null)
					sbMeta.setAttribute(attA);

				sbMeta.setAttribute(attN);
				sbMeta.setAttribute(attT);
				sbMeta.setAttribute(attM);

				SourceBeanAttribute attPersonal = new SourceBeanAttribute(PERSONAL, fieldMetaData.isPersonal());
				sbMeta.setAttribute(attPersonal);
//				SourceBeanAttribute attMasked = new SourceBeanAttribute(MASKED, fieldMetaData.isMaskValue());
//				sbMeta.setAttribute(attMasked);
				SourceBeanAttribute attDecript = new SourceBeanAttribute(DECRIPT, fieldMetaData.isDecript());
				sbMeta.setAttribute(attDecript);
				SourceBeanAttribute attSubjectId = new SourceBeanAttribute(SUBJECT_ID, fieldMetaData.isSubjectId());
				sbMeta.setAttribute(attSubjectId);

				sbColumns.setAttribute(sbMeta);

				// insert properties
				if (properties != null) {
					insertPropertiesInSourceBean(sbMeta, properties);
				}

			}
			SourceBeanAttribute version = new SourceBeanAttribute(VERSION, "1");
			sb.setAttribute(version);
			sb.setAttribute(sbColumns);
			sb.setAttribute(sbDataset);

		} catch (Exception e) {
			logger.error("Error in building xml from metadata", e);
			throw new SpagoBIRuntimeException("Error in building xml from metadata", e);
		}

		String resultXml = sb.toXML(false);
		logger.debug("OUT");

		return resultXml;
	}

	public String buildNoMetadataXML() {
		logger.debug("IN");

		SourceBean sb = null;
		SourceBean sbColumns = null;
		SourceBean sbDataset = null;
		try {
			sbColumns = new SourceBean(DatasetMetadataParser.COLUMNLIST);
			sb = new SourceBean(DatasetMetadataParser.META);
			sbDataset = new SourceBean(DatasetMetadataParser.DATASET);
			SourceBeanAttribute version = new SourceBeanAttribute(VERSION, "-1");
			sb.setAttribute(version);
			sb.setAttribute(sbDataset);
			sb.setAttribute(sbColumns);

		} catch (Exception e) {
			logger.error("Error in building xml from metadata", e);
			throw new SpagoBIRuntimeException("Error in building xml from metadata", e);
		}

		String resultXml = sb.toXML(false);
		logger.debug("OUT");

		return resultXml;
	}

	public void insertPropertiesInSourceBean(SourceBean sbMeta, Map properties) throws SourceBeanException {
		logger.debug("IN");
		for (Iterator iterator = properties.keySet().iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			Assert.assertNotNull(name, "Property name cannot be null");
			Object value = properties.get(name);
			Assert.assertNotNull(value, "Value of property " + name + " cannot be null");
			if (value != null) {
				SourceBean sbP = new SourceBean(DatasetMetadataParser.PROPERTY);
				SourceBeanAttribute attN = new SourceBeanAttribute(NAME, name);
				SourceBeanAttribute attV = new SourceBeanAttribute(VALUE, value.toString());
				sbP.setAttribute(attN);
				sbP.setAttribute(attV);
				sbMeta.setAttribute(sbP);
			}
		}
		logger.debug("OUT");
	}

	public IMetaData xmlToMetadata(String xmlMetadata) throws Exception {

		MetaData dsMeta;
		SourceBean sb = null;
		String encodingFormatVersion;
		SourceBean template;

		try {
			dsMeta = new MetaData();

			if (xmlMetadata == null) {
				logger.error("String rapresentation of metadata is null");
				throw new Exception("Xml Metadata String cannot be null ");
			}
			Assert.assertNotNull(xmlMetadata, "SourceBean in input cannot be not be null");

			sb = SourceBean.fromXMLString(xmlMetadata);
			logger.debug("Parsing template [" + sb.getName() + "] ...");

			encodingFormatVersion = (String) sb.getAttribute(ATTRIBUTE_VERSION);

			if (encodingFormatVersion == null) {
				logger.debug("no version found, default is 0");
				encodingFormatVersion = "0";
			}

			if (encodingFormatVersion.equals("-1")) {// it's a dataset without metadata
				logger.debug("Dataset has no metadata. It has been saved and metadata has not beel already saved");
			} else {
				logger.debug("Row data encoding version  [" + encodingFormatVersion + "]");

				if (encodingFormatVersion.equalsIgnoreCase(CURRENT_VERSION)) {
					template = sb;
				} else {
					logger.warn("Row data encoding version [" + encodingFormatVersion + "] does not match with the current version used by the engine ["
							+ CURRENT_VERSION + "] ");
					logger.debug("Converting from encoding version [" + encodingFormatVersion + "] to encoding version [" + CURRENT_VERSION + "]....");
					IDatasetMetadataXMLTemplateLoader datasetMetadataXMLTemplateLoader;
					datasetMetadataXMLTemplateLoader = DatasetMetadataXMLTemplateLoaderFactory.getInstance().getLoader(encodingFormatVersion);
					if (datasetMetadataXMLTemplateLoader == null) {
						throw new SpagoBIEngineException("Unable to load data stored in format [" + encodingFormatVersion + "] ");
					}
					template = datasetMetadataXMLTemplateLoader.load(sb);
					logger.debug("Encoding conversion has been executed succesfully");
				}

				// Dataset Metadata Properties
				SourceBean sbDataset = (SourceBean) template.getAttribute(DATASET);
				List propertiesDataset = sbDataset.getAttributeAsList(PROPERTY);
				if (propertiesDataset != null && propertiesDataset.size() != 0) {
					try {
						insertPropertiesInMeta(dsMeta, propertiesDataset);
					} catch (Exception e) {
						logger.error("Error in reading properties");
						throw new SpagoBIRuntimeException("Error in inserting properties: " + e.getMessage());
					}
				}

				// Columns Metadata Properties
				SourceBean sbColumns = (SourceBean) template.getAttribute(COLUMNLIST);
				List lst = sbColumns.getAttributeAsList(COLUMN);
				for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
					SourceBean sbRow = (SourceBean) iterator.next();
					String name = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
					String type = sbRow.getAttribute(TYPE) != null ? sbRow.getAttribute(TYPE).toString() : null;
					boolean isMultivalue = sbRow.getAttribute(MULTIVALUE) != null ? Boolean.parseBoolean((String) sbRow.getAttribute(MULTIVALUE)) : false;
					String alias = sbRow.getAttribute(ALIAS) != null ? sbRow.getAttribute(ALIAS).toString() : null;
					String fieldType = sbRow.getAttribute(FIELD_TYPE) != null ? sbRow.getAttribute(FIELD_TYPE).toString() : null;

					Assert.assertNotNull(name, "Name in XML column cannot be null");

					FieldMetadata fieldMeta = new FieldMetadata();
					fieldMeta.setName(name);
					Assert.assertNotNull(type, "type in XML column " + name + " cannot be null");
					// remove class!
					// operation for back compatibility, if there is class remove it otherwise not needed)
					if (type.startsWith("class")) {
						type = type.substring(6);
					}
					fieldMeta.setType(Class.forName(type.trim()));
					fieldMeta.setMultiValue(isMultivalue);

					fieldMeta.setAlias(alias);
					if (fieldType != null && fieldType.equalsIgnoreCase(FieldType.ATTRIBUTE.toString()))
						fieldMeta.setFieldType(FieldType.ATTRIBUTE);
					else if (fieldType != null && fieldType.equalsIgnoreCase(FieldType.MEASURE.toString()))
						fieldMeta.setFieldType(FieldType.MEASURE);
					else if (fieldType != null && fieldType.equalsIgnoreCase(FieldType.SPATIAL_ATTRIBUTE.toString()))
						fieldMeta.setFieldType(FieldType.SPATIAL_ATTRIBUTE);
					else
						fieldMeta.setFieldType(FieldType.ATTRIBUTE);

					boolean personal = sbRow.getAttribute(PERSONAL) != null ? Boolean.parseBoolean((String) sbRow.getAttribute(PERSONAL)) : false;
					fieldMeta.setPersonal(personal);
//					boolean masked = sbRow.getAttribute(MASKED) != null ? Boolean.parseBoolean((String) sbRow.getAttribute(MASKED)) : false;
//					fieldMeta.setMasked(masked);
					boolean decript = sbRow.getAttribute(DECRIPT) != null ? Boolean.parseBoolean((String) sbRow.getAttribute(DECRIPT)) : false;
					fieldMeta.setDecript(decript);
					boolean subjectId = sbRow.getAttribute(SUBJECT_ID) != null ? Boolean.parseBoolean((String) sbRow.getAttribute(SUBJECT_ID)) : false;
					fieldMeta.setSubjectId(subjectId);

					List properties = sbRow.getAttributeAsList(DatasetMetadataParser.PROPERTY);

					if (properties != null && properties.size() != 0) {
						try {
							insertPropertiesInMeta(fieldMeta, properties);
						} catch (Exception e) {
							logger.error("Error in reading properties");
							throw new Exception("Error in inserting properties: " + e.getMessage());
						}
					}

					dsMeta.addFiedMeta(fieldMeta);
				}
			}

		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to parse template [" + xmlMetadata.toString() + "]", t);
		} finally {
			logger.debug("OUT");
		}
		return dsMeta;

	}

	public void insertPropertiesInMeta(IFieldMetaData meta, List propertiesBean) throws SourceBeanException {
		logger.debug("IN");

		Map properties = meta.getProperties();

		for (Iterator iterator = propertiesBean.iterator(); iterator.hasNext();) {
			SourceBean sb = (SourceBean) iterator.next();
			String name = sb.getAttribute(NAME_P) != null ? sb.getAttribute(NAME_P).toString() : null;
			Assert.assertNotNull(name, "Property name cannot be null");
			String value = sb.getAttribute(VALUE) != null ? sb.getAttribute(VALUE).toString() : null;
			Assert.assertNotNull(value, "value of property's " + name + " cannot be null");
			properties.put(name, value);
		}

		logger.debug("OUT");
	}

	public void insertPropertiesInMeta(IMetaData meta, List propertiesBean) throws SourceBeanException {
		logger.debug("IN");

		Map properties = meta.getProperties();

		for (Iterator iterator = propertiesBean.iterator(); iterator.hasNext();) {
			SourceBean sb = (SourceBean) iterator.next();
			String name = sb.getAttribute(NAME_P) != null ? sb.getAttribute(NAME_P).toString() : null;
			Assert.assertNotNull(name, "Property name cannot be null");
			String value = sb.getAttribute(VALUE) != null ? sb.getAttribute(VALUE).toString() : null;
			Assert.assertNotNull(value, "value of property's " + name + " cannot be null");
			properties.put(name, value);
		}

		logger.debug("OUT");
	}

}
