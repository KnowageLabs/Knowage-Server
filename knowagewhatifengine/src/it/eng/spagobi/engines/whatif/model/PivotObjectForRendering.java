/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.engines.whatif.model;

import org.olap4j.OlapConnection;
import org.pivot4j.PivotModel;

/**
 * just a container for all the objects needed for rendering
 * @author ghedin
 *
 */
public class PivotObjectForRendering {

	private OlapConnection connection;
	private PivotModel model;
	private ModelConfig config;
	public OlapConnection getConnection() {
		return connection;
	}
	public void setConnection(OlapConnection connection) {
		this.connection = connection;
	}
	public PivotModel getModel() {
		return model;
	}
	public void setModel(PivotModel model) {
		this.model = model;
	}
	public ModelConfig getConfig() {
		return config;
	}
	public void setConfig(ModelConfig config) {
		this.config = config;
	}
	public PivotObjectForRendering(OlapConnection connection, PivotModel model,
			ModelConfig config) {
		super();
		this.connection = connection;
		this.model = model;
		this.config = config;
	}
	
	
	
}
