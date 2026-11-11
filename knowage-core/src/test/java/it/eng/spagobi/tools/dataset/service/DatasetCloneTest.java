package it.eng.spagobi.tools.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.ISbiDataSetDAO;
import it.eng.spagobi.utilities.exceptions.ActionNotPermittedException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DatasetCloneTest {

	@Test
	public void clonesSelfServiceDatasetWithoutReusingIdentityPersistenceOrLosingParameters() throws Exception {
		UserProfile profile = new UserProfile("alice", "tenant");
		profile.setFunctionalities(Collections.singletonList(CommunityFunctionalityConstants.SELF_SERVICE_DATASET_MANAGEMENT));
		VersionedDataSet source = source();
		IDataSet saved = new VersionedDataSet(new JDBCDataSet(), 1, true);
		saved.setId(91);
		int[] checks = new int[2];
		IDataSetDAO dao = (IDataSetDAO) Proxy.newProxyInstance(IDataSetDAO.class.getClassLoader(),
				new Class<?>[] { IDataSetDAO.class }, (proxy, method, args) -> {
					if ("loadDataSetById".equals(method.getName())) {
						assertEquals(91, args[0]);
						return saved;
					}
					if ("loadDataSetByName".equals(method.getName()) || "loadDataSetByLabel".equals(method.getName())) {
						return null;
					}
					throw new AssertionError("Unexpected DAO call: " + method.getName());
				});
		new MockUp<DatasetManagementAPI>() {
			@Mock
			public void canSee(IDataSet dataset) {
				assertSame(source, dataset);
				assertEquals(12, dataset.getId());
				assertEquals("bob", dataset.getOwner());
				checks[0]++;
			}

			@Mock
			public void canSave(IDataSet dataset) {
				assertEquals("USER", dataset.getScopeCd());
				assertEquals("alice", dataset.getOwner());
				checks[1]++;
			}
		};
		new MockUp<ManageDataSetsForREST>() {
			@Mock
			protected String datasetInsert(IDataSet dataset, IDataSetDAO actualDao, Locale locale,
					UserProfile actualProfile, JSONObject json, HttpServletRequest request) throws Exception {
				assertEquals(1, checks[0]);
				assertEquals(1, checks[1]);
				assertSame(dao, actualDao);
				assertEquals(0, dataset.getId());
				assertEquals("CLONE_sales", dataset.getLabel());
				assertEquals("CLONE_Sales", dataset.getName());
				assertNull(dataset.getScopeId());
				assertNull(dataset.getCategoryId());
				assertFalse(dataset.isPersisted());
				assertFalse(dataset.isPersistedHDFS());
				assertFalse(dataset.isScheduled());
				assertEquals("", dataset.getPersistTableName());
				assertEquals("{\"Query\":\"select :year from sales\"}", dataset.getConfiguration());
				assertEquals("<PARAMETERSLIST><ROWS><ROW NAME=\"year\" TYPE=\"STRING\"/></ROWS></PARAMETERSLIST>",
						dataset.getParameters());
				assertEquals("<METADATA/>", dataset.getDsMetadata());
				assertEquals("", json.getString("id"));
				return "{\"id\":91}";
			}
		};

		assertSame(saved, ManageDataSetsForREST.getInstance().cloneDataset(source, dao, Locale.ENGLISH, profile, null));
	}

	@Test
	public void copiesPermanentFileWithoutUsingOrRemovingSameNamedUpload() throws Exception {
		Path root = Paths.get("target", "dataset-clone-" + UUID.randomUUID()).toAbsolutePath();
		Path files = root.resolve("dataset").resolve("files");
		Path uploads = files.resolve("temp");
		Path original = files.resolve("sales.csv");
		Path uploaded = uploads.resolve("sales.csv");
		Path[] copied = new Path[1];
		Files.createDirectories(uploads);
		try {
			Files.write(original, "original".getBytes(StandardCharsets.UTF_8));
			Files.write(uploaded, "unrelated upload".getBytes(StandardCharsets.UTF_8));
			FileDataSet dataset = new FileDataSet();
			dataset.setLabel("sales");
			dataset.setName("Sales");
			dataset.setResourcePath(root.toString());
			dataset.setConfiguration("{\"fileName\":\"sales.csv\",\"fileType\":\"CSV\"}");
			IDataSet saved = new VersionedDataSet(dataset, 1, true);
			IDataSetDAO dao = (IDataSetDAO) Proxy.newProxyInstance(IDataSetDAO.class.getClassLoader(),
					new Class<?>[] { IDataSetDAO.class }, (proxy, method, args) -> {
						if ("loadDataSetById".equals(method.getName())) {
							return saved;
						}
						return null;
					});
			new MockUp<DatasetManagementAPI>() {
				@Mock
				public void canSee(IDataSet source) {
				}

				@Mock
				public void canSave(IDataSet source) {
				}
			};
			new MockUp<ManageDataSetsForREST>() {
				@Mock
				protected String datasetInsert(IDataSet clone, IDataSetDAO actualDao, Locale locale,
						UserProfile profile, JSONObject json, HttpServletRequest request) throws Exception {
					String fileName = new JSONObject(clone.getConfiguration()).getString("fileName");
					assertTrue(fileName.startsWith("CLONE_"));
					assertTrue(fileName.endsWith(".csv"));
					copied[0] = files.resolve(fileName);
					return "{\"id\":91}";
				}
			};

			ManageDataSetsForREST.getInstance().cloneDataset(new VersionedDataSet(dataset, 2, true), dao,
					Locale.ENGLISH, new UserProfile("alice", "tenant"), null);
			assertEquals("original", new String(Files.readAllBytes(copied[0]), StandardCharsets.UTF_8));
			assertEquals("original", new String(Files.readAllBytes(original), StandardCharsets.UTF_8));
			assertEquals("unrelated upload", new String(Files.readAllBytes(uploaded), StandardCharsets.UTF_8));
		} finally {
			if (copied[0] != null) {
				Files.deleteIfExists(copied[0]);
			}
			Files.deleteIfExists(uploaded);
			Files.deleteIfExists(original);
			Files.deleteIfExists(uploads);
			Files.deleteIfExists(files);
			Files.deleteIfExists(root.resolve("dataset"));
			Files.deleteIfExists(root);
		}
	}

	@Test
	public void removesUnreferencedFailedCloneFileAndAllowsRetry() throws Exception {
		assertFailedCloneCanRetry(false, false);
	}

	@Test
	public void retainsReferencedCloneFileWhenInsertionThrowsAfterCommit() throws Exception {
		assertFailedCloneCanRetry(true, false);
	}

	@Test
	public void retainsFileWhenReferenceLookupFailsButAllowsRetryWithNewDestination() throws Exception {
		assertFailedCloneCanRetry(false, true);
	}

	private void assertFailedCloneCanRetry(boolean referenced, boolean referenceLookupFails) throws Exception {
		Path root = Paths.get("target", "dataset-clone-" + UUID.randomUUID()).toAbsolutePath();
		Path files = root.resolve("dataset").resolve("files");
		Path original = files.resolve("sales.csv");
		Path[] copies = new Path[2];
		Files.createDirectories(files);
		try {
			Files.write(original, "original".getBytes(StandardCharsets.UTF_8));
			IDataSet saved = new VersionedDataSet(new JDBCDataSet(), 1, true);
			IDataSetDAO dao = (IDataSetDAO) Proxy.newProxyInstance(IDataSetDAO.class.getClassLoader(),
					new Class<?>[] { IDataSetDAO.class }, (proxy, method, args) -> {
						return "loadDataSetById".equals(method.getName()) ? saved : null;
					});
			ISbiDataSetDAO references = (ISbiDataSetDAO) Proxy.newProxyInstance(ISbiDataSetDAO.class.getClassLoader(),
					new Class<?>[] { ISbiDataSetDAO.class }, (proxy, method, args) -> {
						assertEquals("isFileNameReferenced", method.getName());
						assertEquals(copies[0].getFileName().toString(), args[0]);
						assertEquals("tenant", args[1]);
						if (referenceLookupFails) {
							throw new SpagoBIRuntimeException("Reference lookup failed");
						}
						return referenced;
					});
			new MockUp<DAOFactory>() {
				@Mock
				public ISbiDataSetDAO getSbiDataSetDAO() {
					return references;
				}
			};
			new MockUp<DatasetManagementAPI>() {
				@Mock
				public void canSee(IDataSet source) {
				}

				@Mock
				public void canSave(IDataSet source) {
				}
			};
			int[] attempts = new int[1];
			new MockUp<ManageDataSetsForREST>() {
				@Mock
				protected String datasetInsert(IDataSet clone, IDataSetDAO actualDao, Locale locale,
						UserProfile profile, JSONObject json, HttpServletRequest request) throws Exception {
					int attempt = attempts[0]++;
					copies[attempt] = files.resolve(new JSONObject(clone.getConfiguration()).getString("fileName"));
					if (attempt == 0) {
						throw new SpagoBIRuntimeException("Insertion failed");
					}
					return "{\"id\":91}";
				}
			};

			UserProfile profile = new UserProfile("alice", "tenant");
			try {
				ManageDataSetsForREST.getInstance().cloneDataset(fileSource(root), dao, Locale.ENGLISH, profile, null);
				fail("Expected insertion failure");
			} catch (SpagoBIRuntimeException expected) {
				assertEquals("Insertion failed", expected.getMessage());
			}
			assertEquals(referenced || referenceLookupFails, Files.exists(copies[0]));
			assertSame(saved, ManageDataSetsForREST.getInstance().cloneDataset(fileSource(root), dao, Locale.ENGLISH, profile, null));
			assertFalse(copies[0].equals(copies[1]));
			assertEquals("original", new String(Files.readAllBytes(copies[1]), StandardCharsets.UTF_8));
			assertEquals("original", new String(Files.readAllBytes(original), StandardCharsets.UTF_8));
			if (referenced || referenceLookupFails) {
				assertEquals("original", new String(Files.readAllBytes(copies[0]), StandardCharsets.UTF_8));
			}
		} finally {
			for (Path copy : copies) {
				if (copy != null) {
					Files.deleteIfExists(copy);
				}
			}
			Files.deleteIfExists(original);
			Files.deleteIfExists(files);
			Files.deleteIfExists(root.resolve("dataset"));
			Files.deleteIfExists(root);
		}
	}

	private IDataSet fileSource(Path root) {
		FileDataSet dataset = new FileDataSet();
		dataset.setLabel("sales");
		dataset.setName("Sales");
		dataset.setResourcePath(root.toString());
		dataset.setConfiguration("{\"fileName\":\"sales.csv\",\"fileType\":\"CSV\"}");
		return new VersionedDataSet(dataset, 2, true);
	}

	@Test
	public void rejectsInvisibleSourceBeforeChangingOrPersistingIt() throws Exception {
		VersionedDataSet source = source();
		new MockUp<DatasetManagementAPI>() {
			@Mock
			public void canSee(IDataSet dataset) throws ActionNotPermittedException {
				throw new ActionNotPermittedException("Not visible", "dataset.not.visible");
			}
		};
		try {
			ManageDataSetsForREST.getInstance().cloneDataset(source, null, Locale.ENGLISH,
					new UserProfile("alice", "tenant"), null);
			fail("Expected source visibility check");
		} catch (ActionNotPermittedException expected) {
			assertEquals(12, source.getId());
			assertEquals("sales", source.getLabel());
			assertEquals("bob", source.getOwner());
			assertTrue(source.isPersisted());
		}
	}

	private VersionedDataSet source() {
		JDBCDataSet dataset = new JDBCDataSet();
		dataset.setId(12);
		dataset.setLabel("sales");
		dataset.setName("Sales");
		dataset.setOwner("bob");
		dataset.setScopeCd("ENTERPRISE");
		dataset.setScopeId(2);
		dataset.setCategoryId(4);
		dataset.setPersisted(true);
		dataset.setPersistedHDFS(true);
		dataset.setScheduled(true);
		dataset.setPersistTableName("SOURCE_TABLE");
		dataset.setConfiguration("{\"Query\":\"select :year from sales\"}");
		dataset.setParameters("<PARAMETERSLIST><ROWS><ROW NAME=\"year\" TYPE=\"STRING\"/></ROWS></PARAMETERSLIST>");
		dataset.setDsMetadata("<METADATA/>");
		return new VersionedDataSet(dataset, 7, true);
	}
}
