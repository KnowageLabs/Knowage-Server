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

}
