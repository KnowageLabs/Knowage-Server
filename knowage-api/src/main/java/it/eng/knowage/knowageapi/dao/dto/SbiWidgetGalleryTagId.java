package it.eng.knowage.knowageapi.dao.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SbiWidgetGalleryTagId implements Serializable {

	@Column(name = "WIDGET_ID", insertable = false, updatable = false)
	private String widgetId;

	@Column(name = "TAG")
	private String tag;

	@Column(name = "ORGANIZATION", insertable = false, updatable = false)
	private String organization;

	public String getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public SbiWidgetGalleryTagId() {
		super();
	}

	public SbiWidgetGalleryTagId(String widgetId, String tag, String organization) {
		super();
		this.widgetId = widgetId;
		this.tag = tag;
		this.organization = organization;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organization == null) ? 0 : organization.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		result = prime * result + ((widgetId == null) ? 0 : widgetId.hashCode());
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
		SbiWidgetGalleryTagId other = (SbiWidgetGalleryTagId) obj;
		if (organization == null) {
			if (other.organization != null)
				return false;
		} else if (!organization.equals(other.organization))
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		if (widgetId == null) {
			if (other.widgetId != null)
				return false;
		} else if (!widgetId.equals(other.widgetId))
			return false;
		return true;
	}

}
