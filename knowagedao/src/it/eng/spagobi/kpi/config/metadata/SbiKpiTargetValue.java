package it.eng.spagobi.kpi.config.metadata;

import it.eng.spagobi.commons.metadata.SbiCommonInfo;

public class SbiKpiTargetValue {
	private SbiKpiTarget target;
	private SbiKpi kpi;
	private Double value;
	private SbiCommonInfo commInfo;

	public SbiKpiTarget getTarget() {
		return target;
	}

	public void setTarget(SbiKpiTarget target) {
		this.target = target;
	}

	public SbiKpi getKpi() {
		return kpi;
	}

	public void setKpi(SbiKpi kpi) {
		this.kpi = kpi;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public SbiCommonInfo getCommInfo() {
		return commInfo;
	}

	public void setCommInfo(SbiCommonInfo commInfo) {
		this.commInfo = commInfo;
	}

	public SbiKpiTargetValue(SbiKpiTarget target, SbiKpi kpi, Double value, SbiCommonInfo commInfo) {
		super();
		this.target = target;
		this.kpi = kpi;
		this.value = value;
		this.commInfo = commInfo;
	}

	public SbiKpiTargetValue(SbiKpiTarget target, SbiKpi kpi) {
		super();
		this.target = target;
		this.kpi = kpi;
	}

}
