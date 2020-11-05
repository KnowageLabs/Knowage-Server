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

	@JsonIgnore
	public List<Parameter> getDinamicParams() {
		List<Parameter> dinamicParams = new ArrayList<>();
		for (int i = 0; i < reports.size(); i++) {
			dinamicParams.addAll(reports.get(i).getDinamicParams());
		}
		return dinamicParams;
	}

}
