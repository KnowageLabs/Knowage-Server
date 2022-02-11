/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.boot.dao.dto;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * @author Marco Libanori
 * @deprecated Prefer {@link IEntity}
 */
@Deprecated
@MappedSuperclass
public abstract class AbstractEntity implements IEntity {

	@Column(name = "SBI_VERSION_IN", insertable = true, updatable = false)
	private String sbiVersionIn;

	@Column(name = "TIME_IN", insertable = true, updatable = false)
	private Instant timeIn;

	@Column(name = "USER_IN", insertable = true, updatable = false)
	private String userIn;

	@Column(name = "SBI_VERSION_UP", insertable = true, updatable = true)
	private String sbiVersionUp;

	@Column(name = "TIME_UP", insertable = true, updatable = true)
	private Instant timeUp;

	@Column(name = "USER_UP", insertable = true, updatable = true)
	private String userUp;

	@Column(name = "SBI_VERSION_DE", insertable = true, updatable = true)
	private String sbiVersionDe;

	@Column(name = "TIME_DE", insertable = true, updatable = true)
	private Instant timeDe;

	@Column(name = "USER_DE", insertable = true, updatable = true)
	private String userDe;

	@Override
	public String getSbiVersionDe() {
		return sbiVersionDe;
	}

	@Override
	public void setSbiVersionDe(String sbiVersionDe) {
		this.sbiVersionDe = sbiVersionDe;
	}

	@Override
	public String getSbiVersionIn() {
		return sbiVersionIn;
	}

	@Override
	public void setSbiVersionIn(String sbiVersionIn) {
		this.sbiVersionIn = sbiVersionIn;
	}

	@Override
	public String getSbiVersionUp() {
		return sbiVersionUp;
	}

	@Override
	public void setSbiVersionUp(String sbiVersionUp) {
		this.sbiVersionUp = sbiVersionUp;
	}

	@Override
	public Instant getTimeDe() {
		return timeDe;
	}

	@Override
	public void setTimeDe(Instant timeDe) {
		this.timeDe = timeDe;
	}

	@Override
	public Instant getTimeIn() {
		return timeIn;
	}

	@Override
	public void setTimeIn(Instant timeIn) {
		this.timeIn = timeIn;
	}

	@Override
	public Instant getTimeUp() {
		return timeUp;
	}

	@Override
	public void setTimeUp(Instant timeUp) {
		this.timeUp = timeUp;
	}

	@Override
	public String getUserDe() {
		return userDe;
	}

	@Override
	public void setUserDe(String userDe) {
		this.userDe = userDe;
	}

	@Override
	public String getUserIn() {
		return userIn;
	}

	@Override
	public void setUserIn(String userIn) {
		this.userIn = userIn;
	}

	@Override
	public String getUserUp() {
		return userUp;
	}

	@Override
	public void setUserUp(String userUp) {
		this.userUp = userUp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sbiVersionDe == null) ? 0 : sbiVersionDe.hashCode());
		result = prime * result + ((sbiVersionIn == null) ? 0 : sbiVersionIn.hashCode());
		result = prime * result + ((sbiVersionUp == null) ? 0 : sbiVersionUp.hashCode());
		result = prime * result + ((timeDe == null) ? 0 : timeDe.hashCode());
		result = prime * result + ((timeIn == null) ? 0 : timeIn.hashCode());
		result = prime * result + ((timeUp == null) ? 0 : timeUp.hashCode());
		result = prime * result + ((userDe == null) ? 0 : userDe.hashCode());
		result = prime * result + ((userIn == null) ? 0 : userIn.hashCode());
		result = prime * result + ((userUp == null) ? 0 : userUp.hashCode());
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
		AbstractEntity other = (AbstractEntity) obj;
		if (sbiVersionDe == null) {
			if (other.sbiVersionDe != null)
				return false;
		} else if (!sbiVersionDe.equals(other.sbiVersionDe))
			return false;
		if (sbiVersionIn == null) {
			if (other.sbiVersionIn != null)
				return false;
		} else if (!sbiVersionIn.equals(other.sbiVersionIn))
			return false;
		if (sbiVersionUp == null) {
			if (other.sbiVersionUp != null)
				return false;
		} else if (!sbiVersionUp.equals(other.sbiVersionUp))
			return false;
		if (timeDe == null) {
			if (other.timeDe != null)
				return false;
		} else if (!timeDe.equals(other.timeDe))
			return false;
		if (timeIn == null) {
			if (other.timeIn != null)
				return false;
		} else if (!timeIn.equals(other.timeIn))
			return false;
		if (timeUp == null) {
			if (other.timeUp != null)
				return false;
		} else if (!timeUp.equals(other.timeUp))
			return false;
		if (userDe == null) {
			if (other.userDe != null)
				return false;
		} else if (!userDe.equals(other.userDe))
			return false;
		if (userIn == null) {
			if (other.userIn != null)
				return false;
		} else if (!userIn.equals(other.userIn))
			return false;
		if (userUp == null) {
			if (other.userUp != null)
				return false;
		} else if (!userUp.equals(other.userUp))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractEntity [sbiVersionIn=" + sbiVersionIn + ", timeIn=" + timeIn + ", userIn=" + userIn + ", sbiVersionUp=" + sbiVersionUp + ", timeUp="
				+ timeUp + ", userUp=" + userUp + ", sbiVersionDe=" + sbiVersionDe + ", timeDe=" + timeDe + ", userDe=" + userDe + "]";
	}

}
