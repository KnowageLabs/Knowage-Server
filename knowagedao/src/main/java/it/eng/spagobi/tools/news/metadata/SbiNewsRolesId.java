package it.eng.spagobi.tools.news.metadata;

import java.io.Serializable;

public class SbiNewsRolesId implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Integer newsId;
	private Integer extRoleId;

	public SbiNewsRolesId() {

	}

	public SbiNewsRolesId(Integer newsId, Integer extRoleId) {
		this.newsId = newsId;
		this.extRoleId = extRoleId;
	}

	public Integer getNewsId() {
		return newsId;
	}

	public void setNewsId(Integer newsId) {
		this.newsId = newsId;
	}

	public Integer getExtRoleId() {
		return extRoleId;
	}

	public void setExtRoleId(Integer extRoleId) {
		this.extRoleId = extRoleId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extRoleId == null) ? 0 : extRoleId.hashCode());
		result = prime * result + ((newsId == null) ? 0 : newsId.hashCode());
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
		SbiNewsRolesId other = (SbiNewsRolesId) obj;
		if (extRoleId == null) {
			if (other.extRoleId != null)
				return false;
		} else if (!extRoleId.equals(other.extRoleId))
			return false;
		if (newsId == null) {
			if (other.newsId != null)
				return false;
		} else if (!newsId.equals(other.newsId))
			return false;
		return true;
	}

}
