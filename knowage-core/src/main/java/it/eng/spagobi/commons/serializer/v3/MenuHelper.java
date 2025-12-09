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
package it.eng.spagobi.commons.serializer.v3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.wapp.bo.Menu;

/**
 * @author Gavardi Giulio
 */
public class MenuHelper {

	private static Logger logger = Logger.getLogger(MenuHelper.class);

	private static final String urlDocumentUserBrowser = "/document-browser";
	private static final String urlWorkspaceManagement = "/workspace";

	public static String findFunctionalityUrl(Menu menu, String contextPath) {
		logger.debug("IN");
		String url = null;
		try {
			String functionality = menu.getFunctionality();
			if (functionality == null || functionality.trim().equals("")) {
				logger.error("Input menu is not associated to a SpagoBI functionality");
			} else {

				if (functionality.equals(CommunityFunctionalityConstants.DOCUMENT_BROWSER_USER)) {
					url = urlDocumentUserBrowser;
					String initialPath = menu.getInitialPath();
					if (initialPath != null && !initialPath.trim().equals("")) {
						// url += "&" + BIObjectsModule.MODALITY + "=" + BIObjectsModule.FILTER_TREE + "&" + TreeObjectsModule.PATH_SUBTREE + "="
						// + initialPath;

						String idsPath = convertPathInIds(initialPath);
//							url += "&" + BIObjectsModule.MODALITY + "=" + BIObjectsModule.FILTER_TREE + "&" + TreeObjectsModule.PATH_SUBTREE + "=" + initialPath
//									+ idsPath;
						url = String.format("%s/%s", url, idsPath);
					}
				} else if (functionality.equals(CommunityFunctionalityConstants.WORKSPACE_MANAGEMENT)) {
					url = urlWorkspaceManagement;
					String initialPath = menu.getInitialPath();
					if (initialPath != null && initialPath.equals("documents")) {
						url += "/recent";
					} else if (initialPath != null && initialPath.equals("datasets")) {
						url += "/data";
					} else if (initialPath != null && initialPath.equals("models")) {
						url += "/models";
					}
				}

			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			logger.debug("OUT: url = [" + url + "]");
		}
		return url;
	}

	public static String convertPathInIds(String subTree) throws EMFUserError {
		// If this is a "custom" Document Browser we have a subtree path as parameter
		List<Integer> functIds = new LinkedList<>();
		logger.debug("IN");
		if (subTree != null) {

			if (!StringUtils.isEmpty(subTree)) {
				ArrayList<LowFunctionality> toMerge = new ArrayList<>();
				LowFunctionality funct = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByPath(subTree, false);
				if (funct != null) {
					toMerge.add(funct);
					Integer parentId = funct.getParentId();
					while (parentId != null) {
						LowFunctionality more = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(parentId, false);
						if (more != null) {
							toMerge.add(more);
							parentId = more.getParentId();
						}
					}

					// toMerge contains all Id to be read in reverse order
					for (int i = toMerge.size() - 1; i >= 0; i--) {
						LowFunctionality lf = toMerge.get(i);
						functIds.add(lf.getId());
					}

				}
			}

		}
		logger.debug("OUT");

		String value = functIds.stream().map(x -> x.toString()).collect(Collectors.joining("/"));

		return value;
	}

}
