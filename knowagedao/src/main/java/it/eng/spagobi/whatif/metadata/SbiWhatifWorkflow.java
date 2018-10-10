package it.eng.spagobi.whatif.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiWhatifWorkflow extends SbiHibernateModel {

	private int id;
	private int userId;
	private int modelId;
	private int sequcence;
	private String state;
	private String notes;
	private String info;

	public SbiWhatifWorkflow() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSequcence() {
		return sequcence;
	}

	public void setSequcence(int sequcence) {
		this.sequcence = sequcence;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getModelId() {
		return modelId;
	}

	public void setModelId(int modelId) {
		this.modelId = modelId;
	}

}
