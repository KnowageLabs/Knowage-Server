package it.eng.spagobi.kpi.bo;

import java.util.ArrayList;
import java.util.List;

public class ScorecardTarget extends ScorecardSubview {

	private final List<KpiExecution> kpis = new ArrayList<>();

	/**
	 * @return the kpis
	 */
	public List<KpiExecution> getKpis() {
		return kpis;
	}

	public void addKpi(KpiExecution kpi) {
		kpis.add(kpi);
		Integer count = getStatusSummary().get(kpi.getStatus());
		if (count == null) {
			getStatusSummary().put(kpi.getStatus(), 1);
		} else {
			count++;
			getStatusSummary().put(kpi.getStatus(), count);
		}
	}

}
