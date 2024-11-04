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

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a <code>Role</code> object.
 *
 * @author sulis
 */

public class Role implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name = "";
	private String description = null;
	private String roleTypeCD = null;
	private String code = null;
	private Integer roleTypeID;
	private String organization = null;
	private Boolean isPublic;

	private boolean isAbleToEditPythonScripts;
	private boolean isAbleToCreateCustomChart;
	private boolean isAbleToSaveSubobjects;
	private boolean isAbleToSeeSubobjects;
	private boolean isAbleToSeeViewpoints;
	private boolean isAbleToSeeSnapshots;
	private boolean isAbleToRunSnapshots;
	private boolean isAbleToSeeNotes;
	private boolean isAbleToSendMail;
	private boolean isAbleToSaveIntoPersonalFolder;
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
	private boolean isAbleToSeeMyWorkspace;
	private boolean isAbleToSeeToDoList;
	private boolean isAbleToCreateDocuments;
	private boolean isAbleToCreateSocialAnalysis;
	private boolean isAbleToViewSocialAnalysis;
	private boolean isAbleToHierarchiesManagement;

	private boolean isAbleToEditAllKpiComm;
	private boolean isAbleToEditMyKpiComm;
	private boolean isAbleToDeleteKpiComm;

	private boolean isAbleToEnableDatasetPersistence;
	private boolean isAbleToEnableFederatedDataset;
	private boolean isAbleToEnableRate;
	private boolean isAbleToEnablePrint;
	private boolean isAbleToEnableCopyAndEmbed;

	private boolean isAbleToManageGlossaryBusiness;
	private boolean isAbleToManageGlossaryTechnical;

	private boolean isAbleToManageKpiValue;

	private boolean isAbleToManageCalendar;

	private boolean isAbleToUseFunctionsCatalog;

	private boolean isAbleToManageInternationalization;

	private boolean ableToCreateSelfServiceCockpit;
	private boolean ableToCreateSelfServiceGeoreport;
	private boolean ableToCreateSelfServiceKpi;

	private boolean isAbleToSeeHelpOnline;
	private boolean isAbleToUseDataPreparation;
	private boolean isAbleToUseDossier;
	private boolean isAbleToUseDashboardThemeManagement;

	private boolean defaultRole;

	private List<RoleMetaModelCategory> roleMetaModelCategories = new ArrayList<>();


	private List<String> roleFunctionalities = new ArrayList<>();
	private List<String> roleAnaliticalDrivers = new ArrayList<>();
	private List<String> roleNews = new ArrayList<>();
	private List<String> roleLayers = new ArrayList<>();
	private List<String> roleUsers = new ArrayList<>();
	private List<String> roleMenu = new ArrayList<>();

	/**
	 * Checks if is able to edit python scripts.
	 *
	 * @return true, if is able to edit python scripts
	 */
	public boolean getAbleToEditPythonScripts() {
		return isAbleToEditPythonScripts;
	}

	/**
	 * Checks if is able to create custom chart.
	 *
	 * @return true, if is able to custom chart
	 */
	public boolean getAbleToCreateCustomChart() {
		return isAbleToCreateCustomChart;
	}

	/**
	 * Checks if is able to save subobjects.
	 *
	 * @return true, if is able to save subobjects
	 */
	public boolean getAbleToSaveSubobjects() {
		return isAbleToSaveSubobjects;
	}

	/**
	 * Sets the checks if is able to save subobjects.
	 *
	 * @param isAbleToSaveSubobjects the new checks if is able to save subobjects
	 */
	public void setAbleToSaveSubobjects(boolean isAbleToSaveSubobjects) {
		this.isAbleToSaveSubobjects = isAbleToSaveSubobjects;
	}

	/**
	 * Checks if is able to see subobjects.
	 *
	 * @return true, if is able to see subobjects
	 */
	public boolean getAbleToSeeSubobjects() {
		return isAbleToSeeSubobjects;
	}

	/**
	 * Sets the check if is able to edit python scripts.
	 *
	 * @param isAbleToEditPythonScripts the new check if is able to edit python scripts
	 */
	public void setAbleToEditPythonScripts(boolean isAbleToEditPythonScripts) {
		this.isAbleToEditPythonScripts = isAbleToEditPythonScripts;
	}

	/**
	 * Sets the check if is able to create custom chart.
	 *
	 * @param isAbleToCreateCustomChart the new check if is able to create custom chart
	 */
	public void setAbleToCreateCustomChart(boolean isAbleToCreateCustomChart) {
		this.isAbleToCreateCustomChart = isAbleToCreateCustomChart;
	}

	/**
	 * Sets the checks if is able to see subobjects.
	 *
	 * @param isAbleToSeeSubobjects the new checks if is able to see subobjects
	 */
	public void setAbleToSeeSubobjects(boolean isAbleToSeeSubobjects) {
		this.isAbleToSeeSubobjects = isAbleToSeeSubobjects;
	}

	/**
	 * Checks if is able to see viewpoints.
	 *
	 * @return true, if is able to see viewpoints
	 */
	public boolean getAbleToSeeViewpoints() {
		return isAbleToSeeViewpoints;
	}

	/**
	 * Sets the checks if is able to see viewpoints.
	 *
	 * @param isAbleToSeeViewpoints the new checks if is able to see viewpoints
	 */
	public void setAbleToSeeViewpoints(boolean isAbleToSeeViewpoints) {
		this.isAbleToSeeViewpoints = isAbleToSeeViewpoints;
	}

	/**
	 * Checks if is able to see snapshots.
	 *
	 * @return true, if is able to see snapshots
	 */
	public boolean getAbleToSeeSnapshots() {
		return isAbleToSeeSnapshots;
	}

	/**
	 * Sets the checks if is able to see snapshots.
	 *
	 * @param isAbleToSeeSnapshots the new checks if is able to see snapshots
	 */
	public void setAbleToSeeSnapshots(boolean isAbleToSeeSnapshots) {
		this.isAbleToSeeSnapshots = isAbleToSeeSnapshots;
	}

	/**
	 * Checks if is able to run snapshots.
	 *
	 * @return true, if is able to run snapshots
	 */
	public boolean getAbleToRunSnapshots() {
		return isAbleToRunSnapshots;
	}

	/**
	 * Sets the checks if is able to run snapshots.
	 *
	 * @param isAbleToRunSnapshots the new checks if is able to run snapshots
	 */
	public void setAbleToRunSnapshots(boolean isAbleToRunSnapshots) {
		this.isAbleToRunSnapshots = isAbleToRunSnapshots;
	}

	/**
	 * Checks if is able to see notes.
	 *
	 * @return true, if is able to see notes
	 */
	public boolean getAbleToSeeNotes() {
		return isAbleToSeeNotes;
	}

	/**
	 * Sets the checks if is able to see notes.
	 *
	 * @param isAbleToSeeNotes the new checks if is able to see notes
	 */
	public void setAbleToSeeNotes(boolean isAbleToSeeNotes) {
		this.isAbleToSeeNotes = isAbleToSeeNotes;
	}

	/**
	 * Checks if is able to send mail.
	 *
	 * @return true, if is able to send mail
	 */
	public boolean getAbleToSendMail() {
		return isAbleToSendMail;
	}

	/**
	 * Sets the checks if is able to send mail.
	 *
	 * @param isAbleToSendMail the new checks if is able to send mail
	 */
	public void setAbleToSendMail(boolean isAbleToSendMail) {
		this.isAbleToSendMail = isAbleToSendMail;
	}

	/**
	 * Checks if is able to save into personal folder.
	 *
	 * @return true, if is able to save into personal folder
	 */
	public boolean getAbleToSaveIntoPersonalFolder() {
		return isAbleToSaveIntoPersonalFolder;
	}

	/**
	 * Sets the checks if is able to save into personal folder.
	 *
	 * @param isAbleToSaveIntoPersonalFolder the new checks if is able to save into personal folder
	 */
	public void setAbleToSaveIntoPersonalFolder(boolean isAbleToSaveIntoPersonalFolder) {
		this.isAbleToSaveIntoPersonalFolder = isAbleToSaveIntoPersonalFolder;
	}

	/**
	 * Checks if is able to save remember me.
	 *
	 * @return true, if is able to save remember me
	 */
	public boolean getAbleToSaveRememberMe() {
		return isAbleToSaveRememberMe;
	}

	/**
	 * Sets the checks if is able to save remember me.
	 *
	 * @param isAbleToSaveRememberMe the new checks if is able to save remember me
	 */
	public void setAbleToSaveRememberMe(boolean isAbleToSaveRememberMe) {
		this.isAbleToSaveRememberMe = isAbleToSaveRememberMe;
	}

	/**
	 * Checks if is able to see metadata.
	 *
	 * @return true, if is able to see metadata
	 */
	public boolean getAbleToSeeMetadata() {
		return isAbleToSeeMetadata;
	}

	/**
	 * Sets the checks if is able to see metadata.
	 *
	 * @param isAbleToSeeMetadata the new checks if is able to see metadata
	 */
	public void setAbleToSeeMetadata(boolean isAbleToSeeMetadata) {
		this.isAbleToSeeMetadata = isAbleToSeeMetadata;
	}

	/**
	 * Checks if is able to save metadata.
	 *
	 * @return the isAbleToSaveMetadata
	 */
	public boolean getAbleToSaveMetadata() {
		return isAbleToSaveMetadata;
	}

	/**
	 * Sets the checks if is able to save metadata.
	 *
	 * @param isAbleToSaveMetadata the new checks if is able to save metadata
	 */
	public void setAbleToSaveMetadata(boolean isAbleToSaveMetadata) {
		this.isAbleToSaveMetadata = isAbleToSaveMetadata;
	}

	/**
	 * Checks if role is able to build and modify QBE queries.
	 *
	 * @return true, if role is able to build and modify QBE queries
	 */
	public boolean getAbleToBuildQbeQuery() {
		return isAbleToBuildQbeQuery;
	}

	/**
	 * Sets if role is able to build and modify QBE queries.
	 *
	 * @param isAbleToBuildQbeQuery
	 */
	public void setAbleToBuildQbeQuery(boolean isAbleToBuildQbeQuery) {
		this.isAbleToBuildQbeQuery = isAbleToBuildQbeQuery;
	}

	/**
	 * Class constructor.
	 */
	public Role() {

	}

	/**
	 * Constructor.
	 *
	 * @param name        the name
	 * @param description the description
	 */
	public Role(String name, String description) {
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
	 * @param description the description to set
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
	 * @param id the role id to set
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
	 * @param name the name to set
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
	 * @param roleTypeCD The roleTypeCD to set.
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
	 * @param roleTypeID The roleTypeID to set.
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
	 * @param code The code to set.
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

	public boolean getAbleToDoMassiveExport() {
		return isAbleToDoMassiveExport;
	}

	public void setAbleToDoMassiveExport(boolean isAbleToDoMassiveExport) {
		this.isAbleToDoMassiveExport = isAbleToDoMassiveExport;
	}

	public boolean getAbleToManageUsers() {
		return isAbleToManageUsers;
	}

	public void setAbleToManageUsers(boolean isAbleToManageUsers) {
		this.isAbleToManageUsers = isAbleToManageUsers;
	}

	/**
	 * @return the isAbleToSeeDocumentBrowser
	 */
	public boolean getAbleToSeeDocumentBrowser() {
		return isAbleToSeeDocumentBrowser;
	}

	/**
	 * @param isAbleToSeeDocumentBrowser the isAbleToSeeDocumentBrowser to set
	 */
	public void setAbleToSeeDocumentBrowser(boolean isAbleToSeeDocumentBrowser) {
		this.isAbleToSeeDocumentBrowser = isAbleToSeeDocumentBrowser;
	}

	/**
	 * @return the isAbleToSeeFavourites
	 */
	public boolean getAbleToSeeFavourites() {
		return isAbleToSeeFavourites;
	}

	/**
	 * @param isAbleToSeeFavourites the isAbleToSeeFavourites to set
	 */
	public void setAbleToSeeFavourites(boolean isAbleToSeeFavourites) {
		this.isAbleToSeeFavourites = isAbleToSeeFavourites;
	}

	/**
	 * @return the isAbleToSeeSubscriptions
	 */
	public boolean getAbleToSeeSubscriptions() {
		return isAbleToSeeSubscriptions;
	}

	/**
	 * @param isAbleToSeeSubscriptions the isAbleToSeeSubscriptions to set
	 */
	public void setAbleToSeeSubscriptions(boolean isAbleToSeeSubscriptions) {
		this.isAbleToSeeSubscriptions = isAbleToSeeSubscriptions;
	}

	/**
	 * @return the isAbleToSeeMyData
	 */
	public boolean getAbleToSeeMyData() {
		return isAbleToSeeMyData;
	}

	/**
	 * @param isAbleToSeeMyData the isAbleToSeeMyData to set
	 */
	public void setAbleToSeeMyData(boolean isAbleToSeeMyData) {
		this.isAbleToSeeMyData = isAbleToSeeMyData;
	}

	/**
	 * @return the isAbleToSeeMyWorkspace
	 */
	public boolean getAbleToSeeMyWorkspace() {
		return isAbleToSeeMyWorkspace;
	}

	/**
	 * @param isAbleToSeeMyWorkspace the isAbleToSeeMyWorkspace to set
	 */
	public void setAbleToSeeMyWorkspace(boolean isAbleToSeeMyWorkspace) {
		this.isAbleToSeeMyWorkspace = isAbleToSeeMyWorkspace;
	}

	/**
	 * @return the isAbleToSeeToDoList
	 */
	public boolean getAbleToSeeToDoList() {
		return isAbleToSeeToDoList;
	}

	/**
	 * @param isAbleToSeeToDoList the isAbleToSeeToDoList to set
	 */
	public void setAbleToSeeToDoList(boolean isAbleToSeeToDoList) {
		this.isAbleToSeeToDoList = isAbleToSeeToDoList;
	}

	/**
	 * @return the isAbleToCreateDocuments
	 */
	public boolean getAbleToCreateDocuments() {
		return isAbleToCreateDocuments;
	}

	/**
	 * @param isAbleToCreateDocuments the isAbleToCreateDocuments to set
	 */
	public void setAbleToCreateDocuments(boolean isAbleToCreateDocuments) {
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
	 * @param roleMetaModelCategories the roleMetaModelCategories to set
	 */
	public void setRoleMetaModelCategories(List<RoleMetaModelCategory> roleMetaModelCategories) {
		this.roleMetaModelCategories = roleMetaModelCategories;
	}

	public boolean getAbleToEditAllKpiComm() {
		return isAbleToEditAllKpiComm;
	}

	public void setAbleToEditAllKpiComm(boolean isAbleToEditAllKpiComm) {
		this.isAbleToEditAllKpiComm = isAbleToEditAllKpiComm;
	}

	public boolean getAbleToEditMyKpiComm() {
		return isAbleToEditMyKpiComm;
	}

	public void setAbleToEditMyKpiComm(boolean isAbleToEditMyKpiComm) {
		this.isAbleToEditMyKpiComm = isAbleToEditMyKpiComm;
	}

	public boolean getAbleToDeleteKpiComm() {
		return isAbleToDeleteKpiComm;
	}

	public void setAbleToDeleteKpiComm(boolean isAbleToDeleteKpiComm) {
		this.isAbleToDeleteKpiComm = isAbleToDeleteKpiComm;
	}

	public boolean getAbleToCreateSocialAnalysis() {
		return isAbleToCreateSocialAnalysis;
	}

	public void setAbleToCreateSocialAnalysis(boolean isAbleToCreateSocialAnalysis) {
		this.isAbleToCreateSocialAnalysis = isAbleToCreateSocialAnalysis;
	}

	public boolean getAbleToViewSocialAnalysis() {
		return isAbleToViewSocialAnalysis;
	}

	public void setAbleToViewSocialAnalysis(boolean isAbleToViewSocialAnalysis) {
		this.isAbleToViewSocialAnalysis = isAbleToViewSocialAnalysis;
	}

	public boolean getAbleToHierarchiesManagement() {
		return isAbleToHierarchiesManagement;
	}

	public void setAbleToHierarchiesManagement(boolean isAbleToHierarchiesManagement) {
		this.isAbleToHierarchiesManagement = isAbleToHierarchiesManagement;
	}

	public boolean getAbleToEnableDatasetPersistence() {
		return isAbleToEnableDatasetPersistence;
	}

	public void setAbleToEnableDatasetPersistence(boolean isAbleToEnableDatasetPersistence) {
		this.isAbleToEnableDatasetPersistence = isAbleToEnableDatasetPersistence;
	}

	public boolean getAbleToManageGlossaryBusiness() {
		return isAbleToManageGlossaryBusiness;
	}

	public void setAbleToManageGlossaryBusiness(boolean isAbleToManageGlossaryBusiness) {
		this.isAbleToManageGlossaryBusiness = isAbleToManageGlossaryBusiness;
	}

	public boolean getAbleToManageGlossaryTechnical() {
		return isAbleToManageGlossaryTechnical;
	}

	public boolean getAbleToManageKpiValue() {
		return isAbleToManageKpiValue;
	}

	public boolean getAbleToManageCalendar() {
		return isAbleToManageCalendar;
	}

	public void setAbleToManageCalendar(boolean isAbleToManageCalendar) {
		this.isAbleToManageCalendar = isAbleToManageCalendar;
	}

	public boolean getAbleToUseFunctionsCatalog() {
		return isAbleToUseFunctionsCatalog;
	}

	public void setAbleToUseFunctionsCatalog(boolean isAbleToUseFunctionsCatalog) {
		this.isAbleToUseFunctionsCatalog = isAbleToUseFunctionsCatalog;
	}

	public void setAbleToManageGlossaryTechnical(boolean isAbleToManageGlossaryTechnical) {
		this.isAbleToManageGlossaryTechnical = isAbleToManageGlossaryTechnical;
	}

	public void setAbleToManageKpiValue(boolean isAbleToManageKpiValue) {
		this.isAbleToManageKpiValue = isAbleToManageKpiValue;
	}

	public boolean getAbleToEnableFederatedDataset() {
		return isAbleToEnableFederatedDataset;
	}

	public void setAbleToEnableFederatedDataset(boolean isAbleToEnableFederatedDataset) {
		this.isAbleToEnableFederatedDataset = isAbleToEnableFederatedDataset;
	}

	public boolean getAbleToEnableRate() {
		return isAbleToEnableRate;
	}

	public void setAbleToEnableRate(boolean isAbleToEnableRate) {
		this.isAbleToEnableRate = isAbleToEnableRate;
	}

	public boolean getAbleToEnablePrint() {
		return isAbleToEnablePrint;
	}

	public void setAbleToEnablePrint(boolean isAbleToEnablePrint) {
		this.isAbleToEnablePrint = isAbleToEnablePrint;
	}

	public boolean getAbleToEnableCopyAndEmbed() {
		return isAbleToEnableCopyAndEmbed;
	}

	public void setAbleToEnableCopyAndEmbed(boolean isAbleToEnableCopyAndEmbed) {
		this.isAbleToEnableCopyAndEmbed = isAbleToEnableCopyAndEmbed;
	}

	public Boolean getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public boolean getAbleToManageInternationalization() {
		return isAbleToManageInternationalization;
	}

	public void setAbleToManageInternationalization(boolean isAbleToManageInternationalization) {
		this.isAbleToManageInternationalization = isAbleToManageInternationalization;
	}

	public boolean getAbleToCreateSelfServiceCockpit() {
		return ableToCreateSelfServiceCockpit;
	}

	public void setAbleToCreateSelfServiceCockpit(boolean ableToCreateSelfServiceCockpit) {
		this.ableToCreateSelfServiceCockpit = ableToCreateSelfServiceCockpit;
	}

	public boolean getAbleToCreateSelfServiceGeoreport() {
		return ableToCreateSelfServiceGeoreport;
	}

	public void setAbleToCreateSelfServiceGeoreport(boolean ableToCreateSelfServiceGeoreport) {
		this.ableToCreateSelfServiceGeoreport = ableToCreateSelfServiceGeoreport;
	}

	public boolean getAbleToCreateSelfServiceKpi() {
		return ableToCreateSelfServiceKpi;
	}

	public void setAbleToCreateSelfServiceKpi(boolean ableToCreateSelfServiceKpi) {
		this.ableToCreateSelfServiceKpi = ableToCreateSelfServiceKpi;
	}

	public void setAbleToSeeHelpOnline(Boolean ableToSeeHelpOnline) {
		this.isAbleToSeeHelpOnline = ableToSeeHelpOnline;
	}

	public Boolean getAbleToSeeHelpOnline() {
		return isAbleToSeeHelpOnline;
	}

	public void setAbleToUseDataPreparation(Boolean ableToUseDataPreparation) {
		this.isAbleToUseDataPreparation = ableToUseDataPreparation;
	}

	public Boolean getAbleToUseDataPreparation() {
		return isAbleToUseDataPreparation;
	}

	public void setAbleToUseDossier(Boolean ableToUseDossier) {
		this.isAbleToUseDossier = ableToUseDossier;
	}

	public Boolean getAbleToUseDossier() {
		return isAbleToUseDossier;
	}

	public void setAbleToUseDashboardThemeManagement(Boolean ableToUseDashboardThemeManagement) {
		this.isAbleToUseDashboardThemeManagement = ableToUseDashboardThemeManagement;
	}

	public Boolean getAbleToUseDashboardThemeManagement() {
		return isAbleToUseDashboardThemeManagement;
	}
	private final void writeObject(ObjectOutputStream aOutputStream) {
		  throw new UnsupportedOperationException("Security violation : cannot serialize object to a stream");
	}

	public List<String> getRoleFunctionalities() {
		return roleFunctionalities;
	}

	public void setRoleFunctionalities(List<String> roleFunctionalities) {
		this.roleFunctionalities = roleFunctionalities;
	}

	public List<String> getRoleAnaliticalDrivers() {
		return roleAnaliticalDrivers;
	}

	public void setRoleAnaliticalDrivers(List<String> roleAnaliticalDrivers) {
		this.roleAnaliticalDrivers = roleAnaliticalDrivers;
	}

	public List<String> getRoleNews() {
		return roleNews;
	}

	public void setRoleNews(List<String> roleNews) {
		this.roleNews = roleNews;
	}

	public List<String> getRoleLayers() {
		return roleLayers;
	}

	public void setRoleLayers(List<String> roleLayers) {
		this.roleLayers = roleLayers;
	}

	public List<String> getRoleUsers() {
		return roleUsers;
	}

	public void setRoleUsers(List<String> roleUsers) {
		this.roleUsers = roleUsers;
	}

	public List<String> getRoleMenu() {
		return roleMenu;
	}

	public void setRoleMenu(List<String> roleMenu) {
		this.roleMenu = roleMenu;
	}


}
