package it.eng.spagobi.workspace.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.workspace.metadata.SbiFunctionsOrganizer;

import java.util.List;

public interface IFunctionsOrganizerDAO extends ISpagoBIDao {

	public List loadFolderByUser() throws EMFUserError;

	public SbiFunctionsOrganizer createFolder(SbiFunctionsOrganizer folder) throws EMFUserError;

	public void deleteFolder(Integer folderId) throws EMFUserError;

}
