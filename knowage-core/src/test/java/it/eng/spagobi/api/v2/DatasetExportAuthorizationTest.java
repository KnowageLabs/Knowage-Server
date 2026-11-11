package it.eng.spagobi.api.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.spagobi.api.v2.export.CSVExportJob;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.ActionNotPermittedException;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DatasetExportAuthorizationTest {

	@After
	public void cleanUp() {
		UserProfileManager.unset();
		TenantManager.unset();
	}

	@Test
	public void rejectsInvisibleCsvAndExcelBeforeUsingParametersOrScheduling() {
		IDataSet source = new JDBCDataSet();
		int[] checks = new int[1];
		mockDao(source);
		new MockUp<DatasetManagementAPI>() {
			@Mock
			public void canSee(IDataSet dataset) throws ActionNotPermittedException {
				assertSame(source, dataset);
				checks[0]++;
				throw new ActionNotPermittedException("Not visible", "dataset.not.visible");
			}
		};

		ExportResource resource = exportResource();
		assertEquals(403, resource.datasetAsCsv(12, "{}").getStatus());
		assertEquals(403, resource.datasetAsXls(12, "{}").getStatus());
		assertEquals(2, checks[0]);
	}

	@Test
	public void rejectsInvisibleFileBeforeInspectingConfigurationOrFilesystem() {
		IDataSet source = new JDBCDataSet();
		mockDao(source);
		UserProfileManager.setProfile(new UserProfile("alice", "tenant"));
		new MockUp<DatasetManagementAPI>() {
			@Mock
			public void canSee(IDataSet dataset) throws ActionNotPermittedException {
				assertSame(source, dataset);
				throw new ActionNotPermittedException("Not visible", "dataset.not.visible");
			}
		};
		assertEquals(403, new DataSetResource().downloadDataSetFile("hidden", "CSV").getStatus());
	}

	@Test
	public void rejectsDatasetWhichBecameInvisibleAfterExportWasQueued() throws Exception {
		IDataSet source = new JDBCDataSet();
		mockDao(source);
		new MockUp<DatasetManagementAPI>() {
			@Mock
			public void canSee(IDataSet dataset) throws ActionNotPermittedException {
				assertSame(source, dataset);
				throw new ActionNotPermittedException("Access revoked", "dataset.not.visible");
			}
		};
		JobDataMap data = new JobDataMap();
		data.put("dataSetId", 12);
		data.put("userProfile", new UserProfile("alice", "tenant"));
		data.put("parameters", Collections.emptyMap());
		data.put("drivers", Collections.emptyMap());
		JobExecutionContext context = (JobExecutionContext) Proxy.newProxyInstance(JobExecutionContext.class.getClassLoader(),
				new Class<?>[] { JobExecutionContext.class }, (proxy, method, args) -> data);
		CSVExportJob job = new CSVExportJob();
		try {
			job.execute(context);
			fail("Expected execution-time visibility check");
		} catch (JobExecutionException expected) {
			assertEquals(ActionNotPermittedException.class, expected.getCause().getClass());
			assertNull(job.getDataFile());
		}
	}

	@Test
	public void returnsNotFoundForDeletedDataset() {
		mockDao(null);
		ExportResource resource = exportResource();
		assertEquals(404, resource.datasetAsCsv(12, "{}").getStatus());
		assertEquals(404, resource.datasetAsXls(12, "{}").getStatus());
		assertEquals(404, new DataSetResource().downloadDataSetFile("missing", "CSV").getStatus());
	}

	private ExportResource exportResource() {
		UserProfileManager.setProfile(new UserProfile("alice", "tenant"));
		ExportResource resource = new ExportResource();
		resource.request = (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
				new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) -> {
					if ("getLocale".equals(method.getName())) {
						return Locale.ENGLISH;
					}
					throw new AssertionError("Unexpected request call: " + method.getName());
				});
		return resource;
	}

	private void mockDao(IDataSet source) {
		IDataSetDAO dao = (IDataSetDAO) Proxy.newProxyInstance(IDataSetDAO.class.getClassLoader(),
				new Class<?>[] { IDataSetDAO.class }, (proxy, method, args) -> {
					if ("loadDataSetById".equals(method.getName()) || "loadDataSetByLabel".equals(method.getName())) {
						return source;
					}
					if ("setUserProfile".equals(method.getName())) {
						return null;
					}
					throw new AssertionError("Unexpected DAO call: " + method.getName());
				});
		new MockUp<DAOFactory>() {
			@Mock
			public IDataSetDAO getDataSetDAO() {
				return dao;
			}
		};
	}
}
