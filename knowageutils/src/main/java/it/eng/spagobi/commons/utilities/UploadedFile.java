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
