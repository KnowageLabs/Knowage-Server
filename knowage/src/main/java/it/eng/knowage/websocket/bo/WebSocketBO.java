/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.knowage.websocket.bo;

/**
 * @author albnale
 *
 */
public class WebSocketBO {

	private WSDownloadBO downloads;
	private WSNewsBO news;

	public WebSocketBO() {

	}

	public WebSocketBO(WSDownloadBO downloads, WSNewsBO news) {
		super();
		this.downloads = downloads;
		this.news = news;
	}

	public WSDownloadBO getDownloads() {
		return downloads;
	}

	public void setDownloads(WSDownloadBO downloads) {
		this.downloads = downloads;
	}

	public WSNewsBO getNews() {
		return news;
	}

	public void setNews(WSNewsBO news) {
		this.news = news;
	}

}
