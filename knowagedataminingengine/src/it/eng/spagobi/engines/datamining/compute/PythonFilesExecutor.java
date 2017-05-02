package it.eng.spagobi.engines.datamining.compute;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.model.DataMiningFile;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jpy.PyLib;

public class PythonFilesExecutor {

	static private Logger logger = Logger.getLogger(PythonDatasetsExecutor.class);

	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;
	int resPythonExecution = 1;

	public PythonFilesExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	protected String evalFilesNeeded(HashMap paramsFilled) throws IOException, Exception {

		logger.debug("IN");
		String codeToExec = null;

		if (PyLib.isPythonRunning() && dataminingInstance.getFiles() != null && !dataminingInstance.getFiles().isEmpty()) {
			for (Iterator fIt = dataminingInstance.getFiles().iterator(); fIt.hasNext();) {
				DataMiningFile f = (DataMiningFile) fIt.next();

				// Save file to filesystem
				String strPathUploadedFile = DataMiningUtils.getUserResourcesPath(profile) + f.getFileName();
				File file = new File(strPathUploadedFile);
				BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
				writer.write(f.getContent());
				writer.flush();
				writer.close();

				// Put file path into the variable called [alias]
				codeToExec = "import os\n" + "import pandas\n" + "os.chdir(r'" + DataMiningUtils.getUserResourcesPath(profile) + "')\n" + f.getAlias() + "='"
						+ strPathUploadedFile + "'\n";
				resPythonExecution = PyLib.execScript(codeToExec);
				if (resPythonExecution < 0) {
					throw new SpagoBIRuntimeException("Python engine error \n" + "Technical details:\n" + "PythonFileExecutor.java:\n" + "\t" + codeToExec
							+ "EXECUTION FAILED\n" + "See log file for other details\n");
				}
			}
		}

		return "";
	}

}
