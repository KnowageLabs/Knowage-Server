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

/**
 *
 * @author Andrew Martin <andrew.martin@ncl.ac.uk>
 * @version 1.8
 * @since 2013-02-18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Organization extends CKANResource {
	private String approval_status;
	private String created;
	private String description;
	private String id;
	private String image_url;
	private boolean is_organization;
	private String name;
	private List<Dataset> packages;
	private String revision_id;
	private String revision_timestamp;
	private String state;
	private String title;
	private String type;
	private List<User> users;

	public String getApproval_status() {
		return approval_status;
	}

	public void setApproval_status(String approval_status) {
		this.approval_status = approval_status;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getImage_url() {
		return image_url;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public boolean isIs_organization() {
		return is_organization;
	}

	public void setIs_organization(boolean is_organization) {
		this.is_organization = is_organization;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Dataset> getPackages() {
		return packages;
	}

	public void setPackages(List<Dataset> packages) {
		this.packages = packages;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
}
