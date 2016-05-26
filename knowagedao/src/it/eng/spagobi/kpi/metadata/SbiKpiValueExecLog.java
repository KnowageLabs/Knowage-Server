/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.kpi.metadata;

import java.util.Date;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.kpi.bo.KpiValueExecLog;

public class SbiKpiValueExecLog extends SbiHibernateModel implements java.io.Serializable {

	private static final long serialVersionUID = 8517408854421461194L;

	private int id;
	private int schedulerId;
	private Date timeRun;
	private byte[] output;
	private int errorCount;
	private int successCount;
	private int totalCount;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSchedulerId() {
		return schedulerId;
	}

	public void setSchedulerId(int schedulerId) {
		this.schedulerId = schedulerId;
	}

	public Date getTimeRun() {
		return timeRun;
	}

	public void setTimeRun(Date timeRun) {
		this.timeRun = timeRun;
	}

	public byte[] getOutput() {
		return output;
	}

	public void setOutput(byte[] output) {
		this.output = output;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public KpiValueExecLog toKpiValueExecLog() {
		KpiValueExecLog log = new KpiValueExecLog();
		log.setErrorCount(this.getErrorCount());
		log.setId(this.getId());
		log.setOutput("");
		if (this.getOutput() != null) {
			log.setOutputPresent(true);
		} else {
			log.setOutputPresent(false);
		}

		log.setSchedulerId(this.getSchedulerId());
		log.setSuccessCount(this.getSuccessCount());
		log.setTimeRun(this.getTimeRun());
		log.setTotalCount(this.getTotalCount());

		return log;

	}
}
