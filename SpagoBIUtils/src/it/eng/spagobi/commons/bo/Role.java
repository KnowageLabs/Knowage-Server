/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.bo;

import java.io.Serializable;
import java.util.List;

/**
 * Defines a <code>Role</code> object.
 *
 * @author sulis
 */

public class Role implements Serializable {

	private Integer id;
	private String name = "";
	private String description = null;
	private String roleTypeCD = null;
	private String code = null;
	private Integer roleTypeID;
	private String organization = null;
	private boolean isAbleToSaveSubobjects;
	private boolean isAbleToSeeSubobjects;
	private boolean isAbleToSeeViewpoints;
	private boolean isAbleToSeeSnapshots;
	private boolean isAbleToSeeNotes;
	private boolean isAbleToSendMail;
	private boolean isAbleToSaveIntoPersonalFolder;
	private boolean isAbleToEditWorksheet;
	private boolean isAbleToSaveRememberMe;
	private boolean isAbleToSeeMetadata;
	private boolean isAbleToSaveMetadata;
	private boolean isAbleToBuildQbeQuery;
	private boolean isAbleToDoMassiveExport;
	private boolean isAbleToManageUsers;
	private boolean isAbleToSeeDocumentBrowser;
	private boolean isAbleToSeeFavourites;
	private boolean isAbleToSeeSubscriptions;
	private boolean isAbleToSeeMyData;
	private boolean isAbleToSeeToDoList;
	private boolean isAbleToCreateDocuments;
	private boolean isAbleToCreateSocialAnalysis;
	private boolean isAbleToViewSocialAnalysis;
	private boolean isAbleToHierarchiesManagement;

	private boolean isAbleToEditAllKpiComm;
	private boolean isAbleToEditMyKpiComm;
	private boolean isAbleToDeleteKpiComm;

	private boolean isAbleToEnableDatasetPersistence;

	private boolean defaultRole;

	private List<RoleMetaModelCategory> roleMetaModelCategories;

	/**
	 * Checks if is able to save subobjects.
	 *
	 * @return true, if is able to save subobjects
	 */
	public boolean isAbleToSaveSubobjects() {
		return isAbleToSaveSubobjects;
	}

	/**
	 * Sets the checks if is able to save subobjects.
	 *
	 * @param isAbleToSaveSubobjects
	 *            the new checks if is able to save subobjects
	 */
	public void setIsAbleToSaveSubobjects(boolean isAbleToSaveSubobjects) {
		this.isAbleToSaveSubobjects = isAbleToSaveSubobjects;
	}

	/**
	 * Checks if is able to see subobjects.
	 *
	 * @return true, if is able to see subobjects
	 */
	public boolean isAbleToSeeSubobjects() {
		return isAbleToSeeSubobjects;
	}

	/**
	 * Sets the checks if is able to see subobjects.
	 *
	 * @param isAbleToSeeSubobjects
	 *            the new checks if is able to see subobjects
	 */
	public void setIsAbleToSeeSubobjects(boolean isAbleToSeeSubobjects) {
		this.isAbleToSeeSubobjects = isAbleToSeeSubobjects;
	}

	/**
	 * Checks if is able to see viewpoints.
	 *
	 * @return true, if is able to see viewpoints
	 */
	public boolean isAbleToSeeViewpoints() {
		return isAbleToSeeViewpoints;
	}

	/**
	 * Sets the checks if is able to see viewpoints.
	 *
	 * @param isAbleToSeeViewpoints
	 *            the new checks if is able to see viewpoints
	 */
	public void setIsAbleToSeeViewpoints(boolean isAbleToSeeViewpoints) {
		this.isAbleToSeeViewpoints = isAbleToSeeViewpoints;
	}

	/**
	 * Checks if is able to see snapshots.
	 *
	 * @return true, if is able to see snapshots
	 */
	public boolean isAbleToSeeSnapshots() {
		return isAbleToSeeSnapshots;
	}

	/**
	 * Sets the checks if is able to see snapshots.
	 *
	 * @param isAbleToSeeSnapshots
	 *            the new checks if is able to see snapshots
	 */
	public void setIsAbleToSeeSnapshots(boolean isAbleToSeeSnapshots) {
		this.isAbleToSeeSnapshots = isAbleToSeeSnapshots;
	}

	/**
	 * Checks if is able to see notes.
	 *
	 * @return true, if is able to see notes
	 */
	public boolean isAbleToSeeNotes() {
		return isAbleToSeeNotes;
	}

	/**
	 * Sets the checks if is able to see notes.
	 *
	 * @param isAbleToSeeNotes
	 *            the new checks if is able to see notes
	 */
	public void setIsAbleToSeeNotes(boolean isAbleToSeeNotes) {
		this.isAbleToSeeNotes = isAbleToSeeNotes;
	}

	/**
	 * Checks if is able to send mail.
	 *
	 * @return true, if is able to send mail
	 */
	public boolean isAbleToSendMail() {
		return isAbleToSendMail;
	}

	/**
	 * Sets the checks if is able to send mail.
	 *
	 * @param isAbleToSendMail
	 *            the new checks if is able to send mail
	 */
	public void setIsAbleToSendMail(boolean isAbleToSendMail) {
		this.isAbleToSendMail = isAbleToSendMail;
	}

	/**
	 * Checks if is able to save into personal folder.
	 *
	 * @return true, if is able to save into personal folder
	 */
	public boolean isAbleToSaveIntoPersonalFolder() {
		return isAbleToSaveIntoPersonalFolder;
	}

	/**
	 * Sets the checks if is able to save into personal folder.
	 *
	 * @param isAbleToSaveIntoPersonalFolder
	 *            the new checks if is able to save into personal folder
	 */
	public void setIsAbleToSaveIntoPersonalFolder(boolean isAbleToSaveIntoPersonalFolder) {
		this.isAbleToSaveIntoPersonalFolder = isAbleToSaveIntoPersonalFolder;
	}

	/**
	 * Checks if is able to save remember me.
	 *
	 * @return true, if is able to save remember me
	 */
	public boolean isAbleToSaveRememberMe() {
		return isAbleToSaveRememberMe;
	}

	/**
	 * Sets the checks if is able to save remember me.
	 *
	 * @param isAbleToSaveRememberMe
	 *            the new checks if is able to save remember me
	 */
	public void setIsAbleToSaveRememberMe(boolean isAbleToSaveRememberMe) {
		this.isAbleToSaveRememberMe = isAbleToSaveRememberMe;
	}

	/**
	 * Checks if is able to see metadata.
	 *
	 * @return true, if is able to see metadata
	 */
	public boolean isAbleToSeeMetadata() {
		return isAbleToSeeMetadata;
	}

	/**
	 * Sets the checks if is able to see metadata.
	 *
	 * @param isAbleToSeeMetadata
	 *            the new checks if is able to see metadata
	 */
	public void setIsAbleToSeeMetadata(boolean isAbleToSeeMetadata) {
		this.isAbleToSeeMetadata = isAbleToSeeMetadata;
	}

	/**
	 * Checks if is able to save metadata.
	 *
	 * @return the isAbleToSaveMetadata
	 */
	public boolean isAbleToSaveMetadata() {
		return isAbleToSaveMetadata;
	}

	/**
	 * Sets the checks if is able to save metadata.
	 *
	 * @param isAbleToSaveMetadata
	 *            the new checks if is able to save metadata
	 */
	public void setIsAbleToSaveMetadata(boolean isAbleToSaveMetadata) {
		this.isAbleToSaveMetadata = isAbleToSaveMetadata;
	}

	/**
	 * Checks if role is able to build and modify QBE queries.
	 *
	 * @return true, if role is able to build and modify QBE queries
	 */
	public Boolean isAbleToBuildQbeQuery() {
		return isAbleToBuildQbeQuery;
	}

	/**
	 * Sets if role is able to build and modify QBE queries.
	 *
	 * @param isAbleToBuildQbeQuery
	 */
	public void setIsAbleToBuildQbeQuery(Boolean isAbleToBuildQbeQuery) {
		this.isAbleToBuildQbeQuery = isAbleToBuildQbeQuery;
	}

	/**
	 * Class constructor.
	 */
	public Role() {
		super();

	}

	/**
	 * Constructor.
	 *
	 * @param name
	 *            the name
	 * @param description
	 *            the description
	 */
	public Role(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	/**
	 * Gets the description.
	 *
	 * @return role description
	 */

	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the id.
	 *
	 * @return role id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the role id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the name.
	 *
	 * @return the role name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the role type cd.
	 *
	 * @return Returns the roleTypeCD.
	 */
	public String getRoleTypeCD() {
		return roleTypeCD;
	}

	/**
	 * Sets the role type cd.
	 *
	 * @param roleTypeCD
	 *            The roleTypeCD to set.
	 */
	public void setRoleTypeCD(String roleTypeCD) {
		this.roleTypeCD = roleTypeCD;
	}

	/**
	 * Gets the role type id.
	 *
	 * @return Returns the roleTypeID.
	 */
	public Integer getRoleTypeID() {
		return roleTypeID;
	}

	/**
	 * Sets the role type id.
	 *
	 * @param roleTypeID
	 *            The roleTypeID to set.
	 */
	public void setRoleTypeID(Integer roleTypeID) {
		this.roleTypeID = roleTypeID;
	}

	/**
	 * Gets the code.
	 *
	 * @return Returns the code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 *
	 * @param code
	 *            The code to set.
	 */
	public void setCode(String code) {
		this.code = code;
	}

	public boolean isDefaultRole() {
		return defaultRole;
	}

	public void setDefaultRole(boolean defaultRole) {
		this.defaultRole = defaultRole;
	}

	public boolean isAbleToDoMassiveExport() {
		return isAbleToDoMassiveExport;
	}

	public void setIsAbleToDoMassiveExport(boolean isAbleToDoMassiveExport) {
		this.isAbleToDoMassiveExport = isAbleToDoMassiveExport;
	}

	public boolean isAbleToEditWorksheet() {
		return isAbleToEditWorksheet;
	}

	public void setIsAbleToEditWorksheet(boolean isAbleToEditWorksheet) {
		this.isAbleToEditWorksheet = isAbleToEditWorksheet;
	}

	public boolean isAbleToManageUsers() {
		return isAbleToManageUsers;
	}

	public void setIsAbleToManageUsers(boolean isAbleToManageUsers) {
		this.isAbleToManageUsers = isAbleToManageUsers;
	}

	/**
	 * @return the isAbleToSeeDocumentBrowser
	 */
	public boolean isAbleToSeeDocumentBrowser() {
		return isAbleToSeeDocumentBrowser;
	}

	/**
	 * @param isAbleToSeeDocumentBrowser
	 *            the isAbleToSeeDocumentBrowser to set
	 */
	public void setIsAbleToSeeDocumentBrowser(boolean isAbleToSeeDocumentBrowser) {
		this.isAbleToSeeDocumentBrowser = isAbleToSeeDocumentBrowser;
	}

	/**
	 * @return the isAbleToSeeFavourites
	 */
	public boolean isAbleToSeeFavourites() {
		return isAbleToSeeFavourites;
	}

	/**
	 * @param isAbleToSeeFavourites
	 *            the isAbleToSeeFavourites to set
	 */
	public void setIsAbleToSeeFavourites(boolean isAbleToSeeFavourites) {
		this.isAbleToSeeFavourites = isAbleToSeeFavourites;
	}

	/**
	 * @return the isAbleToSeeSubscriptions
	 */
	public boolean isAbleToSeeSubscriptions() {
		return isAbleToSeeSubscriptions;
	}

	/**
	 * @param isAbleToSeeSubscriptions
	 *            the isAbleToSeeSubscriptions to set
	 */
	public void setIsAbleToSeeSubscriptions(boolean isAbleToSeeSubscriptions) {
		this.isAbleToSeeSubscriptions = isAbleToSeeSubscriptions;
	}

	/**
	 * @return the isAbleToSeeMyData
	 */
	public boolean isAbleToSeeMyData() {
		return isAbleToSeeMyData;
	}

	/**
	 * @param isAbleToSeeMyData
	 *            the isAbleToSeeMyData to set
	 */
	public void setIsAbleToSeeMyData(boolean isAbleToSeeMyData) {
		this.isAbleToSeeMyData = isAbleToSeeMyData;
	}

	/**
	 * @return the isAbleToSeeToDoList
	 */
	public boolean isAbleToSeeToDoList() {
		return isAbleToSeeToDoList;
	}

	/**
	 * @param isAbleToSeeToDoList
	 *            the isAbleToSeeToDoList to set
	 */
	public void setIsAbleToSeeToDoList(boolean isAbleToSeeToDoList) {
		this.isAbleToSeeToDoList = isAbleToSeeToDoList;
	}

	/**
	 * @return the isAbleToCreateDocuments
	 */
	public boolean isAbleToCreateDocuments() {
		return isAbleToCreateDocuments;
	}

	/**
	 * @param isAbleToCreateDocuments
	 *            the isAbleToCreateDocuments to set
	 */
	public void setIsAbleToCreateDocuments(boolean isAbleToCreateDocuments) {
		this.isAbleToCreateDocuments = isAbleToCreateDocuments;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * @return the roleMetaModelCategories
	 */
	public List<RoleMetaModelCategory> getRoleMetaModelCategories() {
		return roleMetaModelCategories;
	}

	/**
	 * @param roleMetaModelCategories
	 *            the roleMetaModelCategories to set
	 */
	public void setRoleMetaModelCategories(List<RoleMetaModelCategory> roleMetaModelCategories) {
		this.roleMetaModelCategories = roleMetaModelCategories;
	}

	public boolean isAbleToEditAllKpiComm() {
		return isAbleToEditAllKpiComm;
	}

	public void setAbleToEditAllKpiComm(boolean isAbleToEditAllKpiComm) {
		this.isAbleToEditAllKpiComm = isAbleToEditAllKpiComm;
	}

	public boolean isAbleToEditMyKpiComm() {
		return isAbleToEditMyKpiComm;
	}

	public void setAbleToEditMyKpiComm(boolean isAbleToEditMyKpiComm) {
		this.isAbleToEditMyKpiComm = isAbleToEditMyKpiComm;
	}

	public boolean isAbleToDeleteKpiComm() {
		return isAbleToDeleteKpiComm;
	}

	public void setAbleToDeleteKpiComm(boolean isAbleToDeleteKpiComm) {
		this.isAbleToDeleteKpiComm = isAbleToDeleteKpiComm;
	}

	public boolean isAbleToCreateSocialAnalysis() {
		return isAbleToCreateSocialAnalysis;
	}

	public void setIsAbleToCreateSocialAnalysis(boolean isAbleToCreateSocialAnalysis) {
		this.isAbleToCreateSocialAnalysis = isAbleToCreateSocialAnalysis;
	}

	public boolean isAbleToViewSocialAnalysis() {
		return isAbleToViewSocialAnalysis;
	}

	public void setIsAbleToViewSocialAnalysis(boolean isAbleToViewSocialAnalysis) {
		this.isAbleToViewSocialAnalysis = isAbleToViewSocialAnalysis;
	}

	public boolean isAbleToHierarchiesManagement() {
		return isAbleToHierarchiesManagement;
	}

	public void setIsAbleToHierarchiesManagement(boolean isAbleToHierarchiesManagement) {
		this.isAbleToHierarchiesManagement = isAbleToHierarchiesManagement;
	}

	public boolean isAbleToEnableDatasetPersistence() {
		return isAbleToEnableDatasetPersistence;
	}

	public void setIsAbleToEnableDatasetPersistence(boolean isAbleToEnableDatasetPersistence) {
		this.isAbleToEnableDatasetPersistence = isAbleToEnableDatasetPersistence;
	}

}
