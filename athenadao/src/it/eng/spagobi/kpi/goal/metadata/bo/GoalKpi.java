/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.goal.metadata.bo;


public class GoalKpi {
	
	private Integer modelInstanceId;
	private Double weight1;
	private Double weight2;
	private Double threshold1;
	private Double threshold2;
	private Integer sign1;
	private Integer sign2;
	private Integer id;
	private Integer goalNodeId;
	
	
	
	public GoalKpi(Integer modelInstance, Double weight1, Double weight2,
			Double threshold1, Double threshold2, Integer sign1, Integer sign2,
			Integer id, Integer goalNodeId) {
		super();
		this.modelInstanceId = modelInstance;
		this.weight1 = weight1;
		this.weight2 = weight2;
		this.threshold1 = threshold1;
		this.threshold2 = threshold2;
		this.sign1 = sign1;
		this.sign2 = sign2;
		this.id = id;
		this.goalNodeId = goalNodeId;
	}



	public Integer getModelInstanceId() {
		return modelInstanceId;
	}



	public void setModelInstanceId(Integer modelInstanceId) {
		this.modelInstanceId = modelInstanceId;
	}



	public Double getWeight1() {
		return weight1;
	}



	public void setWeight1(Double weight1) {
		this.weight1 = weight1;
	}



	public Double getWeight2() {
		return weight2;
	}



	public void setWeight2(Double weight2) {
		this.weight2 = weight2;
	}



	public Double getThreshold1() {
		return threshold1;
	}



	public void setThreshold1(Double threshold1) {
		this.threshold1 = threshold1;
	}



	public Double getThreshold2() {
		return threshold2;
	}



	public void setThreshold2(Double threshold2) {
		this.threshold2 = threshold2;
	}



	public Integer getSign1() {
		return sign1;
	}



	public void setSign1(Integer sign1) {
		this.sign1 = sign1;
	}



	public Integer getSign2() {
		return sign2;
	}



	public void setSign2(Integer sign2) {
		this.sign2 = sign2;
	}



	public Integer getId() {
		return id;
	}



	public void setId(Integer id) {
		this.id = id;
	}



	public Integer getGoalNodeId() {
		return goalNodeId;
	}



	public void setGoalNodeId(Integer goalNodeId) {
		this.goalNodeId = goalNodeId;
	}



	
	
	
}
