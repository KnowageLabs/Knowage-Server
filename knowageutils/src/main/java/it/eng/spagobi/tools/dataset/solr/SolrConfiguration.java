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

package it.eng.spagobi.tools.dataset.solr;

import org.apache.solr.client.solrj.SolrQuery;

public class SolrConfiguration {

    private String url;
    private String collection;
    private SolrQuery solrQuery;
    private static final String WRITER_TYPE = "json"; //wt
    private static final String DEFAULT_REQUEST_HANDLER = "select";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public SolrQuery getSolrQuery() {
        return solrQuery;
    }

    public void setSolrQuery(SolrQuery solrQuery) {
        this.solrQuery = solrQuery;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        if(!url.endsWith("/")) sb.append("/");
        sb.append(collection);
        sb.append("/");
        sb.append(DEFAULT_REQUEST_HANDLER);
        sb.append("?");
        sb.append(solrQuery);
        sb.append("&");
        sb.append("wt=" + WRITER_TYPE);
        return sb.toString();
    }
}
