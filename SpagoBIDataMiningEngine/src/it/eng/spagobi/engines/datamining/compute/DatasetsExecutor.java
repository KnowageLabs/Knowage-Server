/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.datamining.compute;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;

public class DatasetsExecutor {

	static private Logger logger = Logger.getLogger(DatasetsExecutor.class);

	private Rengine re;

	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;

	public DatasetsExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	public Rengine getRe() {
		return re;
	}

	public void setRe(Rengine re) {
		this.re = re;
	}


	protected void evalDatasetsNeeded(HashMap paramsFilled) throws IOException {
		logger.debug("IN");
		if (dataminingInstance.getDatasets() != null && !dataminingInstance.getDatasets().isEmpty()) {
			for (Iterator dsIt = dataminingInstance.getDatasets().iterator(); dsIt.hasNext();) {
				DataMiningDataset ds = (DataMiningDataset) dsIt.next();
				String options = ds.getOptions();
				if (options == null || options.equals("")) {
					options = "header = TRUE, sep = \",\"";
				}
				if (ds.getType().equalsIgnoreCase("file")) {
					
					// tries to get it from user workspace
					REXP datasetNameInR = re.eval(ds.getName());
					if (datasetNameInR == null) {
						logger.debug("File ds: gets default DS");
						Boolean defaultExists =getAndEvalDefaultDataset(ds);
						if(!defaultExists){						
							
							File fileDSDir = new File(DataMiningUtils.getUserResourcesPath(profile) + ds.getName());
							// /find file in dir
							File[] dsfiles = fileDSDir.listFiles();
							if (dsfiles != null && dsfiles.length != 0) {
								String fileDSPath = dsfiles[0].getPath();
	
								fileDSPath = fileDSPath.replaceAll("\\\\", "/");
	
								String stringToEval = ds.getName() + "<-read." + ds.getReadType() + "(\"" + fileDSPath + "\"," + options + ");";
								re.eval(stringToEval);

							}							
						}

					} else {
						// use it!
						logger.debug("dataset " + ds.getName() + " already loaded in user workspace!");
					}
				} else if (ds.getType().equalsIgnoreCase("spagobi_ds")) {
					logger.debug("SpagoBI ds");
					// spagobi dataset content could change independently from
					// the engine, so it must be recalculated every time
					try {

						String csvToEval = DataMiningUtils.getFileFromSpagoBIDataset(paramsFilled, ds, profile);
						String stringToEval = ds.getName() + "<-read.csv(\"" + csvToEval + "\",header = TRUE, sep = \",\");";
						re.eval(stringToEval);

					} catch (IOException e) {
						logger.error(e.getMessage());
						throw e;
					}

				}
			}
		}
		logger.debug("OUT");
	}
	protected boolean getAndEvalDefaultDataset (DataMiningDataset ds) throws IOException {
		logger.debug("IN");
		//checks relative path
		String relPathToDefDS = ds.getDefaultDS();
		if(relPathToDefDS == null || relPathToDefDS.equals("")){
			return false;
		}
		if(relPathToDefDS.startsWith("/") || relPathToDefDS.startsWith("\\\\")){
			relPathToDefDS = relPathToDefDS.substring(1);
		}
		String defDSRelPath = DataMiningUtils.UPLOADED_FILE_PATH + relPathToDefDS;		
		
		defDSRelPath = defDSRelPath.replaceAll("\\\\", "/");
		logger.debug("Default path "+defDSRelPath);
		File fileDSDefault = new File( defDSRelPath);
		if(!fileDSDefault.exists()){
			logger.debug("Default file doesn't exist");
			return false;
		}
		String stringToEval = ds.getName() + "<-read." + ds.getReadType() + "(\"" + defDSRelPath + "\"," + ds.getOptions() + ");";
		
		logger.debug("R code to eval "+stringToEval);
		re.eval(stringToEval);
		logger.debug("OUT");
		return true;
		
	}
	protected void updateDataset(DataMiningDataset ds) throws IOException {
		logger.debug("IN");
		File fileDSDir = new File(DataMiningUtils.getUserResourcesPath(profile) + ds.getName());
		// /find file in dir
		File[] dsfiles = fileDSDir.listFiles();
		if(dsfiles != null){
			String fileDSPath = dsfiles[0].getPath();
	
			fileDSPath = fileDSPath.replaceAll("\\\\", "/");
			logger.debug("File ds path "+fileDSPath);
	
			String stringToEval = ds.getName() + "<-read." + ds.getReadType() + "(\"" + fileDSPath + "\"," + ds.getOptions() + ");";
			logger.debug("R code to eval "+stringToEval);
			re.eval(stringToEval);
		}
		logger.debug("OUT");
	}
}
