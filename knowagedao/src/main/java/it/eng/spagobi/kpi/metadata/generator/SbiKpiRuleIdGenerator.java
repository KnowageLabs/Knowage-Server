package it.eng.spagobi.kpi.metadata.generator;

import it.eng.spagobi.kpi.metadata.SbiKpiRule;
import it.eng.spagobi.kpi.metadata.SbiKpiRuleId;

import java.io.Serializable;

public class SbiKpiRuleIdGenerator extends IdOrVersionGenerator {

	@Override
	boolean mustIncrementVersion(Object obj) {
		return getId(obj) != null;
	}

	@Override
	Serializable createEntityId(Integer id, Integer version) {
		return new SbiKpiRuleId(id, version);
	}

	@Override
	Integer getId(Object obj) {
		return ((SbiKpiRule) obj).getSbiKpiRuleId().getId();
	}

}
