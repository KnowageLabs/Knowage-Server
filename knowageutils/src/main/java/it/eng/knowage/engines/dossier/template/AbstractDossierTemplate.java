package it.eng.knowage.engines.dossier.template;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import it.eng.knowage.engines.dossier.template.doc.DocDossierTemplate;
import it.eng.knowage.engines.dossier.template.parameter.Parameter;
import it.eng.knowage.engines.dossier.template.ppt.PptDossierTemplate;
import it.eng.knowage.engines.dossier.template.report.Report;

public class AbstractDossierTemplate {

	private String name;
	private PptDossierTemplate pptTemplate;
	private DocDossierTemplate docTemplate;
	private String downloadable;
	private String uploadable;

	@JsonProperty("REPORT")
	List<Report> reports = new ArrayList<>();

	public PptDossierTemplate getPptTemplate() {
		return pptTemplate;
	}

	@JsonSetter("PPT_TEMPLATE")
	public void setPptTemplate(PptDossierTemplate pptTemplate) {
		this.pptTemplate = pptTemplate;
	}

	public DocDossierTemplate getDocTemplate() {
		return docTemplate;
	}

	@JsonSetter("DOC_TEMPLATE")
	public void setDocTemplate(DocDossierTemplate docTemplate) {
		this.docTemplate = docTemplate;
	}

	public String getName() {
		return name;
	}

	@XmlAttribute(name = "name", required = true)
	public void setName(String name) {
		this.name = name;
	}

	public List<Report> getReports() {
		return reports;
	}

	@JsonSetter("REPORT")
	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

	public String getDownloadable() {
		return downloadable;
	}

	@XmlAttribute(name = "downloadable", required = true)
	public void setDownloadable(String downloadable) {
		this.downloadable = downloadable;
	}

	public String getUploadable() {
		return uploadable;
	}

	@XmlAttribute(name = "uploadable", required = true)
	public void setUploadable(String uploadable) {
		this.uploadable = uploadable;
	}

	@JsonIgnore
	public List<Parameter> getDinamicParams() {
		List<Parameter> dinamicParams = new ArrayList<>();
		for (int i = 0; i < reports.size(); i++) {
			dinamicParams.addAll(reports.get(i).getDinamicParams());
		}
		return dinamicParams;
	}

}
