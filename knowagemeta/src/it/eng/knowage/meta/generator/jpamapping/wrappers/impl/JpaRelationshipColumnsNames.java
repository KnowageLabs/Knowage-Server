/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator.jpamapping.wrappers.impl;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class JpaRelationshipColumnsNames {
	
	private String sourceColumnName;
	private String destinationColumnName;
	
	/**
	 * @return the sourceColumnName
	 */
	public String getSourceColumnName() {
		return sourceColumnName;
	}

	/**
	 * @param sourceColumnName the sourceColumnName to set
	 */
	public void setSourceColumnName(String sourceColumnName) {
		this.sourceColumnName = sourceColumnName;
	}

	
	
	/**
	 * @return the destinationColumnName
	 */
	public String getDestinationColumnName() {
		return destinationColumnName;
	}

	/**
	 * @param destinationColumnName the destinationColumnName to set
	 */
	public void setDestinationColumnName(String destinationColumnName) {
		this.destinationColumnName = destinationColumnName;
	}

	public JpaRelationshipColumnsNames(String sourceColumnName, String destinationColumnName){
		this.sourceColumnName = sourceColumnName;
		this.destinationColumnName = destinationColumnName;
	}

}
