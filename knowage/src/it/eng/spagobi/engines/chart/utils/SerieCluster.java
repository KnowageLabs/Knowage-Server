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
