/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.tools.importexport.bo.AssociationFile;

import java.util.List;
import java.util.Map;

public interface IImportManager {

	public static final String IMPORT_ASS_PREDEFINED_MODE = "IMPORT_ASS_PREDEFINED_MODE"; 
	public static final String IMPORT_ASS_DEFAULT_MODE = "IMPORT_ASS_DEFAULT_MODE"; 
	
	public void setUserProfile(IEngUserProfile profile);
	
	/**
	 * Prepare the environment for the import procedure. This method must be invoked first.
	 * 
	 * @param archiveName the name of the compress exported file
	 * @param archiveContent the bytes of the compress exported file
	 * @param pathImportTmpFold the path import tmp fold
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void init(String pathImportTmpFold, String archiveName, byte[] archiveContent) throws EMFUserError;
	
	/**
	 * Open session to exported and current databases. This method must be invoked before any operation.
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void openSession() throws EMFUserError;
	
	/**
	 * Close session to exported and current databases. 
	 * This method must be invoked when all needed operation are performed.
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void closeSession();
	
	/**
	 * Gets the SpagoBI version of the exported file.
	 * 
	 * @return The SpagoBI version of the exported file
	 */
	public String getExportVersion();
	
	/**
	 * Gets the current SpagobI version.
	 * 
	 * @return The current SpagoBI version
	 */
	public String getCurrentVersion();
	
	/**
	 * Imports the exported objects.
	 * 
	 * @param overwrite the overwrite
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public void importObjects(boolean overwrite) throws EMFUserError;
	
	/**
	 * Gets the list of all exported roles.
	 * 
	 * @return The list of exported roles
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getExportedRoles() throws EMFUserError;
	
	/**
	 * Gets the list of all exported engines.
	 * 
	 * @return The list of exported engines
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getExportedEngines() throws EMFUserError;
	
	/**
	 * Gets the list of exported data sources.
	 * 
	 * @return List of the exported data sources
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public List getExportedDataSources() throws EMFUserError;
	
	/**
	 * Commits all changes made on exported and current databases.
	 * 
	 * @return String, the path of the log file
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public ImportResultInfo commitAllChanges() throws EMFUserError;
	
    /**
     * Check the existance of the exported metadata into the current system metadata
     * and insert their associations into the association object MeatadataAssociation.
     * 
     * @throws EMFUserError the EMF user error
     */
	public void checkExistingMetadata() throws EMFUserError;
	
	/**
	 * Ends the import procedure.
	 */
	public void stopImport();
	
    /**
     * Gets the object which contains the association between exported metadata
     * and the current system metadata.
     * 
     * @return MetadataAssociation the object which contains the association
     * between exported metadata and the current system metadata
     */
	public MetadataAssociations getMetadataAssociation();
	
	
	/**
	 * Gets the object which contains only the association set by user between exported metadata
	 * and the current system metadata.
	 * 
	 * @return MetadataAssociation the object which contains the association, set by user,
	 * between exported metadata and the current system metadata
	 */
	public UserAssociationsKeeper getUserAssociation();
	
	
	/**
	 * Returns the AssociationFile involved in the current import operation, if any.
	 * 
	 * @return the AssociationFile involved in the current import operation, if any
	 */
	public AssociationFile getAssociationFile();
	
	
	/**
	 * Sets the AssociationFile involved in the current import operation.
	 * 
	 * @param associationFile the association file
	 */
	public void setAssociationFile(AssociationFile associationFile);
	
	/**
	 * checks if two or more exported roles are associate to the same current role.
	 * 
	 * @param roleAssociations Map of association between exported roles and
	 * roles of the portal in use
	 * 
	 * @throws EMFUserError if two ore more exported roles are associate
	 * to the same current role
	 */
	public void checkRoleReferences(Map roleAssociations) throws EMFUserError;
	
	
	
	/**
	 * Get an existing object identified by the id and the class.
	 * 
	 * @param id The Object id
	 * @param objClass The class of the object
	 * 
	 * @return The existing hibernate object
	 */		
	public Object getExistingObject(Integer id, Class objClass);
	
	
	
	/**
	 * Get an exported object identified by the id and the class.
	 * 
	 * @param id The Object id
	 * @param objClass The class of the object
	 * 
	 * @return The existing hibernate object
	 */		
	public Object getExportedObject(Integer id, Class objClass);
	

	/**
	 * Gets the import association mode.
	 * 
	 * @return the association mode
	 */
	public String getImpAssMode();

	/**
	 * Sets the import association mode.
	 * 
	 * @param impAssMode The import association mode
	 */
	public void setImpAssMode(String impAssMode);
	
	/**
	 * Tries to associate all exported roles using the user associations; returns true is some roles associations is required, false otherwise.
	 * 
	 * @return true is some roles associations is required, false otherwise
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public boolean associateAllExportedRolesByUserAssociation() throws EMFUserError;
	
	/**
	 * Tries to associate all exported engines using the user associations; returns true is some engines associations is required, false otherwise.
	 * 
	 * @return true is some engines associations is required, false otherwise
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public boolean associateAllExportedEnginesByUserAssociation() throws EMFUserError;
	
	/**
	 * Tries to associate all exported data sources using the user associations; returns true is some data sources associations is required, false otherwise.
	 * 
	 * @return true is some data sources associations is required, false otherwise
	 * 
	 * @throws EMFUserError the EMF user error
	 */
	public boolean associateAllExportedDataSourcesByUserAssociation() throws EMFUserError;
	
}
