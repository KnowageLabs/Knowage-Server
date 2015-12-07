package it.eng.spagobi.tools.dataset.bo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.RESTDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader.JSONPathAttribute;
import it.eng.spagobi.utilities.HelperForTest;
import it.eng.spagobi.utilities.exceptions.ConfigurationException;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import junit.framework.TestCase;

public class RESTDataSetTest extends TestCase {

	private SpagoBiDataSet config;
	private String conf;
	private SpagoBiDataSet configParams;
	private String confParams;

	@Override
	protected void setUp() throws Exception {

		UtilitiesForTest.setUpTestJNDI();
		UtilitiesForTest.setUpMasterConfiguration();

		config = new SpagoBiDataSet();
		conf = getConfiguration();
		config.setConfiguration(conf);

		configParams = new SpagoBiDataSet();
		confParams = getConfigurationParams();
		configParams.setConfiguration(confParams);

	}

	public void testRESTDataSet() throws IOException, URISyntaxException {
		RESTDataSet rds = new RESTDataSet(config);
		RESTDataProxy proxy = rds.getDataProxy();
		checkProxy(proxy);
		JSONPathDataReader dataReader = rds.getDataReader();
		checkReader(dataReader);
	}

	public void testRESTDataSetParams() throws IOException, URISyntaxException {
		RESTDataSet rds = new RESTDataSet(configParams);
		rds.setUserProfileAttributes(getUserProfileAttributes());
		rds.setParamsMap(getParamsMap());
		rds.initConf(true);
		RESTDataProxy proxy = rds.getDataProxy();
		checkProxyParams(proxy);
		JSONPathDataReader dataReader = rds.getDataReader();
		checkReaderParams(dataReader);
	}

	private static Map<String, Object> getParamsMap() {
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("address", "addP");
		res.put("entity", "entityP");
		res.put("header_q", "headerP");
		res.put("element", "elP");
		res.put("attribute_path", "attrP");
		res.put("type", "typeP");
		res.put("name", "nameP");
		return res;
	}

	private static Map<String, Object> getUserProfileAttributes() {
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("address", "addU");
		res.put("entity", "entityU");
		res.put("header_q", "headerU");
		res.put("element", "elU");
		res.put("attribute_path", "attrU");
		res.put("type", "typeU");
		res.put("name", "nameU");
		return res;
	}

	public void testRESTDataSetFail() throws IOException, URISyntaxException {
		config.setConfiguration(conf.replace("\"restHttpMethod\": \"post\",", ""));
		boolean done = false;
		try {
			new RESTDataSet(config);
		} catch (ConfigurationException e) {
			done = true;
		}
		assertTrue(done);

	}

	public void testRESTSignatureChange() throws IOException, URISyntaxException {
		String sign1 = new RESTDataSet(config).getSignature();
		config.setConfiguration(conf.replace("q=p", "q=r"));
		String sign2 = new RESTDataSet(config).getSignature();
		assertFalse(sign1.equals(sign2));
	}

	private void checkReader(JSONPathDataReader reader) {
		assertEquals("$.contextResponses[*].contextElement", reader.getJsonPathItems());
		List<JSONPathAttribute> jpas = reader.getJsonPathAttributes();
		assertEquals(5, jpas.size());
		boolean[] done = new boolean[3];
		for (JSONPathAttribute jpa : jpas) {
			if (jpa.getJsonPathType().equals("timestamp yyyy-MM-dd'T'HH:mm:ss.SSSZ")) {
				done[0] = true;
			}
			if (jpa.getJsonPathValue().equals("$.attributes[?(@.name==atTime)].value")) {
				done[1] = true;
			}
			if (jpa.getName().equals("atTime")) {
				done[2] = true;
			}

			assertTrue(HelperForTest.all(done));
		}

		for (int i = 0; i < done.length; i++) {
			assertTrue(Integer.toString(i), done[i]);
		}

		assertTrue(reader.isUseDirectlyAttributes());
	}

	private void checkReaderParams(JSONPathDataReader reader) {
		assertEquals("$.contextResponses[*].elU", reader.getJsonPathItems());
		List<JSONPathAttribute> jpas = reader.getJsonPathAttributes();
		assertEquals(5, jpas.size());
		boolean[] done = new boolean[3];
		for (JSONPathAttribute jpa : jpas) {
			if (jpa.getJsonPathType().equals("typeU")) {
				done[0] = true;
			}
			if (jpa.getJsonPathValue().equals("$.attributes[?(@.name==attrP)].value")) {
				done[1] = true;
			}
			if (jpa.getName().equals("nameP")) {
				done[2] = true;
			}

		}

		for (int i = 0; i < done.length; i++) {
			assertTrue(Integer.toString(i), done[i]);
		}
	}

	private void checkProxy(RESTDataProxy proxy) throws URISyntaxException {
		assertEquals("{\"id\":\"z\"}", proxy.getRequestBody());
		Map<String, String> rh = proxy.getRequestHeaders();
		assertEquals(1, rh.size());
		assertEquals("e", rh.get("d"));
		assertEquals("http://localhost:8090/c?q=p", proxy.getAddress());
		assertEquals(HttpMethod.Post, proxy.getRequestMethod());
		assertEquals("offset1", proxy.getOffsetParam());
		assertEquals("fetch1", proxy.getFetchSizeParam());
		assertEquals("max1", proxy.getMaxResultsParam());

	}

	private void checkProxyParams(RESTDataProxy proxy) throws URISyntaxException {
		assertEquals("{\"id\":\"z\"},{\"entity\":\"entityP\"}", proxy.getRequestBody());
		Map<String, String> rh = proxy.getRequestHeaders();
		assertEquals(2, rh.size());
		assertEquals("e", rh.get("d"));
		assertEquals("headerU", rh.get("q"));
		assertEquals("http://addU:8090/c?q=p", proxy.getAddress());
		assertEquals(HttpMethod.Post, proxy.getRequestMethod());
	}

	public static String getConfiguration() throws IOException {
		return HelperForTest.readFile("restdataset-conf.json", RESTDataSetTest.class);
	}

	private String getConfigurationParams() throws IOException {
		return HelperForTest.readFile("restdataset-params-conf.json", RESTDataSetTest.class);
	}

	public static RESTDataSet getRestDataSet() throws IOException {
		String conf = getConfiguration();
		return getRestDataSet(conf);

	}

	public static RESTDataSet getRestDataSet(String conf) {
		SpagoBiDataSet config = new SpagoBiDataSet();
		config.setConfiguration(conf);
		RESTDataSet dataset = new RESTDataSet(config);
		return dataset;
	}

}
