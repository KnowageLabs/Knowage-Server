package it.eng.spagobi.tools.dataset.utils;

import static org.junit.Assert.assertEquals;
import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersListTest;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.MockDataSet;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DataSetUtilitiesTest {

	private static final String ADMIN_USER = "biadmin";
	public static final String DATA_SET_QUERY_TEST = "Query1";

	@Before
	public void setUp() {
		// setUp for all tests
		UtilitiesForTest.setUpMasterConfiguration();
	}

	/**
	 * Ignore: the application server must be running to run this test.
	 *
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void testGetParamsDefaultValuesUseAlsoService() throws Exception {
		// setUp
		UtilitiesForTest.setUpTestJNDI();
		EnginConf.setTestconfigInputstream(new FileInputStream("resources-test/engine-config.xml"));
		UtilitiesForTest.writeSessionOfWebApp();

		HttpSession session = UtilitiesForTest.getSession();
		MockDataSet dataSet = new MockDataSet();
		dataSet.setLabel(DATA_SET_QUERY_TEST);
		Map<String, ParamDefaultValue> values = DataSetUtilities.getParamsDefaultValuesUseAlsoService(dataSet, ADMIN_USER, session);
		checkQueryDataSetDefaultValues(values);
	}

	@Test
	public void testGetParamsDefaultValues() {
		IDataSet dataSet = new MockDataSet();
		dataSet.setParameters(DataSetParametersListTest.XML);
		Map<String, ParamDefaultValue> pdvs = DataSetUtilities.getParamsDefaultValues(dataSet);
		checkParams(pdvs);
	}

	private static void checkParams(Map<String, ParamDefaultValue> pdvs) {
		assertEquals(2, pdvs.size());
		ParamDefaultValue d = pdvs.get(DataSetParametersListTest.FIRST_PARAM);
		assertEquals(DataSetParametersListTest.FIRST_DEFAULT_VALUE, d.getDefaultValue());
		assertEquals("string", d.getType());
		assertEquals(DataSetParametersListTest.FIRST_PARAM, d.getName());
		d = pdvs.get(DataSetParametersListTest.SECOND_PARAM);
		assertEquals(DataSetParametersListTest.SECOND_DEFAULT_VALUE, d.getDefaultValue());
		assertEquals("number", d.getType());
		assertEquals(DataSetParametersListTest.SECOND_PARAM, d.getName());
	}

	public static void checkQueryDataSetDefaultValues(Map<String, ParamDefaultValue> values) {
		Assert.assertEquals(1, values.size());
		Assert.assertEquals("SBI_DATA_SOURCE", values.get(DataSetParametersListTest.FIRST_PARAM).getDefaultValue());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testFillDefaultValues() {
		IDataSet dataSet = new MockDataSet();
		dataSet.setParameters(DataSetParametersListTest.XML);

		// test params already present
		Map parameters = new HashMap<String, String>();
		parameters.put(DataSetParametersListTest.FIRST_PARAM, "a");
		parameters.put(DataSetParametersListTest.SECOND_PARAM, "b");
		DataSetUtilities.fillDefaultValues(dataSet, parameters);

		assertEquals(2, parameters.size());
		assertEquals("a", parameters.get(DataSetParametersListTest.FIRST_PARAM));
		assertEquals("b", parameters.get(DataSetParametersListTest.SECOND_PARAM));

		// test one param already present
		parameters = new HashMap<String, String>();
		parameters.put(DataSetParametersListTest.FIRST_PARAM, "a");
		DataSetUtilities.fillDefaultValues(dataSet, parameters);

		assertEquals(2, parameters.size());
		assertEquals("a", parameters.get(DataSetParametersListTest.FIRST_PARAM));
		assertEquals(DataSetParametersListTest.SECOND_DEFAULT_VALUE, parameters.get(DataSetParametersListTest.SECOND_PARAM));

		// test no params present
		parameters = new HashMap<String, String>();
		DataSetUtilities.fillDefaultValues(dataSet, parameters);

		assertEquals(2, parameters.size());
		assertEquals("'" + DataSetParametersListTest.FIRST_DEFAULT_VALUE + "'", parameters.get(DataSetParametersListTest.FIRST_PARAM));
		assertEquals(DataSetParametersListTest.SECOND_DEFAULT_VALUE, parameters.get(DataSetParametersListTest.SECOND_PARAM));

		// test one param present with empty
		parameters = new HashMap<String, String>();
		parameters.put(DataSetParametersListTest.FIRST_PARAM, "");
		DataSetUtilities.fillDefaultValues(dataSet, parameters);

		assertEquals(2, parameters.size());
		assertEquals("'" + DataSetParametersListTest.FIRST_DEFAULT_VALUE + "'", parameters.get(DataSetParametersListTest.FIRST_PARAM));
		assertEquals(DataSetParametersListTest.SECOND_DEFAULT_VALUE, parameters.get(DataSetParametersListTest.SECOND_PARAM));

		// test two params present with empty
		parameters = new HashMap<String, String>();
		parameters.put(DataSetParametersListTest.FIRST_PARAM, "");
		parameters.put(DataSetParametersListTest.SECOND_PARAM, "");
		DataSetUtilities.fillDefaultValues(dataSet, parameters);

		assertEquals(2, parameters.size());
		assertEquals("'" + DataSetParametersListTest.FIRST_DEFAULT_VALUE + "'", parameters.get(DataSetParametersListTest.FIRST_PARAM));
		assertEquals(DataSetParametersListTest.SECOND_DEFAULT_VALUE, parameters.get(DataSetParametersListTest.SECOND_PARAM));

		// test two params present with null
		parameters = new HashMap<String, String>();
		parameters.put(DataSetParametersListTest.FIRST_PARAM, null);
		parameters.put(DataSetParametersListTest.SECOND_PARAM, null);
		DataSetUtilities.fillDefaultValues(dataSet, parameters);

		assertEquals(2, parameters.size());
		assertEquals("'" + DataSetParametersListTest.FIRST_DEFAULT_VALUE + "'", parameters.get(DataSetParametersListTest.FIRST_PARAM));
		assertEquals(DataSetParametersListTest.SECOND_DEFAULT_VALUE, parameters.get(DataSetParametersListTest.SECOND_PARAM));

		// test two params present with empty, string with ''
		parameters = new HashMap<String, String>();
		parameters.put(DataSetParametersListTest.FIRST_PARAM, "''"); // string type
		parameters.put(DataSetParametersListTest.SECOND_PARAM, "");
		DataSetUtilities.fillDefaultValues(dataSet, parameters);

		assertEquals(2, parameters.size());
		assertEquals("'" + DataSetParametersListTest.FIRST_DEFAULT_VALUE + "'", parameters.get(DataSetParametersListTest.FIRST_PARAM));
		assertEquals(DataSetParametersListTest.SECOND_DEFAULT_VALUE, parameters.get(DataSetParametersListTest.SECOND_PARAM));

		// testnull
		parameters = null;
		DataSetUtilities.fillDefaultValues(dataSet, parameters);
		Assert.assertNull(parameters);
	}

}
