package it.eng.spagobi.analiticalmodel.document.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.user.UserProfileManager;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DriverRuntimeMetadataTest {

	@Test
	public void loadsLovMetadataWithoutReloadingTheResolvedModality() {
		new MockUp<DAOFactory>() {
			@Mock
			public IParameterUseDAO getParameterUseDAO() {
				throw new AssertionError("The role-specific modality has already been loaded");
			}
		};
		ILovDetail lov = (ILovDetail) Proxy.newProxyInstance(ILovDetail.class.getClassLoader(),
				new Class<?>[] { ILovDetail.class }, (proxy, method, args) -> {
					switch (method.getName()) {
					case "getVisibleColumnNames":
						return Arrays.asList("CODE", "DESCRIPTION");
					case "getInvisibleColumnNames":
						return Collections.emptyList();
					case "getValueColumnName":
						return "CODE";
					case "getDescriptionColumnName":
						return "DESCRIPTION";
					default:
						throw new AssertionError("Unexpected LOV call: " + method.getName());
					}
				});
		DocumentRuntime documentRuntime = new DocumentRuntime(null, Locale.ENGLISH) {
			@Override
			public ILovDetail getLovDetail(AbstractDriver driver) {
				return lov;
			}
		};
		AbstractDriverRuntime<BIObjectParameter> runtime = runtime();
		ParameterUse modality = new ParameterUse();
		modality.setValueSelection("lov");
		runtime.analyticalDriverExecModality = modality;
		runtime.selectionType = "LOOKUP";

		runtime.loadAdmissibleValues(new BIObjectParameter(), documentRuntime);

		assertEquals("CODE", runtime.getLovValueColumnName());
		assertEquals("DESCRIPTION", runtime.getLovDescriptionColumnName());
		assertEquals("CODE", runtime.getColPlaceholder2ColName().get("_col0"));
		assertEquals("DESCRIPTION", runtime.getColPlaceholder2ColName().get("_col1"));
		assertNull(runtime.getAdmissibleValues());
	}

	@Test
	public void resolvesLovDependenciesUsingTheLoadedDriverLabels() {
		new MockUp<UserProfileManager>() {
			@Mock
			public UserProfile getProfile() {
				return new UserProfile("alice", "tenant");
			}
		};
		ILovDetail lov = (ILovDetail) Proxy.newProxyInstance(ILovDetail.class.getClassLoader(),
				new Class<?>[] { ILovDetail.class }, (proxy, method, args) -> {
					if ("getParameterNames".equals(method.getName())) {
						return Collections.singleton("COUNTRY");
					}
					throw new AssertionError("Unexpected LOV call: " + method.getName());
				});
		new MockUp<AbstractBIResourceRuntime>() {
			@Mock
			public ILovDetail getLovDetail(AbstractDriver driver) {
				return lov;
			}
		};
		AbstractDriverRuntime<BIObjectParameter> runtime = runtime();
		runtime.locale = Locale.ENGLISH;
		runtime.initLovDependencies(driver("CITY", "city"), Arrays.asList(
				driver("COUNTRY", "country"), driver("COUNTRY", "other_country"), driver("YEAR", "year")));

		assertEquals(Arrays.asList("country", "other_country"), runtime.getLovDependencies());
	}

	@Test
	public void resolvesDefaultLovDependenciesSeparatelyFromAdmissibleValues() {
		new MockUp<UserProfileManager>() {
			@Mock
			public UserProfile getProfile() {
				return new UserProfile("alice", "tenant");
			}
		};
		ILovDetail[] defaultLov = { (ILovDetail) Proxy.newProxyInstance(ILovDetail.class.getClassLoader(),
				new Class<?>[] { ILovDetail.class }, (proxy, method, args) -> {
					if ("getParameterNames".equals(method.getName())) {
						return Collections.singleton("YEAR");
					}
					throw new AssertionError("Unexpected LOV call: " + method.getName());
				}) };
		new MockUp<AbstractBIResourceRuntime>() {
			@Mock
			public ILovDetail getLovDetailForDefault(AbstractDriver driver) {
				return defaultLov[0];
			}
		};
		BIObjectParameter comparison = driver("COMPARISON_YEAR", "comparison_year");
		List<BIObjectParameter> drivers = Arrays.asList(
				driver("YEAR", "year"), driver("YEAR", "other_year"), driver("COUNTRY", "country"));
		AbstractDriverRuntime<BIObjectParameter> runtime = runtime();
		runtime.initDefaultValueDependencies(comparison, drivers);

		assertEquals(Arrays.asList("year", "other_year"), runtime.getDefaultValueDependencies());
		assertNull(runtime.getLovDependencies());

		AbstractDriverRuntime<BIObjectParameter> selfReference = runtime();
		selfReference.initDefaultValueDependencies(drivers.get(0), drivers);
		assertEquals(Collections.singletonList("other_year"), selfReference.getDefaultValueDependencies());

		defaultLov[0] = null;
		AbstractDriverRuntime<BIObjectParameter> withoutDefaultLov = runtime();
		withoutDefaultLov.initDefaultValueDependencies(comparison, drivers);
		assertTrue(withoutDefaultLov.getDefaultValueDependencies().isEmpty());
	}

	@Test
	public void reusesDependenciesWithoutSharingRoleOrRequestMetadata() throws Exception {
		int[] modalityLookups = { 0 };
		int[] dependencyLookups = { 0 };
		IParameterUseDAO uses = (IParameterUseDAO) Proxy.newProxyInstance(IParameterUseDAO.class.getClassLoader(),
				new Class<?>[] { IParameterUseDAO.class }, (proxy, method, args) -> {
					assertEquals("loadByParameterIdandRole", method.getName());
					assertEquals(Integer.valueOf(7), args[0]);
					modalityLookups[0]++;
					ParameterUse use = new ParameterUse();
					use.setUseID("analyst".equals(args[1]) ? 11 : 12);
					return use;
				});
		IObjParuseDAO dependencies = (IObjParuseDAO) Proxy.newProxyInstance(IObjParuseDAO.class.getClassLoader(),
				new Class<?>[] { IObjParuseDAO.class }, (proxy, method, args) -> {
					assertEquals("loadObjParuse", method.getName());
					assertEquals(Integer.valueOf(9), args[0]);
					dependencyLookups[0]++;
					return Collections.emptyList();
				});
		new MockUp<DAOFactory>() {
			@Mock
			public IParameterUseDAO getParameterUseDAO() {
				return uses;
			}

			@Mock
			public IObjParuseDAO getObjParuseDAO() {
				return dependencies;
			}
		};
		BIObjectParameter driver = driver("CITY", "city");
		driver.setParID(7);
		driver.setId(9);
		DocumentRuntime context = new DocumentRuntime(null, Locale.ENGLISH);
		context.getParameterUse(driver, "analyst");
		context.getDependencies(driver, "analyst");
		context.getDependencies(driver, "analyst");
		context.getDependencies(driver, "admin");
		context.getDependencies(driver, "admin");
		assertEquals(2, modalityLookups[0]);
		assertEquals(2, dependencyLookups[0]);

		new DocumentRuntime(null, Locale.ENGLISH).getDependencies(driver, "analyst");
		context.setUserProfile(new UserProfile("bob", "tenant"));
		context.getDependencies(driver, "analyst");
		assertEquals(4, modalityLookups[0]);
		assertEquals(4, dependencyLookups[0]);
	}

	@Test
	public void doesNotResolveAmbiguousSyntheticIdsAsDependencyParents() {
		BIObjectParameter first = driver("COUNTRY", "country");
		first.setId(7);
		BIObjectParameter second = driver("CITY", "city");
		second.setId(8);
		DocumentRuntime context = new DocumentRuntime(null, Locale.ENGLISH);
		context.registerDrivers(Arrays.asList(first, second));
		assertSame(first, context.getDriverById(7));

		second.setId(7);
		BIObjectParameter third = driver("YEAR", "year");
		third.setId(7);
		context.registerDrivers(Arrays.asList(first, second, third));
		assertNull(context.getDriverById(7));
	}

	@Test
	public void reusesQueryDefinitionsButNotStatefulLovProviders() {
		QueryDetail query = new QueryDetail();
		ILovDetail dynamic = (ILovDetail) Proxy.newProxyInstance(ILovDetail.class.getClassLoader(),
				new Class<?>[] { ILovDetail.class }, (proxy, method, args) -> {
					throw new AssertionError("Only LOV metadata should be parsed");
				});
		int[] parses = { 0 };
		new MockUp<LovDetailFactory>() {
			@Mock
			public ILovDetail getLovFromXML(String definition) {
				parses[0]++;
				return "query".equals(definition) ? query : dynamic;
			}
		};
		BIObjectParameter driver = driver("CITY", "city");
		ModalitiesValue lov = new ModalitiesValue();
		lov.setLovProvider("query");
		driver.getParameter().setModalityValue(lov);
		driver.getParameter().setModalityValueForDefault(lov);
		driver.getParameter().setModalityValueForMin(lov);
		driver.getParameter().setModalityValueForMax(lov);
		DocumentRuntime context = new DocumentRuntime(null, Locale.ENGLISH);

		assertSame(query, context.getLovDetail(driver));
		assertSame(query, context.getLovDetailForDefault(driver));
		assertSame(query, context.getLovDetailForMin(driver));
		assertSame(query, context.getLovDetailForMax(driver));
		assertEquals(1, parses[0]);

		lov.setLovProvider("dynamic");
		assertSame(dynamic, context.getLovDetail(driver));
		assertSame(dynamic, context.getLovDetail(driver));
		assertEquals(3, parses[0]);
	}

	@Test
	public void deduplicatesLovRowsWithoutCollapsingDistinctLabelsOrDescriptions() throws Exception {
		AbstractDriverRuntime<BIObjectParameter> runtime = lovRuntime();
		JSONArray rows = new JSONArray()
				.put(lovRow("IT", "Italy", "First").put("EXTRA", "one"))
				.put(lovRow("IT", "Italy", "First").put("EXTRA", "two"))
				.put(lovRow("IT", "Italia", "First"))
				.put(lovRow("IT", "Italy", "Second"));

		runtime.appendAdmissibleValues(rows, true);

		assertEquals(3, runtime.getAdmissibleValues().size());
		assertEquals("First", runtime.getAdmissibleValues().get(0).get("description"));
		assertEquals("Italia", runtime.getAdmissibleValues().get(1).get("label"));
		assertEquals("Second", runtime.getAdmissibleValues().get(2).get("description"));
	}

	@Test
	public void preservesSparseRowsJsonNullAndLookupDuplicates() throws Exception {
		AbstractDriverRuntime<BIObjectParameter> runtime = lovRuntime();
		JSONArray rows = new JSONArray()
				.put(new JSONObject().put("value", "A"))
				.put(lovRow("A", "A", "A"))
				.put(new JSONObject().put("value", "FR").put("description", "France"))
				.put(new JSONObject().put("value", "FR").put("description", "France"))
				.put(new JSONObject().put("value", "missing_description"))
				.put(lovRow(JSONObject.NULL, "null", JSONObject.NULL))
				.put(lovRow(JSONObject.NULL, "null", JSONObject.NULL));

		runtime.appendAdmissibleValues(rows, true);

		assertEquals(4, runtime.getAdmissibleValues().size());
		assertEquals("A", runtime.getAdmissibleValues().get(0).get("description"));
		assertSame(JSONObject.NULL, runtime.getAdmissibleValues().get(3).get("value"));

		runtime = lovRuntime();
		runtime.appendAdmissibleValues(new JSONArray().put(lovRow("A", "A", "A"))
				.put(lovRow("A", "A", "A")), false);
		assertEquals(2, runtime.getAdmissibleValues().size());
	}

	@Test
	public void crossDescriptionsKeepTheFirstMatchAndSelectedValueOrder() {
		AbstractDriverRuntime<BIObjectParameter> runtime = lovRuntime();
		runtime.admissibleValues.add(mappedRow("A", "First"));
		runtime.admissibleValues.add(mappedRow("A", "Second"));
		runtime.admissibleValues.add(mappedRow("B", JSONObject.NULL));
		runtime.admissibleValues.add(mappedRow(7, "Numeric"));
		runtime.admissibleValues.add(mappedRow("C", 42));

		assertEquals(Arrays.asList("B", "First", "First", "C"),
				runtime.getCrossDescriptions(Arrays.asList("B", "A", "A", "unknown", 7, null, "C")));
	}

	@Test
	public void validationDescriptionMergeKeepsFirstMatchesAndDefaultDescriptions() throws Exception {
		BIObjectParameter driver = driver("CITY", "city");
		driver.setParameterValues(Arrays.asList("B", "A", "A", "D"));
		BIObjectParameter validated = driver("CITY", "city");
		validated.setParameterValues(Arrays.asList("A", "B", "A"));
		validated.setParameterValuesDescription(Arrays.asList("First A", "Description B", "Second A"));
		DefaultValuesList defaults = new DefaultValuesList();
		LovValue defaultValue = new LovValue();
		defaultValue.setValue("D");
		defaultValue.setDescription("Default D");
		defaults.add(defaultValue);
		Method merge = DriversValidationAPI.class.getDeclaredMethod("mergeDescriptions",
				AbstractDriver.class, DefaultValuesList.class, AbstractDriver.class);
		merge.setAccessible(true);

		merge.invoke(new DriversValidationAPI(), driver, defaults, validated);

		assertEquals(Arrays.asList("Description B", "First A", "First A", "Default D"),
				driver.getParameterValuesDescription());
	}

	@Test
	public void validationIndexPreservesCaseInsensitiveTrimmedLovMembership() throws Exception {
		BIObjectParameter driver = driver("CITY", "city");
		driver.setParameterValues(Arrays.asList("i", "I"));

		List<?> errors = validateLovValues(driver, "<ROWS><ROW CODE=\" I \" DESCRIPTION=\"Letter\"/></ROWS>");

		assertTrue(errors.isEmpty());
	}

	@Test
	public void validationChecksEachSelectedValueIndependently() throws Exception {
		for (List<String> values : Arrays.asList(
				Arrays.asList("A", "invalid"),
				Arrays.asList("invalid", "A"),
				Arrays.asList("", "invalid"),
				Arrays.asList("A", "invalid", "invalid"))) {
			BIObjectParameter driver = driver("CITY", "city");
			driver.setParameterValues(values);

			List<?> errors = validateLovValues(driver, "<ROWS><ROW CODE=\"A\" DESCRIPTION=\"City A\"/></ROWS>");

			assertEquals(values.toString(), Collections.frequency(values, "invalid"), errors.size());
			assertEquals("NOT ADMISSIBLE", driver.getParameterValuesDescription().get(values.indexOf("invalid")));
		}
	}

	private List<?> validateLovValues(BIObjectParameter driver, String lovResult) throws Exception {
		ILovDetail lov = (ILovDetail) Proxy.newProxyInstance(ILovDetail.class.getClassLoader(),
				new Class<?>[] { ILovDetail.class }, (proxy, method, args) -> {
					switch (method.getName()) {
					case "getLovType":
						return "query";
					case "getValueColumnName":
						return "CODE";
					case "getDescriptionColumnName":
						return "DESCRIPTION";
					default:
						throw new AssertionError("Unexpected LOV call: " + method.getName());
					}
				});
		Method validate = DriversValidationAPI.class.getDeclaredMethod("getValidationErrorsOnValuesByLovResult",
				String.class, AbstractDriver.class, ILovDetail.class, String.class);
		validate.setAccessible(true);

		return (List<?>) validate.invoke(new DriversValidationAPI(new UserProfile("alice", "tenant"), Locale.ENGLISH),
				lovResult, driver, lov, "analyst");
	}

	private JSONObject lovRow(Object value, Object label, Object description) throws Exception {
		return new JSONObject().put("value", value).put("label", label).put("description", description);
	}

	private HashMap<String, Object> mappedRow(Object value, Object description) {
		HashMap<String, Object> row = new HashMap<>();
		row.put("value", value);
		row.put("description", description);
		return row;
	}

	private AbstractDriverRuntime<BIObjectParameter> lovRuntime() {
		AbstractDriverRuntime<BIObjectParameter> runtime = runtime();
		runtime.admissibleValues = new ArrayList<>();
		runtime.lovVisibleColumnsNames = Collections.emptyList();
		runtime.lovInvisibleColumnsNames = Collections.emptyList();
		return runtime;
	}

	private BIObjectParameter driver(String label, String urlName) {
		Parameter parameter = new Parameter();
		parameter.setLabel(label);
		BIObjectParameter driver = new BIObjectParameter();
		driver.setParameter(parameter);
		driver.setParameterUrlName(urlName);
		return driver;
	}

	private AbstractDriverRuntime<BIObjectParameter> runtime() {
		return new AbstractDriverRuntime<BIObjectParameter>() {
			@Override
			public void initVisualDependencies(BIObjectParameter driver) {
			}

			@Override
			public void initDataDependencies(BIObjectParameter driver) {
			}
		};
	}
}
