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

package it.eng.spagobi.api.dto;

import javax.validation.constraints.Size;
import javax.ws.rs.FormParam;

import it.eng.spagobi.services.validation.AlphanumericNoSpaces;
import it.eng.spagobi.services.validation.ExtendedAlphanumeric;

public class SelfServiceDataSetDTO {

	@FormParam("label")
	private String label;

	@FormParam("meta")
	private String meta;

	@FormParam("persist")
	private String persist;

	@FormParam("tableName")
	private String tableName;

	@FormParam("id")
	private String id;

	@FormParam("type")
	private String type;

	@FormParam("description")
	private String description;

	@FormParam("name")
	private String name;

	@FormParam("catTypeId")
	private String catTypeId;

	@FormParam("ckanUrl")
	private String ckanUrl;

	@FormParam("ckanId")
	private String ckanId;

	@FormParam("fileName")
	private String fileName;

	@FormParam("fileType")
	private String fileType;

	@FormParam("newFileUploaded")
	private String newFileUploaded;

	@FormParam("fileUploaded")
	private String fileUploaded;

	@FormParam("qbeJSONQuery")
	private String qbeJSONQuery;

	@FormParam("qbeDatamarts")
	private String qbeDatamarts;

	@FormParam("qbeDataSource")
	private String qbeDataSource;

	@FormParam("csvDelimiter")
	private String csvDelimiter;

	@FormParam("csvQuote")
	private String csvQuote;

	@FormParam("csvEncoding")
	private String csvEncoding;

	@FormParam("skipRows")
	private String skipRows;

	@FormParam("limitRows")
	private String limitRows;

	@FormParam("xslSheetNumber")
	private String xslSheetNumber;

	@FormParam("dateFormat")
	private String dateFormat;

	@FormParam("limitPreview")
	private String limitPreview;

	@FormParam("timestampFormat")
	private String timestampFormat;

	@FormParam("datasetMetadata")
	private String datasetMetadata;

	@FormParam("config")
	private String config;

	@ExtendedAlphanumeric
	@Size(max = 50)
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

	public String getPersist() {
		return persist;
	}

	public void setPersist(String persist) {
		this.persist = persist;
	}

	@Size(max = 50)
	@AlphanumericNoSpaces
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Size(max = 50)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Size(max = 160)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@ExtendedAlphanumeric
	@Size(max = 50)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCatTypeId() {
		return catTypeId;
	}

	public void setCatTypeId(String catTypeId) {
		this.catTypeId = catTypeId;
	}

	public String getCkanUrl() {
		return ckanUrl;
	}

	public void setCkanUrl(String ckanUrl) {
		this.ckanUrl = ckanUrl;
	}

	public String getCkanId() {
		return ckanId;
	}

	public void setCkanId(String ckanId) {
		this.ckanId = ckanId;
	}

	@ExtendedAlphanumeric
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getNewFileUploaded() {
		return newFileUploaded;
	}

	public void setNewFileUploaded(String newFileUploaded) {
		this.newFileUploaded = newFileUploaded;
	}

	public String getFileUploaded() {
		return fileUploaded;
	}

	public void setFileUploaded(String fileUploaded) {
		this.fileUploaded = fileUploaded;
	}

	public String getQbeJSONQuery() {
		return qbeJSONQuery;
	}

	public void setQbeJSONQuery(String qbeJSONQuery) {
		this.qbeJSONQuery = qbeJSONQuery;
	}

	public String getQbeDatamarts() {
		return qbeDatamarts;
	}

	public void setQbeDatamarts(String qbeDatamarts) {
		this.qbeDatamarts = qbeDatamarts;
	}

	public String getQbeDataSource() {
		return qbeDataSource;
	}

	public void setQbeDataSource(String qbeDataSource) {
		this.qbeDataSource = qbeDataSource;
	}

	public String getCsvDelimiter() {
		return csvDelimiter;
	}

	public void setCsvDelimiter(String csvDelimiter) {
		this.csvDelimiter = csvDelimiter;
	}

	public String getCsvQuote() {
		return csvQuote;
	}

	public void setCsvQuote(String csvQuote) {
		this.csvQuote = csvQuote;
	}

	public String getCsvEncoding() {
		return csvEncoding;
	}

	public void setCsvEncoding(String csvEncoding) {
		this.csvEncoding = csvEncoding;
	}

	public String getSkipRows() {
		return skipRows;
	}

	public void setSkipRows(String skipRows) {
		this.skipRows = skipRows;
	}

	public String getLimitRows() {
		return limitRows;
	}

	public void setLimitRows(String limitRows) {
		this.limitRows = limitRows;
	}

	public String getXslSheetNumber() {
		return xslSheetNumber;
	}

	public void setXslSheetNumber(String xslSheetNumber) {
		this.xslSheetNumber = xslSheetNumber;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getLimitPreview() {
		return limitPreview;
	}

	public void setLimitPreview(String limitPreview) {
		this.limitPreview = limitPreview;
	}

	public String getTimestampFormat() {
		return timestampFormat;
	}

	public void setTimestampFormat(String timestampFormat) {
		this.timestampFormat = timestampFormat;
	}

	public String getDatasetMetadata() {
		return datasetMetadata;
	}

	public void setDatasetMetadata(String datasetMetadata) {
		this.datasetMetadata = datasetMetadata;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

}
