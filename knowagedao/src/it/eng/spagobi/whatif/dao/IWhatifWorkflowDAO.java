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
package it.eng.spagobi.whatif.dao;

import java.util.List;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.whatif.metadata.SbiWhatifWorkflow;

public interface IWhatifWorkflowDAO extends ISpagoBIDao {

	public void updateWorkflow(List<SbiWhatifWorkflow> workflow, int mId);

	public void createNewWorkflow(List<SbiWhatifWorkflow> newWorkflow);

	public void startWorkflow(int modelId);

	public List<Integer> getWorkflowUsersOfModel(int modelId);

	public SbiWhatifWorkflow loadUsersWorkflow();

	public int isWorkflowStarted(int modelId);

	public String getActiveUserIdByModel(int modelId);

	/**
	 * Updates the workflow giving control to next user
	 * 
	 * @param modelId
	 */
	public String goNextUserByModel(int modelId);

	public int idByUserAndModel(int userId, int modelId);

}
