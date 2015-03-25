/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.utils;

public class SerieCluster {

	String serie;
	
	double[] x;
	double[] y;
	double[] z;
	
	
	public SerieCluster(String serie, double[] x, double[] y, double[] z) {
		super();
		this.serie = serie;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public String getSerie() {
		return serie;
	}
	public void setSerie(String serie) {
		this.serie = serie;
	}
	public double[] getX() {
		return x;
	}
	public void setX(double[] x) {
		this.x = x;
	}
	public double[] getY() {
		return y;
	}
	public void setY(double[] y) {
		this.y = y;
	}
	public double[] getZ() {
		return z;
	}
	public void setZ(double[] z) {
		this.z = z;
	}

	
	
}
