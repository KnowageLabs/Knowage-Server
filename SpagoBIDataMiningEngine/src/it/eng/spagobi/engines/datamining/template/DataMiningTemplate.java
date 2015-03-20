/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
