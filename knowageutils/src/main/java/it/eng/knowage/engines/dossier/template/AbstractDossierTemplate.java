package it.eng.knowage.engines.dossier.template;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

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
