package it.eng.spagobi.analiticalmodel.document.handlers;

import java.util.ArrayList;
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

	public abstract List<? extends AbstractParuse> getDependencies(T driver, String role);

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

	public void refreshParameterForFilters(AbstractDriver biparam, JSONObject parameter) {
		refreshParameter(biparam, parameter, false);
	}

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
				} else if (oDescr instanceof Integer) {
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

}
