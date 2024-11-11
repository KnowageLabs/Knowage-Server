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
package it.eng.spagobi.analiticalmodel.document.bo;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.services.validation.Alphanumeric;
import it.eng.spagobi.services.validation.ExtendedAlphanumeric;
import it.eng.spagobi.services.validation.Xss;
import it.eng.spagobi.tools.catalogue.metadata.IDrivableBIResource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * Defines a Business Intelligence object.
 */
public class BIObject implements Serializable, Cloneable, IDrivableBIResource<BIObjectParameter> {

	// BIOBJ_ID NUMBER N Business Intelligence Object identifier
	private Integer id = null;

	// ENGINE_ID NUMBER N Engine identifier (FK)
	@NotNull
	private Engine engine = null;

	// DATA_SOURCE_ID NUMBER N DataSource identifier (FK)
	private Integer dataSourceId = null;

	// DATA_SOURCE_ID NUMBER N DataSource identifier (FK)
	private Integer dataSetId = null;

	// DESCR VARCHAR2(128) Y BI Object description
	@NotEmpty
	@ExtendedAlphanumeric
	@Size(max = 200)
	private String name = null;

	// DESCR VARCHAR2(128) Y BI Object description
	@ExtendedAlphanumeric
	@Size(max = 400)
	private String description = null;

	// DATASET
	private ArrayList<Integer> datasetsIds = null;

	// LABEL VARCHAR2(36) Y Engine label (short textual identifier)
	@NotEmpty
	@Alphanumeric
	@Size(max = 100)
	private String label = null;

	// ENCRYPT NUMBER Y Parameter encryption request.
	private Integer encrypt = null;

	// VISIBLE NUMBER Y Parameter visible request.
	@Range(min = 0, max = 1)
	private Integer visible = null;

	@Xss
	@Size(max = 400)
	private String profiledVisibility;

	// REL_NAME VARCHAR2(256) Y Relative path + file object name
	@Xss
	@Size(max = 400)
	private String relName = null;

	// STATE_ID NUMBER N State identifier (actually not used)
	@NotNull
	private Integer stateID = null;

	// STATE_CD VARCHAR2(18) N State code. Initially hard-coded valued, in the
	// future, managed by a states workflow with historical storage.
	@NotEmpty
	private String stateCode = null;

	// BIOBJ_TYPE_ID NUMBER N Business Intelligence Object Type identifier.
	private Integer biObjectTypeID = null;

	// BIOBJ_TYPE_CD VARCHAR2(18) N Business Intelligence Object Type code (ex.
	// report, OLAP, Data mining, Dashboard). Denormalizated attribute from
	// SBI_DOMAINS.
	private String biObjectTypeCode = null;

	private List<BIObjectParameter> biObjectParameters = null;

	private List<BIMetaModelParameter> biBIMetaModelParameter = null;

	private String path = null;

	private String uuid = null;

	private List<Integer> functionalities = null;

	// add this properties for metadata
	private Date creationDate = null;
	private String creationUser = null;

	private Integer refreshSeconds = null;

	private List<DocumentMetadataProperty> objMetaDataAndContents = null;

	private String tenant = null;

	private String previewFile = null;

	private boolean publicDoc = false;

	private Integer docVersion = null; // defines the version of template if is different by the default (last version)

	private String parametersRegion = null;

	private String lockedByUser = null;

	private List<OutputParameter> outputParameters = new ArrayList();

	private String stateCodeStr = null;

	private static final DateTimeFormatter dateFormat_v2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private static final DateTimeFormatter dateFormat_v3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * Gets the id.
	 *
	 * @return Returns the id.
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param businessObjectID The id to set.
	 */
	public void setId(Integer businessObjectID) {
		this.id = businessObjectID;
	}

	// /**
	// * Gets the bi object parameters.
	// *
	// * @return Returns the biObjectParameters.
	// */
	// @JsonIgnore
	// public List<BIObjectParameter> getDrivers() {
	// return biObjectParameters;
	// }
	//
	// /**
	// * Sets the bi object parameters.
	// *
	// * @param businessObjectParameters
	// * The biObjectParameters to set.
	// */
	// public void setDrivers(List<BIObjectParameter> businessObjectParameters) {
	// this.biObjectParameters = businessObjectParameters;
	// }

	/**
	 * Gets the description.
	 *
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the encrypt.
	 *
	 * @return Returns the encrypt.
	 */
	@JsonIgnore
	public Integer getEncrypt() {
		return encrypt;
	}

	/**
	 * Sets the encrypt.
	 *
	 * @param encrypt The encrypt to set.
	 */
	public void setEncrypt(Integer encrypt) {
		this.encrypt = encrypt;
	}

	/**
	 * Gets the visible.
	 *
	 * @return the visible
	 */
	@JsonIgnore
	public Integer getVisible() {
		return visible;
	}

	/**
	 * Sets the visible.
	 *
	 * @param visible the new visible
	 */
	@JsonIgnore
	public void setVisible(Integer visible) {
		this.visible = visible;
	}

	@JsonProperty(value = "visible")
	public void setVisible(boolean visible) {
		this.visible = visible ? new Integer(1) : new Integer(0);
	}

	public boolean isVisible() {
		return (this.visible != null && this.visible.intValue() == 1);
	}

	/**
	 * Gets the engine.
	 *
	 * @return Returns the engine.
	 */
	@JsonIgnore
	public Engine getEngine() {
		return engine;
	}

	@JsonProperty(value = "engine")
	public String getEngineLabel() {
		return engine.getLabel();
	}

	/**
	 * Sets the engine.
	 *
	 * @param engine The engine to set.
	 */
	@JsonIgnore
	public void setEngine(Engine engine) {
		this.engine = engine;
	}

	@JsonProperty(value = "engine")
	public void setEngineWithName(String engineName) {
		try {
			IEngineDAO dao = DAOFactory.getEngineDAO();
			dao.setUserID(creationUser);
			dao.setTenant(tenant);

			this.engine = dao.loadEngineByLabel(engineName);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while retrieving the engine [" + engineName + "]", e);
		}
	}

	/**
	 * Gets the data source id.
	 *
	 * @return Returns the datasource.
	 */
	@JsonIgnore
	public Integer getDataSourceId() {
		return dataSourceId;
	}

	/**
	 * Sets the data source id.
	 *
	 * @param dataSourceId the data source id
	 */
	public void setDataSourceId(Integer dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	public String getDataSourceLabel() throws EMFUserError {
		if (dataSourceId == null) {
			return null;
		}

		IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
		return dataSourceDAO.loadDataSourceByID(dataSourceId).getLabel();
	}

	public void setDataSourceLabel(String label) throws EMFUserError {
		if (label != null) {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();

			this.dataSourceId = dataSourceDAO.loadDataSourceByLabel(label).getDsId();
		}
	}

	/**
	 * Gets the label.
	 *
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the rel name.
	 *
	 * @return Returns the relName.
	 */
	@JsonIgnore
	public String getRelName() {
		return relName;
	}

	/**
	 * Sets the rel name.
	 *
	 * @param relName The relName to set.
	 */
	@JsonSetter
	public void setRelName(String relName) {
		this.relName = relName;
	}

	/**
	 * Gets the bi object type code.
	 *
	 * @return Returns the biObjectTypeCode.
	 */
	@JsonProperty(value = "typeCode")
	public String getBiObjectTypeCode() {
		return biObjectTypeCode;
	}

	/**
	 * Sets the bi object type code.
	 *
	 * @param businessObjectTypeCD The biObjectTypeCode to set.
	 */
	@JsonIgnore
	public void setBiObjectTypeCode(String businessObjectTypeCD) {
		this.biObjectTypeCode = businessObjectTypeCD;
	}

	/**
	 * Sets the bi object type code and update the type id.
	 *
	 * @param businessObjectTypeCD The biObjectTypeCode to set.
	 * @throws EMFUserError
	 */
	@JsonProperty(value = "typeCode")
	public void setTypeCodeAndId(String businessObjectTypeCD) throws EMFUserError {
		this.biObjectTypeCode = businessObjectTypeCD;

		IDomainDAO domainDAO = DAOFactory.getDomainDAO();
		this.biObjectTypeID = domainDAO.loadDomainByCodeAndValue("BIOBJ_TYPE", businessObjectTypeCD).getValueId();
	}

	/**
	 * Gets the bi object type id.
	 *
	 * @return Returns the biObjectTypeID.
	 */
	@JsonIgnore
	public Integer getBiObjectTypeID() {
		return biObjectTypeID;
	}

	/**
	 * Sets the bi object type id.
	 *
	 * @param biObjectTypeID The biObjectTypeID to set.
	 */
	public void setBiObjectTypeID(Integer biObjectTypeID) {
		this.biObjectTypeID = biObjectTypeID;
	}

	/**
	 * Gets the state code.
	 *
	 * @return Returns the stateCode.
	 */
	@JsonGetter
	public String getStateCode() {
		return stateCode;
	}

	/**
	 * Sets the state code.
	 *
	 * @param stateCD The stateCode to set.
	 */
	@JsonIgnore
	public void setStateCode(String stateCD) {
		this.stateCode = stateCD;
	}

	/**
	 * Sets the state code and update the state id.
	 *
	 * @param stateCD The stateCode to set.
	 * @throws EMFUserError
	 */
	@JsonProperty(value = "stateCode")
	public void setStateCodeAndId(String stateCD) throws EMFUserError {
		this.stateCode = stateCD;

		IDomainDAO domainDAO = DAOFactory.getDomainDAO();
		this.stateID = domainDAO.loadDomainByCodeAndValue("STATE", stateCD).getValueId();
	}

	/**
	 * Gets the state id.
	 *
	 * @return Returns the stateID.
	 */
	@JsonIgnore
	public Integer getStateID() {
		return stateID;
	}

	/**
	 * Sets the state id.
	 *
	 * @param stateID The stateID to set.
	 */
	public void setStateID(Integer stateID) {
		this.stateID = stateID;
	}

	/**
	 * Gets the path.
	 *
	 * @return Returns the path.
	 */
	@JsonIgnore
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path.
	 *
	 * @param path The path to set.
	 */
	public void setPath(String path) {
		this.path = path;
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
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the uuid.
	 *
	 * @return the uuid
	 */
	@JsonIgnore
	public String getUuid() {
		return uuid;
	}

	/**
	 * Sets the uuid.
	 *
	 * @param uuid the new uuid
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Gets the functionalities.
	 *
	 * @return the functionalities
	 */
	@JsonIgnore
	public List<Integer> getFunctionalities() {
		return functionalities;
	}

	/**
	 * Sets the functionalities.
	 *
	 * @param functionalities the new functionalities
	 */
	public void setFunctionalities(List<Integer> functionalities) {
		this.functionalities = functionalities;
	}

	@JsonProperty(value = "functionalities")
	public List getFunctionalitiesNames() throws EMFUserError {
		ILowFunctionalityDAO functionalitiesDao = DAOFactory.getLowFunctionalityDAO();
		List<String> list = new ArrayList<>();

		for (Integer functionalityID : functionalities) {
			list.add(functionalitiesDao.loadLowFunctionalityByID(functionalityID, false).getPath());
		}

		return list;
	}

	public void setFunctionalitiesNames(List<String> paths) throws EMFUserError {
		ILowFunctionalityDAO functionalitiesDao = DAOFactory.getLowFunctionalityDAO();
		this.functionalities = new ArrayList<>();

		for (String path : paths) {
			this.functionalities.add(functionalitiesDao.loadLowFunctionalityByPath(path, false).getId());
		}
	}

	/**
	 * Gets the active template.
	 *
	 * @return the active template
	 */
	@JsonIgnore
	public ObjTemplate getActiveTemplate() {
		ObjTemplate template = null;
		try {
			IObjTemplateDAO objtempdao = DAOFactory.getObjTemplateDAO();
			template = objtempdao.getBIObjectActiveTemplate(this.getId());
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getActiveTemplate",
					"Error while recovering current template \n", e);
		}
		return template;
	}

	/**
	 * Gets the template list.
	 *
	 * @return the template list
	 */
	@JsonIgnore
	public List<ObjTemplate> getTemplateList() {
		List<ObjTemplate> templates = new ArrayList();
		try {
			IObjTemplateDAO objtempdao = DAOFactory.getObjTemplateDAO();
			templates = objtempdao.getBIObjectTemplateList(this.getId());
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getTemplateList",
					"Error while recovering template list\n", e);
		}
		return templates;
	}

	/**
	 * Gets the creation date.
	 *
	 * @return the creation date
	 */
	@JsonIgnore
	public Date getCreationDate() {
		return creationDate;
	}

	@JsonProperty(value = "creationDate")
	public String getFormattedDate() {
		String formattedDate = null;
		if (creationDate != null) {
			if (creationDate instanceof Timestamp) {
				formattedDate = dateFormat_v3.format(creationDate.toInstant().atZone(ZoneId.systemDefault()));
			} else if (creationDate instanceof TemporalAccessor) {
				formattedDate = dateFormat_v2.format((TemporalAccessor) creationDate);
			}
		}
		return formattedDate;
	}

	/**
	 * Sets the creation date.
	 *
	 * @param creationDate the new creation date
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Gets the creation user.
	 *
	 * @return the creation user
	 */
	@JsonGetter
	public String getCreationUser() {
		return creationUser;
	}

	/**
	 * Sets the creation user.
	 *
	 * @param creationUser the new creation user
	 */
	@JsonIgnore
	public void setCreationUser(String creationUser) {
		this.creationUser = creationUser;
	}

	/**
	 * Gets the data set id.
	 *
	 * @return the data set id
	 */
	// @JsonIgnore
	public Integer getDataSetId() {
		return dataSetId;
	}

	/**
	 * Sets the data set id.
	 *
	 * @param dataSetId the new data set id
	 */
	public void setDataSetId(Integer dataSetId) {
		this.dataSetId = dataSetId;
	}

	public String getDataSetLabel() throws EMFUserError {
		// if (dataSetId == null)
		// return null;
		//
		// IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
		//
		// return dataSetDao.loadDataSetById(dataSetId).getLabel();
		return null;
	}

	// public void setDataSetLabel(String label) throws EMFUserError {
	// if (label != null) {
	// IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
	//
	// this.dataSetId = dataSetDao.loadDataSetByLabel(label).getId();
	// }
	// }
	/**
	 * Gets refresh Seconds.
	 *
	 * @return refresh Seconds
	 */

	public Integer getRefreshSeconds() {
		return refreshSeconds;
	}

	/**
	 * Sets refresh Seconds.
	 *
	 * @param refreshSeconds
	 */

	public void setRefreshSeconds(Integer refreshSeconds) {
		this.refreshSeconds = refreshSeconds;
	}

	public String getProfiledVisibility() {
		return profiledVisibility;
	}

	public void setProfiledVisibility(String profiledVisibility) {
		this.profiledVisibility = profiledVisibility;
	}

	public List<DocumentMetadataProperty> getObjMetaDataAndContents() {
		return objMetaDataAndContents;
	}

	public void setObjMetaDataAndContents(List<DocumentMetadataProperty> objMetaDataAndContents) {
		this.objMetaDataAndContents = objMetaDataAndContents;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	/**
	 * @return the previewFile
	 */
	public String getPreviewFile() {
		return previewFile;
	}

	/**
	 * @param previewFile the previewFile to set
	 */
	public void setPreviewFile(String previewFile) {
		this.previewFile = previewFile;
	}

	/**
	 * @return the publicDoc
	 */
	@JsonProperty(value = "public")
	public boolean isPublicDoc() {
		return publicDoc;
	}

	/**
	 * @param publicDoc the publicDoc to set
	 */
	public void setPublicDoc(boolean publicDoc) {
		this.publicDoc = publicDoc;
	}

	/**
	 * @return the docVersion
	 */
	public Integer getDocVersion() {
		return docVersion;
	}

	/**
	 * @param docVersion the docVersion to set
	 */
	public void setDocVersion(Integer docVersion) {
		this.docVersion = docVersion;
	}

	public void setOutputParameters(List<OutputParameter> outputParameters) {
		this.outputParameters = outputParameters;
	}

	public List<OutputParameter> getOutputParameters() {
		return outputParameters;
	}

	@Override
	public String toString() {
		return "Document [label=" + label + "]";
	}

	public String getParametersRegion() {
		return parametersRegion;
	}

	public void setParametersRegion(String parametersRegion) {
		this.parametersRegion = parametersRegion;
	}

	public String getLockedByUser() {
		return lockedByUser;
	}

	public void setLockedByUser(String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}

	/**
	 * Clone the object.. NOTE: it does not clone the id property
	 */
	@Override
	public final BIObject clone() {
		BIObject clone = new BIObject();
		clone.setEngine(this.engine);
		clone.setDataSourceId(dataSourceId);
		clone.setDataSetId(dataSetId);
		clone.setName(name);
		clone.setDescription(description);
		clone.setLabel(label);
		clone.setEncrypt(encrypt);
		clone.setVisible(visible);
		clone.setProfiledVisibility(profiledVisibility);
		clone.setRelName(relName);
		clone.setStateID(stateID);
		clone.setStateCode(stateCode);
		clone.setBiObjectTypeID(biObjectTypeID);
		clone.setBiObjectTypeCode(biObjectTypeCode);
		clone.setDrivers(biObjectParameters);
		clone.setPath(path);
		clone.setUuid(uuid);
		clone.setFunctionalities(functionalities);
		clone.setCreationDate(creationDate);
		clone.setCreationUser(creationUser);
		clone.setRefreshSeconds(refreshSeconds);
		clone.setObjMetaDataAndContents(objMetaDataAndContents);
		clone.setTenant(tenant);
		clone.setPreviewFile(previewFile);
		clone.setPublicDoc(publicDoc);
		clone.setDocVersion(docVersion);
		clone.setParametersRegion(parametersRegion);
		clone.setOutputParameters(outputParameters);
		clone.setLockedByUser(lockedByUser);
		return clone;
	}

	public ArrayList<Integer> getDatasetsIds() {
		return datasetsIds;
	}

	public void setDatasetsIds(ArrayList<Integer> datasetsIds) {
		this.datasetsIds = datasetsIds;
	}

	@JsonGetter
	public String getStateCodeStr() {
		return stateCodeStr;
	}

	@JsonIgnore
	public void setStateCodeStr(String stateCodeStr) {
		this.stateCodeStr = stateCodeStr;
	}

	@Override
	public boolean equals(Object v) {
		if (this == v) {
			return true;
		}

		boolean toReturn = false;

		if (v instanceof BIObject) {
			BIObject ptr = (BIObject) v;
			if (ptr.id != null) {
				toReturn = ptr.id.longValue() == this.id;
			} else {
				toReturn = ptr.label.equals(this.label);
			}
		}

		return toReturn;
	}

	@Override
	public List<BIObjectParameter> getDrivers() {
		return biObjectParameters;
	}

	@Override
	public void setDrivers(List<BIObjectParameter> drivers) {
		this.biObjectParameters = drivers;
	}

	@Override
	public List<BIMetaModelParameter> getMetamodelDrivers() {
		return biBIMetaModelParameter;
	}

	@Override
	public void setMetamodelDrivers(List<BIMetaModelParameter> drivers) {
		this.biBIMetaModelParameter = drivers;
	}

	private final void writeObject(ObjectOutputStream aOutputStream) {
		throw new UnsupportedOperationException("Security violation : cannot serialize object to a stream");
	}

}
