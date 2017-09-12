package it.eng.spagobi.engines.datamining.compute;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.model.DataMiningFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

public class RFilesExecutor {

	static private Logger logger = Logger.getLogger(RDatasetsExecutor.class);

	private REngine re;

	DataMiningEngineInstance dataminingInstance;
	IEngUserProfile profile;

	public RFilesExecutor(DataMiningEngineInstance dataminingInstance, IEngUserProfile profile) {
		this.dataminingInstance = dataminingInstance;
		this.profile = profile;
	}

	public REngine getRe() {
		return re;
	}

	public void setRe(REngine re) {
		this.re = re;
	}

	protected String evalFilesNeeded(HashMap paramsFilled) throws IOException, REngineException, REXPMismatchException {
		logger.debug("IN");
		if (re != null && dataminingInstance.getFiles() != null && !dataminingInstance.getFiles().isEmpty()) {
			for (Iterator fIt = dataminingInstance.getFiles().iterator(); fIt.hasNext();) {
				DataMiningFile f = (DataMiningFile) fIt.next();

				// Save file
				String strPathUploadedFile = DataMiningUtils.getUserResourcesPath(profile) + f.getFileName();
				File file = new File(strPathUploadedFile);
				BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
				writer.write(f.getContent());
				writer.flush();
				writer.close();

				// put file path into variable
				String stringToEval = f.getAlias() + "='" + strPathUploadedFile + "'\n";
				re.parseAndEval(stringToEval);

			}
		}
		logger.debug("OUT");
		return "";
	}

}
