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
package it.eng.spagobi.commons.bo;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

import it.eng.spagobi.services.validation.Xss;

/**
 * Defines a <code>Role</code> object.
 *
 * @author Petrovic Stefan ( o_stpetrov, Stefan.Petrovic@mht.net )
 */

public class RoleBO implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6593919640188023895L;

	@Xss
	@NotNull
	@Max(value = 11)
	private Integer id;
	@Xss
	@NotNull
	@Max(value = 100)
	private String name;
	@Xss
	@Max(value = 160)
	private String description;
	@Xss
	@NotNull
	@Max(value = 20)
	private String roleTypeCD;
	@Xss
	@Max(value = 20)
	private String code;
	@Xss
	@NotNull
	@Max(value = 11)
	private Integer roleTypeID;
	@Xss
	@Max(value = 20)
	private String organization;

	private boolean ableToSaveSubobjects;
	private boolean ableToSeeSubobjects;
	private boolean ableToSeeViewpoints;
	private boolean ableToSeeSnapshots;
	private boolean ableToSeeNotes;
	private boolean ableToSendMail;
	private boolean ableToSaveIntoPersonalFolder;
	private boolean ableToEditWorksheet;
	private boolean ableToSaveRememberMe;
	private boolean ableToSeeMetadata;
	private boolean ableToSaveMetadata;
	private boolean ableToBuildQbeQuery;
	private boolean ableToDoMassiveExport;
	private boolean ableToManageUsers;
	private boolean ableToSeeDocumentBrowser;
	private boolean ableToSeeFavourites;
	private boolean ableToSeeSubscriptions;
	private boolean ableToSeeMyData;
	private boolean ableToSeeToDoList;
	private boolean ableToCreateDocuments;
	private boolean ableToCreateSocialAnalysis;
	private boolean ableToViewSocialAnalysis;
	private boolean ableToHierarchiesManagement;

	private boolean ableToEditAllKpiComm;
	private boolean ableToEditMyKpiComm;
	private boolean ableToDeleteKpiComm;

	private boolean ableToEnableDatasetPersistence;
	private boolean ableToEnableFederatedDataset;

	private boolean ableToManageGlossaryBusiness;
	private boolean ableToManageGlossaryTechnical;

	private boolean defaultRole;

	private List<RoleMetaModelCategory> roleMetaModelCategories;

	/**
	 * Class constructor.
	 */
	public RoleBO() {
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
	public RoleBO(String name, String description) {
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

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public boolean isAbleToSaveSubobjects() {
		return ableToSaveSubobjects;
	}

	public void setAbleToSaveSubobjects(boolean ableToSaveSubobjects) {
		this.ableToSaveSubobjects = ableToSaveSubobjects;
	}

	public boolean isAbleToSeeSubobjects() {
		return ableToSeeSubobjects;
	}

	public void setAbleToSeeSubobjects(boolean ableToSeeSubobjects) {
		this.ableToSeeSubobjects = ableToSeeSubobjects;
	}

	public boolean isAbleToSeeViewpoints() {
		return ableToSeeViewpoints;
	}

	public void setAbleToSeeViewpoints(boolean ableToSeeViewpoints) {
		this.ableToSeeViewpoints = ableToSeeViewpoints;
	}

	public boolean isAbleToSeeSnapshots() {
		return ableToSeeSnapshots;
	}

	public void setAbleToSeeSnapshots(boolean ableToSeeSnapshots) {
		this.ableToSeeSnapshots = ableToSeeSnapshots;
	}

	public boolean isAbleToSeeNotes() {
		return ableToSeeNotes;
	}

	public void setAbleToSeeNotes(boolean ableToSeeNotes) {
		this.ableToSeeNotes = ableToSeeNotes;
	}

	public boolean isAbleToSendMail() {
		return ableToSendMail;
	}

	public void setAbleToSendMail(boolean ableToSendMail) {
		this.ableToSendMail = ableToSendMail;
	}

	public boolean isAbleToSaveIntoPersonalFolder() {
		return ableToSaveIntoPersonalFolder;
	}

	public void setAbleToSaveIntoPersonalFolder(boolean ableToSaveIntoPersonalFolder) {
		this.ableToSaveIntoPersonalFolder = ableToSaveIntoPersonalFolder;
	}

	public boolean isAbleToEditWorksheet() {
		return ableToEditWorksheet;
	}

	public void setAbleToEditWorksheet(boolean ableToEditWorksheet) {
		this.ableToEditWorksheet = ableToEditWorksheet;
	}

	public boolean isAbleToSaveRememberMe() {
		return ableToSaveRememberMe;
	}

	public void setAbleToSaveRememberMe(boolean ableToSaveRememberMe) {
		this.ableToSaveRememberMe = ableToSaveRememberMe;
	}

	public boolean isAbleToSeeMetadata() {
		return ableToSeeMetadata;
	}

	public void setAbleToSeeMetadata(boolean ableToSeeMetadata) {
		this.ableToSeeMetadata = ableToSeeMetadata;
	}

	public boolean isAbleToSaveMetadata() {
		return ableToSaveMetadata;
	}

	public void setAbleToSaveMetadata(boolean ableToSaveMetadata) {
		this.ableToSaveMetadata = ableToSaveMetadata;
	}

	public boolean isAbleToBuildQbeQuery() {
		return ableToBuildQbeQuery;
	}

	public void setAbleToBuildQbeQuery(boolean ableToBuildQbeQuery) {
		this.ableToBuildQbeQuery = ableToBuildQbeQuery;
	}

	public boolean isAbleToDoMassiveExport() {
		return ableToDoMassiveExport;
	}

	public void setAbleToDoMassiveExport(boolean ableToDoMassiveExport) {
		this.ableToDoMassiveExport = ableToDoMassiveExport;
	}

	public boolean isAbleToManageUsers() {
		return ableToManageUsers;
	}

	public void setAbleToManageUsers(boolean ableToManageUsers) {
		this.ableToManageUsers = ableToManageUsers;
	}

	public boolean isAbleToSeeDocumentBrowser() {
		return ableToSeeDocumentBrowser;
	}

	public void setAbleToSeeDocumentBrowser(boolean ableToSeeDocumentBrowser) {
		this.ableToSeeDocumentBrowser = ableToSeeDocumentBrowser;
	}

	public boolean isAbleToSeeFavourites() {
		return ableToSeeFavourites;
	}

	public void setAbleToSeeFavourites(boolean ableToSeeFavourites) {
		this.ableToSeeFavourites = ableToSeeFavourites;
	}

	public boolean isAbleToSeeSubscriptions() {
		return ableToSeeSubscriptions;
	}

	public void setAbleToSeeSubscriptions(boolean ableToSeeSubscriptions) {
		this.ableToSeeSubscriptions = ableToSeeSubscriptions;
	}

	public boolean isAbleToSeeMyData() {
		return ableToSeeMyData;
	}

	public void setAbleToSeeMyData(boolean ableToSeeMyData) {
		this.ableToSeeMyData = ableToSeeMyData;
	}

	public boolean isAbleToSeeToDoList() {
		return ableToSeeToDoList;
	}

	public void setAbleToSeeToDoList(boolean ableToSeeToDoList) {
		this.ableToSeeToDoList = ableToSeeToDoList;
	}

	public boolean isAbleToCreateDocuments() {
		return ableToCreateDocuments;
	}

	public void setAbleToCreateDocuments(boolean ableToCreateDocuments) {
		this.ableToCreateDocuments = ableToCreateDocuments;
	}

	public boolean isAbleToCreateSocialAnalysis() {
		return ableToCreateSocialAnalysis;
	}

	public void setAbleToCreateSocialAnalysis(boolean ableToCreateSocialAnalysis) {
		this.ableToCreateSocialAnalysis = ableToCreateSocialAnalysis;
	}

	public boolean isAbleToViewSocialAnalysis() {
		return ableToViewSocialAnalysis;
	}

	public void setAbleToViewSocialAnalysis(boolean ableToViewSocialAnalysis) {
		this.ableToViewSocialAnalysis = ableToViewSocialAnalysis;
	}

	public boolean isAbleToHierarchiesManagement() {
		return ableToHierarchiesManagement;
	}

	public void setAbleToHierarchiesManagement(boolean ableToHierarchiesManagement) {
		this.ableToHierarchiesManagement = ableToHierarchiesManagement;
	}

	public boolean isAbleToEditAllKpiComm() {
		return ableToEditAllKpiComm;
	}

	public void setAbleToEditAllKpiComm(boolean ableToEditAllKpiComm) {
		this.ableToEditAllKpiComm = ableToEditAllKpiComm;
	}

	public boolean isAbleToEditMyKpiComm() {
		return ableToEditMyKpiComm;
	}

	public void setAbleToEditMyKpiComm(boolean ableToEditMyKpiComm) {
		this.ableToEditMyKpiComm = ableToEditMyKpiComm;
	}

	public boolean isAbleToDeleteKpiComm() {
		return ableToDeleteKpiComm;
	}

	public void setAbleToDeleteKpiComm(boolean ableToDeleteKpiComm) {
		this.ableToDeleteKpiComm = ableToDeleteKpiComm;
	}

	public boolean isAbleToEnableDatasetPersistence() {
		return ableToEnableDatasetPersistence;
	}

	public void setAbleToEnableDatasetPersistence(boolean ableToEnableDatasetPersistence) {
		this.ableToEnableDatasetPersistence = ableToEnableDatasetPersistence;
	}

	public boolean isAbleToEnableFederatedDataset() {
		return ableToEnableFederatedDataset;
	}

	public void setAbleToEnableFederatedDataset(boolean ableToEnableFederatedDataset) {
		this.ableToEnableFederatedDataset = ableToEnableFederatedDataset;
	}

	public boolean isAbleToManageGlossaryBusiness() {
		return ableToManageGlossaryBusiness;
	}

	public void setAbleToManageGlossaryBusiness(boolean ableToManageGlossaryBusiness) {
		this.ableToManageGlossaryBusiness = ableToManageGlossaryBusiness;
	}

	public boolean isAbleToManageGlossaryTechnical() {
		return ableToManageGlossaryTechnical;
	}

	public void setAbleToManageGlossaryTechnical(boolean ableToManageGlossaryTechnical) {
		this.ableToManageGlossaryTechnical = ableToManageGlossaryTechnical;
	}

	public List<RoleMetaModelCategory> getRoleMetaModelCategories() {
		return roleMetaModelCategories;
	}

	public void setRoleMetaModelCategories(List<RoleMetaModelCategory> roleMetaModelCategories) {
		this.roleMetaModelCategories = roleMetaModelCategories;
	}

}
