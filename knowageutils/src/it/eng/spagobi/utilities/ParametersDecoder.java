/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities;

import it.eng.spagobi.container.IContainer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author Gioia
 *
 */
public class ParametersDecoder {

	static private Logger logger = Logger.getLogger(ParametersDecoder.class);
	
	private String openBlockMarker;
	private String closeBlockMarker;
	
	public static final String DEFAULT_OPEN_BLOCK_MARKER = "{";
	public static final String DEFAULT_CLOSE_BLOCK_MARKER = "}";
	
	
	/////////////////////////////////////////////////////////////
	//	CONSTRUCTORS
	/////////////////////////////////////////////////////////////
	
	/**
	 * Instantiates a new parameters decoder.
	 */
	public ParametersDecoder() {
		this(DEFAULT_OPEN_BLOCK_MARKER, DEFAULT_CLOSE_BLOCK_MARKER);
	}
	
	/**
	 * Instantiates a new parameters decoder.
	 * 
	 * @param openBlockMarker the open block marker
	 * @param closeBlockMarker the close block marker
	 */
	public ParametersDecoder(String openBlockMarker, String closeBlockMarker) {
		this.openBlockMarker = openBlockMarker;
		this.closeBlockMarker = closeBlockMarker;
	}
	
	
	/////////////////////////////////////////////////////////////
	//	ACCESS METHODS
	/////////////////////////////////////////////////////////////
	
	/**
	 * Gets the close block marker.
	 * 
	 * @return the close block marker
	 */
	public String getCloseBlockMarker() {
		return closeBlockMarker;
	}

	/**
	 * Sets the close block marker.
	 * 
	 * @param closeBlockMarker the new close block marker
	 */
	public void setCloseBlockMarker(String closeBlockMarker) {
		this.closeBlockMarker = closeBlockMarker;
	}

	/**
	 * Gets the open block marker.
	 * 
	 * @return the open block marker
	 */
	public String getOpenBlockMarker() {
		return openBlockMarker;
	}

	/**
	 * Sets the open block marker.
	 * 
	 * @param openBlockMarker the new open block marker
	 */
	public void setOpenBlockMarker(String openBlockMarker) {
		this.openBlockMarker = openBlockMarker;
	}
	
	
	/////////////////////////////////////////////////////////////
	//	PUBLIC METHODS
	/////////////////////////////////////////////////////////////
	
	/**
	 * Checks if is multi values.
	 * 
	 * @param value the value
	 * 
	 * @return true, if is multi values
	 */
	public boolean isMultiValues(String value) {
		return (value.trim().startsWith(openBlockMarker));
	}
	
	/**
	 * Decode.
	 * 
	 * @param value the value
	 * 
	 * @return the list
	 */
	public List decode(String value) {
		logger.debug("IN: value = " + value);
		List values = null;
		
		if(value == null) return null;
		
		if(isMultiValues(value)) {
			values = new ArrayList();
			String separator = getSeparator(value);
			String innerBlock = getInnerBlock(value);
			String parameterType = getParameterType(value);
			String[] chunks = innerBlock.split(separator);
			for(int i = 0; i < chunks.length; i++) {
				String singleValue = chunks[i];
				if (parameterType.equalsIgnoreCase("STRING")) {
					logger.debug("Single string value = [" + singleValue + "]");
					singleValue = singleValue.replaceAll("'", "''");
					logger.debug("After single quotes (') escape, single string value is = [" + singleValue + "]");
					logger.debug("Adding quotes to parameter value ... ");
					singleValue = "'" + singleValue + "'";
					logger.debug("Final single string value is = [" + singleValue + "]");
					/*
					if (singleValue.trim().equals("")) {
						logger.debug("Adding quotes to parameter value ... ");
						singleValue = "'" + singleValue + "'";
					} else {
						if (!singleValue.startsWith("'") && !singleValue.endsWith("'")) {
							logger.debug("Adding quotes to parameter value ... ");
							singleValue = "'" + singleValue + "'";
						}
					}
					*/
				}
				values.add(singleValue);
			}
		} else {
			values = new ArrayList();
			values.add(value);
		}
		
		logger.debug("OUT: list of values = " + (values == null ? null : values.toString()));
		return values;
	}
	
	/**
	 * Get the original values (without adding the quotes)
	 * 
	 * @param value the value
	 * 
	 * @return the list
	 */
	public List getOriginalValues(String value) {
		logger.debug("IN: value = " + value);
		List values = null;
		
		if(value == null) return null;
		
		if(isMultiValues(value)) {
			values = new ArrayList();
			String separator = getSeparator(value);
			String innerBlock = getInnerBlock(value);
			String[] chunks = innerBlock.split(separator);
			for(int i = 0; i < chunks.length; i++) {
				String singleValue = chunks[i];
				values.add(singleValue);
			}
		} else {
			values = new ArrayList();
			values.add(value);
		}
		
		logger.debug("OUT: list of values = " + (values == null ? null : values.toString()));
		return values;
	}
	
	/////////////////////////////////////////////////////////////
	//	UTILITY METHODS
	/////////////////////////////////////////////////////////////
	
	private String getSeparator(String value) {
		logger.debug("IN: value = " + value);
		String separator = null;
		
		int outerBlockOpeningIndex = value.trim().indexOf(openBlockMarker);
		int innerBlockOpeningIndex = value.trim().indexOf(openBlockMarker, outerBlockOpeningIndex + 1);	
		separator = value.substring(outerBlockOpeningIndex + 1, innerBlockOpeningIndex).trim();
		
		logger.debug("OUT: separator = " + separator);
		return separator;
	}
	
	private String getParameterType(String value) {
		logger.debug("IN: value = " + value);
		String parameterType = null;
		
		int innerBlockClosingIndex = value.trim().indexOf(closeBlockMarker);
		int outerBlockClosingIndex = value.trim().indexOf(closeBlockMarker, innerBlockClosingIndex + 1);	
		parameterType = value.substring(innerBlockClosingIndex + 1, outerBlockClosingIndex).trim();
		
		logger.debug("OUT: parameterType = " + parameterType);
		return parameterType;
	}
	
	private String getInnerBlock(String value) {
		logger.debug("IN: value = " + value);
		String innerBlock = null;
		
		int outerBlockOpeningIndex = value.trim().indexOf(openBlockMarker);
		int innerBlockOpeningIndex = value.trim().indexOf(openBlockMarker, outerBlockOpeningIndex + 1);
		int innerBlockClosingIndex = value.trim().indexOf(closeBlockMarker, innerBlockOpeningIndex + 1);	
		innerBlock = value.substring(innerBlockOpeningIndex + 1, innerBlockClosingIndex).trim();
		
		logger.debug("OUT: innerBlock = " + innerBlock);
		return innerBlock;
	}
	

	/**
	 * Gets all decoded parameters defined on http request. Multivalue parameters are converted into List
	 * The returned HashMap will contain the request parameters' names as key; for each parameter, 
	 * the value will be a String if it has a single value, it will be a List if it is multi value (each element of the List being a String).
	 *  
	 * @param servletRequest The http request
	 * @return an HashMap containing all decoded parameters defined on http request. Multivalue parameters are converted into List
	 */
	public static HashMap getDecodedRequestParameters(HttpServletRequest servletRequest) {
		logger.debug("IN");
		HashMap requestParameters = new HashMap();
		ParametersDecoder decoder = new ParametersDecoder();
		Enumeration enumer = servletRequest.getParameterNames();
		while (enumer.hasMoreElements()) {
			String key = (String) enumer.nextElement();
			Object value = null;
			String valueStr = servletRequest.getParameter(key);
			logger.debug("Found request parameter with key = [" + key + "] and value = [" + valueStr + "]");
			try {
				if (decoder.isMultiValues(valueStr)) {
				    value = decoder.getOriginalValues(valueStr).toArray();
				} else {
					value = valueStr;
				}
			} catch (Exception e) {
				logger.warn("Error while decoding parameter with key = [" + key + "] and value = [" + valueStr + "]. It will be not decoded");
				value = valueStr;
			}
			requestParameters.put(key, value);
		}
		logger.debug("OUT");
		return requestParameters;
	}
	
	public static HashMap getDecodedRequestParameters(IContainer requestContainer) {
		logger.debug("IN");
		HashMap requestParameters = new HashMap();
		ParametersDecoder decoder = new ParametersDecoder();
		Iterator it = requestContainer.getKeys().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = null;
			String valueStr = requestContainer.getString(key);
			logger.debug("Found request parameter with key = [" + key + "] and value = [" + valueStr + "]");
			try {
				if (decoder.isMultiValues(valueStr)) {
				    value = decoder.getOriginalValues(valueStr).toArray();
				} else {
					value = valueStr;
				}
			} catch (Exception e) {
				logger.error("Error while decoding parameter with key = [" + key + "] and value = [" + valueStr + "]. It will be not decoded");
				value = valueStr;
			}
			requestParameters.put(key, value);
		}
		logger.debug("OUT");
		return requestParameters;
	}
	
	/////////////////////////////////////////////////////////////
	//	MAIN METHOD
	/////////////////////////////////////////////////////////////
	
	/**
	 * Just for test purpose ;-).
	 * 
	 * @param args the args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
