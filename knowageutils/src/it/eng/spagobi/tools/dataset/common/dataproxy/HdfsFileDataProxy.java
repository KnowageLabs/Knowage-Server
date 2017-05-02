package it.eng.spagobi.tools.dataset.common.dataproxy;

import it.eng.spagobi.hdfs.Hdfs;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class HdfsFileDataProxy extends FileDataProxy {

	private static transient Logger logger = Logger.getLogger(HdfsFileDataProxy.class);
	private Hdfs hdfs;

	public HdfsFileDataProxy(String resourcePath) {
		super(resourcePath);
	}

	public HdfsFileDataProxy(String resourcePath, Hdfs hdfs) {
		super(resourcePath);
		this.hdfs = hdfs;
	}

	@Override
	public IDataStore load(IDataReader dataReader) {

		IDataStore dataStore = null;
		InputStream inputStream = null;

		String filePath = getCompleteFilePath();
		try {
			// recover the file from resources!
			Path hdfsFilePath = new Path(filePath);
			FileSystem fs = hdfs.getFs();
			if (fs.exists(hdfsFilePath)) {
				inputStream = fs.open(hdfsFilePath);
				dataReader.setMaxResults(this.getMaxResultsReader());
				dataStore = dataReader.read(inputStream);
			}
		} catch (FileNotFoundException fnf) {
			logger.error("Impossible to find file in HDFS with path: \"" + filePath + "\" ");
			throw new SpagoBIRuntimeException("Impossible to find file in HDFS with path: \"" + filePath + "\" " + fnf);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to load dataset from HDFS", t);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("Error closing input stream", e);
				}
			}
		}
		return dataStore;
	}

	@Override
	public String getCompleteFilePath() {
		String hdfsPath = hdfs.getFs().getWorkingDirectory().toString();
		String filePath = resPath == null ? hdfsPath : resPath;
		if (useTempFile) {
			filePath += Path.SEPARATOR + "dataset" + Path.SEPARATOR + "files" + Path.SEPARATOR + "temp";
		} else {
			filePath += Path.SEPARATOR + "dataset" + Path.SEPARATOR + "files";
		}
		filePath += Path.SEPARATOR + fileName;
		return filePath;
	}

	public Hdfs getHdfs() {
		return hdfs;
	}

	public void setHdfs(Hdfs hdfs) {
		this.hdfs = hdfs;
	}

}
