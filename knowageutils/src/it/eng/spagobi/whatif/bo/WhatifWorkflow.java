package it.eng.spagobi.whatif.bo;

import java.io.Serializable;

public class WhatifWorkflow implements Serializable {

	private int id;
	private int userId;
	private int sequcence;
	private int modelId;
	private String state;
	private String notes;
	private String info;

	public WhatifWorkflow(int id, int userId, int sequcence, String state, String notes, String info, int modelId) {
		super();
		this.id = id;
		this.userId = userId;
		this.sequcence = sequcence;
		this.state = state;
		this.notes = notes;
		this.info = info;
		this.modelId = modelId;
	}

	public WhatifWorkflow() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

}
