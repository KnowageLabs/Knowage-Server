package it.eng.spagobi.kpi.metadata.generator;

import it.eng.spagobi.kpi.metadata.SbiKpiKpi;
import it.eng.spagobi.kpi.metadata.SbiKpiKpiId;

import java.io.Serializable;

public class SbiKpiKpiIdGenerator extends IdOrVersionGenerator {

	@Override
	boolean mustIncrementVersion(Object obj) {
		return getId(obj) != null;
	}

	@Override
	Serializable createEntityId(Integer id, Integer version) {
		return new SbiKpiKpiId(id, version);
	}

	@Override
	Integer getId(Object obj) {
		return ((SbiKpiKpi) obj).getSbiKpiKpiId().getId();
	}

}
