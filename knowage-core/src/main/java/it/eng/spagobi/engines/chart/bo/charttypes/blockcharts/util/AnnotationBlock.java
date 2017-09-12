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
