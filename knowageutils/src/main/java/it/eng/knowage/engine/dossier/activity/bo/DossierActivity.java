/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2024 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.engine.dossier.activity.bo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DossierActivity {

	private Integer id;
	private Integer documentId;
	private String activity;
	private String parameters;
	private Integer partial;
	private Integer total;
	private String status;
	private Integer progressId;
	@JsonIgnore
	private byte[] binContent;
	private boolean hasBinContent;
	private boolean hasDocBinContent;
	private boolean hasPptV2BinContent;
	private boolean pptExists;
	private Date creationDate;
	private String configContent;
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
	private String executionRole;

	@JsonIgnore
	private byte[] docBinContent;

	@JsonIgnore
	private byte[] pptV2BinContent;

	public String getActivity() {
		return activity;
	}

	@JsonIgnore
	public byte[] getBinContent() {
		return binContent;
	}

	public String getConfigContent() {
		return configContent;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public byte[] getDocBinContent() {
		return docBinContent;
	}

	public Integer getDocumentId() {
		return documentId;
	}

	public String getExecutionRole() {
		return executionRole;
	}

	public Integer getId() {
		return id;
	}

	public String getParameters() {
		return parameters;
	}

	public Integer getPartial() {
		return partial;
	}

	public byte[] getPptV2BinContent() {
		return pptV2BinContent;
	}

	public Integer getProgressId() {
		return progressId;
	}

	public String getStatus() {
		return status;
	}

	public Integer getTotal() {
		return total;
	}

	public boolean isHasBinContent() {
		return hasBinContent;
	}

	public boolean isHasDocBinContent() {
		return hasDocBinContent;
	}

	public boolean isHasPptV2BinContent() {
		return hasPptV2BinContent;
	}

	public boolean isPptExists() {
		return pptExists;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	@JsonIgnore
	public void setBinContent(byte[] binContent) {
		this.binContent = binContent;
	}

	public void setConfigContent(String configContent) {
		this.configContent = configContent;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public void setDocBinContent(byte[] docBinContent) {
		this.docBinContent = docBinContent;
	}

	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}

	public void setExecutionRole(String role) {
		this.executionRole = role;
	}

	public void setHasBinContent(boolean hasBinContent) {
		this.hasBinContent = hasBinContent;
	}

	public void setHasDocBinContent(boolean hasDocBinContent) {
		this.hasDocBinContent = hasDocBinContent;
	}

	public void setHasPptV2BinContent(boolean hasPptV2BinContent) {
		this.hasPptV2BinContent = hasPptV2BinContent;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public void setPartial(Integer partial) {
		this.partial = partial;
	}

	public void setPptExists(boolean pptExists) {
		this.pptExists = pptExists;
	}

	public void setPptV2BinContent(byte[] pptV2BinContent) {
		this.pptV2BinContent = pptV2BinContent;
	}

	public void setProgressId(Integer progressId) {
		this.progressId = progressId;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

}
