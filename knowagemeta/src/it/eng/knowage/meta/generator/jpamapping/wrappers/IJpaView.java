/**
 SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0. If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/.
 
**/
package it.eng.knowage.meta.generator.jpamapping.wrappers;

import it.eng.knowage.meta.generator.jpamapping.wrappers.impl.JpaViewInnerJoinRelatioship;
import it.eng.knowage.meta.generator.jpamapping.wrappers.impl.JpaViewInnerTable;
import it.eng.knowage.meta.generator.jpamapping.wrappers.impl.JpaViewOuterRelationship;

import java.util.List;

public interface IJpaView {
	
	String getPackage();

	public String getName();
	
	public String getDescription();
	
	String getClassName();
	
	String getQualifiedClassName();
	
	String getUniqueName();
	
	String getAttribute(String name);
	
	
	

	List<IJpaTable> getInnerTables();

	List<IJpaColumn> getColumns(JpaViewInnerTable table);
	
	List<JpaViewInnerJoinRelatioship> getJoinRelationships();
	
	List<JpaViewOuterRelationship> getRelationships();
	
	List<IJpaSubEntity> getSubEntities();

}