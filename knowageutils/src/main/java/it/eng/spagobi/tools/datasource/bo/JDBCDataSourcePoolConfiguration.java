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
	@Size(max = 20)
	private Integer maxTotal;

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
	@Size(max = 30)
	private Integer maxIdle;

	@NotNull
	@Xss
	@Size(max = 30)
	private Long minEvictableIdleTimeMillis;

	@Xss
	@Size(max = 200)
	private String validationQuery;

	@Xss
	@Size(max = 30)
	private Integer validationQueryTimeout;

	private Boolean removeAbandonedOnBorrow;
	private Boolean removeAbandonedOnMaintenance;
	private Boolean logAbandoned;
	private Boolean testOnReturn;
	private Boolean testWhileIdle;

	public JDBCDataSourcePoolConfiguration() {
		this.maxTotal = 20;
		this.maxWait = 60000L;
		this.abandonedTimeout = 60;
		this.timeBetweenEvictionRuns = 10000L;
		this.minEvictableIdleTimeMillis = 60000L;
		this.removeAbandonedOnBorrow = true;
		this.removeAbandonedOnMaintenance = true;
		this.logAbandoned = true;
		this.testOnReturn = true;
		this.testWhileIdle = true;
	}

	public JDBCDataSourcePoolConfiguration(Integer maxTotal, Long maxWait, Integer maxIdle, Integer abandonedTimeout, Long timeBetweenEvictionRuns,
			Long minEvictableIdleTimeMillis, String validationQuery, Integer validationQueryTimeout, Boolean removeAbandonedOnBorrow,
			Boolean removeAbandonedOnMaintenance, Boolean logAbandoned, Boolean testOnReturn, Boolean testWhileIdle) {
		super();
		this.maxTotal = maxTotal;
		this.maxWait = maxWait;
		this.maxIdle = maxIdle;
		this.abandonedTimeout = abandonedTimeout;
		this.timeBetweenEvictionRuns = timeBetweenEvictionRuns;
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
		this.validationQuery = validationQuery;
		this.validationQueryTimeout = validationQueryTimeout;
		this.removeAbandonedOnBorrow = removeAbandonedOnBorrow;
		this.removeAbandonedOnMaintenance = removeAbandonedOnMaintenance;
		this.logAbandoned = logAbandoned;
		this.testOnReturn = testOnReturn;
		this.testWhileIdle = testWhileIdle;
	}

	public Integer getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(Integer maxTotal) {
		this.maxTotal = maxTotal;
	}

	public Integer getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
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

	public Long getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(Long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	public Integer getValidationQueryTimeout() {
		return validationQueryTimeout;
	}

	public void setValidationQueryTimeout(Integer validationQueryTimeout) {
		this.validationQueryTimeout = validationQueryTimeout;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abandonedTimeout == null) ? 0 : abandonedTimeout.hashCode());
		result = prime * result + ((logAbandoned == null) ? 0 : logAbandoned.hashCode());
		result = prime * result + ((maxIdle == null) ? 0 : maxIdle.hashCode());
		result = prime * result + ((maxTotal == null) ? 0 : maxTotal.hashCode());
		result = prime * result + ((maxWait == null) ? 0 : maxWait.hashCode());
		result = prime * result + ((minEvictableIdleTimeMillis == null) ? 0 : minEvictableIdleTimeMillis.hashCode());
		result = prime * result + ((removeAbandonedOnBorrow == null) ? 0 : removeAbandonedOnBorrow.hashCode());
		result = prime * result + ((removeAbandonedOnMaintenance == null) ? 0 : removeAbandonedOnMaintenance.hashCode());
		result = prime * result + ((testOnReturn == null) ? 0 : testOnReturn.hashCode());
		result = prime * result + ((testWhileIdle == null) ? 0 : testWhileIdle.hashCode());
		result = prime * result + ((timeBetweenEvictionRuns == null) ? 0 : timeBetweenEvictionRuns.hashCode());
		result = prime * result + ((validationQuery == null) ? 0 : validationQuery.hashCode());
		result = prime * result + ((validationQueryTimeout == null) ? 0 : validationQueryTimeout.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JDBCDataSourcePoolConfiguration other = (JDBCDataSourcePoolConfiguration) obj;
		if (abandonedTimeout == null) {
			if (other.abandonedTimeout != null)
				return false;
		} else if (!abandonedTimeout.equals(other.abandonedTimeout))
			return false;
		if (logAbandoned == null) {
			if (other.logAbandoned != null)
				return false;
		} else if (!logAbandoned.equals(other.logAbandoned))
			return false;
		if (maxIdle == null) {
			if (other.maxIdle != null)
				return false;
		} else if (!maxIdle.equals(other.maxIdle))
			return false;
		if (maxTotal == null) {
			if (other.maxTotal != null)
				return false;
		} else if (!maxTotal.equals(other.maxTotal))
			return false;
		if (maxWait == null) {
			if (other.maxWait != null)
				return false;
		} else if (!maxWait.equals(other.maxWait))
			return false;
		if (minEvictableIdleTimeMillis == null) {
			if (other.minEvictableIdleTimeMillis != null)
				return false;
		} else if (!minEvictableIdleTimeMillis.equals(other.minEvictableIdleTimeMillis))
			return false;
		if (removeAbandonedOnBorrow == null) {
			if (other.removeAbandonedOnBorrow != null)
				return false;
		} else if (!removeAbandonedOnBorrow.equals(other.removeAbandonedOnBorrow))
			return false;
		if (removeAbandonedOnMaintenance == null) {
			if (other.removeAbandonedOnMaintenance != null)
				return false;
		} else if (!removeAbandonedOnMaintenance.equals(other.removeAbandonedOnMaintenance))
			return false;
		if (testOnReturn == null) {
			if (other.testOnReturn != null)
				return false;
		} else if (!testOnReturn.equals(other.testOnReturn))
			return false;
		if (testWhileIdle == null) {
			if (other.testWhileIdle != null)
				return false;
		} else if (!testWhileIdle.equals(other.testWhileIdle))
			return false;
		if (timeBetweenEvictionRuns == null) {
			if (other.timeBetweenEvictionRuns != null)
				return false;
		} else if (!timeBetweenEvictionRuns.equals(other.timeBetweenEvictionRuns))
			return false;
		if (validationQuery == null) {
			if (other.validationQuery != null)
				return false;
		} else if (!validationQuery.equals(other.validationQuery))
			return false;
		if (validationQueryTimeout == null) {
			if (other.validationQueryTimeout != null)
				return false;
		} else if (!validationQueryTimeout.equals(other.validationQueryTimeout))
			return false;
		return true;
	}

}
