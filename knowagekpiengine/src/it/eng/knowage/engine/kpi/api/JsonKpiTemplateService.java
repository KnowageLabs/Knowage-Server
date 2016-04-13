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
package it.eng.knowage.engine.kpi.api;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.engine.kpi.KpiEngineInstance;
import it.eng.knowage.engine.util.KpiEngineDataUtil;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.dataset.common.behaviour.UserProfileUtils;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/jsonKpiTemplate")
@ManageAuthorization
public class JsonKpiTemplateService extends AbstractFullKpiEngineResource {

	@POST
	@Path("/readKpiTemplate")
	@SuppressWarnings("rawtypes")
	public String getJSONKpiTemplate(@Context HttpServletRequest req, @Context HttpServletResponse servletResponse) {
		try {
			String result = "";

			KpiEngineInstance engineInstance = getEngineInstance();

			Map profileAttributes = UserProfileUtils.getProfileAttributes((UserProfile) this.getEnv().get(EngineConstants.ENV_USER_PROFILE));
			JSONObject jsonTemplate = RestUtilities.readBodyAsJSONObject(req);

			if (StringUtilities.isEmpty(result)) {
				result = KpiEngineDataUtil.loadJsonData(jsonTemplate);
			}

			return result;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

}