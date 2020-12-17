package it.eng.spagobi.tools.dataset.service.federated.dto;

import javax.validation.constraints.NotNull;

public class FederationQueryDTO {

	Integer federationId;

	@NotNull
	public Integer getFederationId() {
		return federationId;
	}

	public void setFederationId(Integer federationId) {
		this.federationId = federationId;
	}

}
