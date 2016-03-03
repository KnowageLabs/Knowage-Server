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
package it.eng.spagobi.engines.datamining.template;

import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.model.DataMiningScript;

import java.util.List;

/**
 * @author Monica Franceschini
 */
public class DataMiningTemplate {

	private List<DataMiningScript> scripts;

	private List<DataMiningCommand> commands;

	private List<DataMiningDataset> datasets;

	public List<DataMiningCommand> getCommands() {
		return commands;
	}

	public void setCommands(List<DataMiningCommand> commands) {
		this.commands = commands;
	}

	public List<DataMiningDataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<DataMiningDataset> datasets) {
		this.datasets = datasets;
	}

	private List<Parameter> parameters;

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public List<DataMiningScript> getScripts() {
		return scripts;
	}

	public void setScripts(List<DataMiningScript> scripts) {
		this.scripts = scripts;
	}

	public class Parameter {
		private String name;
		private String alias;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

	}

}
