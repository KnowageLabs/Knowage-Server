/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport;

import java.util.HashMap;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;


/**
 * Implements methods for recording the association of roles, engines, and data sources 
 * setted by the user. The association recorder can be exported into xml format 
 */
public class UserAssociationsKeeper {


	static private Logger logger = Logger.getLogger(UserAssociationsKeeper.class);

	private SourceBean associationSB = null;
	private SourceBean roleAssSB = null;
	private SourceBean engineAssSB = null;
	private SourceBean datasourceAssSB = null;

	private HashMap<Integer, Integer> dsExportedToUser=new HashMap<Integer, Integer>();
	private HashMap<String, String> dsExportedToUserLabel=new HashMap<String, String>();

	/**
	 * Defines the internal structure for recording associations.
	 */
	public UserAssociationsKeeper() {
		logger.debug("IN");
		try{
			associationSB = new SourceBean("USER_ASSOCIATIONS");
			roleAssSB = new SourceBean("ROLE_ASSOCIATIONS");
			engineAssSB = new SourceBean("ENGINE_ASSOCIATIONS");
			datasourceAssSB = new SourceBean("DATA_SOURCE_ASSOCIATIONS");
			associationSB.setAttribute(roleAssSB);
			associationSB.setAttribute(engineAssSB);
			associationSB.setAttribute(datasourceAssSB);
		} catch (Exception e) {
			logger.error("Error while creating the association SourceBean \n " , e );
		}finally{
			logger.debug("OUT");
		}
	}


	/**
	 * Records an association between an exported role and an existing one.
	 * 
	 * @param exportedRoleName the name of the exported role
	 * @param existingRolename the name of the existing role
	 */
	public void recordRoleAssociation(String exportedRoleName, String existingRolename) {
		logger.debug("IN");
		if( (associationSB==null) || (roleAssSB==null) ) {
			logger.warn("Cannot record the association between exported role "+exportedRoleName+" " +
					"and the role " + existingRolename + ", the association SourceBean is null");
			return;
		}
		try{
			SourceBean roleSB = (SourceBean) roleAssSB.getFilteredSourceBeanAttribute("ROLE_ASSOCIATION", "exported", exportedRoleName);
			// association already recorder
			if (roleSB != null) {
				roleSB.updAttribute("associatedTo", existingRolename);
				//return;
			} else {
				// record association
				roleSB = new SourceBean("ROLE_ASSOCIATION");
				roleSB.setAttribute("exported", exportedRoleName);
				roleSB.setAttribute("associatedTo", existingRolename);
				roleAssSB.setAttribute(roleSB);
			}
		} catch (Exception e) {
			logger.error( "Error while recording the association between exported role "+exportedRoleName+" " +
					"and the role " + existingRolename + " \n " , e);
		}finally{
			logger.debug("OUT");
		}
	}


	/**
	 * Records an association between an exported engine and an existing one.
	 * 
	 * @param exportedEngineLabel the label of the exported engine
	 * @param existingEngineLabel the label of the existing engine
	 */
	public void recordEngineAssociation(String exportedEngineLabel, String existingEngineLabel) {
		logger.debug("IN");
		if( (associationSB==null) || (engineAssSB==null) ) {
			logger.warn("Cannot record the association between exported engine "+exportedEngineLabel+" " +
					"and the engine " + existingEngineLabel + ", the association SourceBean is null");
			return;
		}
		try{
			SourceBean engineSB = (SourceBean) engineAssSB.getFilteredSourceBeanAttribute("ENGINE_ASSOCIATION", "exported", exportedEngineLabel);
			// association already recorder
			if(engineSB != null) {
				engineSB.updAttribute("associatedTo", existingEngineLabel);
				//return;
			} else {
				// record association
				engineSB = new SourceBean("ENGINE_ASSOCIATION");
				engineSB.setAttribute("exported", exportedEngineLabel);
				engineSB.setAttribute("associatedTo", existingEngineLabel);
				engineAssSB.setAttribute(engineSB);
			}
		} catch (Exception e) {
			logger.error("Error while recording the association between exported engine "+exportedEngineLabel+" " +
					"and the engine " + existingEngineLabel + " \n " , e);
		}finally{
			logger.debug("OUT");
		}
	}


	public void recordDataSourceAssociation(Integer idExport, Integer idAssociated) {
		logger.debug("IN");
		if(dsExportedToUser==null) dsExportedToUser=new HashMap<Integer, Integer>();
		dsExportedToUser.put(idExport, idAssociated);
		logger.debug("OUT");
	}


	public boolean isDataSourceAssociated(Integer idExport) {
		logger.debug("IN");
		boolean toReturn=false;
		if(dsExportedToUser!=null){
			if(dsExportedToUser.get(idExport)!=null){
				toReturn=true;
			}
			else toReturn=false;
		}
		logger.debug("OUT");
		return toReturn;
	}



	/**
	 * Records an association between an exported data source and an existing one.
	 * 
	 * @param exportedDataSourceName the name of the exported data source
	 * @param existingDataSourceName the name of the existing data source
	 */
	public void recordDataSourceAssociation(String exportedDataSourceName, String existingDataSourceName) {
		logger.debug("IN");
		if( (associationSB==null) || (datasourceAssSB==null) ) {
			logger.warn("Cannot record the association between exported  "+exportedDataSourceName+" " +
					"and the data source " + existingDataSourceName + ", the association SourceBean is null");
			return;
		}
		try{
			SourceBean dsSB = (SourceBean) datasourceAssSB.getFilteredSourceBeanAttribute("DATA_SOURCE_ASSOCIATION", "exported", exportedDataSourceName);
			// association already recorder
			if(dsSB != null) {
				dsSB.updAttribute("associatedTo", existingDataSourceName);
				//return;
			} else {
				// record association
				dsSB = new SourceBean("DATA_SOURCE_ASSOCIATION");
				dsSB.setAttribute("exported", exportedDataSourceName);
				dsSB.setAttribute("associatedTo", existingDataSourceName);
				datasourceAssSB.setAttribute(dsSB);
				if(dsExportedToUserLabel==null)dsExportedToUserLabel=new HashMap<String, String>();
				dsExportedToUserLabel.put(exportedDataSourceName, existingDataSourceName);
			}
		} catch (Exception e) {
			logger.error("Error while recording the association between exported data source "+exportedDataSourceName+" " +
					"and the data source " + existingDataSourceName + " \n " , e);
		}finally{
			logger.debug("OUT");

		}
	}


	/**
	 * Exports the associations as xml.
	 * 
	 * @return the xml representation of the associations
	 */
	public String toXml() {
		logger.debug("IN");
		String xml = "";
		try{
			xml = associationSB.toXML(false);
		} catch (Exception e) {
			logger.error("Error while exporting the association SourceBean to xml  \n " , e);
		}
		logger.debug("OUT");
		return xml;
	}


	/**
	 * Fill the associations reading an xml string.
	 * 
	 * @param xmlStr the xml string which defines the associations
	 */
	public void fillFromXml(String xmlStr) {
		logger.debug("IN");
		try {
			SourceBean associationSBtmp = SourceBean.fromXMLString(xmlStr);
			SourceBean roleAssSBtmp = (SourceBean)associationSBtmp.getAttribute("ROLE_ASSOCIATIONS");
			if(roleAssSBtmp==null) throw new Exception("Cannot recover ROLE_ASSOCIATIONS bean");
			SourceBean engineAssSBtmp = (SourceBean)associationSBtmp.getAttribute("ENGINE_ASSOCIATIONS");
			if(engineAssSBtmp==null) throw new Exception("Cannot recover ENGINE_ASSOCIATIONS bean");
			SourceBean datasourceAssSBtmp = (SourceBean)associationSBtmp.getAttribute("DATA_SOURCE_ASSOCIATIONS");
			if(datasourceAssSBtmp==null) throw new Exception("Cannot recover DATA_SOURCE_ASSOCIATIONS bean");
			associationSB = associationSBtmp;
			roleAssSB = roleAssSBtmp;
			engineAssSB = engineAssSBtmp;
			datasourceAssSB = datasourceAssSBtmp;
		} catch (Exception e) {
			LogMF.error(logger, e, "Error while loading SourceBean from xml \n {0}", new Object[] {xmlStr});
			throw new SpagoBIRuntimeException("Error while loading SourceBean from xml" , e);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Gets the associated role.
	 * 
	 * @param expRoleName the exp role name
	 * 
	 * @return the associated role
	 */
	public String getAssociatedRole(String expRoleName) {
		logger.debug("IN");
		String assRole = null;
		SourceBean assRoleSB = (SourceBean)roleAssSB.getFilteredSourceBeanAttribute("ROLE_ASSOCIATION", "exported", expRoleName);
		if(assRoleSB!=null) {
			assRole = (String)assRoleSB.getAttribute("associatedTo");
			if(assRole.trim().equals("")) {
				assRole = null;
			}
		}
		logger.debug("OUT");
		return assRole;
	}

	/**
	 * Gets the associated engine.
	 * 
	 * @param expEngineLabel the exp engine label
	 * 
	 * @return the associated engine
	 */
	public String getAssociatedEngine(String expEngineLabel) {
		logger.debug("IN");
		String assEngine = null;
		SourceBean assEngineSB = (SourceBean)engineAssSB.getFilteredSourceBeanAttribute("ENGINE_ASSOCIATION", "exported", expEngineLabel);
		if(assEngineSB!=null) {
			assEngine = (String)assEngineSB.getAttribute("associatedTo");
			if(assEngine.trim().equals("")) {
				assEngine = null;
			}
		}
		logger.debug("OUT");
		return assEngine;
	}	

	/**
	 * Gets the associated data source.
	 * 
	 * @param expDataSourceName the exp data source name
	 * 
	 * @return the associated data source
	 */
	public String getAssociatedDataSource(String expDataSourceName) {
		logger.debug("IN");
		String assDataSource = null;
		SourceBean assDataSourceSB = (SourceBean)datasourceAssSB.getFilteredSourceBeanAttribute("DATA_SOURCE_ASSOCIATION", "exported", expDataSourceName);
		if(assDataSourceSB!=null) {
			assDataSource = (String)assDataSourceSB.getAttribute("associatedTo");
			if(assDataSource.trim().equals("")) {
				assDataSource = null;
			}
		}
		logger.debug("OUT");
		return assDataSource;
	}


	public HashMap<Integer, Integer> getDsExportedToUser() {
		return dsExportedToUser;
	}


	public void setDsExportedToUser(HashMap<Integer, Integer> dsExportedToUser) {
		this.dsExportedToUser = dsExportedToUser;
	}


	public HashMap<String, String> getDsExportedToUserLabel() {
		return dsExportedToUserLabel;
	}


	public void setDsExportedToUserLabel(
			HashMap<String, String> dsExportedToUserLabel) {
		this.dsExportedToUserLabel = dsExportedToUserLabel;
	}


	public SourceBean getDatasourceAssSB() {
		return datasourceAssSB;
	}


	public void setDatasourceAssSB(SourceBean datasourceAssSB) {
		this.datasourceAssSB = datasourceAssSB;
	}

	


}
