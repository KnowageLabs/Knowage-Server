package it.eng.spagobi.kpi.bo;

import java.util.ArrayList;
import java.util.List;

public class ScorecardTarget extends ScorecardSubview {

	private final List<Kpi> kpis = new ArrayList<>();

	/**
	 * @return the kpis
	 */
	public List<Kpi> getKpis() {
		return kpis;
	}

}
