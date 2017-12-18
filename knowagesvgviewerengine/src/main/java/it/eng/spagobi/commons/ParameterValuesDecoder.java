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
package it.eng.spagobi.commons;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gioia
 *
 */
public class ParameterValuesDecoder {

	private String openBlockMarker;
	private String closeBlockMarker;
	
	public static final String DEFAULT_OPEN_BLOCK_MARKER = "{";
	public static final String DEFAULT_CLOSE_BLOCK_MARKER = "}";
	
	
	/////////////////////////////////////////////////////////////
	//	CONSTRUCTORS
	/////////////////////////////////////////////////////////////
	
	/**
	 * Instantiates a new parameter values decoder.
	 */
	public ParameterValuesDecoder() {
		this(DEFAULT_OPEN_BLOCK_MARKER, DEFAULT_CLOSE_BLOCK_MARKER);
	}
	
	/**
	 * Instantiates a new parameter values decoder.
	 * 
	 * @param openBlockMarker the open block marker
	 * @param closeBlockMarker the close block marker
	 */
	public ParameterValuesDecoder(String openBlockMarker, String closeBlockMarker) {
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
		List values = null;
		
		if(value == null) return null;
		
		if(isMultiValues(value)) {
			values = new ArrayList();
			String separator = getSeparator(value);
			String innerBlock = getInnerBlock(value);
			String[] chunks = innerBlock.split(separator);
			for(int i = 0; i < chunks.length; i++) {
				values.add(chunks[i]);
			}
		} else {
			values = new ArrayList();
			values.add(value);
		}
		
		return values;
	}
	
	/////////////////////////////////////////////////////////////
	//	UTILITY METHODS
	/////////////////////////////////////////////////////////////
	
	private String getSeparator(String value) {
		String separator = null;
		
		int outerBlockOpeningIndex = value.trim().indexOf(openBlockMarker);
		int innerBlockOpeningIndex = value.trim().indexOf(openBlockMarker, outerBlockOpeningIndex + 1);	
		separator = value.substring(outerBlockOpeningIndex + 1, innerBlockOpeningIndex).trim();
		
		return separator;
	}
	
	private String getInnerBlock(String value) {
		String innerBlock = null;
		
		int outerBlockOpeningIndex = value.trim().indexOf(openBlockMarker);
		int innerBlockOpeningIndex = value.trim().indexOf(openBlockMarker, outerBlockOpeningIndex + 1);
		int innerBlockClosingIndex = value.trim().indexOf(closeBlockMarker, innerBlockOpeningIndex + 1);	
		innerBlock = value.substring(innerBlockOpeningIndex + 1, innerBlockClosingIndex).trim();
		
		return innerBlock;
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
