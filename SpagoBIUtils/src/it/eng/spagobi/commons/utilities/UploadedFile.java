/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 4-mag-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.utilities;

import java.io.Serializable;

/**
 * Defines an <code>UploadedFile</code> object.
 * 
 * @author Zoppello
 *
 */
public class UploadedFile implements Serializable {
	private byte[] fileContent = null;
	
	private String fileName = null;
	
	private long sizeInBytes;
	
	/**
	 * Gets the field name in form.
	 * 
	 * @return Returns the fieldNameInForm.
	 */
	public String getFieldNameInForm() {
		return fieldNameInForm;
	}
	
	/**
	 * Sets the field name in form.
	 * 
	 * @param fieldNameInForm The fieldNameInForm to set.
	 */
	public void setFieldNameInForm(String fieldNameInForm) {
		this.fieldNameInForm = fieldNameInForm;
	}
	
	/**
	 * Gets the file content.
	 * 
	 * @return Returns the fileContent.
	 */
	public byte[] getFileContent() {
		return fileContent;
	}
	
	/**
	 * Sets the file content.
	 * 
	 * @param fileContent The fileContent to set.
	 */
	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}
	
	/**
	 * Gets the file name.
	 * 
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Sets the file name.
	 * 
	 * @param fileName The fileName to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * Gets the size in bytes.
	 * 
	 * @return Returns the sizeInBytes.
	 */
	public long getSizeInBytes() {
		return sizeInBytes;
	}
	
	/**
	 * Sets the size in bytes.
	 * 
	 * @param sizeInBytes The sizeInBytes to set.
	 */
	public void setSizeInBytes(long sizeInBytes) {
		this.sizeInBytes = sizeInBytes;
	}
	private String fieldNameInForm = null;
}
