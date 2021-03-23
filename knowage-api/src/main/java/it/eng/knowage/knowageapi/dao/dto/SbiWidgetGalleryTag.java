package it.eng.knowage.knowageapi.dao.dto;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The persistent class for the sbi_widget_gallery_tags database table.
 *
 */
@Entity
@Table(name = "sbi_widget_gallery_tags")
@NamedQuery(name = "SbiWidgetGalleryTag.findAll", query = "SELECT s FROM SbiWidgetGalleryTag s")
public class SbiWidgetGalleryTag implements Serializable {
	private static final long serialVersionUID = 1L;

	private String organization;

	@Column(name = "SBI_VERSION_DE")
	private String sbiVersionDe;

	@Column(name = "SBI_VERSION_IN")
	private String sbiVersionIn;

	@Column(name = "SBI_VERSION_UP")
	private String sbiVersionUp;

	@EmbeddedId
	private SbiWidgetGalleryTagId id;

	@Column(name = "TIME_DE")
	private Timestamp timeDe;

	@Column(name = "TIME_IN")
	private Timestamp timeIn;

	@Column(name = "TIME_UP")
	private Timestamp timeUp;

	@Column(name = "USER_DE")
	private String userDe;

	@Column(name = "USER_IN")
	private String userIn;

	@Column(name = "USER_UP")
	private String userUp;

	// bi-directional many-to-one association to SbiWidgetGallery
	@MapsId("widgetId")
	@ManyToOne
	@JoinColumn(name = "WIDGET_ID")
	private SbiWidgetGallery sbiWidgetGallery;

	public SbiWidgetGalleryTag() {
	}

	public SbiWidgetGalleryTagId getId() {
		return id;
	}

	public void setId(SbiWidgetGalleryTagId id) {
		this.id = id;
	}

	public String getOrganization() {
		return this.organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getSbiVersionDe() {
		return this.sbiVersionDe;
	}

	public void setSbiVersionDe(String sbiVersionDe) {
		this.sbiVersionDe = sbiVersionDe;
	}

	public String getSbiVersionIn() {
		return this.sbiVersionIn;
	}

	public void setSbiVersionIn(String sbiVersionIn) {
		this.sbiVersionIn = sbiVersionIn;
	}

	public String getSbiVersionUp() {
		return this.sbiVersionUp;
	}

	public void setSbiVersionUp(String sbiVersionUp) {
		this.sbiVersionUp = sbiVersionUp;
	}

	public Timestamp getTimeDe() {
		return this.timeDe;
	}

	public void setTimeDe(Timestamp timeDe) {
		this.timeDe = timeDe;
	}

	public Timestamp getTimeIn() {
		return this.timeIn;
	}

	public void setTimeIn(Timestamp timeIn) {
		this.timeIn = timeIn;
	}

	public Timestamp getTimeUp() {
		return this.timeUp;
	}

	public void setTimeUp(Timestamp timeUp) {
		this.timeUp = timeUp;
	}

	public String getUserDe() {
		return this.userDe;
	}

	public void setUserDe(String userDe) {
		this.userDe = userDe;
	}

	public String getUserIn() {
		return this.userIn;
	}

	public void setUserIn(String userIn) {
		this.userIn = userIn;
	}

	public String getUserUp() {
		return this.userUp;
	}

	public void setUserUp(String userUp) {
		this.userUp = userUp;
	}

	public SbiWidgetGallery getSbiWidgetGallery() {
		return this.sbiWidgetGallery;
	}

	public void setSbiWidgetGallery(SbiWidgetGallery sbiWidgetGallery) {
		this.sbiWidgetGallery = sbiWidgetGallery;
	}

}