package it.eng.spagobi.api.v2;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentRuntime;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DocumentExecutionFiltersTest {

	@Test
	public void skipsSessionLookupsWithoutLosingCrossNavigationValues() throws Exception {
		new MockUp<DAOFactory>() {
			@Mock
			public IParameterUseDAO getParameterUseDAO() {
				throw new AssertionError("No modality lookup is needed without session values");
			}
		};
		BIObjectParameter country = driver("country");
		country.setParameterValues(Collections.singletonList("IT"));
		BIObjectParameter city = driver("city");
		BIObject document = new BIObject();
		document.setDrivers(Arrays.asList(country, city));
		List<String> crossParameters = new ArrayList<>();
		JSONObject values = new JSONObject().put("city", "Rome")
				.put("city_field_visible_description", "Roma");

		applyRequestParameters(document, values, Collections.emptyMap(), crossParameters);

		assertEquals(Collections.singletonList("IT"), country.getParameterValues());
		assertEquals(Collections.singletonList("Rome"), city.getParameterValues());
		assertEquals(Collections.singletonList("Roma"), city.getParameterValuesDescription());
		assertEquals(Collections.singletonList("city"), crossParameters);
	}

	@Test
	public void stillResolvesTheRequestedRoleWhenSessionValuesArePresent() throws Exception {
		int[] lookups = { 0 };
		IParameterUseDAO uses = (IParameterUseDAO) Proxy.newProxyInstance(IParameterUseDAO.class.getClassLoader(),
				new Class<?>[] { IParameterUseDAO.class }, (proxy, method, args) -> {
					assertEquals("loadByParameterIdandRole", method.getName());
					assertEquals(Integer.valueOf(7), args[0]);
					assertEquals("analyst", args[1]);
					lookups[0]++;
					ParameterUse modality = new ParameterUse();
					modality.setLabel("COUNTRY_LOV");
					return modality;
				});
		new MockUp<DAOFactory>() {
			@Mock
			public IParameterUseDAO getParameterUseDAO() {
				return uses;
			}
		};
		BIObject document = new BIObject();
		document.setDrivers(Collections.singletonList(driver("country")));
		Map<String, JSONObject> sessionValues = Collections.singletonMap("OTHER_LOV", new JSONObject());

		applyRequestParameters(document, new JSONObject(), sessionValues, new ArrayList<>());

		assertEquals(1, lookups[0]);
	}

	@Test
	public void admissibilityPreservesWildcardsDuplicatesAndValueTypes() throws Exception {
		List<String> values = new ArrayList<>(Arrays.asList("A", "invalid", "$", "A", "7", null));
		ArrayList<HashMap<String, Object>> admissible = new ArrayList<>();
		for (Object value : Arrays.asList("A", 7, JSONObject.NULL)) {
			HashMap<String, Object> row = new HashMap<>();
			row.put("value", value);
			admissible.add(row);
		}
		admissible.add(new HashMap<>());
		Method method = DocumentExecutionResource.class.getDeclaredMethod("checkIfValuesAreAdmissible",
				Object.class, ArrayList.class);
		method.setAccessible(true);

		method.invoke(new DocumentExecutionResource(), values, admissible);

		assertEquals(Arrays.asList("A", "$", "A", null), values);
	}

	private BIObjectParameter driver(String urlName) {
		ModalitiesValue lov = new ModalitiesValue();
		lov.setITypeCd("QUERY");
		Parameter parameter = new Parameter();
		parameter.setLabel("COUNTRY");
		parameter.setModalityValue(lov);
		BIObjectParameter driver = new BIObjectParameter();
		driver.setParID(7);
		driver.setParameterUrlName(urlName);
		driver.setParameter(parameter);
		return driver;
	}

	private void applyRequestParameters(BIObject document, JSONObject values,
			Map<String, JSONObject> sessionValues, List<String> crossParameters) throws Exception {
		DocumentExecutionResource resource = new DocumentExecutionResource() {
			@Override
			public UserProfile getUserProfile() {
				return new UserProfile("alice", "tenant");
			}
		};
		Method method = DocumentExecutionResource.class.getDeclaredMethod("applyRequestParameters", BIObject.class,
				JSONObject.class, Map.class, String.class, List.class, DocumentRuntime.class);
		method.setAccessible(true);
		method.invoke(resource, document, values, sessionValues, "analyst", crossParameters,
				new DocumentRuntime(resource.getUserProfile(), Locale.ENGLISH));
	}
}
