package it.eng.spagobi.whatif.dao;

import java.util.List;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.whatif.metadata.SbiWhatifWorkflow;

public interface IWhatifWorkflowDAO extends ISpagoBIDao {

	// public List loadWorkflowByDocumentId(int id);

	public void updateWorkflow(List<SbiWhatifWorkflow> workflow);

	public void createNewWorkflow(List<SbiWhatifWorkflow> newWorkflow);

	public void startWorkflow(int modelId);

	public List<Integer> getWorkflowUsersOfModel(int modelId);

	public SbiWhatifWorkflow loadUsersWorkflow();

	public int isWorkflowStarted(int modelId);

	// public String loadLockerUser(int documentId);
}
