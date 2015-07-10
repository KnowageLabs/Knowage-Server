package it.eng.spagobi.tools.dataset.ckan.result.impl;

import it.eng.spagobi.tools.dataset.ckan.resource.impl.Organization;
import it.eng.spagobi.tools.dataset.ckan.result.CKANResult;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationResult extends CKANResult {

	private Organization result;

	public Organization getResult() {
		return result;
	}

	public void setResult(Organization result) {
		this.result = result;
	}

}