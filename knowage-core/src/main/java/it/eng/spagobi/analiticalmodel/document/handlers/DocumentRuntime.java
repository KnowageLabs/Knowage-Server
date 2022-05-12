package it.eng.spagobi.analiticalmodel.document.handlers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.LightNavigationConstants;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.drivers.IEngineDriver;
import it.eng.spagobi.engines.drivers.kpi.KpiDriver;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class DocumentRuntime extends AbstractBIResourceRuntime<BIObjectParameter> {

	static private Logger logger = Logger.getLogger(DocumentRuntime.class);

	private final List<DocumentDriverRuntime> drivers = null;

	public DocumentRuntime(IEngUserProfile userProfile, Locale locale) {
		super(userProfile, locale);
	}

	@Override
	public List<DocumentDriverRuntime> getDrivers() {
		return drivers;
	}

	private void addSystemParametersForExternalEngines(Map mapPars, Locale locale, BIObject obj, String executionModality, String role) {
		// mapPars.put("SBI_EXECUTION_ID", this.executionId);
		mapPars.put(SpagoBIConstants.EXECUTION_ROLE, role);
		Integer auditId = createAuditId(obj, executionModality, role);
		if (auditId != null) {
			mapPars.put(AuditManager.AUDIT_ID, auditId);
		}
		if (locale != null) {
			if (locale.getLanguage() != null) {
				mapPars.put(SpagoBIConstants.SBI_LANGUAGE, locale.getLanguage());
			}
			if (locale.getCountry() != null) {
				mapPars.put(SpagoBIConstants.SBI_COUNTRY, locale.getCountry());
			}
			if (StringUtils.isNotBlank(locale.getScript())) {
				mapPars.put(SpagoBIConstants.SBI_SCRIPT, locale.getScript());
			}
		}
	}

	private Integer createAuditId(BIObject obj, String executionModality, String role) {
		logger.debug("IN");
		try {
			AuditManager auditManager = AuditManager.getInstance();
			Integer executionAuditId = auditManager.insertAudit(obj, null, this.getUserProfile(), role, executionModality);
			return executionAuditId;
		} finally {
			logger.debug("OUT");
		}
	}

	public String getExecutionUrl(BIObject obj, String executionModality, String role) {
		logger.debug("IN");
		Monitor getExecutionUrlMonitor = MonitorFactory.start("Knowage.DocumentRuntime.getExecutionUrl");

		String url = null;
		Engine engine = obj.getEngine();
		Domain engineType;
		try {
			engineType = DAOFactory.getDomainDAO().loadDomainById(engine.getEngineTypeId());
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("Impossible to load engine type domain", e);
		}
		// IF THE ENGINE IS EXTERNAL
		if ("EXT".equalsIgnoreCase(engineType.getValueCd())) {
			// instance the driver class
			String driverClassName = engine.getDriverName();
			IEngineDriver aEngineDriver = null;
			try {
				aEngineDriver = (IEngineDriver) Class.forName(driverClassName).newInstance();
			} catch (Exception e) {
				throw new SpagoBIServiceException("Cannot istantiate engine driver class: " + driverClassName, e);
			}
			// get the map of the parameters
			Map mapPars = aEngineDriver.getParameterMap(obj, this.getUserProfile(), role);
			// adding "system" parameters
			addSystemParametersForExternalEngines(mapPars, this.getLocale(), obj, executionModality, role);
			url = GeneralUtilities.getUrl(engine.getUrl(), mapPars);

		}
		// IF THE ENGINE IS INTERNAL
		else {
			StringBuffer buffer = new StringBuffer();
			buffer.append(GeneralUtilities.getSpagoBIProfileBaseUrl(((UserProfile) this.getUserProfile()).getUserId().toString()));
			buffer.append("&PAGE=ExecuteBIObjectPage");
			buffer.append("&" + SpagoBIConstants.TITLE_VISIBLE + "=FALSE");
			buffer.append("&" + SpagoBIConstants.TOOLBAR_VISIBLE + "=FALSE");
			buffer.append("&" + ObjectsTreeConstants.OBJECT_LABEL + "=" + obj.getLabel());
			buffer.append("&" + SpagoBIConstants.ROLE + "=" + role);
			buffer.append("&" + SpagoBIConstants.RUN_ANYWAY + "=TRUE");
			buffer.append("&" + SpagoBIConstants.IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS + "=TRUE");
			// buffer.append("&SBI_EXECUTION_ID=" + this.executionId); // adds constants if it works!!

			String kpiClassName = KpiDriver.class.getCanonicalName();
			if (engine.getClassName().equals(kpiClassName)) {
				Integer auditId = createAuditId(obj, executionModality, role);
				if (auditId != null) {
					buffer.append("&" + AuditManager.AUDIT_ID + "=" + auditId); // adds constants if it works!!
				}
			}

			// identity string for context
			UUIDGenerator uuidGen = UUIDGenerator.getInstance();
			UUID uuid = uuidGen.generateRandomBasedUUID();
			buffer.append("&" + LightNavigationConstants.LIGHT_NAVIGATOR_ID + "=" + uuid.toString());
			List parameters = obj.getDrivers();
			if (parameters != null && parameters.size() > 0) {
				Iterator it = parameters.iterator();
				while (it.hasNext()) {
					BIObjectParameter aParameter = (BIObjectParameter) it.next();

					List list = aParameter.getParameterValues();
					if (list != null && !list.isEmpty()) {
						Iterator r = list.iterator();
						while (r.hasNext()) {
							String value = (String) r.next();
							if (value != null && !value.equals("")) {
								// encoding value
								try {
									value = URLEncoder.encode(value, "UTF-8");
								} catch (UnsupportedEncodingException e) {
									logger.warn("UTF-8 encoding is not supported!!!", e);
									logger.warn("Using system encoding...");
									value = URLEncoder.encode(value);
								}
								buffer.append("&" + aParameter.getParameterUrlName() + "=" + value);
							}
						}
					}
					/*
					 * ParameterValuesEncoder encoder = new ParameterValuesEncoder(); String encodedValue = encoder.encode(aParameter); if(encodedValue!=null &&
					 * !encodedValue.equals("")){ buffer.append("&" + aParameter.getParameterUrlName() + "=" + encodedValue); }
					 */
				}
			}
			url = buffer.toString();
		}
		logger.debug("OUT: returning url = [" + url + "]");
		getExecutionUrlMonitor.stop();
		return url;
	}

	public ILovDetail getLovDetail(BIObjectParameter driver) {
		return super.getLovDetail(driver);
	}

	@Override
	public List<ObjParuse> getDependencies(AbstractDriver driver, String role) {

		List<ObjParuse> biParameterExecDependencies = new ArrayList<ObjParuse>();
		try {
			IParameterUseDAO parusedao = DAOFactory.getParameterUseDAO();
			ParameterUse biParameterExecModality = parusedao.loadByParameterIdandRole(driver.getParID(), role);
			IObjParuseDAO objParuseDAO = DAOFactory.getObjParuseDAO();
			biParameterExecDependencies.addAll(objParuseDAO.loadObjParuse(driver.getId(), biParameterExecModality.getUseID()));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get dependencies", e);
		}
		return biParameterExecDependencies;
	}

	public ILovDetail getLovDetailForDefault(BIObjectParameter driver) {
		return super.getLovDetailForDefault(driver);
	}

//	validateInputs
//	getDefaultValues
//	getAdmissibleValues
//	setValues

}
