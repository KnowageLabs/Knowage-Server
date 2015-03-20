/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.blockcharts.util;

public class AnnotationBlock {

	Double xStart;
	Double yStart;
	Double xEnd;
	Double yEnd;

	Double xPosition;
	Double yPosition;

	String annotation;


	public AnnotationBlock(String annotation) {
		super();
		this.annotation = annotation;
	}

	public Double getXPosition() {
		return xPosition;
	}
	public void setXPosition(Double position) {
		xPosition = position;
	}
	public Double getYPosition() {
		return yPosition;
	}
	public void setYPosition(Double position) {
		yPosition = position;
	}
	public String getAnnotation() {
		return annotation;
	}
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public Double getXStart() {
		return xStart;
	}

	public void setXStart(Double start) {
		xStart = start;
	}

	public Double getYStart() {
		return yStart;
	}

	public void setYStart(Double start) {
		yStart = start;
	}

	public Double getXEnd() {
		return xEnd;
	}

	public void setXEnd(Double end) {
		if(xEnd!=null && end<xEnd){
		}
		else{
			xEnd = end;
		}
	}

	public Double getYEnd() {
		return yEnd;
	}

	public void setYEnd(Double end) {
		if(yEnd!=null && end<yEnd){
		}
		else{
			yEnd = end;
		}
	}



}
