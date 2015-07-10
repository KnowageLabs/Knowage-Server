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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a related item on a dataset which shows information about an external item using a dataset
 *
 * @author Andrew Martin <andrew.martin@ncl.ac.uk>, Ross Jones <ross.jones@okfn.org>
 * @version 1.7
 * @since 2012-05-01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Related extends CKANResource {

	private String id;
	private String type;
	private String title;
	private String description;
	private String owner_id;
	private String created;
	private int view_count;
	private boolean featured;
	private String image_url;
	private String url;

	public Related() {
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setOwner_id(String owner_id) {
		this.owner_id = owner_id;
	}

	public String getOwner_id() {
		return owner_id;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getCreated() {
		return created;
	}

	public void setView_count(int view_count) {
		this.view_count = view_count;
	}

	public int getView_count() {
		return view_count;
	}

	public void setFeatured(boolean featured) {
		this.featured = featured;
	}

	public boolean isFeatured() {
		return featured;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public String getImage_url() {
		return image_url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

}
