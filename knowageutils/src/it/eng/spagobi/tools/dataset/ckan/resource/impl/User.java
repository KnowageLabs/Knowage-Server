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
 * Represents a CKAN User
 *
 * @author Andrew Martin <andrew.martin@ncl.ac.uk>, Ross Jones <ross.jones@okfn.org>
 * @version 1.8
 * @since 2012-05-01
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends CKANResource {
	private String about;
	private String activity_streams_email_notifications;
	private String apikey;
	private String capacity;
	private String created;
	private List<Dataset> datasets;
	private String display_name;
	private String email;
	private String email_hash;
	private String fullname;
	private String id;
	private String name;
	private String number_administered_packages;
	private int num_followers;
	private int number_of_edits;
	private String openid;
	private String reset_key;
	private String sys_admin;
	/** Returned from ? **/
	private String sysadmin;

	/** Returned from showGroup **/

	public User() {
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public String getActivity_streams_email_notifications() {
		return activity_streams_email_notifications;
	}

	public void setActivity_streams_email_notifications(String activity_streams_email_notifications) {
		this.activity_streams_email_notifications = activity_streams_email_notifications;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public List<Dataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail_hash() {
		return email_hash;
	}

	public void setEmail_hash(String email_hash) {
		this.email_hash = email_hash;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNum_followers() {
		return num_followers;
	}

	public void setNum_followers(int num_followers) {
		this.num_followers = num_followers;
	}

	public String getNumber_administered_packages() {
		return number_administered_packages;
	}

	public void setNumber_administered_packages(String number_administered_packages) {
		this.number_administered_packages = number_administered_packages;
	}

	public int getNumber_of_edits() {
		return number_of_edits;
	}

	public void setNumber_of_edits(int number_of_edits) {
		this.number_of_edits = number_of_edits;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getReset_key() {
		return reset_key;
	}

	public void setReset_key(String reset_key) {
		this.reset_key = reset_key;
	}

	public String getSys_admin() {
		return sys_admin;
	}

	public void setSys_admin(String sys_admin) {
		this.sys_admin = sys_admin;
	}

	public String getSysadmin() {
		return sysadmin;
	}

	public void setSysadmin(String sysadmin) {
		this.sysadmin = sysadmin;
	}

	public enum OrderBy {
		NAME("name");
		private OrderBy(final String text) {
			this.text = text;
		}

		private final String text;

		@Override
		public String toString() {
			return text;
		}
	}
}
