/*
CKANClient-J - Data Catalogue Software client in Java
Copyright (C) 2013 Newcastle University

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.dataset.ckan.resource.impl;

import it.eng.spagobi.tools.dataset.ckan.resource.CKANResource;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a CKAN Dataset (previously a Package)
 *
 * @author Andrew Martin <andrew.martin@ncl.ac.uk>, Ross Jones <ross.jones@okfn.org>
 * @version 1.8
 * @since 2012-05-01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dataset extends CKANResource {
	public Dataset() {
	}

	private String author;
	private String author_email;
	private String ckan_url;
	private String download_url;
	private List<Extra> extras;
	private List<Group> groups;
	private String id;
	@JsonProperty("isopen")
	private boolean isOpen; // RENAMED
	@JsonProperty("private")
	private boolean isPrivate; // NEW
	@JsonProperty("searchable")
	private String isSearchable; // ONLY IN USER_SHOW -> RESULT -> DATASETS
	private String license;
	private String license_id;
	private String license_title;
	private String license_url;
	private String maintainer;
	private String maintainer_email;
	private String metadata_created;
	private String metadata_modified;
	private String name;
	private String notes;
	private String notes_rendered;
	private Organization organization; // NEW
	private String owner_org; // NEW
	// private List<?> relationships_as_object; //NEW
	// private List<?> relationships_as_subject; //NEW
	private List<Resource> resources;
	private String revision_id;
	private String revision_timestamp; // NEW
	private String state;
	private List<Tag> tags;
	private String title;
	private TrackingSummary tracking_summary; // NEW
	private String type;
	private String url;
	private String version;

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor_email() {
		return author_email;
	}

	public void setAuthor_email(String author_email) {
		this.author_email = author_email;
	}

	public String getCkan_url() {
		return ckan_url;
	}

	public void setCkan_url(String ckan_url) {
		this.ckan_url = ckan_url;
	}

	public String getDownload_url() {
		return download_url;
	}

	public void setDownload_url(String download_url) {
		this.download_url = download_url;
	}

	public List<Extra> getExtras() {
		return extras;
	}

	public void setExtras(List<Extra> extras) {
		this.extras = extras;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public String isSearchable() {
		return isSearchable;
	}

	public void setSearchable(String isSearchable) {
		this.isSearchable = isSearchable;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getLicense_id() {
		return license_id;
	}

	public void setLicense_id(String license_id) {
		this.license_id = license_id;
	}

	public String getLicense_title() {
		return license_title;
	}

	public void setLicense_title(String license_title) {
		this.license_title = license_title;
	}

	public String getLicense_url() {
		return license_url;
	}

	public void setLicense_url(String license_url) {
		this.license_url = license_url;
	}

	public String getMaintainer() {
		return maintainer;
	}

	public void setMaintainer(String maintainer) {
		this.maintainer = maintainer;
	}

	public String getMaintainer_email() {
		return maintainer_email;
	}

	public void setMaintainer_email(String maintainer_email) {
		this.maintainer_email = maintainer_email;
	}

	public String getMetadata_created() {
		return metadata_created;
	}

	public void setMetadata_created(String metadata_created) {
		this.metadata_created = metadata_created;
	}

	public String getMetadata_modified() {
		return metadata_modified;
	}

	public void setMetadata_modified(String metadata_modified) {
		this.metadata_modified = metadata_modified;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes_rendered() {
		return notes_rendered;
	}

	public void setNotes_rendered(String notes_rendered) {
		this.notes_rendered = notes_rendered;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getOwner_org() {
		return owner_org;
	}

	public void setOwner_org(String owner_org) {
		this.owner_org = owner_org;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	public String getRevision_id() {
		return revision_id;
	}

	public void setRevision_id(String revision_id) {
		this.revision_id = revision_id;
	}

	public String getRevision_timestamp() {
		return revision_timestamp;
	}

	public void setRevision_timestamp(String revision_timestamp) {
		this.revision_timestamp = revision_timestamp;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public TrackingSummary getTracking_summary() {
		return tracking_summary;
	}

	public void setTracking_summary(TrackingSummary tracking_summary) {
		this.tracking_summary = tracking_summary;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "<Dataset:" + this.id + "," + this.name + "," + this.title + "," + this.author + "," + this.url + ">";
	}
}
