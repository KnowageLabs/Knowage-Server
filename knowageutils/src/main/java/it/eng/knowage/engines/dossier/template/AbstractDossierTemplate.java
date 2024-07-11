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
package it.eng.knowage.engines.dossier.template;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import it.eng.knowage.engines.dossier.template.doc.DocDossierTemplate;
import it.eng.knowage.engines.dossier.template.parameter.Parameter;
import it.eng.knowage.engines.dossier.template.ppt.PptDossierTemplate;
import it.eng.knowage.engines.dossier.template.ppt.PptDossierTemplateV2;
import it.eng.knowage.engines.dossier.template.report.Report;

public class AbstractDossierTemplate {

	private String name;
	private PptDossierTemplate pptTemplate;
	private PptDossierTemplateV2 pptTemplateV2;
	private DocDossierTemplate docTemplate;
	private String downloadable;
	private String uploadable;
	private String prefix;
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
	private String executionRole;

	@JsonProperty("REPORT")
	List<Report> reports = new ArrayList<>();

	@JsonIgnore
	public List<Parameter> getDinamicParams() {
		List<Parameter> dinamicParams = new ArrayList<>();
		for (int i = 0; i < reports.size(); i++) {
			dinamicParams.addAll(reports.get(i).getDinamicParams());
		}
		return dinamicParams;
	}

	public DocDossierTemplate getDocTemplate() {
		return docTemplate;
	}

	public String getDownloadable() {
		return downloadable;
	}

	public String getExecutionRole() {
		return executionRole;
	}

	public String getName() {
		return name;
	}

	public PptDossierTemplate getPptTemplate() {
		return pptTemplate;
	}

	public PptDossierTemplateV2 getPptTemplateV2() {
		return pptTemplateV2;
	}

	@JsonIgnore
	public String getPrefix() {
		return prefix;
	}

	public List<Report> getReports() {
		return reports;
	}

	public String getUploadable() {
		return uploadable;
	}

	@JsonSetter("DOC_TEMPLATE")
	public void setDocTemplate(DocDossierTemplate docTemplate) {
		this.docTemplate = docTemplate;
	}

	@XmlAttribute(name = "downloadable", required = true)
	public void setDownloadable(String downloadable) {
		this.downloadable = downloadable;
	}

	public void setExecutionRole(String executionRole) {
		this.executionRole = executionRole;
	}

	@XmlAttribute(name = "name", required = true)
	public void setName(String name) {
		this.name = name;
	}

	@JsonSetter("PPT_TEMPLATE")
	public void setPptTemplate(PptDossierTemplate pptTemplate) {
		this.pptTemplate = pptTemplate;
	}

	@JsonSetter("PPT_TEMPLATE_V2")
	public void setPptTemplateV2(PptDossierTemplateV2 pptTemplateV2) {
		this.pptTemplateV2 = pptTemplateV2;
	}

	@XmlAttribute(name = "prefix", required = false)
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@JsonSetter("REPORT")
	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

	@XmlAttribute(name = "uploadable", required = true)
	public void setUploadable(String uploadable) {
		this.uploadable = uploadable;
	}

}
