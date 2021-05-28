package it.eng.knowage.knowageapi.dao.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SbiWidgetGalleryId implements Serializable {

	@Column(name = "UUID")
	protected String uuid;

	@Column(name = "ORGANIZATION")
	protected String organization;

	@Column(columnDefinition = "UUID", updatable = false)
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Column(columnDefinition = "ORGANIZATION", updatable = false)
	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organization == null) ? 0 : organization.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		SbiWidgetGalleryId other = (SbiWidgetGalleryId) obj;
		if (organization == null) {
			if (other.organization != null)
				return false;
		} else if (!organization.equals(other.organization))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	public SbiWidgetGalleryId() {
		super();
	}

	public SbiWidgetGalleryId(String uuid, String organization) {
		super();
		this.uuid = uuid;
		this.organization = organization;
	}

}
