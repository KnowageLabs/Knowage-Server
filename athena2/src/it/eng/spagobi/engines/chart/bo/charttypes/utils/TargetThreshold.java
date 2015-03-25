/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.utils;

import it.eng.spagobi.services.execute.service.ServiceChartImpl;

import java.awt.Color;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class TargetThreshold {

	String name="";
	Double value=null;
	java.awt.Color color=null;
	boolean main=false;	
	boolean isTarget=false;
	boolean	visible=true;

	private static transient Logger logger=Logger.getLogger(TargetThreshold.class);



	public TargetThreshold(String name, Double value, Color color, boolean main, boolean visible) {
		super();
		this.name = name;
		this.value = value;
		this.color = color;
		this.main = main;
		this.visible=visible;
	}

	/**
	 * 
	 * @param parameterValue: a string in the form [name: nome, color: #FF0000, value: 2.0, main: false]
	 */
	public TargetThreshold(String parameterValue) {
		logger.debug("IN");
		String parameterValueNoBrackets=parameterValue.substring(1, parameterValue.length()-1);

		StringTokenizer tokenizer=new StringTokenizer(parameterValueNoBrackets,",");
		while(tokenizer.hasMoreTokens()){
			String token=tokenizer.nextToken();
			int indexEqual=token.indexOf(":");
			String namePar=token.substring(0,indexEqual).trim();
			String valuePar=token.substring(indexEqual+1).trim();

			if(namePar.equalsIgnoreCase("name")){
				this.name=valuePar;
			}
			else if(namePar.equalsIgnoreCase("value")){
				try{
					Double val=Double.valueOf(valuePar.trim());
					this.value=val;
				}
				catch (NumberFormatException e) {
					logger.error("value is not a double");
					this.value=null;
				}
			}
			else if(namePar.equalsIgnoreCase("color")){
				try{
					Color color=Color.decode(valuePar.trim());
					this.color=color;
				}
				catch (NumberFormatException e) {
					logger.error("color is not correctly defined");
					this.color=null;
				}
			}
			else if(namePar.equalsIgnoreCase("main")){
				boolean isMain=false;
				if(valuePar.equalsIgnoreCase("true")){
					isMain=true;
				}
				this.main=isMain;
			}
			// visible can be false and true or 0 and !=0
			else if(namePar.equalsIgnoreCase("visible")){
				boolean isVisible=true;
				if(valuePar.equalsIgnoreCase("false") || valuePar.equalsIgnoreCase("0") || valuePar.equalsIgnoreCase("0.0")){
					isVisible=false;
				}

				this.visible=isVisible;
			}			

		}
	}



	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	public java.awt.Color getColor() {
		return color;
	}
	public void setColor(java.awt.Color color) {
		this.color = color;
	}
	public boolean isMain() {
		return main;
	}
	public void setMain(boolean main) {
		this.main = main;
	}
	public boolean isTarget() {
		return isTarget;
	}
	public void setTarget(boolean isTarget) {
		this.isTarget = isTarget;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}



}
