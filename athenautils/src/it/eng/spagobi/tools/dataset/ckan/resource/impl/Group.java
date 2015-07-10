/*
CKANClient-J - Data Catalogue Software client in Java
Copyright (C) 2013 Newcastle University
Copyright (C) 2012 Open Knowledge Foundation

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
 * Represents a CKAN group
 *
 * @author Andrew Martin <andrew.martin@ncl.ac.uk>, Ross Jones <ross.jones@okfn.org>
 * @version 1.8
 * @since 2012-05-01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends CKANResource {
	private String approval_status;
	private String capacity;
	private String created;
	private String description;
	private String display_name;
	private List<Extra> extras;
	private List<Group> groups;
	private String id;
	private String image_url;
	private String is_organization;
	private String name;
	private int num_followers;
	private List<Package> packages;
	private String revision_id;
	private String state;
	private List<Tag> tags;
	private String title;
	private String type;
	private List<User> users;

	public Group() {
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public String getIs_organization() {
		return is_organization;
	}

	public void setIs_organization(String is_organization) {
		this.is_organization = is_organization;
	}

	public int getNum_followers() {
		return num_followers;
	}

	public void setNum_followers(int num_followers) {
		this.num_followers = num_followers;
	}

	public String getRevision_id() {
		return revision_id;
	}

	public void setRevision_id(String revision_id) {
		this.revision_id = revision_id;
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

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public String getImage_url() {
		return image_url;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getCreated() {
		return created;
	}

	public void setApproval_status(String approval_status) {
		this.approval_status = approval_status;
	}

	public String getApproval_status() {
		return approval_status;
	}

	public void setExtras(List<Extra> extras) {
		this.extras = extras;
	}

	public List<Extra> getExtras() {
		return extras;
	}

	public void setPackages(List<Package> packages) {
		this.packages = packages;
	}

	public List<Package> getPackages() {
		return packages;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	@Override
	public String toString() {
		return "<Group: " + this.getName() + ", " + this.getTitle() + "  (" + this.getType() + ")>";
	}

}
