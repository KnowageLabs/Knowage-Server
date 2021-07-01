package it.eng.knowage.resourcemanager.service;

import it.eng.knowage.resourcemanager.resource.utils.RootFolderDTO;

public interface ResourceManagerAPI {

	public RootFolderDTO getFolders(String path);

}
