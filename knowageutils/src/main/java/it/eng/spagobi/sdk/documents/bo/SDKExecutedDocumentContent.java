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

public class SDKExecutedDocumentContent implements java.io.Serializable {
	private javax.activation.DataHandler content;

	private String fileName;

	private String fileType;

	public SDKExecutedDocumentContent() {
	}

	public SDKExecutedDocumentContent(javax.activation.DataHandler content, String fileName, String fileType) {
		this.content = content;
		this.fileName = fileName;
		this.fileType = fileType;
	}

	/**
	 * Gets the content value for this SDKExecutedDocumentContent.
	 *
	 * @return content
	 */
	public javax.activation.DataHandler getContent() {
		return content;
	}

	/**
	 * Sets the content value for this SDKExecutedDocumentContent.
	 *
	 * @param content
	 */
	public void setContent(javax.activation.DataHandler content) {
		this.content = content;
	}

	/**
	 * Gets the fileName value for this SDKExecutedDocumentContent.
	 *
	 * @return fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the fileName value for this SDKExecutedDocumentContent.
	 *
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets the fileType value for this SDKExecutedDocumentContent.
	 *
	 * @return fileType
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * Sets the fileType value for this SDKExecutedDocumentContent.
	 *
	 * @param fileType
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKExecutedDocumentContent))
			return false;
		SDKExecutedDocumentContent other = (SDKExecutedDocumentContent) obj;
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
				&& ((this.fileType == null && other.getFileType() == null)
						|| (this.fileType != null && this.fileType.equals(other.getFileType())));
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
		if (getFileType() != null) {
			_hashCode += getFileType().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
