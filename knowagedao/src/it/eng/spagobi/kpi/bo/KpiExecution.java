package it.eng.spagobi.kpi.bo;

import it.eng.spagobi.kpi.bo.ScorecardStatus.STATUS;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class KpiExecution extends Kpi {
	public static final Map<String, STATUS> colorMap = new HashMap<>();
	{
		colorMap.put("#FF0000", STATUS.RED);
		colorMap.put("#FFFF00", STATUS.YELLOW);
		colorMap.put("#00FF00", STATUS.GREEN);
	}

	private STATUS status;

	/**
	 * @return the status
	 */
	public STATUS getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(STATUS status) {
		this.status = status;
	}

	@JsonIgnore
	public void setColor(String color) {
		if (colorMap.containsKey(color)) {
			this.status = colorMap.get(color);
		} else {
			this.status = STATUS.GRAY;
		}
	}

}
