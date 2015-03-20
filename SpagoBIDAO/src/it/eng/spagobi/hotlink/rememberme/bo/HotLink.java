/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.hotlink.rememberme.bo;

import java.io.Serializable;


/**
 * This class is a bean that contains all information needed to build a hot link:
 * document identifier and other document attributes, and execution parameters.
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class HotLink implements Serializable{

	protected Integer objId;
	protected String documentLabel;
	protected String documentName;
	protected String documentDescription;
	protected String documentType;
	protected Integer subObjId;
	protected String subObjName;
	protected String parameters;
	protected String engineName;
	
	/**
	 * Gets the obj id.
	 * 
	 * @return the obj id
	 */
	public Integer getObjId() {
		return objId;
	}
	
	/**
	 * Sets the obj id.
	 * 
	 * @param objId the new obj id
	 */
	public void setObjId(Integer objId) {
		this.objId = objId;
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
	 * Gets the document description.
	 * 
	 * @return the document description
	 */
	public String getDocumentDescription() {
		return documentDescription;
	}
	
	/**
	 * Sets the document description.
	 * 
	 * @param documentDescription the new document description
	 */
	public void setDocumentDescription(String documentDescription) {
		this.documentDescription = documentDescription;
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
	 * Gets the parameters.
	 * 
	 * @return the parameters
	 */
	public String getParameters() {
		return parameters;
	}
	
	/**
	 * Sets the parameters.
	 * 
	 * @param parameters the new parameters
	 */
	public void setParameters(String parameters) {
		this.parameters = parameters;
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
	
}
