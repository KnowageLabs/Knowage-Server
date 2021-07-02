package it.eng.knowage.resourcemanager.service;

import it.eng.knowage.resourcemanager.resource.utils.RootFolderDTO;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

public interface ResourceManagerAPI {

	public RootFolderDTO getFolders(SpagoBIUserProfile profile, String path);

	boolean canSee(String path, SpagoBIUserProfile profile);

	boolean createFolder(String path, SpagoBIUserProfile profile);

	boolean deleteFolder(String path, SpagoBIUserProfile profile);

	java.nio.file.Path getDownloadPath(String path, SpagoBIUserProfile profile);
}
