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

package it.eng.spagobi.tools.datasource.bo;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import it.eng.spagobi.services.validation.Xss;

public class JDBCDataSourcePoolConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Xss
	@Size(max = 30)
	private Long maxWait;

	@NotNull
	@Xss
	@Size(max = 20)
	private Integer abandonedTimeout;

	@NotNull
	@Xss
	@Size(max = 30)
	private Long timeBetweenEvictionRuns;

	@Xss
	@Size(max = 200)
	private String validationQuery;

	private Boolean removeAbandonedOnBorrow;
	private Boolean removeAbandonedOnMaintenance;
	private Boolean logAbandoned;
	private Boolean testOnReturn;
	private Boolean testWhileIdle;

	public JDBCDataSourcePoolConfiguration() {
		this.maxWait = 60000L;
		this.abandonedTimeout = 60;
		this.timeBetweenEvictionRuns = 10000L;
		this.removeAbandonedOnBorrow = true;
		this.removeAbandonedOnMaintenance = true;
		this.logAbandoned = true;
		this.testOnReturn = true;
		this.testWhileIdle = true;
	}

	public JDBCDataSourcePoolConfiguration(Long maxWait, Integer abandonedTimeout, Long timeBetweenEvictionRuns, String validationQuery,
			Boolean removeAbandonedOnBorrow, Boolean removeAbandonedOnMaintenance, Boolean logAbandoned, Boolean testOnReturn, Boolean testWhileIdle) {
		super();
		this.maxWait = maxWait;
		this.abandonedTimeout = abandonedTimeout;
		this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
		this.validationQuery = validationQuery;
		this.removeAbandonedOnBorrow = removeAbandonedOnBorrow;
		this.removeAbandonedOnMaintenance = removeAbandonedOnMaintenance;
		this.logAbandoned = logAbandoned;
		this.testOnReturn = testOnReturn;
		this.testWhileIdle = testWhileIdle;
	}

	public Long getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(Long maxWait) {
		this.maxWait = maxWait;
	}

	public Integer getAbandonedTimeout() {
		return abandonedTimeout;
	}

	public void setAbandonedTimeout(Integer abandonedTimeout) {
		this.abandonedTimeout = abandonedTimeout;
	}

	public Long getTimeBetweenEvictionRuns() {
		return timeBetweenEvictionRuns;
	}

	public void setTimeBetweenEvictionRuns(Long timeBetweenEvictionRuns) {
		this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	public Boolean getRemoveAbandonedOnBorrow() {
		return removeAbandonedOnBorrow;
	}

	public void setRemoveAbandonedOnBorrow(Boolean removeAbandonedOnBorrow) {
		this.removeAbandonedOnBorrow = removeAbandonedOnBorrow;
	}

	public Boolean getRemoveAbandonedOnMaintenance() {
		return removeAbandonedOnMaintenance;
	}

	public void setRemoveAbandonedOnMaintenance(Boolean removeAbandonedOnMaintenance) {
		this.removeAbandonedOnMaintenance = removeAbandonedOnMaintenance;
	}

	public Boolean getLogAbandoned() {
		return logAbandoned;
	}

	public void setLogAbandoned(Boolean logAbandoned) {
		this.logAbandoned = logAbandoned;
	}

	public Boolean getTestOnReturn() {
		return testOnReturn;
	}

	public void setTestOnReturn(Boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public Boolean getTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(Boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

}
