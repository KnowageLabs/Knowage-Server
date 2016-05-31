package it.eng.spagobi.tools.alert.listener;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.kpi.bo.KpiValue;
import it.eng.spagobi.kpi.bo.ThresholdValue;
import it.eng.spagobi.kpi.metadata.SbiKpiThresholdValue;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.alert.exception.AlertListenerException;
import it.eng.spagobi.tools.alert.job.AbstractAlertListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class KpiListener extends AbstractAlertListener {
	private static Logger logger = Logger.getLogger(KpiListener.class);

	private static IMessageBuilder message = MessageBuilderFactory.getMessageBuilder();
	private static final String VALUE_TABLE_PLACEHOLDER = "*VALUE_TABLE*";
	private static final String ALL = "ALL";

	private final Set<ThresholdValue> thresholds = new HashSet<>();
	private final Map<Integer, List<KpiValue>> valueMap = new HashMap<>();

	@Override
	public void executeListener(String jsonParameters) throws AlertListenerException {
		logger.info("KpiListener running...");
		InputParameter par = (InputParameter) JsonConverter.jsonToObject(jsonParameters, InputParameter.class);

		try {
			loadThresholdMap(par);
			Date lastDate = (Date) loadLastKey(Date.class);
			List<KpiValue> resultSet = DAOFactory.getKpiDAO().findKpiValues(par.getKpiId(), par.getKpiVersion(), lastDate, false, null, null, new HashMap());
			if (resultSet != null && !resultSet.isEmpty()) {
				saveLastKey(resultSet.get(0).getTimeRun());
				for (KpiValue kpiValue : resultSet) {
					Integer thresholdId = selectThreshold(kpiValue);
					if (thresholdId != null) {
						// In this case the value of this sbiKpiValue is in a threshold
						// so we have to execute related actions
						addValueToThresholdMap(thresholdId, kpiValue);
					}
				}
				for (Action action : par.getActions()) {
					if (hasValues(action)) {
						Map<String, String> parameters = new HashMap<>();
						parameters.put(VALUE_TABLE_PLACEHOLDER, makeHtmlValueTable(action.getThresholdValues()));
						executeAction(JsonConverter.objectToJson(par, par.getClass()).toString(), action.getIdAction(),
								JsonConverter.objectToJson(action, action.getClass()).toString(), parameters);
					}

				}
			} else {
				String msg;
				if (lastDate != null) {
					msg = MessageFormat.format(message.getMessage("KpiListener.noResultWithKey"), lastDate);
				} else {
					msg = message.getMessage("KpiListener.noResult");
				}
				writeAlertLog(JsonConverter.objectToJson(par, par.getClass()).toString(), null, null, msg);
			}
		} catch (EMFUserError e) {
			logger.error(e.getMessage(), e);
			throw new AlertListenerException(e);
		}

		logger.info("KpiListener ended");
	}

	/*
	 * private void writeActionLog(InputParameter par, Action action, String errorMsg) throws EMFUserError { SbiAlertLog alertLog = new SbiAlertLog(); if
	 * (action != null) { alertLog.setActionId(action.getIdAction()); alertLog.setActionParams(action.getJsonActionParameters()); }
	 * alertLog.setListenerId(getListenerId()); alertLog.setDetail(errorMsg); alertLog.setListenerParams(JsonConverter.objectToJson(par, par.getClass()));
	 * alertLog.getCommonInfo().setTimeIn(new Date()); alertLog.getCommonInfo().setSbiVersionIn(SbiCommonInfo.SBI_VERSION); Tenant tenant =
	 * TenantManager.getTenant(); if (tenant != null) { alertLog.getCommonInfo().setOrganization(tenant.getName()); }
	 * DAOFactory.getKpiDAO().insertAlertLog(alertLog); }
	 */

	private void loadThresholdMap(InputParameter par) throws EMFUserError {
		Set<Integer> thresholdIds = new HashSet<>();
		for (Action action : par.getActions()) {
			thresholdIds.addAll(action.getThresholdValues());
			/*
			 * for (Integer thresholdId : action.getThresholdValues()) { if (!thresholds.contains(new ThresholdValue(thresholdId))) { SbiKpiThresholdValue
			 * sbiThreshold = (SbiKpiThresholdValue) session.load(SbiKpiThresholdValue.class, thresholdId); thresholds.add(from(sbiThreshold)); } }
			 */
		}
		List<SbiKpiThresholdValue> lst = DAOFactory.getKpiDAO().listThresholdValueByIds(thresholdIds);
		for (SbiKpiThresholdValue sbiKpiThresholdValue : lst) {
			thresholds.add(from(sbiKpiThresholdValue));
		}

	}

	private void addValueToThresholdMap(Integer thresholdId, KpiValue sbiKpiValue) {
		List<KpiValue> lst = valueMap.get(thresholdId);
		if (lst == null) {
			valueMap.put(thresholdId, new ArrayList<KpiValue>());
		}
		valueMap.get(thresholdId).add(sbiKpiValue);
	}

	private String makeHtmlValueTable(List<Integer> thresholdValues) {
		for (Integer thresholdId : thresholdValues) {
			List<KpiValue> values = valueMap.get(thresholdId);
			boolean showYear = false;
			boolean showQuarter = false;
			boolean showMonth = false;
			boolean showWeek = false;
			boolean showDay = false;
			if (values != null && !values.isEmpty()) {

				for (KpiValue sbiKpiValue : values) {
					if (!ALL.equals(sbiKpiValue.getTheYear())) {
						showYear = true;
					}
					if (!ALL.equals(sbiKpiValue.getTheQuarter())) {
						showQuarter = true;
					}
					if (!ALL.equals(sbiKpiValue.getTheMonth())) {
						showMonth = true;
					}
					if (!ALL.equals(sbiKpiValue.getTheWeek())) {
						showWeek = true;
					}
					if (!ALL.equals(sbiKpiValue.getTheDay())) {
						showDay = true;
					}
				}

				StringBuffer sb = new StringBuffer();
				String tableStyle = " style=\"border:1px solid;border-collapse:collapse;\" ";
				for (ThresholdValue tValue : thresholds) {
					if (tValue.getId().equals(thresholdId)) {
						sb.append("<table " + tableStyle + "></tr>");
						sb.append("<th>Threshold label</th>");
						sb.append("<th>Color</th>");
						sb.append("<th>Severity</th>");
						sb.append("<th>Min value</th>");
						sb.append("<th>Max value</th>");
						sb.append("</tr><tr>");
						sb.append("<td>" + clean(tValue.getLabel()) + "</td>");
						sb.append(clean(tValue.getColor()).isEmpty() ? "<td></td>" : "<td style=\"background-color:" + clean(tValue.getColor()) + ";\"></td>");
						sb.append("<td>" + clean(tValue.getSeverityCd()) + "</td>");
						sb.append("<td>" + clean(tValue.getMinValue()) + "</td>");
						sb.append("<td>" + clean(tValue.getMaxValue()) + "</td>");
						sb.append("</tr></table>");
						break;
					}
				}
				sb.append("<table" + tableStyle + "><tr>");
				sb.append("<th>Logical Key</th>");
				if (showYear) {
					sb.append("<th>The Year</th>");
				}
				if (showQuarter) {
					sb.append("<th>The Quarter</th>");
				}
				if (showMonth) {
					sb.append("<th>The Month</th>");
				}
				if (showWeek) {
					sb.append("<th>The Week</th>");
				}
				if (showDay) {
					sb.append("<th>The Day</th>");
				}
				sb.append("<th>Computed Value</th>");
				sb.append("<th>Manual Value</th>");
				sb.append("</tr>");
				for (KpiValue sbiKpiValue : values) {
					sb.append("<tr>");
					sb.append("<td>" + clean(sbiKpiValue.getLogicalKey()) + "</td>");
					if (showYear) {
						sb.append("<td>" + sbiKpiValue.getTheYear() + "</td>");
					}
					if (showQuarter) {
						sb.append("<td>" + sbiKpiValue.getTheQuarter() + "</td>");
					}
					if (showMonth) {
						sb.append("<td>" + sbiKpiValue.getTheMonth() + "</td>");
					}
					if (showWeek) {
						sb.append("<td>" + sbiKpiValue.getTheWeek() + "</td>");
					}
					if (showDay) {
						sb.append("<td>" + sbiKpiValue.getTheDay() + "</td>");
					}
					sb.append("<td>" + clean(sbiKpiValue.getComputedValue()) + "</td>");
					sb.append("<td>" + clean(sbiKpiValue.getManualValue()) + "</td>");
					sb.append("</tr>");
				}
				sb.append("</table>");
				return sb.toString();
			}
		}
		return null;
	}

	private boolean hasValues(Action action) {
		for (Integer thresholdId : action.getThresholdValues()) {
			List<KpiValue> values = valueMap.get(thresholdId);
			if (values != null && !values.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private ThresholdValue from(SbiKpiThresholdValue sbiValue) {
		ThresholdValue tv = new ThresholdValue();
		tv.setColor(sbiValue.getColor());
		tv.setId(sbiValue.getId());
		tv.setIncludeMin(sbiValue.getIncludeMin() != null && sbiValue.getIncludeMin().charValue() == 'T');
		tv.setMinValue(sbiValue.getMinValue());
		tv.setIncludeMax(sbiValue.getIncludeMax() != null && sbiValue.getIncludeMax().charValue() == 'T');
		tv.setMaxValue(sbiValue.getMaxValue());
		tv.setLabel(sbiValue.getLabel());
		tv.setPosition(sbiValue.getPosition());
		if (sbiValue.getSeverity() != null) {
			tv.setSeverityId(sbiValue.getSeverity().getValueId());
			tv.setSeverityCd(sbiValue.getSeverity().getValueCd());
		}
		return tv;
	}

	private Integer selectThreshold(KpiValue kpiValue) {
		for (ThresholdValue threshold : thresholds) {
			double value = kpiValue.getManualValue() != null ? kpiValue.getManualValue().doubleValue() : kpiValue.getComputedValue();
			boolean minValueOk = threshold.getMinValue() == null || value > threshold.getMinValue().doubleValue() || threshold.isIncludeMin()
					&& value == threshold.getMinValue().doubleValue();
			boolean maxValueOk = threshold.getMaxValue() == null || value < threshold.getMaxValue().doubleValue() || threshold.isIncludeMax()
					&& value == threshold.getMaxValue().doubleValue();
			if (minValueOk && maxValueOk) {
				return threshold.getId();
			}
		}
		return null;
	}

	private String clean(Object o) {
		return o != null ? o.toString() : "";
	}

}

class InputParameter {
	private Integer kpiId;
	private Integer kpiVersion;
	private List<Action> actions;

	/**
	 * @return the kpiId
	 */
	public Integer getKpiId() {
		return kpiId;
	}

	/**
	 * @param kpiId
	 *            the kpiId to set
	 */
	public void setKpiId(Integer kpiId) {
		this.kpiId = kpiId;
	}

	/**
	 * @return the kpiVersion
	 */
	public Integer getKpiVersion() {
		return kpiVersion;
	}

	/**
	 * @param kpiVersion
	 *            the kpiVersion to set
	 */
	public void setKpiVersion(Integer kpiVersion) {
		this.kpiVersion = kpiVersion;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

}

class Action {
	private Integer idAction;
	private List<Integer> thresholdValues;
	private String jsonActionParameters;

	public Integer getIdAction() {
		return idAction;
	}

	public void setIdAction(Integer idAction) {
		this.idAction = idAction;
	}

	public List<Integer> getThresholdValues() {
		return thresholdValues;
	}

	public void setThresholdValues(List<Integer> thresholdValues) {
		this.thresholdValues = thresholdValues;
	}

	public String getJsonActionParameters() {
		return jsonActionParameters;
	}

	public void setJsonActionParameters(String jsonActionParameters) {
		this.jsonActionParameters = jsonActionParameters;
	}

}