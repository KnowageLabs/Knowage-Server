/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.functionalitytree.metadata.SbiFunctions;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParameters;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuse;
import it.eng.spagobi.behaviouralmodel.check.metadata.SbiChecks;
import it.eng.spagobi.behaviouralmodel.lov.metadata.SbiLov;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.metadata.SbiAuthorizations;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarm;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact;
import it.eng.spagobi.kpi.config.metadata.SbiKpi;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstPeriod;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstance;
import it.eng.spagobi.kpi.config.metadata.SbiKpiPeriodicity;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModel;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelInst;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelResources;
import it.eng.spagobi.kpi.model.metadata.SbiResources;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitGrantNodesId;
import it.eng.spagobi.kpi.threshold.metadata.SbiThreshold;
import it.eng.spagobi.kpi.threshold.metadata.SbiThresholdValue;
import it.eng.spagobi.tools.objmetadata.metadata.SbiObjMetacontents;
import it.eng.spagobi.tools.objmetadata.metadata.SbiObjMetadata;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MetadataAssociations {

	private Map parameterIDAssociation = new HashMap();
	private Map parameterAssociation = new HashMap();
	private Map roleIDAssociation = new HashMap();
	private Map roleAssociation = new HashMap();
	private Map biobjIDAssociation = new HashMap();
	private Map biobjAssociation = new HashMap();
	private Map lovIDAssociation = new HashMap();
	private Map lovAssociation = new HashMap();
	private Map functIDAssociation = new HashMap();
	private Map functAssociation = new HashMap();
	private Map engineIDAssociation = new HashMap();
	private Map engineAssociation = new HashMap();
	private Map checkIDAssociation = new HashMap();
	private Map checkAssociation = new HashMap();
	private Map paruseIDAssociation = new HashMap();
	private Map paruseAssociation = new HashMap();
	private Map domainIDAssociation = new HashMap();
	private Map domainAssociation = new HashMap();
	private Map objparIDAssociation = new HashMap();
	private Map objparAssociation = new HashMap();
	private Map datasourcesIDAssociation = new HashMap();
	private Map datasetsIDAssociation = new HashMap();
	private Map metaModelIDAssociation = new HashMap();
	private Map artifactIDAssociation = new HashMap();

	private Map mapsIDAssociation = new HashMap();
	private Map featuresIDAssociation = new HashMap();
	private Map kpiIDAssociation = new HashMap();
	private Map kpiAssociation = new HashMap();
	private Map kpiInstanceIDAssociation = new HashMap();
	private Map kpiInstanceAssociation = new HashMap();
	private Map modelIDAssociation = new HashMap();
	private Map modelAssociation = new HashMap();
	private Map modelInstanceIDAssociation = new HashMap();
	private Map modelInstanceAssociation = new HashMap();
	private Map thresholdIDAssociation = new HashMap();
	private Map thresholdAssociation = new HashMap();
	private Map thresholdValueIDAssociation = new HashMap();
	private Map thresholdValueAssociation = new HashMap();
	private Map resourcesIDAssociation = new HashMap();
	private Map resourcesAssociation = new HashMap();
	private Map modelResourcesIDAssociation = new HashMap();
	private Map modelResourcesAssociation = new HashMap();
	private Map periodicityIDAssociation = new HashMap();
	private Map periodicityAssociation = new HashMap();
	private Map kpiInstPeriodIDAssociation = new HashMap();
	private Map kpiInstPeriodAssociation = new HashMap();
	private Map alarmIDAssociation = new HashMap();
	private Map alarmAssociation = new HashMap();
	private Map alarmContactIDAssociation = new HashMap();
	private Map alarmContactAssociation = new HashMap();
	private Map objMetadataIDAssociation = new HashMap();
	private Map objMetadataAssociation = new HashMap();
	private Map objMetacontentsIDAssociation = new HashMap();
	private Map objMetacontentsAssociation = new HashMap();
	private Map subObjectIDAssociation = new HashMap();
	private Map kpiRelAssociation = new HashMap();
	private Map udpAssociation = new HashMap();
	private Map udpValueAssociation = new HashMap();
	private Map ouAssociation = new HashMap();
	private Map ouHierarchiesAssociation = new HashMap();
	private Map ouNodesAssociation = new TreeMap();
	private Map ouGrantAssociation = new HashMap();
	private Map ouGrantNodesAssociation = new HashMap();
	private Map authorizationsIDAssociation = new HashMap();
	private Map authorizationsAssociation = new HashMap();

	
	/**
	 * Checks if the metadata association is empty.
	 * 
	 * @return boolean, true is associations are empty false otherwise
	 */
	public boolean isEmpty() {
		if(!parameterAssociation.keySet().isEmpty())
			return false;
		if(!roleAssociation.keySet().isEmpty())
			return false;
		if(!biobjAssociation.keySet().isEmpty())
			return false;
		if(!lovAssociation.keySet().isEmpty())
			return false;
		if(!functAssociation.keySet().isEmpty())
			return false;
		if(!engineAssociation.keySet().isEmpty())
			return false;
		if(!checkAssociation.keySet().isEmpty())
			return false;
		if(!paruseAssociation.keySet().isEmpty())
			return false;
		if(!kpiAssociation.keySet().isEmpty())
			return false;
		if(!kpiInstanceAssociation.keySet().isEmpty())
			return false;
		if(!modelAssociation.keySet().isEmpty())
			return false;
		if(!modelInstanceAssociation.keySet().isEmpty())
			return false;
		if(!thresholdAssociation.keySet().isEmpty())
			return false;
		if(!thresholdValueAssociation.keySet().isEmpty())
			return false;
		if(!resourcesIDAssociation.keySet().isEmpty())
			return false;
		if(!resourcesAssociation.keySet().isEmpty())
			return false;
		if(!modelResourcesIDAssociation.keySet().isEmpty())
			return false;
		if(!modelResourcesAssociation.keySet().isEmpty())
			return false;
		if(!periodicityIDAssociation.keySet().isEmpty())
			return false;
		if(!periodicityAssociation.keySet().isEmpty())
			return false;
		if(!kpiInstPeriodIDAssociation.keySet().isEmpty())
			return false;
		if(!kpiInstPeriodAssociation.keySet().isEmpty())
			return false;		
		if(!alarmIDAssociation.keySet().isEmpty())
			return false;
		if(!alarmAssociation.keySet().isEmpty())
			return false;
		if(!alarmContactIDAssociation.keySet().isEmpty())
			return false;
		if(!alarmContactAssociation.keySet().isEmpty())
			return false;
		if(!objMetadataIDAssociation.keySet().isEmpty())
			return false;	
		if(!objMetadataAssociation.keySet().isEmpty())
			return false;	
		if(!objMetacontentsIDAssociation.keySet().isEmpty())
			return false;	
		if(!objMetacontentsAssociation.keySet().isEmpty())
			return false;	
		if(!subObjectIDAssociation.keySet().isEmpty())
			return false;	
		if(!kpiRelAssociation.keySet().isEmpty())
			return false;
		if(!udpValueAssociation.keySet().isEmpty())
			return false;
		if(!udpAssociation.keySet().isEmpty())
			return false;
		if(!ouAssociation.keySet().isEmpty())
			return false;
		if(!ouHierarchiesAssociation.keySet().isEmpty())
			return false;
		if(!ouNodesAssociation.keySet().isEmpty())
			return false;
		if(!ouGrantAssociation.keySet().isEmpty())
			return false;
		if(!ouGrantNodesAssociation.keySet().isEmpty())
			return false;
		if(!metaModelIDAssociation.keySet().isEmpty())
			return false;
		if(!artifactIDAssociation.keySet().isEmpty())
			return false;
		if(!authorizationsAssociation.keySet().isEmpty())
			return false;
		if(!authorizationsIDAssociation.keySet().isEmpty())
			return false;
		
		return true;
	}

	/**
	 * Clears all the inforamtion about associations.
	 */
	public void clear() {
		parameterIDAssociation = new HashMap();
		parameterAssociation = new HashMap();
		roleIDAssociation = new HashMap();
		roleAssociation = new HashMap();
		biobjIDAssociation = new HashMap();
		biobjAssociation = new HashMap();
		lovIDAssociation = new HashMap();
		lovAssociation = new HashMap();
		functIDAssociation = new HashMap();
		functAssociation = new HashMap();
		engineIDAssociation = new HashMap();
		engineAssociation = new HashMap();
		checkIDAssociation = new HashMap();
		checkAssociation = new HashMap();
		paruseIDAssociation = new HashMap();
		paruseAssociation = new HashMap();
		datasourcesIDAssociation = new HashMap();
		datasetsIDAssociation = new HashMap();
		mapsIDAssociation = new HashMap();
		featuresIDAssociation = new HashMap();
		kpiIDAssociation = new HashMap();
		kpiAssociation = new HashMap();
		kpiInstanceIDAssociation = new HashMap();
		kpiInstanceAssociation = new HashMap();
		modelIDAssociation = new HashMap();
		modelAssociation = new HashMap();
		modelInstanceIDAssociation = new HashMap();
		modelInstanceAssociation = new HashMap();
		thresholdIDAssociation = new HashMap();
		thresholdAssociation = new HashMap();
		thresholdValueIDAssociation = new HashMap();
		thresholdValueAssociation = new HashMap();
		resourcesIDAssociation = new HashMap();
		resourcesAssociation = new HashMap();
		modelResourcesIDAssociation = new HashMap();
		modelResourcesAssociation = new HashMap();
		periodicityIDAssociation = new HashMap();
		periodicityAssociation = new HashMap();
		kpiInstPeriodIDAssociation = new HashMap();
		kpiInstPeriodAssociation = new HashMap();
		alarmIDAssociation = new HashMap();
		alarmAssociation = new HashMap();
		alarmContactIDAssociation = new HashMap();
		alarmContactAssociation = new HashMap();
		objMetadataIDAssociation = new HashMap();
		objMetadataAssociation = new HashMap();
		objMetacontentsIDAssociation = new HashMap();
		objMetacontentsAssociation = new HashMap();
		subObjectIDAssociation = new HashMap();
		kpiRelAssociation = new HashMap ();
		udpValueAssociation = new HashMap ();
		udpAssociation = new HashMap();
		ouAssociation = new HashMap ();
		ouHierarchiesAssociation = new HashMap();
		ouNodesAssociation = new TreeMap();
		ouGrantAssociation = new HashMap();
		ouGrantNodesAssociation = new HashMap();
		metaModelIDAssociation = new HashMap();
		artifactIDAssociation = new HashMap();	
		authorizationsAssociation = new HashMap();	
		authorizationsIDAssociation = new HashMap();	
		
	}


	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isParameterAssEmpty(){
		return parameterAssociation.keySet().isEmpty();
	}

	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isRoleAssEmpty(){
		return roleAssociation.keySet().isEmpty();
	}

	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isBIObjAssEmpty(){
		return biobjAssociation.keySet().isEmpty();
	}

	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isLovAssEmpty(){
		return lovAssociation.keySet().isEmpty();
	}

	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isFunctAssEmpty(){
		return functAssociation.keySet().isEmpty();
	}

	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isEngineAssEmpty(){
		return engineAssociation.keySet().isEmpty();
	}

	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isCheckAssEmpty(){
		return checkAssociation.keySet().isEmpty();
	}

	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isParuseAssEmpty(){
		return paruseAssociation.keySet().isEmpty();
	}


	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isKpiEmpty(){
		return kpiAssociation.keySet().isEmpty();
	}

	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isModelEmpty(){
		return modelAssociation.keySet().isEmpty();
	}

	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isModelInstanceEmpty(){
		return modelInstanceAssociation.keySet().isEmpty();
	}


	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isThresholdInstanceEmpty(){
		return thresholdAssociation.keySet().isEmpty();
	}

	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isThresholdValueInstanceEmpty(){
		return thresholdValueAssociation.keySet().isEmpty();
	}

	/**
	 * Checks if Associations for the specific object are empty.
	 * 
	 * @return boolean, true if associations are empty, false otherwise
	 */
	public boolean isKpiInstanceEmpty(){
		return kpiInstanceAssociation.keySet().isEmpty();
	}



	/**
	 * Gets the Map of associations between current and exported parameter ids.
	 * 
	 * @return Map of assocaitions
	 */
	public Map getParameterIDAssociation() {
		return parameterIDAssociation;
	}

	/**
	 * Gets the Map of associations between current and exported parameters.
	 * 
	 * @return Map of associations
	 */
	public Map getParameterAssociation() {
		return parameterAssociation;
	}

	/**
	 * Inserts a couple of parameters into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleParameter(SbiParameters exp, SbiParameters curr) {
		//parameterIDAssociation.put(exp.getParId().toString(), curr.getParId().toString());
		parameterAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of parameter ids into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleParameter(Integer exp, Integer curr) {
		parameterIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported role ids.
	 * 
	 * @return Map of associations
	 */
	public Map getRoleIDAssociation() {
		return roleIDAssociation;
	}

	/**
	 * Gets the Map of associations between current and exported roles.
	 * 
	 * @return Map of associations
	 */
	public Map getRoleAssociation() {
		return roleAssociation;
	}

	/**
	 * Inserts a couple of roles into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleRole(SbiExtRoles exp, SbiExtRoles curr) {
		//roleIDAssociation.put(exp.getExtRoleId().toString(), curr.getExtRoleId().toString());
		roleAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of role ids into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleRole(Integer exp, Integer curr) {
		roleIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported biobject ids.
	 * 
	 * @return Map of associations
	 */
	public Map getBIobjIDAssociation() {
		return biobjIDAssociation;
	}

	/**
	 * Gets the Map of associations between current and exported biobjects.
	 * 
	 * @return Map of associations
	 */
	public Map getBIObjAssociation() {
		return biobjAssociation;
	}

	/**
	 * Inserts a couple of biobjects into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleBIObj(SbiObjects exp, SbiObjects curr) {
		//biobjIDAssociation.put(exp.getBiobjId().toString(), curr.getBiobjId().toString());
		biobjAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of biobject ids into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleBIObj(Integer exp, Integer curr) {
		biobjIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported lovs ids.
	 * 
	 * @return Map of associations
	 */
	public Map getLovIDAssociation() {
		return lovIDAssociation;
	}

	/**
	 * Gets the Map of associations between current and exported lovs.
	 * 
	 * @return Map of associations
	 */
	public Map getLovAssociation() {
		return lovAssociation;
	}

	/**
	 * Inserts a couple of lovs into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleLov(SbiLov exp, SbiLov curr) {
		//lovIDAssociation.put(exp.getLovId().toString(), curr.getLovId().toString());
		lovAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of lov ids into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleLov(Integer exp, Integer curr) {
		lovIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported functionality ids.
	 * 
	 * @return Map of associations
	 */
	public Map getFunctIDAssociation() {
		return functIDAssociation;
	}

	/**
	 * Gets the Map of associations between current and exported functionalities.
	 * 
	 * @return Map of associations
	 */
	public Map getFunctAssociation() {
		return functAssociation;
	}

	/**
	 * Inserts a couple of functionalities into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleFunct(SbiFunctions exp, SbiFunctions curr) {
		functAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of functionality ids into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleFunct(Integer exp, Integer curr) {
		functIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported engine ids.
	 * 
	 * @return Map of associations
	 */
	public Map getEngineIDAssociation() {
		return engineIDAssociation;
	}

	/**
	 * Gets the Map of associations between current and exported engines.
	 * 
	 * @return Map of associations
	 */
	public Map getEngineAssociation() {
		return engineAssociation;
	}

	/**
	 * Inserts a couple of engines into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleEngine(SbiEngines exp, SbiEngines curr) {
		//engineIDAssociation.put(exp.getEngineId().toString(), curr.getEngineId().toString());
		engineAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of engine ids into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleEngine(Integer exp, Integer curr) {
		engineIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported check ids.
	 * 
	 * @return Map of associations
	 */
	public Map getCheckIDAssociation() {
		return checkIDAssociation;
	}

	/**
	 * Gets the Map of associations between current and exported checks.
	 * 
	 * @return Map of associations
	 */
	public Map getCheckAssociation() {
		return checkAssociation;
	}

	/**
	 * Inserts a couple of checks into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleCheck(SbiChecks exp, SbiChecks curr) {
		//checkIDAssociation.put(exp.getCheckId().toString(), curr.getCheckId().toString());
		checkAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of check ids into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleCheck(Integer exp, Integer curr) {
		checkIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported parameter use ids.
	 * 
	 * @return Map of associations
	 */
	public Map getParuseIDAssociation() {
		return paruseIDAssociation;
	}

	/**
	 * Gets the Map of associations between current and exported parameter uses.
	 * 
	 * @return Map of associations
	 */
	public Map getParuseAssociation() {
		return paruseAssociation;
	}

	/**
	 * Inserts a couple of parameter uses into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleParuse(SbiParuse exp, SbiParuse curr) {
		//paruseIDAssociation.put(exp.getUseId().toString(), curr.getUseId().toString());
		paruseAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of parameter use ids into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleParuse(Integer exp, Integer curr) {
		paruseIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported domain id.
	 * 
	 * @return Map of associations
	 */
	public Map getDomainIDAssociation() {
		return domainIDAssociation;
	}

	/**
	 * Gets the Map of associations between current and exported domains.
	 * 
	 * @return Map of associations
	 */
	public Map getDomainAssociation() {
		return domainAssociation;
	}

	/**
	 * Inserts a couple of domains into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleDomain(SbiDomains exp, SbiDomains curr) {
		domainAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of domain id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleDomain(Integer exp, Integer curr) {
		domainIDAssociation.put(exp, curr);
	}


	/**
	 * Gets the Map of associations between current and exported objpar id.
	 * 
	 * @return Map of associations
	 */
	public Map getObjparIDAssociation() {
		return objparIDAssociation;
	}

	/**
	 * Gets the Map of associations between current and exported objpars.
	 * 
	 * @return Map of associations
	 */
	public Map getObjparAssociation() {
		return objparAssociation;
	}

	/**
	 * Inserts a couple of objpar into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleObjpar(SbiObjPar exp, SbiObjPar curr) {
		objparAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of objpar id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleObjpar(Integer exp, Integer curr) {
		objparIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported datasources.
	 * 
	 * @return Map of associations
	 */
	public Map getDataSourceIDAssociation() {
		return datasourcesIDAssociation;
	}

	/**
	 * Inserts a couple of datasource id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleDataSources(Integer exp, Integer curr) {
		datasourcesIDAssociation.put(exp, curr);
	}


	/**
	 * Gets the Map of associations between current and exported datasets.
	 * 
	 * @return Map of associations
	 */
	public Map getDataSetIDAssociation() {
		return datasetsIDAssociation;
	}

	/**
	 * Inserts a couple of dataset id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleDataSets(Integer exp, Integer curr) {
		datasetsIDAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of metaMdel id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	
	public void insertCoupleMetaModel(Integer exp, Integer curr) {
		metaModelIDAssociation.put(exp, curr);
	}
	
	/**
	 * Gets the Map of associations between current and exported metamodel.
	 * 
	 * @return Map of associations
	 */
	public Map getMetaModelIDAssociation() {
		return metaModelIDAssociation;
	}

	/**
	 * Inserts a couple of artifacts id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	
	public void insertCoupleArtifact(Integer exp, Integer curr) {
		artifactIDAssociation.put(exp, curr);
	}
	
	/**
	 * Gets the Map of associations between current and exported artifact.
	 * 
	 * @return Map of associations
	 */
	public Map getArtifactIDAssociation() {
		return artifactIDAssociation;
	}
	
	
	/**
	 * Gets the Map of associations between current and exported maps.
	 * 
	 * @return Map of associations
	 */
	public Map getMapIDAssociation() {
		return mapsIDAssociation;
	}

	/**
	 * Inserts a couple of maps id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleMaps(Integer exp, Integer curr) {
		mapsIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported features.
	 * 
	 * @return Map of associations
	 */
	public Map getFeaturesIDAssociation() {
		return featuresIDAssociation;
	}

	/**
	 * Inserts a couple of features id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleFeatures(Integer exp, Integer curr) {
		featuresIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported kpi.
	 * 
	 * @return Map of kpi
	 */
	public Map getKpiIDAssociation() {
		return kpiIDAssociation;
	}

	/**
	 * Inserts a couple of kpi id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleKpi(Integer exp, Integer curr) {
		kpiIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported kpi.
	 * 
	 * @return Map of kpi
	 */
	public Map getKpiAssociation() {
		return kpiAssociation;
	}

	/**
	 * Inserts a couple of kpi id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleKpi(SbiKpi exp, SbiKpi curr) {
		kpiIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported kpi instance.
	 * 
	 * @return Map of kpi
	 */
	public Map getKpiInstanceIDAssociation() {
		return kpiInstanceIDAssociation;
	}

	/**
	 * Inserts a couple of kpi instanceid into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleKpiInstance(Integer exp, Integer curr) {
		kpiInstanceIDAssociation.put(exp, curr);
	}


	/**
	 * Gets the Map of associations between current and exported kpi instance.
	 * 
	 * @return Map of kpi
	 */
	public Map getKpiInstanceAssociation() {
		return kpiInstanceAssociation;
	}

	/**
	 * Inserts a couple of kpi instanceid into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleKpiInstance(SbiKpiInstance exp, SbiKpiInstance curr) {
		kpiInstanceAssociation.put(exp, curr);
	}


	/**
	 * Gets the Map of associations between current and exported model.
	 * 
	 * @return Map of models
	 */
	public Map getModelIDAssociation() {
		return modelIDAssociation;
	}

	/**
	 * Inserts a couple of model id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleModel(Integer exp, Integer curr) {
		modelIDAssociation.put(exp, curr);
	}


	/**
	 * Gets the Map of associations between current and exported model.
	 * 
	 * @return Map of models
	 */
	public Map getModelAssociation() {
		return modelAssociation;
	}

	/**
	 * Inserts a couple of model id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleModel(SbiKpiModel exp, SbiKpiModel curr) {
		modelAssociation.put(exp, curr);
	}



	/**
	 * Gets the Map of associations between current and exported model instance.
	 * 
	 * @return Map of kpi
	 */
	public Map getModelInstanceIDAssociation() {
		return modelInstanceIDAssociation;
	}

	/**
	 * Inserts a couple of model instanceid into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleModelInstance(Integer exp, Integer curr) {
		modelInstanceIDAssociation.put(exp, curr);
	}




	/**
	 * Gets the Map of associations between current and exported model instance ID.
	 * 
	 * @return Map of kpi
	 */
	public Map getModelInstanceAssociation() {
		return modelInstanceAssociation;
	}

	/**
	 * Inserts a couple of model instance into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleModelInstance(SbiKpiModelInst exp, SbiKpiModelInst curr) {
		modelInstanceAssociation.put(exp, curr);
	}










	/**
	 * Gets the Map of associations between current and exported threshold.
	 * 
	 * @return Map of threshold
	 */
	public Map getTresholdIDAssociation() {
		return thresholdIDAssociation;
	}

	/**
	 * Inserts a couple of threshold into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleThreshold(Integer exp, Integer curr) {
		thresholdIDAssociation.put(exp, curr);
	}


	/**
	 * Gets the Map of associations between current and exported threshold.
	 * 
	 * @return Map of threshold
	 */
	public Map getTresholdAssociation() {
		return thresholdAssociation;
	}

	/**
	 * Inserts a couple of threshold into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleThreshold(SbiThreshold exp, SbiThreshold curr) {
		thresholdAssociation.put(exp, curr);
	}




	/**
	 * Gets the Map of associations between current and exported thresholdValue.
	 * 
	 * @return Map of threshold
	 */
	public Map getTresholdValueIDAssociation() {
		return thresholdValueIDAssociation;
	}

	/**
	 * Inserts a couple of threshold value into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleThresholdValue(Integer exp, Integer curr) {
		thresholdValueIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported thresholdValue.
	 * 
	 * @return Map of threshold
	 */
	public Map getTresholdValueAssociation() {
		return thresholdValueAssociation;
	}

	/**
	 * Inserts a couple of threshold value into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleThresholdValue(SbiThresholdValue exp, SbiThresholdValue curr) {
		thresholdValueAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported Resources.
	 * 
	 * @return Map of Resources
	 */
	public Map getResourcesIDAssociation() {
		return resourcesIDAssociation;
	}

	/**
	 * Inserts a couple of resources value into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleResources(Integer exp, Integer curr) {
		resourcesIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported Resources.
	 * 
	 * @return Map of Resources
	 */
	public Map getResourcesAssociation() {
		return resourcesAssociation;
	}

	/**
	 * Inserts a couple of resources into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleResources(SbiResources exp, SbiResources curr) {
		resourcesAssociation.put(exp, curr);	
	}



	/**
	 * Gets the Map of associations between current and exported ModelResources.
	 * 
	 * @return Map of ModelResources
	 */
	public Map getModelResourcesIDAssociation() {
		return modelResourcesIDAssociation;
	}

	/**
	 * Inserts a couple of model resources value into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleModelResources(Integer exp, Integer curr) {
		modelResourcesIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported Model Resources.
	 * 
	 * @return Map of Model Resources
	 */
	public Map getModelResourcesAssociation() {
		return modelResourcesAssociation;
	}

	/**
	 * Inserts a couple of model Resources into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleModelResources(SbiKpiModelResources exp, SbiKpiModelResources curr) {
		modelResourcesAssociation.put(exp, curr);	
	}




	/**
	 * Gets the Map of associations between current and exported Periodicity.
	 * 
	 * @return Map of Periodicity
	 */
	public Map getPeriodicityIDAssociation() {
		return periodicityIDAssociation;
	}

	/**
	 * Inserts a couple of periodicity value into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCouplePeriodicity(Integer exp, Integer curr) {
		periodicityIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported Periodicity.
	 * 
	 * @return Map of Periodicity
	 */
	public Map getPeriodicityAssociation() {
		return periodicityAssociation;
	}

	/**
	 * Inserts a couple of Periodicity into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCouplePeriodicity(SbiKpiPeriodicity exp, SbiKpiPeriodicity curr) {
		periodicityAssociation.put(exp, curr);	
	}




	/**
	 * Gets the Map of associations between current and exported kpiInstPeriod.
	 * 
	 * @return Map of Periodicity
	 */
	public Map getKpiInstPeriodIDAssociation() {
		return kpiInstPeriodIDAssociation;
	}

	/**
	 * Inserts a couple of kpiInstPeriod value into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleKpiInstPeriod(Integer exp, Integer curr) {
		kpiInstPeriodIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported kpiInstPeriod.
	 * 
	 * @return Map of kpiInstPeriod
	 */
	public Map getKpiInstPeriodAssociation() {
		return kpiInstPeriodAssociation;
	}

	/**
	 * Inserts a couple of kpiInstPeriod into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleKpiInstPeriod(SbiKpiInstPeriod exp, SbiKpiInstPeriod curr) {
		kpiInstPeriodAssociation.put(exp, curr);	
	}



	/**
	 * Gets the Map of associations between current and exported Alarms
	 * 
	 * @return Map of Alarm
	 */
	public Map getAlarmIDAssociation() {
		return alarmIDAssociation;
	}

	/**
	 * Inserts a couple of Alarm value into the associations
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleAlarm(Integer exp, Integer curr) {
		alarmIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported Alarm.
	 * 
	 * @return Map of Alarm
	 */
	public Map getAlarmAssociation() {
		return alarmAssociation;
	}

	/**
	 * Inserts a couple of alarm into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleAlarm(SbiAlarm exp, SbiAlarm curr) {
		alarmAssociation.put(exp, curr);	
	}

	/**
	 * Gets the Map of associations between current and exported AlarmsContact
	 * 
	 * @return Map of AlarmContact
	 */
	public Map getAlarmContactIDAssociation() {
		return alarmContactIDAssociation;
	}

	/**
	 * Inserts a couple of Alarm Contact value into the associations
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleAlarmContact(Integer exp, Integer curr) {
		alarmContactIDAssociation.put(exp, curr);
	}

	/**
	 * Gets the Map of associations between current and exported AlarmContact.
	 * 
	 * @return Map of AlarmContact
	 */
	public Map getAlarmContactAssociation() {
		return alarmContactAssociation;
	}

	/**
	 * Inserts a couple of alarmContact into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleAlarmContact(SbiAlarmContact exp, SbiAlarmContact curr) {
		alarmContactAssociation.put(exp, curr);	
	}


	/**
	 * Gets the Map of associations between current and exported ObjMetadata.
	 * 
	 * @return Map of ObjMetadata
	 */
	public Map getObjMetadataAssociation() {
		return objMetadataAssociation;
	}

	/**
	 * Inserts a couple of ObjMetadata into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleObjMetadataAssociation(SbiObjMetadata exp, SbiObjMetadata curr) {
		objMetadataAssociation.put(exp, curr);	
	}

	
	/**
	 * Gets the Map of associations between current and exported ObjMetadata ID.
	 * 
	 * @return Map of ObjMetadata
	 */
	public Map getObjMetadataIDAssociation() {
		return objMetadataIDAssociation;
	}

	/**
	 * Inserts a couple of ObjMetadata ID into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleObjMetadataIDAssociation(Integer exp, Integer curr) {
		objMetadataIDAssociation.put(exp, curr);	
	}


	/**
	 * Gets the Map of associations between current and exported ObjMetacontents.
	 * 
	 * @return Map of ObjMetacontents
	 */
	public Map getObjMetacontentsAssociation() {
		return objMetacontentsAssociation;
	}

	/**
	 * Inserts a couple of ObjMetacontents into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleObjMetacontentsAssociation(SbiObjMetacontents exp, SbiObjMetacontents curr) {
		objMetacontentsAssociation.put(exp, curr);	
	}

	
	/**
	 * Gets the Map of associations between current and exported ObjMetacontents ID.
	 * 
	 * @return Map of ObjMetacontents
	 */
	public Map getObjMetacontentsIDAssociation() {
		return objMetacontentsIDAssociation;
	}

	/**
	 * Inserts a couple of ObjMetacontents ID into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleObjMetacontentsIDAssociation(Integer exp, Integer curr) {
		objMetacontentsIDAssociation.put(exp, curr);	
	}

	
	/**
	 * Gets the Map of associations between current and exported SubObjects ID.
	 * 
	 * @return Map of SubObjects
	 */
	public Map getObjSubObjectIDAssociation() {
		return subObjectIDAssociation;
	}

	/**
	 * Inserts a couple of SubObjects ID into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleSubObjectsIDAssociation(Integer exp, Integer curr) {
		subObjectIDAssociation.put(exp, curr);	
	}
	/**
	 * Gets the Map of associations between current and exported kpi relation ID.
	 * 
	 * @return Map of SubObjects
	 */
	public Map getKpiRelAssociation() {
		return kpiRelAssociation;
	}

	/**
	 * Inserts a couple of KpiRel ID into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleKpiRelAssociation(Integer exp, Integer curr) {
		kpiRelAssociation.put(exp, curr);	
	}
	/**
	 * Gets the Map of associations between current and exported udp.
	 * 
	 * @return Map of udp
	 */
	public Map getUdpAssociation() {
		return udpAssociation;
	}
	
	/**
	 * Inserts a couple of Udp into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleUdpAssociation(Integer exp, Integer curr) {
		udpAssociation.put(exp, curr);	
	}
	/**
	 * Gets the Map of associations between current and exported udp value.
	 * 
	 * @return Map of udp
	 */
	public Map getUdpValueAssociation() {
		return udpValueAssociation;
	}
	/**
	 * Inserts a couple of Udp value into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleUdpValueAssociation(Integer exp, Integer curr) {
		udpValueAssociation.put(exp, curr);	
	}
	/**
	 * Gets the Map of associations between current and exported ou.
	 * 
	 * @return Map of ou
	 */
	public Map getOuAssociation() {
		return ouAssociation;
	}
	/**
	 * Inserts a couple of ou id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleIdOuAssociation(Integer exp, Integer curr) {
		ouAssociation.put(exp, curr);	
	}
	/**
	 * Gets the Map of associations between current and exported ou  hierarchy.
	 * 
	 * @return Map of hierarchies
	 */
	public Map getOuHierarchiesAssociation() {
		return ouHierarchiesAssociation;
	}
	/**
	 * Inserts a couple of ou hierarchy id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleIdOuHierarchyAssociation(Integer exp, Integer curr) {
		ouHierarchiesAssociation.put(exp, curr);	
	}
	/**
	 * Gets the Map of associations between current and exported ou node.
	 * 
	 * @return Map of ou nodes
	 */
	public Map getOuNodeAssociation() {
		return ouNodesAssociation;
	}
	/**
	 * Inserts a couple of ou nodes id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleIdOuNodeAssociation(Integer exp, Integer curr) {
		ouNodesAssociation.put(exp, curr);	
	}
	/**
	 * Gets the Map of associations between current and exported ou grant.
	 * 
	 * @return Map of ou grants
	 */
	public Map getOuGrantAssociation() {
		return ouGrantAssociation;
	}
	/**
	 * Inserts a couple of ou Grant id into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleIdOuGrantAssociation(Integer exp, Integer curr) {
		ouGrantAssociation.put(exp, curr);	
	}
	/**
	 * Gets the Map of associations between current and exported ou grant node.
	 * 
	 * @return Map of ou grant nodes
	 */
	public Map getOuGrantNodesAssociation() {
		return ouGrantNodesAssociation;
	}
	/**
	 * Inserts a couple of ou Grant node id objects into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleIdOuGrantNodesAssociation(SbiOrgUnitGrantNodesId exp, SbiOrgUnitGrantNodesId curr) {
		ouGrantNodesAssociation.put(exp, curr);	
	}
	
	
	

	/**
	 * Gets the Map of associations between current and exported authorizations ids.
	 * 
	 * @return Map of associations
	 */
	public Map getAuthorizationsIDAssociation() {
		return authorizationsIDAssociation;
	}

	/**
	 * Gets the Map of associations between current and exported authorizations.
	 * 
	 * @return Map of associations
	 */
	public Map getAuthorizationsAssociation() {
		return authorizationsAssociation;
	}

	/**
	 * Inserts a couple of authorizations into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleAuthorizations(SbiAuthorizations exp, SbiAuthorizations curr) {
		authorizationsAssociation.put(exp, curr);
	}

	/**
	 * Inserts a couple of authorizations ids into the associations.
	 * 
	 * @param exp the exp
	 * @param curr the curr
	 */
	public void insertCoupleAuthorizations(Integer exp, Integer curr) {
		authorizationsIDAssociation.put(exp, curr);
	}
	
}
