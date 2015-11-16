package it.eng.spagobi.commons.utilities;

import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.tools.dataset.bo.DataSetParametersListTest;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilitiesTest;
import it.eng.spagobi.tools.dataset.utils.ParamDefaultValue;
import it.eng.spagobi.utilities.MockDataSet;

import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GeneralUtilitiesTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
		UtilitiesForTest.setUpMasterConfiguration();
		UtilitiesDAOForTest.setUpDatabaseTestJNDI();
	}

	@Test
	public void testGetParamsDefaultValuesEmptyNoDSLabel() {
		IDataSet dummy = new MockDataSet();
		Map<String, ?> dvs = GeneralUtilities.getParamsDefaultValuesUseAlsoDB(dummy);
		Assert.assertNull(dvs);
	}

	@Test
	public void testGetParamsDefaultValuesParamsStringNoDSLabel() {
		IDataSet dummy = new MockDataSet();
		dummy.setParameters(DataSetParametersListTest.XML);
		Map<String, ?> dvs = GeneralUtilities.getParamsDefaultValuesUseAlsoDB(dummy);
		Assert.assertEquals(2, dvs.size());
		Assert.assertEquals(DataSetParametersListTest.FIRST_DEFAULT_VALUE, dvs.get(DataSetParametersListTest.FIRST_PARAM));
		Assert.assertEquals(DataSetParametersListTest.SECOND_DEFAULT_VALUE, dvs.get(DataSetParametersListTest.SECOND_PARAM));
	}

	@Test
	public void testGetParamsDefaultValuesWithDSLabelFromDB() {
		IDataSet dummy = new MockDataSet();
		dummy.setLabel(DataSetUtilitiesTest.DATA_SET_QUERY_TEST);
		Map<String, ParamDefaultValue> dvs = GeneralUtilities.getParamsDefaultValuesUseAlsoDB(dummy);
		DataSetUtilitiesTest.checkQueryDataSetDefaultValues(dvs);
	}

	@Test
	public void testGetParamsDefaultValuesEmptyDSAndNoDB() {
		IDataSet dummy = new MockDataSet();
		dummy.setLabel("doesntExist18");
		Map<String, ?> dvs = GeneralUtilities.getParamsDefaultValuesUseAlsoDB(dummy);
		Assert.assertNull(dvs);
	}

}
