package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class FolderManagementAPI extends AbstractSpagoBIResource {
	static protected Logger logger = Logger.getLogger(FolderManagementAPI.class);

	public List<LowFunctionality> getFolders(Boolean recoverBIObjects, String permissionOnFolder, String dateFilter, String status) {

		List<LowFunctionality> folders = new ArrayList<LowFunctionality>();
		try {
			UserProfile profile = getUserProfile();
			ILowFunctionalityDAO dao = DAOFactory.getLowFunctionalityDAO();
			dao.setUserProfile(profile);
			List<LowFunctionality> allFolders = new ArrayList<>();
			String filterByDate = dateFilter != null && !dateFilter.equals("undefined") ? dateFilter : null;
			String filterByStatus = status != null && !status.equals("undefined") ? status : null;

			if (filterByDate != null || filterByStatus != null) {
				allFolders = dao.loadAllLowFunctionalities(true, null, filterByDate, filterByStatus);
			} else {
				allFolders = dao.loadAllLowFunctionalities(recoverBIObjects);
			}

			if (permissionOnFolder != null && !permissionOnFolder.isEmpty()) {
				for (LowFunctionality lf : allFolders) {
					if (ObjectsAccessVerifier.canSee(lf, profile) && checkPermissionOnFolder(permissionOnFolder, lf, profile)) {
						folders.add(lf);
					}
				}
			} else {
				for (LowFunctionality lf : allFolders) {
					if (ObjectsAccessVerifier.canSee(lf, profile)) {
						folders.add(lf);
					}
				}
			}

		} catch (Exception e) {
			String errorString = "Error while getting the list of folders";
			logger.error(errorString, e);
			throw new SpagoBIRuntimeException(errorString, e);
		} finally {
			logger.debug("OUT");
		}
		return folders;
	}

	public String getFoldersAsString(Boolean recoverBIObjects, String permissionOnFolder, String dateFilter, String status) {
		List<LowFunctionality> folders = getFolders(recoverBIObjects, permissionOnFolder, dateFilter, status);
		return JsonConverter.objectToJson(folders, folders.getClass());
	}

	private boolean checkPermissionOnFolder(String permission, LowFunctionality lf, UserProfile profile) {
		boolean result = false;

		switch (permission.toUpperCase()) {
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP:
			result = ObjectsAccessVerifier.canDev(lf, profile);
			break;
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST:
			result = ObjectsAccessVerifier.canTest(lf, profile);
			break;
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE:
			result = ObjectsAccessVerifier.canExec(lf, profile);
			break;
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE:
			result = ObjectsAccessVerifier.canCreate(lf, profile);
			break;
		}

		return result;
	}
}
