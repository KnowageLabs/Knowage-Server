/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.monitoring.metadata;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiSubObjects;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.engines.config.metadata.SbiEngines;

import java.util.Date;

public class SbiAudit extends SbiHibernateModel  {

	//	Fields
	private Integer id;
	private String userName;
	private String userGroup;
	private SbiObjects sbiObject;
	private Integer documentId;
	private String documentLabel;
	private String documentName;
	private String documentType;
	private String documentState;
	private String documentParameters;
	private SbiSubObjects sbiSubObject;
	private Integer subObjId;
	private String subObjName;
	private String subObjOwner;
	private Short subObjIsPublic;
	private SbiEngines sbiEngine;
	private Integer engineId;
	private String engineLabel;
	private String engineName;
	private String engineType;
	private String engineUrl;
	private String engineDriver;
	private String engineClass;
	private Date requestTime;
	private Date executionStartTime;
	private Date executionEndTime;
	private Integer executionTime;
	private String executionState;
	private Short error;
	private String errorMessage;
	private String errorCode;
	private String executionModality;
	
    // Constructors

	/**
     * default constructor.
     */
    public SbiAudit() {
    }
    
    /**
     * constructor with id.
     * 
     * @param id the id
     */
    public SbiAudit(Integer id) {
        this.id = id;
    }
    
	/**
	 * Gets the document id.
	 * 
	 * @return the document id
	 */
	public Integer getDocumentId() {
		return documentId;
	}

	/**
	 * Sets the document id.
	 * 
	 * @param documentId the new document id
	 */
	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}
    
	/**
	 * Gets the document label.
	 * 
	 * @return the document label
	 */
	public String getDocumentLabel() {
		return documentLabel;
	}

	/**
	 * Sets the document label.
	 * 
	 * @param documentLabel the new document label
	 */
	public void setDocumentLabel(String documentLabel) {
		this.documentLabel = documentLabel;
	}

	/**
	 * Gets the document name.
	 * 
	 * @return the document name
	 */
	public String getDocumentName() {
		return documentName;
	}

	/**
	 * Sets the document name.
	 * 
	 * @param documentName the new document name
	 */
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	/**
	 * Gets the document parameters.
	 * 
	 * @return the document parameters
	 */
	public String getDocumentParameters() {
		return documentParameters;
	}

	/**
	 * Sets the document parameters.
	 * 
	 * @param documentParameters the new document parameters
	 */
	public void setDocumentParameters(String documentParameters) {
		this.documentParameters = documentParameters;
	}

	/**
	 * Gets the document type.
	 * 
	 * @return the document type
	 */
	public String getDocumentType() {
		return documentType;
	}

	/**
	 * Sets the document type.
	 * 
	 * @param documentType the new document type
	 */
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	/**
	 * Gets the document state.
	 * 
	 * @return the document state
	 */
	public String getDocumentState() {
		return documentState;
	}

	/**
	 * Sets the document state.
	 * 
	 * @param documentState the new document state
	 */
	public void setDocumentState(String documentState) {
		this.documentState = documentState;
	}
	
	/**
	 * Gets the engine class.
	 * 
	 * @return the engine class
	 */
	public String getEngineClass() {
		return engineClass;
	}

	/**
	 * Sets the engine class.
	 * 
	 * @param engineClass the new engine class
	 */
	public void setEngineClass(String engineClass) {
		this.engineClass = engineClass;
	}

	/**
	 * Gets the engine driver.
	 * 
	 * @return the engine driver
	 */
	public String getEngineDriver() {
		return engineDriver;
	}

	/**
	 * Sets the engine driver.
	 * 
	 * @param engineDriver the new engine driver
	 */
	public void setEngineDriver(String engineDriver) {
		this.engineDriver = engineDriver;
	}

	/**
	 * Gets the engine id.
	 * 
	 * @return the engine id
	 */
	public Integer getEngineId() {
		return engineId;
	}

	/**
	 * Sets the engine id.
	 * 
	 * @param engineId the new engine id
	 */
	public void setEngineId(Integer engineId) {
		this.engineId = engineId;
	}
	
	/**
	 * Gets the engine label.
	 * 
	 * @return the engine label
	 */
	public String getEngineLabel() {
		return engineLabel;
	}

	/**
	 * Sets the engine label.
	 * 
	 * @param engineLabel the new engine label
	 */
	public void setEngineLabel(String engineLabel) {
		this.engineLabel = engineLabel;
	}

	/**
	 * Gets the engine name.
	 * 
	 * @return the engine name
	 */
	public String getEngineName() {
		return engineName;
	}

	/**
	 * Sets the engine name.
	 * 
	 * @param engineName the new engine name
	 */
	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}

	/**
	 * Gets the engine type.
	 * 
	 * @return the engine type
	 */
	public String getEngineType() {
		return engineType;
	}

	/**
	 * Sets the engine type.
	 * 
	 * @param engineType the new engine type
	 */
	public void setEngineType(String engineType) {
		this.engineType = engineType;
	}

	/**
	 * Gets the engine url.
	 * 
	 * @return the engine url
	 */
	public String getEngineUrl() {
		return engineUrl;
	}

	/**
	 * Sets the engine url.
	 * 
	 * @param engineUrl the new engine url
	 */
	public void setEngineUrl(String engineUrl) {
		this.engineUrl = engineUrl;
	}

	/**
	 * Gets the error code.
	 * 
	 * @return the error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Sets the error code.
	 * 
	 * @param errorCode the new error code
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Gets the error message.
	 * 
	 * @return the error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets the error message.
	 * 
	 * @param errorMessage the new error message
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Gets the execution end time.
	 * 
	 * @return the execution end time
	 */
	public Date getExecutionEndTime() {
		return executionEndTime;
	}

	/**
	 * Sets the execution end time.
	 * 
	 * @param executionEndTime the new execution end time
	 */
	public void setExecutionEndTime(Date executionEndTime) {
		this.executionEndTime = executionEndTime;
	}

	/**
	 * Gets the execution modality.
	 * 
	 * @return the execution modality
	 */
	public String getExecutionModality() {
		return executionModality;
	}

	/**
	 * Sets the execution modality.
	 * 
	 * @param executionModality the new execution modality
	 */
	public void setExecutionModality(String executionModality) {
		this.executionModality = executionModality;
	}

	/**
	 * Gets the execution start time.
	 * 
	 * @return the execution start time
	 */
	public Date getExecutionStartTime() {
		return executionStartTime;
	}

	/**
	 * Sets the execution start time.
	 * 
	 * @param executionStartTime the new execution start time
	 */
	public void setExecutionStartTime(Date executionStartTime) {
		this.executionStartTime = executionStartTime;
	}

	/**
	 * Gets the execution state.
	 * 
	 * @return the execution state
	 */
	public String getExecutionState() {
		return executionState;
	}

	/**
	 * Sets the execution state.
	 * 
	 * @param executionState the new execution state
	 */
	public void setExecutionState(String executionState) {
		this.executionState = executionState;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the request time.
	 * 
	 * @return the request time
	 */
	public Date getRequestTime() {
		return requestTime;
	}

	/**
	 * Sets the request time.
	 * 
	 * @param requestTime the new request time
	 */
	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}

	/**
	 * Gets the sbi engine.
	 * 
	 * @return the sbi engine
	 */
	public SbiEngines getSbiEngine() {
		return sbiEngine;
	}

	/**
	 * Sets the sbi engine.
	 * 
	 * @param sbiEngine the new sbi engine
	 */
	public void setSbiEngine(SbiEngines sbiEngine) {
		this.sbiEngine = sbiEngine;
	}

	/**
	 * Gets the sbi object.
	 * 
	 * @return the sbi object
	 */
	public SbiObjects getSbiObject() {
		return sbiObject;
	}

	/**
	 * Sets the sbi object.
	 * 
	 * @param sbiObject the new sbi object
	 */
	public void setSbiObject(SbiObjects sbiObject) {
		this.sbiObject = sbiObject;
	}

	/**
	 * Gets the user group.
	 * 
	 * @return the user group
	 */
	public String getUserGroup() {
		return userGroup;
	}

	/**
	 * Sets the user group.
	 * 
	 * @param userGroup the new user group
	 */
	public void setUserGroup(String userGroup) {
		this.userGroup = userGroup;
	}

	/**
	 * Gets the user name.
	 * 
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 * 
	 * @param userName the new user name
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the error.
	 * 
	 * @return the error
	 */
	public Short getError() {
		return error;
	}

	/**
	 * Sets the error.
	 * 
	 * @param error the new error
	 */
	public void setError(Short error) {
		this.error = error;
	}

	/**
	 * Gets the execution time.
	 * 
	 * @return the execution time
	 */
	public Integer getExecutionTime() {
		return executionTime;
	}

	/**
	 * Sets the execution time.
	 * 
	 * @param executionTime the new execution time
	 */
	public void setExecutionTime(Integer executionTime) {
		this.executionTime = executionTime;
	}

	/**
	 * Gets the sbi sub object.
	 * 
	 * @return the sbi sub object
	 */
	public SbiSubObjects getSbiSubObject() {
		return sbiSubObject;
	}

	/**
	 * Sets the sbi sub object.
	 * 
	 * @param sbiSubObject the new sbi sub object
	 */
	public void setSbiSubObject(SbiSubObjects sbiSubObject) {
		this.sbiSubObject = sbiSubObject;
	}

	/**
	 * Gets the sub obj name.
	 * 
	 * @return the sub obj name
	 */
	public String getSubObjName() {
		return subObjName;
	}

	/**
	 * Sets the sub obj name.
	 * 
	 * @param subObjName the new sub obj name
	 */
	public void setSubObjName(String subObjName) {
		this.subObjName = subObjName;
	}

	/**
	 * Gets the sub obj owner.
	 * 
	 * @return the sub obj owner
	 */
	public String getSubObjOwner() {
		return subObjOwner;
	}

	/**
	 * Sets the sub obj owner.
	 * 
	 * @param subObjOwner the new sub obj owner
	 */
	public void setSubObjOwner(String subObjOwner) {
		this.subObjOwner = subObjOwner;
	}

	/**
	 * Gets the sub obj is public.
	 * 
	 * @return the sub obj is public
	 */
	public Short getSubObjIsPublic() {
		return subObjIsPublic;
	}

	/**
	 * Sets the sub obj is public.
	 * 
	 * @param subObjIsPublic the new sub obj is public
	 */
	public void setSubObjIsPublic(Short subObjIsPublic) {
		this.subObjIsPublic = subObjIsPublic;
	}

	/**
	 * Gets the sub obj id.
	 * 
	 * @return the sub obj id
	 */
	public Integer getSubObjId() {
		return subObjId;
	}

	/**
	 * Sets the sub obj id.
	 * 
	 * @param subObjId the new sub obj id
	 */
	public void setSubObjId(Integer subObjId) {
		this.subObjId = subObjId;
	}
	
}
