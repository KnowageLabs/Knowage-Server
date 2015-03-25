/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.chart.bo.charttypes.utils;

import it.eng.spagobi.commons.constants.ObjectsTreeConstants;

import org.apache.log4j.Logger;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.data.general.PieDataset;

public class MyPieUrlGenerator extends StandardPieURLGenerator{

	private String categoryUrlLabel="catergory";
	private boolean document_composition=false;
	private static transient Logger logger=Logger.getLogger(MyPieUrlGenerator.class);
	private String URL=null;
	private String drillDocTitle = null;
	private String target = "self";

	/* (non-Javadoc)
	 * @see org.jfree.chart.urls.StandardPieURLGenerator#generateURL(org.jfree.data.general.PieDataset, java.lang.Comparable, int)
	 */
	public String generateURL(PieDataset dataset, Comparable key, int pieIndex) {
		logger.debug("IN");
		URL=new String();
		URL=super.generateURL(dataset, key, 0);

		// take the categoryUrlLabel, default is "category"
		if(categoryUrlLabel==null){categoryUrlLabel="category";}
		String categoryToMove=replaceAndGetParameter("category=", categoryUrlLabel);

		// this is the string to move inside PARAMETERS=
		String toMove=categoryToMove;

		// insert into PARAMETERS=
		String parameters=ObjectsTreeConstants.PARAMETERS;
		URL=URL.replaceAll(parameters+"=", parameters+"="+toMove);

		//if(document_composition){
			URL=URL+toMove;
			if(drillDocTitle!=null && target!=null && target.equalsIgnoreCase("tab")){
				URL +="','','"+drillDocTitle+"','tab";
			}else if(drillDocTitle!=null){
				URL +="','','"+drillDocTitle;
			}
			URL=URL+"');";
			logger.debug("Linked URL:"+URL);
		//}

		logger.debug("OUT");
		return URL;
	}

	/**
	 * Instantiates a new my pie url generator.
	 */
	public MyPieUrlGenerator() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	
	
	
	/**
	 * Instantiates a new my pie url generator.
	 * 
	 * @param prefix the prefix
	 * @param categoryParameterName the category parameter name
	 * @param indexParameterName the index parameter name
	 */
	public MyPieUrlGenerator(String prefix, String categoryParameterName,
			String indexParameterName) {
		super(prefix, categoryParameterName, indexParameterName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new my pie url generator.
	 * 
	 * @param prefix the prefix
	 * @param categoryParameterName the category parameter name
	 */
	public MyPieUrlGenerator(String prefix, String categoryParameterName) {
		super(prefix, categoryParameterName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new my pie url generator.
	 * 
	 * @param prefix the prefix
	 */
	public MyPieUrlGenerator(String prefix) {
		super(prefix);
		// TODO Auto-generated constructor stub
	}


	/**
	 * Gets the category url label.
	 * 
	 * @return the category url label
	 */
	public String getCategoryUrlLabel() {
		return categoryUrlLabel;
	}

	/**
	 * Sets the category url label.
	 * 
	 * @param categoryUrlLabel the new category url label
	 */
	public void setCategoryUrlLabel(String categoryUrlLabel) {
		this.categoryUrlLabel = categoryUrlLabel;
	}

	/**
	 * Checks if is document_composition.
	 * 
	 * @return true, if is document_composition
	 */
	public boolean isDocument_composition() {
		return document_composition;
	}

	/**
	 * Sets the document_composition.
	 * 
	 * @param document_composition the new document_composition
	 */
	public void setDocument_composition(boolean document_composition) {
		this.document_composition = document_composition;
	}

	
	

	private String replaceAndGetParameter(String toReplace, String replacer){
		// toReplace, series= or category=

		// Start index to substitute, check there is only one
		int startIndex=URL.indexOf(toReplace);
		int otherStart=URL.lastIndexOf(toReplace);
		if(startIndex!=otherStart){ //menas that there are more occurrence of the same parameter... ERROR
			logger.error("Too many occurrence of the same parameter defined in template");
			return null;
		}
		//end index of thing to substitute
		int endIndex=URL.indexOf('&', startIndex);

		// if there is no end index that is the end of the string
		if(endIndex==-1)endIndex=URL.length();
		String toMove=URL.substring(startIndex, endIndex);

		URL=URL.replaceAll("&amp;"+toMove, "");
		URL=URL.replaceAll(toMove, "");
		
		if(URL.contains("?")){
			int indexQuestion=URL.indexOf('?');
			URL=URL.replace("?", "");
		}
		
		toMove=toMove.replaceAll("category", replacer);	

		toMove=toMove.replaceAll("=", "%3D");
		toMove="%26"+toMove;


		return toMove;


	}

	public String getDrillDocTitle() {
		return drillDocTitle;
	}

	public void setDrillDocTitle(String drillDocTitle) {
		this.drillDocTitle = drillDocTitle;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
}
