/**
 *
 */
package it.eng.knowage.export.cockpit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.junit.Test;

import it.eng.knowage.document.cockpit.CockpitDocument;
import it.eng.knowage.document.cockpit.template.widget.ICockpitWidget;
import it.eng.knowage.document.cockpit.template.widget.WidgetReaderFactory;
import it.eng.knowage.document.export.cockpit.converter.ConverterFactory;
import it.eng.knowage.document.export.cockpit.converter.DataStoreConfigurationConverter;
import it.eng.knowage.document.export.cockpit.converter.IJsonConfiguration;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.configuration.FileCreatorConfiguration;
import it.eng.spagobi.commons.dao.DAOConfig;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;

/**
 * @author Dragan Pirkovic
 *
 */
public class DataStoreConfigurationConverterTest {

	private final DataStoreConfigurationConverter converter;

	/**
	 * @throws JSONException
	 *
	 */
	public DataStoreConfigurationConverterTest() throws JSONException {

		System.setProperty("AF_CONFIG_FILE", "master.xml");
		String absolutePath = "C:\\Users\\dpirkovic\\";
		ConfigSingleton.setConfigurationCreation(new FileCreatorConfiguration(absolutePath));
		ConfigSingleton.getRootPath();
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("DataSetDAO", "it.eng.spagobi.tools.dataset.dao.DataSetDAOImpl");
		mappings.put("IDomainDAO", "it.eng.spagobi.tools.news.dao.DomainDAOHibImpl");

		DAOConfig.setMappings(mappings);
		TenantManager.setTenant(new Tenant("kte"));
		DAOConfig.setHibernateConfigurationFile("hibernate.cfg.oracle.xml");
		Map<String, String> documentParams = new HashMap<String, String>();

		documentParams.put("country", "USA");
		documentParams.put("city", "Belgrade");
		documentParams.put("sadsd", "Belgradde");
		CockpitDocument cockpitDocument = new CockpitDocument(documentParams, 123, "document123");
		this.converter = new DataStoreConfigurationConverter(cockpitDocument);
		ICockpitWidget widget = WidgetReaderFactory.getWidget(cockpitDocument.getWidgets().getJSONObject(0));
		IJsonConfiguration configuration = ConverterFactory.getJsonConfigurationConverter(cockpitDocument).convert(widget);
		converter.setJsonConfiguration(configuration);
	}

	/**
	 * Test method for {@link it.eng.knowage.document.export.cockpit.converter.DataStoreConfigurationConverter#getDataset()}.
	 */
	@Test
	public void testGetDataset() {

		assertEquals(this.converter.getDataset().getLabel(), "cockpit_export_csv_1");
	}

	@Test
	public void testGetProjections() {
		assertEquals(this.converter.getProjections().size(), 4);
	}

	@Test
	public void testGetSortings() {
		assertNotNull(this.converter.getSortings());
	}

	@Test
	public void testgetFilter() {
		assertNotNull(this.converter.getFilter());
	}

}
