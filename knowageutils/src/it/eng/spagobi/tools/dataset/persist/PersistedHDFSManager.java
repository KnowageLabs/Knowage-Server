package it.eng.spagobi.tools.dataset.persist;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.log4j.Logger;

public class PersistedHDFSManager implements IPersistedManager {

	private static final String HDFS_RESOURCE_JNDI = "SPAGOBI.ORGANIZATIONAL-UNIT.hdfsResource";
	private static final Logger logger = Logger.getLogger(PersistedHDFSManager.class);

	private Configuration config = null;
	private FileSystem fs = null;

	public PersistedHDFSManager() {
	}

	public PersistedHDFSManager(HdfsConfiguration config) {
		this.config = config;
	}

	public PersistedHDFSManager(HdfsConfiguration config, FileSystem fs) {
		this.config = config;
		this.fs = fs;
	}

	@Override
	public void persistDataSet(IDataSet dataSet) throws Exception {
		logger.debug("Start persisting DataSet");
		Calendar calendar = Calendar.getInstance();
		Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
		String folderName = Helper.sha256(dataSet.getSignature());
		String fileName = dataSet.getLabel() + "_" + timestamp.toLocalDateTime().toString();
		dataSet.loadData();
		IDataStore dataStore = dataSet.getDataStore();
		persistDataSet(dataStore, fileName, folderName);
		logger.debug("Finish persisting DataSet");
	}

	public Object persistDataSet(IDataStore dataStore, String fileName, String folderName) {
		if (config == null || fs == null) {
			logger.error("No configuration or File System found. Please initialize both before");
			throw new SpagoBIRuntimeException("No configuration or File System found. Please initialize both before");
		}
		FSDataOutputStream fsOS = openHdfsFile(fileName, folderName);
		IMetaData mt = dataStore.getMetaData();
		int nFields = mt.getFieldCount();
		try {
			logger.debug("Starting writing the DataSet metadata");
			for (int f = 1; f <= nFields; f++) {
				String sep = f == nFields ? "\n" : ",";
				fsOS.writeChars("\"" + mt.getFieldName(f) + "\"" + sep);
			}
			logger.debug("End metadata writing. Starting writing the data");
			long nRecords = dataStore.getRecordsCount();
			for (int i = 1; i <= nRecords; i++) {
				IRecord record = dataStore.getRecordAt(i);
				for (int f = 1; f <= nFields; f++) {
					String sep = f == nFields ? "\n" : ",";
					IField field = record.getFieldAt(f);
					Class clz = mt.getFieldType(f);
					Object value = field.getValue();
					appendObjectWithCast(fsOS, value, clz);
					fsOS.writeChars(sep);
				}
			}
			logger.debug("End data writing. Closing file..");
			fsOS.close();
		} catch (IOException e) {
			logger.error("Impossible to write on hdfs, error during writing ");
			throw new SpagoBIRuntimeException("Impossible to write on hdfs, error during writing " + e);
		}
		return fsOS;
	}

	public Configuration initConfiguration() {
		if (config == null) {
			logger.debug("Start initialize Configuration from JNDI resource");
			config = new HdfsConfiguration();
			String jndiResourcePath = SingletonConfig.getInstance().getConfigValue(HDFS_RESOURCE_JNDI);
			String hdfsResource = SpagoBIUtilities.readJndiResource(jndiResourcePath);
			if (hdfsResource == null || hdfsResource.length() == 0) {
				logger.error("Impossible to load jndi resource for hdfs");
				throw new SpagoBIRuntimeException("Impossible to load jndi resource for hdfs");
			}
			hdfsResource = hdfsResource.replace('\\', '/');
			config.set("fs.defaultFS", hdfsResource);
			logger.debug("Finish initialize Configuration from JNDI resource");
		}
		return config;
	}

	public Configuration initConfiguration(String filePathHdfsConfig) {
		if (config == null) {
			config = new HdfsConfiguration();
			config.addResource(new Path(filePathHdfsConfig));
		}
		return config;
	}

	public FileSystem initializeFileSystem(Configuration conf) {
		logger.debug("Initialize HDFS FileSystem");
		if (fs == null) {
			try {
				fs = FileSystem.get(conf);
			} catch (IOException e) {
				logger.error("Impossible to initialize File System");
				throw new SpagoBIRuntimeException("Impossible to initialize File System" + e);
			}
		}
		logger.debug("End initialization HDFS FileSystem");
		return fs;
	}

	public FSDataOutputStream openHdfsFile(String fileName, String folderName) {
		logger.debug("Begin file opening");
		FSDataOutputStream fsOS = null;
		Path filePath = null;
		try {
			filePath = fs.getWorkingDirectory();
			if (folderName != null && folderName.length() > 0) {
				filePath = Path.mergePaths(filePath, new Path("/", folderName));
				if (!fs.exists(filePath) || !fs.isDirectory(filePath)) {
					fs.mkdirs(filePath);
				}
			}
			filePath = Path.mergePaths(filePath, new Path("/" + fileName));
			boolean existsFile = fs.exists(filePath);
			if (existsFile) {
				logger.debug("File is already present in folder, it will be deleted and replaced with new file");
				fs.delete(filePath, true);
			}
			fsOS = fs.create(filePath, true);
		} catch (IOException e) {
			logger.error("Impossible to open file in File System");
			throw new SpagoBIRuntimeException("Impossible to open file in File System" + e);
		}
		logger.debug("File opened");
		return fsOS;
	}

	private void appendObjectWithCast(FSDataOutputStream fsOS, Object value, Class clz) {
		Class objClz = value.getClass();
		try {
			if (clz.equals(String.class)) {
				fsOS.writeChars("\"" + (String) value + "\"");
			} else if (clz.equals(Integer.class)) {
				fsOS.writeChars(value.toString());
			} else if (clz.equals(Double.class)) {
				fsOS.writeChars(value.toString());
			} else if (clz.equals(Long.class)) {
				fsOS.writeChars(value.toString());
			} else if (clz.equals(Date.class)) {
				Date dt = (Date) value;
				fsOS.writeChars("\"" + dt.toString() + "\"");
			} else if (clz.equals(Timestamp.class)) {
				Timestamp ts = (Timestamp) value;
				fsOS.writeChars("\"" + ts.toString() + "\"");
			} else {
				fsOS.writeChars((String) value);
			}
		} catch (IOException e) {
			logger.error("Impossible to write on hdfs, error during casting object for writing ");
			throw new SpagoBIRuntimeException("Impossible to write on hdfs, error during casting object for writing " + e);
		}
	}

	public String getFilePathFromHdfsResourcePath(String fileName) {
		return SpagoBIUtilities.getResourcePath() + File.separatorChar + "hdfs" + File.separatorChar + fileName;
	}

	public FileSystem getFs() {
		return fs;
	}

	public void setFs(FileSystem fs) {
		this.fs = fs;
	}

	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}
}
