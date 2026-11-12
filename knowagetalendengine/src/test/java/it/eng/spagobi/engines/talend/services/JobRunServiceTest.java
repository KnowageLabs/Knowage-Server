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
package it.eng.spagobi.engines.talend.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;

public class JobRunServiceTest {

	@Test
	public void preparesParametersAndCallbacksWithoutResolvingDatasets() {
		Map<String, Object> common = new HashMap<>();
		Object auditCallback = new Object();
		Object eventCallback = new Object();
		common.put("customer", "ACME");
		common.put(EngineConstants.ENV_AUDIT_SERVICE_PROXY, auditCallback);
		common.put(EngineConstants.ENV_EVENT_SERVICE_PROXY, eventCallback);
		common.put(EngineConstants.ENV_DATASET, "reserved request parameter");
		common.put(EngineConstants.ENV_DATASOURCE, "reserved request parameter");

		EngineStartServletIOManager ioManager = new EngineStartServletIOManager(request(), response()) {
			@Override
			public Map getEnvForWidget() {
				return common;
			}

			@Override
			public String getDocumentId() {
				return "42";
			}

			@Override
			public Map getEnv() {
				throw new AssertionError("Talend must not resolve the dataset environment");
			}

			@Override
			public IDataSet getDataSet() {
				throw new AssertionError("Talend must not construct local datasets");
			}

			@Override
			public IDataSource getDataSource() {
				throw new AssertionError("Talend must not fetch unused datasources");
			}
		};

		Map environment = new JobRunService().getJobEnvironment(ioManager);
		assertEquals("ACME", environment.get("customer"));
		assertEquals("42", environment.get(EngineConstants.ENV_DOCUMENT_ID));
		assertSame(auditCallback, environment.get(EngineConstants.ENV_AUDIT_SERVICE_PROXY));
		assertSame(eventCallback, environment.get(EngineConstants.ENV_EVENT_SERVICE_PROXY));
		assertNull(environment.get(EngineConstants.ENV_DATASET));
		assertNull(environment.get(EngineConstants.ENV_DATASOURCE));
		assertEquals("reserved request parameter", common.get(EngineConstants.ENV_DATASET));
	}

	@Test
	public void linksServletAndJobRunnerSignaturesWithoutDatasetLibraries() throws Exception {
		String[] classes = {
				"it.eng.spagobi.engines.talend.services.JobRunService",
				"it.eng.spagobi.engines.talend.services.EngineInfoService",
				"it.eng.spagobi.engines.talend.runtime.JavaJobRunner",
				"it.eng.spagobi.engines.talend.runtime.PerlJobRunner",
				"it.eng.spagobi.engines.talend.runtime.TalendWork",
				"it.eng.spagobi.utilities.engines.EngineStartServletIOManager",
				"it.eng.spagobi.utilities.filters.SpagoBIAccessFilter",
				"it.eng.knowage.utils.filters.LoggerSetupFilter"
		};
		for (String name : classes) {
			Class<?> type = Class.forName(name, false, getClass().getClassLoader());
			type.getDeclaredMethods();
			type.getDeclaredConstructors();
		}
	}

	private HttpServletRequest request() {
		Map<String, Object> attributes = new HashMap<>();
		HttpSession session = (HttpSession) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpSession.class }, (proxy, method, args) -> {
					if ("getAttribute".equals(method.getName())) {
						return attributes.get(args[0]);
					}
					if ("setAttribute".equals(method.getName())) {
						attributes.put((String) args[0], args[1]);
					}
					return null;
				});
		return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) ->
					"getSession".equals(method.getName()) ? session : null);
	}

	private HttpServletResponse response() {
		return (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { HttpServletResponse.class }, (proxy, method, args) -> null);
	}
}
