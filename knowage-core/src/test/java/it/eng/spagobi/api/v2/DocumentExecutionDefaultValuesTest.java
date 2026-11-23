package it.eng.spagobi.api.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.AbstractBIResourceRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.DriversRuntimeLoader;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesRetriever;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.catalogue.metadata.IDrivableBIResource;
import it.eng.spagobi.utilities.rest.RestUtilities;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DocumentExecutionDefaultValuesTest {

	@Test
	public void evaluatesDefaultsWithCurrentDriverValuesRoleAndLocale() throws Exception {
		BIObjectParameter year = driver("year");
		year.setParameterValues(Collections.singletonList("2026"));
		BIObjectParameter comparison = driver("comparison_year");
		BIObject document = new BIObject();
		document.setDrivers(Arrays.asList(year, comparison));
		JSONObject selectedYear = new JSONObject().put("urlName", "year").put("value", 2025).put("description", "2025");
		JSONObject body = body(new JSONArray().put(selectedYear));
		HttpServletRequest request = request();
		DocumentExecutionParametersResource resource = mockExecution(body, document, comparison, object -> {
			int comparisonYear = Integer.parseInt(year.getParameterValues().get(0).toString()) - 1;
			LovValue value = new LovValue();
			value.setValue(comparisonYear);
			value.setDescription("Comparison " + comparisonYear);
			DefaultValuesList values = new DefaultValuesList();
			values.add(value);
			return values;
		});

		JSONObject first = new JSONObject(resource.getDefaultValues(request));
		assertEquals(2024, first.getJSONArray("defaultValues").getJSONObject(0).getInt("value"));
		assertEquals("Comparison 2024", first.getJSONArray("defaultValues").getJSONObject(0).getString("description"));

		selectedYear.put("value", 2024).put("description", "2024");
		JSONObject second = new JSONObject(resource.getDefaultValues(request));
		assertEquals(2023, second.getJSONArray("defaultValues").getJSONObject(0).getInt("value"));
	}

	@Test
	public void returnsAnEmptyListWhenTheDefaultLovHasNoValues() throws Exception {
		BIObjectParameter comparison = driver("comparison_year");
		BIObject document = new BIObject();
		document.setDrivers(Collections.singletonList(comparison));
		DocumentExecutionParametersResource resource = mockExecution(body(new JSONArray()), document, comparison,
				object -> new DefaultValuesList());

		JSONObject result = new JSONObject(resource.getDefaultValues(request()));

		assertEquals(0, result.getJSONArray("defaultValues").length());
	}

	private DocumentExecutionParametersResource mockExecution(JSONObject body, BIObject document,
			BIObjectParameter comparison, Function<BIObject, DefaultValuesList> evaluate) {
		UserProfile profile = new UserProfile("reader", "tenant");
		new MockUp<RestUtilities>() {
			@Mock
			public JSONObject readBodyAsJSONObject(HttpServletRequest request) {
				return body;
			}
		};
		new MockUp<DriversRuntimeLoader>() {
			@Mock
			public BIObject loadBIObjectForExecutionByLabelAndRole(String label, String role) {
				assertEquals("Anno_default", label);
				assertEquals("analyst", role);
				return document;
			}
		};
		new MockUp<DefaultValuesRetriever>() {
			@Mock
			public DefaultValuesList getDefaultValuesDum(AbstractDriver driver, IDrivableBIResource object,
					IEngUserProfile userProfile, Locale locale, String role, AbstractBIResourceRuntime runtime) {
				assertSame(comparison, driver);
				assertSame(document, object);
				assertSame(profile, userProfile);
				assertEquals(Locale.ITALIAN, locale);
				assertEquals("analyst", role);
				return evaluate.apply((BIObject) object);
			}
		};
		return new DocumentExecutionParametersResource() {
			@Override
			public UserProfile getUserProfile() {
				return profile;
			}
		};
	}

	private JSONObject body(JSONArray parameters) throws Exception {
		return new JSONObject().put("label", "Anno_default").put("role", "analyst")
				.put("paramId", "comparison_year").put("parameters", parameters);
	}

	private BIObjectParameter driver(String urlName) {
		Parameter parameter = new Parameter();
		parameter.setLabel(urlName);
		parameter.setType("NUM");
		BIObjectParameter driver = new BIObjectParameter();
		driver.setParameter(parameter);
		driver.setParameterUrlName(urlName);
		return driver;
	}

	private HttpServletRequest request() {
		return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
				new Class<?>[] { HttpServletRequest.class },
				(proxy, method, args) -> "getLocale".equals(method.getName()) ? Locale.ITALIAN : null);
	}
}
