/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.news.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiNewsRead extends SbiHibernateModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String user;
	private SbiNews sbiNews;
	private Integer newsReadID;
	private Integer newsId;

	public SbiNewsRead() {

	}

	public Integer getNewsId() {
		return newsId;
	}

	public void setNewsId(Integer newsId) {
		this.newsId = newsId;
	}

	public SbiNewsRead(String user, Integer newsId) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Integer getNewsReadID() {
		return newsReadID;
	}

	public void setNewsReadID(Integer newsReadID) {
		this.newsReadID = newsReadID;
	}

	public SbiNews getSbiNews() {
		return sbiNews;
	}

	public void setSbiNews(SbiNews sbiNews) {
		this.sbiNews = sbiNews;
	}
}
