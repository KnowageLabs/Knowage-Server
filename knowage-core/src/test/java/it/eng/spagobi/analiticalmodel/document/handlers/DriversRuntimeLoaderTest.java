package it.eng.spagobi.analiticalmodel.document.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.DriversRuntimeLoader.DatasetMetadata;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIMetaModelParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.dataset.bo.BIObjDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IBIObjDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DriversRuntimeLoaderTest {

	@Test
	public void doesNotLoadInheritedDriversWhenTheDocumentHasItsOwn() throws Exception {
		List<BIObjectParameter> drivers = Collections.singletonList(new BIObjectParameter());
		int[] datasetLookups = { 0 };
		prepareDaos(drivers, datasetLookups);

		BIObject document = new DriversRuntimeLoader().loadBIObjectForExecutionByLabelAndRole("SALES", "analyst");

		assertSame(drivers, document.getDrivers());
		assertEquals(0, datasetLookups[0]);
	}

	@Test
	public void checksForInheritedDriversOnlyOnceWhenNoneAreAvailable() throws Exception {
		int[] datasetLookups = { 0 };
		prepareDaos(Collections.emptyList(), datasetLookups);

		new DriversRuntimeLoader().loadBIObjectForExecutionByLabelAndRole("SALES", "analyst");

		assertEquals(1, datasetLookups[0]);
	}

	@Test
	public void loadsInheritedMetadataOnceAndKeepsTheRoleSpecificParameter() throws Exception {
		int[] loads = new int[5];
		BIObject document = new BIObject();
		document.setId(12);
		BIObjDataSet link = new BIObjDataSet();
		link.setDataSetId(42);
		MetaModel model = new MetaModel();
		model.setId(5);
		Parameter parameter = new Parameter();
		parameter.setId(7);
		parameter.setType("STRING");
		BIMetaModelParameter inherited = new BIMetaModelParameter();
		inherited.setParameter(parameter);
		inherited.setParID(7);
		inherited.setParameterUrlName("country");
		Parameter authorizedParameter = new Parameter();
		authorizedParameter.setLabel("COUNTRY");

		IBIObjectDAO documents = dao(IBIObjectDAO.class, (proxy, method, args) -> {
			if ("loadBIObjectByLabel".equals(method.getName())) {
				return document;
			}
			assertEquals("loadDocumentDrivers", method.getName());
			assertEquals("analyst", args[1]);
			return Collections.emptyList();
		});
		IBIObjDataSetDAO links = dao(IBIObjDataSetDAO.class, (proxy, method, args) -> {
			assertEquals("getBiObjDataSets", method.getName());
			loads[0]++;
			return Collections.singletonList(link);
		});
		IDataSet dataset = dao(IDataSet.class, (proxy, method, args) -> {
			if ("getDsType".equals(method.getName())) {
				return "SbiQbeDataSet";
			}
			assertEquals("getConfiguration", method.getName());
			return "{\"qbeDatamarts\":\"MODEL\"}";
		});
		IDataSetDAO datasets = dao(IDataSetDAO.class, (proxy, method, args) -> {
			assertEquals("loadDataSetById", method.getName());
			loads[1]++;
			return dataset;
		});
		IMetaModelsDAO models = dao(IMetaModelsDAO.class, (proxy, method, args) -> {
			assertEquals("loadMetaModelByName", method.getName());
			loads[2]++;
			return model;
		});
		IBIMetaModelParameterDAO drivers = dao(IBIMetaModelParameterDAO.class, (proxy, method, args) -> {
			assertEquals("loadBIMetaModelParameterByMetaModelId", method.getName());
			loads[3]++;
			return Collections.singletonList(inherited);
		});
		IParameterDAO parameters = dao(IParameterDAO.class, (proxy, method, args) -> {
			assertEquals("loadForExecutionByParameterIDandRoleName", method.getName());
			assertEquals(Integer.valueOf(7), args[0]);
			assertEquals("analyst", args[1]);
			loads[4]++;
			return authorizedParameter;
		});
		new MockUp<DAOFactory>() {
			@Mock
			public IBIObjectDAO getBIObjectDAO() {
				return documents;
			}

			@Mock
			public IBIObjDataSetDAO getBIObjDataSetDAO() {
				return links;
			}

			@Mock
			public IDataSetDAO getDataSetDAO() {
				return datasets;
			}

			@Mock
			public IMetaModelsDAO getMetaModelsDAO() {
				return models;
			}

			@Mock
			public IBIMetaModelParameterDAO getBIMetaModelParameterDAO() {
				return drivers;
			}

			@Mock
			public IParameterDAO getParameterDAO() {
				return parameters;
			}
		};
		DatasetMetadata metadata = new DatasetMetadata();
		new DriversRuntimeLoader().loadBIObjectForExecutionByLabelAndRole("SALES", "analyst", metadata);
		metadata.getDocumentDatasets(12);
		metadata.getDataset(42);

		for (int loadCount : loads) {
			assertEquals(1, loadCount);
		}
		assertSame(authorizedParameter, document.getDrivers().get(0).getParameter());
		assertEquals("country", document.getDrivers().get(0).getParameterUrlName());

		new DatasetMetadata().getDataset(42);
		assertEquals(2, loads[1]);
	}

	private <T> T dao(Class<T> type, InvocationHandler handler) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler));
	}

	private void prepareDaos(List<BIObjectParameter> drivers, int[] datasetLookups) {
		BIObject document = new BIObject();
		document.setId(12);
		document.setLabel("SALES");
		IBIObjectDAO documents = (IBIObjectDAO) Proxy.newProxyInstance(IBIObjectDAO.class.getClassLoader(),
				new Class<?>[] { IBIObjectDAO.class }, (proxy, method, args) -> {
					if ("loadBIObjectByLabel".equals(method.getName())) {
						assertEquals("SALES", args[0]);
						return document;
					}
					if ("loadDocumentDrivers".equals(method.getName())) {
						assertSame(document, args[0]);
						assertEquals("analyst", args[1]);
						return drivers;
					}
					throw new AssertionError("Unexpected document DAO call: " + method.getName());
				});
		IBIObjDataSetDAO datasets = (IBIObjDataSetDAO) Proxy.newProxyInstance(IBIObjDataSetDAO.class.getClassLoader(),
				new Class<?>[] { IBIObjDataSetDAO.class }, (proxy, method, args) -> {
					if ("getBiObjDataSets".equals(method.getName())) {
						datasetLookups[0]++;
						return Collections.emptyList();
					}
					throw new AssertionError("Unexpected dataset DAO call: " + method.getName());
				});
		new MockUp<DAOFactory>() {
			@Mock
			public IBIObjectDAO getBIObjectDAO() {
				return documents;
			}

			@Mock
			public IBIObjDataSetDAO getBIObjDataSetDAO() {
				return datasets;
			}

			@Mock
			public IBIMetaModelParameterDAO getBIMetaModelParameterDAO() {
				return null;
			}

			@Mock
			public IMetaModelsDAO getMetaModelsDAO() {
				return null;
			}

			@Mock
			public IDataSetDAO getDataSetDAO() {
				return null;
			}
		};
	}
}
