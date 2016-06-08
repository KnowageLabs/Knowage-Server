package it.eng.spagobi.workspace.bo;

import it.eng.spagobi.hotlink.rememberme.bo.HotLink;

public class DocumentOrganizer extends HotLink {

	private Integer functId;
	private Integer biObjId;

	public DocumentOrganizer() {
		super();
	}

	public DocumentOrganizer(Integer functId, Integer biObjId) {
		super();
		this.functId = functId;
		this.biObjId = biObjId;
	}

	public Integer getFunctId() {
		return functId;
	}

	public void setFunctId(Integer functId) {
		this.functId = functId;
	}

	public Integer getBiObjId() {
		return biObjId;
	}

	public void setBiObjId(Integer biObjId) {
		this.biObjId = biObjId;
	}

}
