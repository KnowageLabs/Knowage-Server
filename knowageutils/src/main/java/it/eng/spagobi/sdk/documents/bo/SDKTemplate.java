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

package it.eng.spagobi.sdk.documents.bo;

public class SDKTemplate implements java.io.Serializable {
	private javax.activation.DataHandler content;

	private String fileName;

	private String folderName;

	public SDKTemplate() {
	}

	public SDKTemplate(javax.activation.DataHandler content, String fileName, String folderName) {
		this.content = content;
		this.fileName = fileName;
		this.folderName = folderName;
	}

	/**
	 * Gets the content value for this SDKTemplate.
	 *
	 * @return content
	 */
	public javax.activation.DataHandler getContent() {
		return content;
	}

	/**
	 * Sets the content value for this SDKTemplate.
	 *
	 * @param content
	 */
	public void setContent(javax.activation.DataHandler content) {
		this.content = content;
	}

	/**
	 * Gets the fileName value for this SDKTemplate.
	 *
	 * @return fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the fileName value for this SDKTemplate.
	 *
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets the folderName value for this SDKTemplate.
	 *
	 * @return folderName
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * Sets the folderName value for this SDKTemplate.
	 *
	 * @param folderName
	 */
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKTemplate))
			return false;
		SDKTemplate other = (SDKTemplate) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true
				&& ((this.content == null && other.getContent() == null)
						|| (this.content != null && this.content.equals(other.getContent())))
				&& ((this.fileName == null && other.getFileName() == null)
						|| (this.fileName != null && this.fileName.equals(other.getFileName())))
				&& ((this.folderName == null && other.getFolderName() == null)
						|| (this.folderName != null && this.folderName.equals(other.getFolderName())));
		__equalsCalc = null;
		return _equals;
	}

	private boolean __hashCodeCalc = false;

	@Override
	public synchronized int hashCode() {
		if (__hashCodeCalc) {
			return 0;
		}
		__hashCodeCalc = true;
		int _hashCode = 1;
		if (getContent() != null) {
			_hashCode += getContent().hashCode();
		}
		if (getFileName() != null) {
			_hashCode += getFileName().hashCode();
		}
		if (getFolderName() != null) {
			_hashCode += getFolderName().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
