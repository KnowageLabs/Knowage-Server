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
package it.eng.spagobi.engines.commonj.services;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import commonj.work.WorkEvent;
import de.myfoo.commonj.work.FooRemoteWorkItem;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.commonj.runtime.CommonjWorkContainer;
import it.eng.spagobi.engines.commonj.utils.GeneralUtils;
import it.eng.spagobi.engines.commonj.utils.ProcessesStatusContainer;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.AbstractEngineAction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.service.JSONFailure;
import it.eng.spagobi.utilities.service.JSONSuccess;

public class StopWorkAction extends AbstractEngineAction {

	private static final Logger LOGGER = Logger.getLogger(StopWorkAction.class);
	private static final String PID = "PROCESS_ID";

	@Override
	public void service(SourceBean request, SourceBean response) {
		String pid;
		JSONObject info;
		int statusWI;

		LOGGER.debug("IN");

		try {
			super.service(request, response);

			HttpSession session = getHttpSession();

			UserProfile profile = (UserProfile) session.getAttribute("ENG_USER_PROFILE");
			if (profile != null) {
				String tenantId = profile.getOrganization();
				LOGGER.debug("Retrieved tenantId from user profile object : [" + tenantId + "]");
				// putting tenant id on thread local
				if (tenantId != null) {
					Tenant tenant = new Tenant(tenantId);
					TenantManager.setTenant(tenant);
				}
			}

			pid = getAttributeAsString(PID);
			LOGGER.debug("Parameter [" + PID + "] is equals to [" + pid + "]");
			Assert.assertTrue(!StringUtils.isEmpty(pid), "Parameter [" + PID + "] cannot be null or empty");

			ProcessesStatusContainer processesStatusContainer = ProcessesStatusContainer.getInstance();
			Object o = processesStatusContainer.getPidContainerMap().get(pid);

			// if process still in session stop the process and delete the attribute, otherwise it could have been finished
			if (o != null) {
				CommonjWorkContainer container = (CommonjWorkContainer) o;
				FooRemoteWorkItem fooRwi = container.getFooRemoteWorkItem();
				// release the resource
				fooRwi.release();
				// Use it to give time to set the status
				Thread.sleep(1000);
				statusWI = container.getWorkItem().getStatus();
				// session.removeAttribute("SBI_PROCESS_"+document_id);
				processesStatusContainer.getPidContainerMap().remove(pid);

			} // if no more in session is completed
			else
				statusWI = WorkEvent.WORK_COMPLETED;

			info = GeneralUtils.buildJSONObject(pid, statusWI);
			try {
				writeBackToClient(new JSONSuccess(info));
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}
		} catch (Exception e) {
			LOGGER.error("Error in stopping the work");
			try {
				writeBackToClient(new JSONFailure(e));
			} catch (IOException | JSONException e1) {
				LOGGER.error("Error in stopping the work and in writing back to client", e);
				throw new SpagoBIEngineServiceException(getActionName(),
						"Error in stopping the work and in writing back to client", e1);
			}

		} finally {
			LOGGER.debug("OUT");
		}

	}

}
