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
package it.eng.spagobi.dossier.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.massiveExport.metadata.SbiProgressThread;

public class SbiDossierActivity extends SbiHibernateModel {

	private static final long serialVersionUID = 1104724670349915546L;

	private Integer id;
	private Integer documentId;
	private String activity;
	private String parameters;
	private SbiProgressThread progress;
	private byte[] binContent;
	private byte[] docBinContent;
	private String configContent;
	private byte[] pptV2BinContent;

	public SbiDossierActivity(Integer id) {
		this.id=id;
	}

	public String getConfigContent() {
		return configContent;
	}

	public void setConfigContent(String configContent) {
		this.configContent = configContent;
	}

	public byte[] getBinContent() {
		return binContent;
	}

	public void setBinContent(byte[] binContent) {
		this.binContent = binContent;
	}

	public Integer getId() {
		return id;
	}

	private void setId(Integer id) {
		this.id = id;
	}

	public SbiProgressThread getProgress() {
		return progress;
	}

	public void setProgress(SbiProgressThread progress) {
		this.progress = progress;
	}

	public Integer getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public byte[] getDocBinContent() {
		return docBinContent;
	}

	public void setDocBinContent(byte[] docBinContent) {
		this.docBinContent = docBinContent;
	}

	public byte[] getPptV2BinContent() {
		return pptV2BinContent;
	}

	public void setPptV2BinContent(byte[] pptV2BinContent) {
		this.pptV2BinContent = pptV2BinContent;
	}

}
