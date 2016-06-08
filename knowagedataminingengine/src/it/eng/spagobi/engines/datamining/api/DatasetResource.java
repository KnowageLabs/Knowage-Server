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
package it.eng.spagobi.engines.datamining.api;

import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.common.AbstractDataMiningEngineService;
import it.eng.spagobi.engines.datamining.common.utils.DataMiningConstants;
import it.eng.spagobi.engines.datamining.compute.DataMiningRExecutor;
import it.eng.spagobi.engines.datamining.compute.DataMiningUtils;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.model.DataMiningScript;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/1.0/dataset")
public class DatasetResource extends AbstractDataMiningEngineService {

	public static transient Logger logger = Logger.getLogger(DatasetResource.class);

	@GET
	@Path("/{commandName}")
	@Produces("text/html; charset=UTF-8")
	public String getDatasets(@PathParam("commandName") String commandName) {
		logger.debug("IN");

		DataMiningEngineInstance dataMiningEngineInstance = getDataMiningEngineInstance();

		List<String> scripts = getScriptsForDatasets(dataMiningEngineInstance, commandName);
		List<String> datasetNames = geDatasetsToDisplay(dataMiningEngineInstance, scripts);

		String datasetsJson = "";
		List<DataMiningDataset> datasets = null;
		List<DataMiningDataset> datasetsToReturn = new ArrayList<DataMiningDataset>();
		if (dataMiningEngineInstance.getDatasets() != null && !dataMiningEngineInstance.getDatasets().isEmpty()) {
			datasets = dataMiningEngineInstance.getDatasets();

			logger.debug("Finds existing files for datasets");
			for (Iterator dsIt = dataMiningEngineInstance.getDatasets().iterator(); dsIt.hasNext();) {
				DataMiningDataset ds = (DataMiningDataset) dsIt.next();
				if (ds.getType().equalsIgnoreCase(DataMiningConstants.DATASET_TYPE_FILE) && datasetNames.contains(ds.getName())) {
					File fileDSDir;
					try {
						fileDSDir = new File(DataMiningUtils.getUserResourcesPath(getUserProfile()) + ds.getName());
						logger.debug(fileDSDir);
						File[] dsfiles = fileDSDir.listFiles();
						if (dsfiles != null && dsfiles.length != 0) {
							String fileName = dsfiles[0].getName();
							ds.setFileName(fileName);
						}
						datasetsToReturn.add(ds);
					} catch (IOException e) {
						throw new SpagoBIEngineRuntimeException("Error getting dataset", e);
					}

				}

			}
			datasetsJson = serializeDatasetsList(datasetsToReturn);
		}

		if (!isNullOrEmpty(datasetsJson)) {
			logger.debug("Returning dataset list");
		} else {
			logger.debug("No dataset list found");
		}

		logger.debug("OUT");
		return datasetsJson;
	}

	@GET
	@Path("/updateDataset/{fileName}/{dsName}")
	@Produces("text/html; charset=UTF-8")
	public String updateDataset(@PathParam("fileName") String fileName, @PathParam("dsName") String dsName) {
		logger.debug("IN");

		DataMiningEngineInstance dataMiningEngineInstance = getDataMiningEngineInstance();
		String datasetsJson = "";
		List<DataMiningDataset> datasets = null;
		if (dataMiningEngineInstance.getDatasets() != null && !dataMiningEngineInstance.getDatasets().isEmpty()) {
			datasets = dataMiningEngineInstance.getDatasets();
			// finds existing files for datasets
			logger.debug("finds existing files for datasets");
			for (Iterator dsIt = dataMiningEngineInstance.getDatasets().iterator(); dsIt.hasNext();) {
				DataMiningDataset ds = (DataMiningDataset) dsIt.next();
				if (ds.getType().equalsIgnoreCase(DataMiningConstants.DATASET_TYPE_FILE) && ds.getName().equals(dsName)) {
					ds.setFileName(fileName);

					logger.debug("and update its content in R workspace!");
					DataMiningRExecutor executor = new DataMiningRExecutor(dataMiningEngineInstance, getUserProfile());

					setCommandExecutable(dataMiningEngineInstance);
					/*
					 * try { //executor.updateDatasetInWorkspace(ds, getUserProfile());
					 * 
					 * 
					 * } catch (IOException e) { throw new SpagoBIEngineRuntimeException("Error updating file dataset", e); }
					 */
				}

			}
		}

		if (!isNullOrEmpty(datasetsJson)) {
			logger.debug("Returning dataset list");
		} else {
			logger.debug("No dataset list found");
		}

		logger.debug("OUT");
		return getJsonSuccess();
	}

	private void setCommandExecutable(DataMiningEngineInstance dataminingInstance) {
		logger.debug("IN");
		if (dataminingInstance.getCommands() != null && !dataminingInstance.getCommands().isEmpty()) {
			for (Iterator it = dataminingInstance.getCommands().iterator(); it.hasNext();) {
				DataMiningCommand command = (DataMiningCommand) it.next();
				if (command.getMode().equals(DataMiningConstants.EXECUTION_TYPE_AUTO)) {
					logger.debug("Sets mode to false in order to be riexecuted by the script");
					command.setExecuted(false);
				}
			}
		}
		logger.debug("OUT");
	}

	@POST
	@Path("/loadDataset/{fieldName}")
	@Consumes("multipart/form-data")
	@Produces("text/html; charset=UTF-8")
	public String loadDataset(@Context HttpServletRequest req, MultipartFormDataInput input, @PathParam("fieldName") String fieldName) {
		logger.debug("IN");
		String fileName = "";

		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

		List<InputPart> inputParts = uploadForm.get(fieldName);

		for (InputPart inputPart : inputParts) {

			try {

				MultivaluedMap<String, String> header = inputPart.getHeaders();
				fileName = getFileName(header);

				// convert the uploaded file to inputstream
				InputStream inputStream = inputPart.getBody(InputStream.class, null);
				logger.debug("Convert the uploaded file to inputstream");

				byte[] bytes = IOUtils.toByteArray(inputStream);

				double megabytes = (bytes.length / (1024 * 1024));
				if (megabytes >= 50) {
					throw new SpagoBIEngineRuntimeException("Dataset too big: exceeded 50MB");
				}

				File dirToSaveDS = new File(DataMiningUtils.getUserResourcesPath(getUserProfile()) + fieldName);

				if (!dirToSaveDS.mkdirs()) {
					logger.debug("Creating" + dirToSaveDS.getAbsolutePath());
				}

				logger.debug("created dir");
				//
				File[] dsfiles = dirToSaveDS.listFiles();
				if (dsfiles.length >= 1) {
					for (int i = 0; i < dsfiles.length; i++) {
						File olFile = dsfiles[i];
						olFile.delete();
					}
				}
				logger.debug("Left just une file per dataset");
				// // constructs upload file path
				fileName = dirToSaveDS.getPath() + "/" + Paths.get(fileName).getFileName().toString();

				logger.debug("Constructs upload file path " + fileName);
				writeFile(bytes, fileName);

			} catch (IOException e) {
				logger.error(e.getMessage());
				throw new SpagoBIEngineRuntimeException("Error loading file dataset", e);
			}

		}
		logger.debug("OUT");
		return getJsonSuccessTrue();

	}

	private String getFileName(MultivaluedMap<String, String> header) {
		logger.debug("IN");
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		logger.debug("OUT");
		return "unknown";
	}

	// save to somewhere
	private void writeFile(byte[] content, String filename) throws IOException {
		logger.debug("IN");
		FileOutputStream fop = null;
		try {
			File file = new File(filename);

			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			if (!file.exists()) {
				file.createNewFile();
			}

			fop = new FileOutputStream(file);

			fop.write(content);
			fop.flush();
			fop.close();
		} finally {
			if (fop != null)
				fop.close();
		}
		logger.debug("OUT");
	}

	private List<String> getScriptsForDatasets(DataMiningEngineInstance dataminingInstance, String commandName) {
		logger.debug("IN");
		List<String> scriptsToLoad = new ArrayList<String>();
		if (dataminingInstance.getCommands() != null && !dataminingInstance.getCommands().isEmpty()) {
			for (Iterator it = dataminingInstance.getCommands().iterator(); it.hasNext();) {
				DataMiningCommand command = (DataMiningCommand) it.next();
				if (command.getName().equals(commandName)) {
					scriptsToLoad.add(command.getScriptName());
					if (command.getOutputs() != null && !command.getOutputs().isEmpty()) {
						for (Iterator it2 = command.getOutputs().iterator(); it2.hasNext();) {
							Output output = (Output) it2.next();
							if (output.getOutputMode().equals(DataMiningConstants.EXECUTION_TYPE_AUTO)
									&& output.getOutputType().equalsIgnoreCase(DataMiningConstants.SCRIPT_OUTPUT)) {
								scriptsToLoad.add(output.getOutputValue());
							}
						}
					}
				}
			}
		}
		logger.debug("OUT");
		return scriptsToLoad;
	}

	// script tag refers to comma separated list of datasets in datasets
	// attributes
	private List<String> geDatasetsToDisplay(DataMiningEngineInstance dataminingInstance, List<String> scriptNames) {
		logger.debug("IN");
		String datasets = "";
		List<String> dsNames = new ArrayList<String>();
		if (dataminingInstance.getScripts() != null && !dataminingInstance.getScripts().isEmpty()) {
			for (Iterator it = dataminingInstance.getScripts().iterator(); it.hasNext();) {
				DataMiningScript script = (DataMiningScript) it.next();
				if (scriptNames.contains(script.getName())) {
					datasets += script.getDatasets() + ",";
				}
			}
			logger.debug("datasets " + datasets);
		}
		String[] datasetNames = datasets.split(",");
		for (int i = 0; i < datasetNames.length; i++) {
			dsNames.add(datasetNames[i].trim());
		}

		logger.debug("OUT");
		return dsNames;
	}

}
