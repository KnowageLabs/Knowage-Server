package it.eng.spagobi.tools.dataset.ckan.result.list.impl;

import it.eng.spagobi.tools.dataset.ckan.resource.impl.OrganizationSummary;
import it.eng.spagobi.tools.dataset.ckan.result.CKANResult;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationSummaryList extends CKANResult {

	private List<OrganizationSummary> result;

	public List<OrganizationSummary> getResult() {
		return result;
	}

	public void setResult(List<OrganizationSummary> result) {
		this.result = result;
	}

}
