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

	public void countKpi(KpiExecution kpi) {
		CountByStatus count = getGroupedKpiMap().get(kpi.getStatus());
		if (count == null) {
			getGroupedKpiMap().put(kpi.getStatus(), new CountByStatus(kpi.getStatus()));
		} else {
			count.add();
		}

	}

}
