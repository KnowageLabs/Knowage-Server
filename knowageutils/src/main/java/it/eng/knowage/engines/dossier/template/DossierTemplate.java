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
package it.eng.knowage.engines.dossier.template;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import it.eng.knowage.engines.dossier.template.parameter.Parameter;
import it.eng.knowage.engines.dossier.template.ppt.PptTemplate;
import it.eng.knowage.engines.dossier.template.report.Report;




public class DossierTemplate {
	
	private PptTemplate  pptTemplate;
	@JsonProperty("REPORT")
	private List<Report> reports;
	
	public DossierTemplate() {
		reports = new ArrayList<>();
	}
		
	public List<Report> getReports() {
		return reports;
	}
	@JsonSetter("REPORT")
	public void setReports(List<Report> reports) {
		this.reports = reports;
	}
	public PptTemplate getPptTemplate() {
		return pptTemplate;
	}
	@JsonSetter("PPT_TEMPLATE")
	public void setPptTemplate(PptTemplate pptTemplate) {
		this.pptTemplate = pptTemplate;
	}
	

	
	@JsonIgnore
	public List<Parameter> getDinamicParams(){
		List<Parameter> dinamicParams = new ArrayList<>();
		for (int i = 0; i < reports.size(); i++) {
			dinamicParams.addAll(reports.get(i).getDinamicParams());
		}
		return dinamicParams;
	}

	


	
	
}
