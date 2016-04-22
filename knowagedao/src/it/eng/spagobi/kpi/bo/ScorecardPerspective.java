package it.eng.spagobi.kpi.bo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScorecardPerspective extends ScorecardSubview {

	private final List<ScorecardTarget> targets = new ArrayList<>();

	/**
	 * @return the targets
	 */
	public List<ScorecardTarget> getTargets() {
		return targets;
	}

	public void countKpiByGoal(ScorecardTarget goal) {
		for (CountByStatus goalCount : goal.getGroupedKpis()) {
			CountByStatus count = getGroupedKpiMap().get(goalCount.getStatus());
			if (count == null) {
				getGroupedKpiMap().put(goalCount.getStatus(), goalCount);
			} else {
				count.sum(goalCount.getCount());
			}
		}
	}
}
