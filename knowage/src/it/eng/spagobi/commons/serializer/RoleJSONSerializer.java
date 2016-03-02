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
package it.eng.spagobi.commons.serializer;

import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class RoleJSONSerializer implements Serializer {

	public static final String ROLE_ID = "id";
	private static final String ROLE_NAME = "name";
	private static final String ROLE_DESCRIPTION = "description";
	private static final String ROLE_CODE = "code";
	private static final String ROLE_TYPE_ID = "typeId";
	private static final String ROLE_TYPE_CD = "typeCd";
	private static final String SAVE_PERSONAL_FOLDER = "savePersonalFolder";
	private static final String SAVE_META = "saveMeta";
	private static final String SAVE_REMEMBER = "saveRemember";
	private static final String SAVE_SUBOBJ = "saveSubobj";
	private static final String SEE_META = "seeMeta";
	private static final String SEE_NOTES = "seeNotes";
	private static final String SEE_SNAPSHOT = "seeSnapshot";
	private static final String SEE_SUBOBJ = "seeSubobj";
	private static final String SEE_VIEWPOINTS = "seeViewpoints";
	private static final String SEND_MAIL = "sendMail";
	private static final String BUILD_QBE = "buildQbe";
	private static final String DO_MASSIVE_EXPORT = "doMassiveExport";
	private static final String MANAGE_USERS = "manageUsers";
	private static final String DEFAULT_ROLE = "defaultRole";
	private static final String EDIT_WORKSHEET = "editWorksheet";
	private static final String SEE_DOC_BROWSER = "seeDocBrowser";
	private static final String SEE_MY_DATA = "seeMyData";
	private static final String SEE_FAVOURITES = "seeFavourites";
	private static final String SEE_SUBSCRIPTIONS = "seeSubscriptions";
	private static final String SEE_TODO_LIST = "seeToDoList";
	private static final String CREATE_DOCUMENT = "createDocument";
	private static final String BUSINESS_MODEL_CATEGORIES = "bmCategories";
	private static final String KPI_COMMENT_EDIT_ALL = "kpiCommentEditAll";
	private static final String KPI_COMMENT_EDIT_MY = "kpiCommentEditMy";
	private static final String KPI_COMMENT_DELETE = "kpiCommentDelete";
	private static final String CREATE_SOCIAL_ANALYSIS = "createSocialAnalysis";
	private static final String VIEW_SOCIAL_ANALYSIS = "viewSocialAnalysis";
	private static final String HIERARCHIES_MANAGEMENT = "hierarchiesManagement";
	private static final String ENABLE_DATASET_PERSISTENCE = "enableDatasetPersistence";
	private static final String ENABLE_FEDERATED_DATASET = "enableFederatedDataset";

	public static final String MANAGE_GLOSSARY_BUSINESS = "manageGlossaryBusiness";
	public static final String MANAGE_GLOSSARY_TECHNICAL = "manageGlossaryTechnical";

	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject result = null;

		if (!(o instanceof Role)) {
			throw new SerializationException("RoleJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}

		try {
			Role role = (Role) o;
			result = new JSONObject();

			result.put(ROLE_ID, role.getId());
			result.put(ROLE_NAME, role.getName());
			result.put(ROLE_DESCRIPTION, role.getDescription());
			result.put(ROLE_CODE, role.getCode());
			result.put(ROLE_TYPE_ID, role.getRoleTypeID());
			result.put(ROLE_TYPE_CD, role.getRoleTypeCD());
			result.put(SAVE_PERSONAL_FOLDER, role.isAbleToSaveIntoPersonalFolder());
			result.put(SAVE_META, role.isAbleToSaveMetadata());
			result.put(SAVE_REMEMBER, role.isAbleToSaveRememberMe());
			result.put(SAVE_SUBOBJ, role.isAbleToSaveSubobjects());
			result.put(SEE_META, role.isAbleToSeeMetadata());
			result.put(SEE_NOTES, role.isAbleToSeeNotes());
			result.put(SEE_SNAPSHOT, role.isAbleToSeeSnapshots());
			result.put(SEE_SUBOBJ, role.isAbleToSeeSubobjects());
			result.put(SEE_VIEWPOINTS, role.isAbleToSeeViewpoints());
			result.put(SEND_MAIL, role.isAbleToSendMail());
			result.put(BUILD_QBE, role.isAbleToBuildQbeQuery());
			result.put(DO_MASSIVE_EXPORT, role.isAbleToDoMassiveExport());
			result.put(MANAGE_USERS, role.isAbleToManageUsers());
			result.put(DEFAULT_ROLE, role.isDefaultRole());
			result.put(EDIT_WORKSHEET, role.isAbleToEditWorksheet());
			result.put(SEE_DOC_BROWSER, role.isAbleToSeeDocumentBrowser());
			result.put(SEE_MY_DATA, role.isAbleToSeeMyData());
			result.put(SEE_FAVOURITES, role.isAbleToSeeFavourites());
			result.put(SEE_SUBSCRIPTIONS, role.isAbleToSeeSubscriptions());
			result.put(SEE_TODO_LIST, role.isAbleToSeeToDoList());
			result.put(CREATE_DOCUMENT, role.isAbleToCreateDocuments());
			result.put(KPI_COMMENT_EDIT_ALL, role.isAbleToEditAllKpiComm());
			result.put(KPI_COMMENT_EDIT_MY, role.isAbleToEditMyKpiComm());
			result.put(KPI_COMMENT_DELETE, role.isAbleToDeleteKpiComm());
			result.put(CREATE_SOCIAL_ANALYSIS, role.isAbleToCreateSocialAnalysis());
			result.put(VIEW_SOCIAL_ANALYSIS, role.isAbleToViewSocialAnalysis());
			result.put(HIERARCHIES_MANAGEMENT, role.isAbleToHierarchiesManagement());
			result.put(MANAGE_GLOSSARY_BUSINESS, role.isAbleToManageGlossaryBusiness());
			result.put(MANAGE_GLOSSARY_TECHNICAL, role.isAbleToManageGlossaryTechnical());
			result.put(ENABLE_DATASET_PERSISTENCE, role.isAbleToEnableDatasetPersistence());
			result.put(ENABLE_FEDERATED_DATASET, role.isAbleToEnableFederatedDataset());

			// create an array for Business Model Categories Ids
			JSONArray bmCategories = new JSONArray();
			IRoleDAO dao = DAOFactory.getRoleDAO();
			List<RoleMetaModelCategory> roleMetaModelCategories = dao.getMetaModelCategoriesForRole(role.getId());
			if (roleMetaModelCategories != null) {
				for (RoleMetaModelCategory roleMetaModelCategory : roleMetaModelCategories) {
					bmCategories.put(roleMetaModelCategory.getCategoryId());
				}
			}
			result.put(BUSINESS_MODEL_CATEGORIES, bmCategories);

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}

		return result;
	}

}
