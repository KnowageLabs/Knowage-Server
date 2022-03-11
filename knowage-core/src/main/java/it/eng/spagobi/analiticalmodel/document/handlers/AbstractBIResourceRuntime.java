package it.eng.spagobi.analiticalmodel.document.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.tools.catalogue.metadata.IDrivableBIResource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public abstract class AbstractBIResourceRuntime<T extends AbstractDriver> {

	static private Logger logger = Logger.getLogger(DocumentRuntime.class);
	private static final String TREE_INNER_LOV_TYPE = "treeinner";

	private IEngUserProfile userProfile = null;
	private Locale locale = null;

	public IEngUserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(IEngUserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	DefaultValuesList defaultValues;

	public AbstractBIResourceRuntime(IEngUserProfile userProfile, Locale locale) {
		this.userProfile = userProfile;
		this.locale = locale;
	}

	public DefaultValuesList getDefaultValues() {
		return defaultValues;
	}

	public abstract List<? extends AbstractDriverRuntime> getDrivers();

	public ILovDetail getLovDetail(AbstractDriver driver) {
		Parameter par = driver.getParameter();
		ModalitiesValue lov = par.getModalityValue();
		// build the ILovDetail object associated to the lov
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = null;
		try {
			lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get lov detail associated to input BIObjectParameter", e);
		}
		return lovProvDet;
	}

	public abstract List<? extends AbstractParuse> getDependencies(AbstractDriver driver, String role);

	public ILovDetail getLovDetailForDefault(AbstractDriver driver) {
		Parameter par = driver.getParameter();
		ModalitiesValue lov = par.getModalityValueForDefault();
		if (lov == null) {
			logger.debug("No LOV for default values defined");
			return null;
		}
		logger.debug("A LOV for default values is defined : " + lov);
		// build the ILovDetail object associated to the lov
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = null;
		try {
			lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get LOV detail associated to the analytical driver for default values", e);
		}
		return lovProvDet;
	}

	/**
	 *
	 * @param driver
	 * @return
	 * @author Marco Libanori
	 */
	public ILovDetail getLovDetailForMax(AbstractDriver driver) {
		Parameter par = driver.getParameter();
		ModalitiesValue lov = par.getModalityValueForMax();
		if (lov == null) {
			logger.debug("No LOV for max value defined");
			return null;
		}
		logger.debug("A LOV for max value is defined : " + lov);
		// build the ILovDetail object associated to the lov
		String lovProv = lov.getLovProvider();
		ILovDetail lovProvDet = null;
		try {
			lovProvDet = LovDetailFactory.getLovFromXML(lovProv);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get LOV detail associated to the analytical driver for max value", e);
		}
		return lovProvDet;
	}

	/**
	 * @deprecated Replaced by {@link #refreshParametersValues(JSONObject, boolean, IDrivableBIResource)}
	 */
	@Deprecated
	public void refreshParametersValues(JSONObject jsonObject, boolean transientMode, IDrivableBIResource object) {
		logger.debug("IN");
		Monitor refreshParametersValuesMonitor = MonitorFactory.start("Knowage.DocumentRuntime.refreshParametersValues");

		Assert.assertNotNull(jsonObject, "JSONObject in input is null!!");
		List biparams = object.getDrivers();
		Iterator iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			AbstractDriver biparam = (AbstractDriver) iterParams.next();
			refreshParameter(biparam, jsonObject, transientMode);
		}
		logger.debug("OUT");
		refreshParametersValuesMonitor.stop();
	}

	/**
	 * Refresh parameters value from user selection.
	 *
	 * @param jsonArray Something like:
	 *   <pre>
	 *     [
	 *       {
	 *         "name": "Driver #1 Name",
	 *         "value": "Driver #1 Value",
	 *         "description": "Driver #1 Value's Description"
	 *       },
	 *       {
	 *         "name": "Driver #2 Name",
	 *         "value": "Driver #2 Value",
	 *         "description": "Driver #2 Value's Description"
	 *       },
	 *       {
	 *         "name": "Driver #3 Name",
	 *         "value": "Driver #3 Value",
	 *         "description": "Driver #3 Value's Description"
	 *       },
	 *       {
	 *         "name": "Driver #4 Name",
	 *         "value": "Driver #4 Value",
	 *         "description": "Driver #4 Value's Description"
	 *       }
	 *     ]
	 *   </pre>
	 * @param transientMode
	 * @param object
	 * @deprecated Where possible, prefer {@link #refreshParametersValues(JSONArray, boolean, Collection)}
	 */
	@Deprecated
	public void refreshParametersValues(JSONArray jsonObject, boolean transientMode, IDrivableBIResource object) {
		refreshParametersValues(jsonObject, transientMode, object.getDrivers());
	}

	public void refreshParametersValues(JSONArray jsonObject, boolean transientMode, Collection<? extends AbstractDriver> biparams) {
		logger.debug("IN");
		Monitor refreshParametersValuesMonitor = MonitorFactory.start("Knowage.DocumentRuntime.refreshParametersValues");

		Assert.assertNotNull(jsonObject, "JSONObject in input is null!!");
		Iterator iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			AbstractDriver biparam = (AbstractDriver) iterParams.next();
			refreshParameter(biparam, jsonObject, transientMode);
		}
		logger.debug("OUT");
		refreshParametersValuesMonitor.stop();
	}

	/**
	 * @deprecated Replaced by {@link #refreshParametersMetamodelValues(JSONArray, boolean, IDrivableBIResource)}
	 */
	@Deprecated
	public void refreshParametersMetamodelValues(JSONObject jsonObject, boolean transientMode, IDrivableBIResource object) {
		logger.debug("IN");
		Monitor refreshParametersValuesMonitor = MonitorFactory.start("Knowage.DocumentRuntime.refreshParametersValues");

		Assert.assertNotNull(jsonObject, "JSONObject in input is null!!");
		List biparams = object.getMetamodelDrivers();
		Iterator iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			AbstractDriver biparam = (AbstractDriver) iterParams.next();
			refreshParameter(biparam, jsonObject, transientMode);
		}
		logger.debug("OUT");
		refreshParametersValuesMonitor.stop();
	}

	/**
	 *
	 * @param jsonArray Something like:
	 *   <pre>
	 *     [
	 *       {
	 *         "name": "Driver #1 Name",
	 *         "value": "Driver #1 Value",
	 *         "description": "Driver #1 Value's Description"
	 *       },
	 *       {
	 *         "name": "Driver #2 Name",
	 *         "value": "Driver #2 Value",
	 *         "description": "Driver #2 Value's Description"
	 *       },
	 *       {
	 *         "name": "Driver #3 Name",
	 *         "value": "Driver #3 Value",
	 *         "description": "Driver #3 Value's Description"
	 *       },
	 *       {
	 *         "name": "Driver #4 Name",
	 *         "value": "Driver #4 Value",
	 *         "description": "Driver #4 Value's Description"
	 *       }
	 *     ]
	 *   </pre>
	 * @param transientMode
	 * @param object
	 * @deprecated Where possible prefer {@link #refreshParametersMetamodelValues(JSONArray, boolean, Collection)}
	 */
	@Deprecated
	public void refreshParametersMetamodelValues(JSONArray jsonArray, boolean transientMode, IDrivableBIResource object) {
		List biparams = object.getMetamodelDrivers();
		refreshParametersMetamodelValues(jsonArray, transientMode, biparams);
	}

	public void refreshParametersMetamodelValues(JSONArray jsonArray, boolean transientMode, Collection<? extends AbstractDriver> biparams) {
		logger.debug("IN");
		Monitor refreshParametersValuesMonitor = MonitorFactory.start("Knowage.DocumentRuntime.refreshParametersValues");

		Assert.assertNotNull(jsonArray, "JSONObject in input is null!!");
		Iterator<? extends AbstractDriver> iterParams = biparams.iterator();
		while (iterParams.hasNext()) {
			AbstractDriver biparam = iterParams.next();
			refreshParameter(biparam, jsonArray, transientMode);
		}
		logger.debug("OUT");
		refreshParametersValuesMonitor.stop();
	}

	public void refreshParameterForFilters(AbstractDriver biparam, JSONObject parameter) {
		refreshParameter(biparam, parameter, false);
	}

	/**
	 * @deprecated Replaced by {@link #refreshParameter(AbstractDriver, JSONArray, boolean)}
	 */
	@Deprecated
	private void refreshParameter(AbstractDriver biparam, JSONObject jsonObject, boolean transientMode) {
		logger.debug("IN");
		Assert.assertNotNull(biparam, "Parameter in input is null!!");
		Assert.assertNotNull(jsonObject, "JSONObject in input is null!!");
		String nameUrl = biparam.getParameterUrlName();
		List values = new ArrayList();
		List descrs = new ArrayList();
		try {
			Object o = jsonObject.opt(nameUrl);
			if (o != null) {
				if (o instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) o;
					for (int c = 0; c < jsonArray.length(); c++) {
						Object anObject = jsonArray.get(c);
						if (anObject != null) {
							values.add(anObject.toString());
						}
					}
				} else if (o.toString().startsWith("{;{}")) {
					// old multivalue parameters case (for back compatibily with documetn composition engine
				} else {
					// trim value at beginning and end of the string
					String valToInsert = o.toString();
					valToInsert = valToInsert.trim();
					if (!valToInsert.isEmpty()) {
						values.add(valToInsert);
					}
				}

			}

			// put also descriptions
			Object oDescr = jsonObject.opt(nameUrl + "_field_visible_description");
			if (oDescr != null) {

				if (oDescr instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) oDescr;
					for (int c = 0; c < jsonArray.length(); c++) {
						Object anObject = jsonArray.get(c);
						if (anObject != null) {
							descrs.add(anObject.toString());
						}
					}
//				} else if (oDescr instanceof Integer) {
//					descrs.add(oDescr);
				} else if (oDescr instanceof Number) {
					descrs.add(oDescr);
				} else {
					// should be in thew form of ;
					StringTokenizer stk = new StringTokenizer((String) oDescr, ";");
					while (stk.hasMoreTokens()) {
						descrs.add(stk.nextToken());
					}
				}
			}

		} catch (JSONException e) {
			logger.error("Cannot get " + nameUrl + " values from JSON object", e);
			throw new SpagoBIServiceException("Cannot retrieve values for biparameter " + biparam.getLabel(), e);
		}

		if (values.size() > 0) {
			logger.debug("Updating values of biparameter " + biparam.getLabel() + " to " + values.toString());
			biparam.setParameterValues(values);
			biparam.setParameterValuesDescription(descrs);

		} else {
			logger.debug("Erasing values of biparameter " + biparam.getLabel());
			biparam.setParameterValues(null);
			biparam.setParameterValuesDescription(null);
		}

		biparam.setTransientParmeters(transientMode);
		logger.debug("OUT");
	}

	/**
	 * Refresh parameters value from user selection.
	 *
	 * @param biparam
	 * @param jsonArray Something like:
	 *   <pre>
	 *     [
	 *       {
	 *         "name": "KNOWAGE-6401-1-1",
	 *         "value": "Ice Cream",
	 *         "description": "Descrizione di Ice Cream"
	 *       },
	 *       {
	 *         "name": "KNOWAGE-6401-1-3",
	 *         "value": [],
	 *         "description": ""
	 *       },
	 *       {
	 *         "name": "KNOWAGE-6401-1-2",
	 *         "value": [ "Spices" ],
	 *         "description": "Descrizione di Spices"
	 *       },
	 *       {
	 *         "name": "KNOWAGE-6401-1-4",
	 *         "value": "",
	 *         "description": ""
	 *       }
	 *     ]
	 *   </pre>
	 * @param transientMode
	 */
	private void refreshParameter(AbstractDriver biparam, JSONArray jsonArray, boolean transientMode) {
		logger.debug("IN");
		Assert.assertNotNull(biparam, "biparam is null!");
		Assert.assertNotNull(jsonArray, "jsonArray is null!");
		String nameUrl = biparam.getParameterUrlName();
		List<Object> values = new ArrayList<>();
		List<Object> descrs = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				JSONObject jsonObject = (JSONObject) jsonArray.get(i);

				String name = (String) jsonObject.get("label");
				if (biparam.getLabel().equals(name)) {
					Object value = jsonObject.get("value");
					Object description = jsonObject.get("description");

					if (value != null) {
						if (value instanceof JSONArray) {
							JSONArray valuesArray = (JSONArray) value;
							for (int c = 0; c < valuesArray.length(); c++) {
								Object anObject = valuesArray.get(c);
								if (anObject != null) {
									values.add(anObject.toString());
								}
							}
						} else if (value.toString().startsWith("{;{}")) {
							// old multivalue parameters case (for back compatibily with documetn composition engine
						} else {
							// trim value at beginning and end of the string
							String valToInsert = value.toString();
							valToInsert = valToInsert.trim();
							if (!valToInsert.isEmpty()) {
								values.add(valToInsert);
							}
						}

					}

					// put also descriptions
					if (description != null) {

						if (description instanceof JSONArray) {
							JSONArray descriptionsArray = (JSONArray) description;
							for (int c = 0; c < descriptionsArray.length(); c++) {
								Object anObject = descriptionsArray.get(c);
								if (anObject != null) {
									descrs.add(anObject.toString());
								}
							}
						} else if (description instanceof Number) {
							descrs.add(description);
						} else {
							// should be in thew form of ;
							StringTokenizer stk = new StringTokenizer((String) description, ";");
							while (stk.hasMoreTokens()) {
								descrs.add(stk.nextToken());
							}
						}
					}

					break;
				}
			} catch (JSONException e) {
				logger.error("Cannot get " + nameUrl + " values from JSON object", e);
				throw new SpagoBIServiceException("Cannot retrieve values for biparameter " + biparam.getLabel(), e);
			}

		}

		if (!values.isEmpty()) {
			logger.debug("Updating values of biparameter " + biparam.getLabel() + " to " + values.toString());
			biparam.setParameterValues(values);
			biparam.setParameterValuesDescription(descrs);

		} else {
			logger.debug("Erasing values of biparameter " + biparam.getLabel());
			biparam.setParameterValues(null);
			biparam.setParameterValuesDescription(null);
		}

		biparam.setTransientParmeters(transientMode);
		logger.debug("OUT");
	}

}
