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

}
