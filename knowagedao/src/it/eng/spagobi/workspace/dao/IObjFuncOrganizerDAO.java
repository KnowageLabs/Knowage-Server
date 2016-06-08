package it.eng.spagobi.workspace.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.workspace.metadata.SbiObjFuncOrganizer;

import java.util.List;

public interface IObjFuncOrganizerDAO extends ISpagoBIDao {

	public List loadDocumentsByFolder(Integer folderId) throws EMFUserError;

	public SbiObjFuncOrganizer addDocumentToOrganizer(Integer documentId) throws EMFUserError;

	public void removeDocumentFromOrganizer(SbiObjFuncOrganizer documentToRemove) throws EMFUserError;

	public void moveDocumentToDifferentFolder(SbiObjFuncOrganizer documentToMove) throws EMFUserError;

}
