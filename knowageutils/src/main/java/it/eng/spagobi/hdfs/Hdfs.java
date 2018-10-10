package it.eng.spagobi.hdfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class Hdfs {
	private static final Logger logger = Logger.getLogger(Hdfs.class);

	private String label = null;
	private String description = null;
	private Configuration configuration = null;
	private FileSystem fs = null;

	public Hdfs() {
		this.label = "default";
		this.description = "";
	}

	public Hdfs(String label, String description) {
		this.label = label;
		this.description = description;
	}

	public void init() {
		logger.debug("Initialization of HDFS \"" + label + "\"");
		configuration = new Configuration();
		int nFiles = addFilesConfigFromResource(configuration);
		if (nFiles == 0) {
			logger.error("No configuration files found, problems can appears in future");
		}
		initializeFileSystem(configuration);
		logger.debug("Initialization completed");
	}

	public void close() {
		try {
			if (fs != null) {
				fs.close();
			}
		} catch (IOException e) {
			logger.error("Impossible to close FileSystem");
			throw new SpagoBIRuntimeException("Impossible to close FileSystem" + e);
		}
	}

	private int addFilesConfigFromResource(Configuration conf) {
		String hdfsConfigFolderPath = SpagoBIUtilities.getResourcePath() + File.separator + "hdfs" + File.separator + label + File.separator;
		logger.debug("Init HDFS configuration. Search config files in: " + hdfsConfigFolderPath);
		File file = new File(hdfsConfigFolderPath);
		int nFiles = 0;
		if (file.exists() && file.isDirectory()) {
			String[] names = file.list();
			for (String singleConfigFilePath : names) {
				if (singleConfigFilePath.endsWith(".xml")) {
					conf.addResource(new Path(hdfsConfigFolderPath + singleConfigFilePath));
					nFiles++;
					logger.debug("Add \"" + singleConfigFilePath + "\" configuration file");
				} else if (singleConfigFilePath.endsWith(".properties")) {
					String propertyFilePath = hdfsConfigFolderPath + File.separator + singleConfigFilePath;
					// add the properties contained in property file, if it contains at least one property count it has valid file;
					if (addPropertyFileToConfiguration(propertyFilePath, conf) == true) {
						nFiles++;
					}
				}
			}
		}
		logger.debug("Find " + nFiles + " configuration files");
		return nFiles;
	}

	public boolean deleteFile(String path) {
		Path pathFile = new Path(path);
		try {
			if (fs.exists(pathFile)) {
				boolean isDeleted = fs.delete(pathFile, true);
				return isDeleted;
			}
		} catch (IOException e) {
			logger.error("Impossible to delete dataSet file from HDFS" + e);
			return false;
		}
		return true;
	}

	public boolean moveToLocalFile(String pathSrc, String pathDst) {
		Path srcFileHdfsFormatPath = new Path(pathSrc);
		Path destFileHdfsFormatPath = new Path(pathDst);
		try {
			fs.moveToLocalFile(srcFileHdfsFormatPath, destFileHdfsFormatPath);
		} catch (IOException e) {
			logger.error("Impossible to move file from \"" + pathSrc + "\" to \"" + pathDst + "\"" + e);
			return false;
		}
		return true;
	}

	public boolean copyFromLocalFile(String pathSrc, String pathDst) {
		Path srcFileHdfsFormatPath = new Path(pathSrc);
		Path destFileHdfsFormatPath = new Path(pathDst);
		try {
			fs.copyFromLocalFile(srcFileHdfsFormatPath, destFileHdfsFormatPath);
		} catch (IOException e) {
			logger.error("Impossible to copy file from \"" + pathSrc + "\" to \"" + pathDst + "\"" + e);
			return false;
		}
		return true;
	}

	public boolean moveFromLocalFile(String pathSrc, String pathDst) {
		Path srcFileHdfsFormatPath = new Path(pathSrc);
		Path destFileHdfsFormatPath = new Path(pathDst);
		try {
			fs.moveFromLocalFile(srcFileHdfsFormatPath, destFileHdfsFormatPath);
		} catch (IOException e) {
			logger.error("Impossible to move file from \"" + pathSrc + "\" to \"" + pathDst + "\"" + e);
			return false;
		}
		return true;

	}

	public boolean copy(String source, String dest, boolean b) {
		Path sourcePath = new Path(source);
		Path destPath = new Path(dest);
		try {
			FileUtil.copy(fs, sourcePath, fs, destPath, b, fs.getConf());
		} catch (IOException e) {
			logger.error("Impossible to move the file from HDFS path\"" + source + "\" to path: \"" + dest + "\"" + e);
			return false;
		}
		return true;
	}

	public boolean mkdirs(String path) {
		Path pathHdfs = new Path(path);
		try {
			if (!fs.exists(pathHdfs)) {
				fs.mkdirs(pathHdfs);
			}
		} catch (IOException e) {
			logger.error("Impossible to make dirs at path \"" + path + "\"" + e);
			return false;
		}
		return true;
	}

	public boolean mkdirsParent(String path) {
		Path pathHdfs = new Path(path);
		try {
			if (!fs.exists(pathHdfs.getParent())) {
				fs.mkdirs(pathHdfs.getParent());
			}
		} catch (IOException e) {
			logger.error("Impossible to make dirs at path \"" + path + "\"" + e);
			return false;
		}
		return true;
	}

	public String getWorkingDirectory() {
		return fs.getWorkingDirectory().toString();
	}

	private boolean addPropertyFileToConfiguration(String propertyFilePath, Configuration conf) {
		try {
			Properties properties = new Properties();
			File propertyFile = new File(propertyFilePath);
			boolean isPresentOneProperty = false;
			if (propertyFile.exists()) {
				InputStream propertiesStream = new FileInputStream(propertyFile);
				properties.load(propertiesStream);
				Iterator propertiesSetIterator = properties.entrySet().iterator();
				while (propertiesSetIterator.hasNext()) {
					Entry propertyEntry = (Entry) propertiesSetIterator.next();
					String key = (String) propertyEntry.getKey();
					String value = (String) propertyEntry.getValue();
					if (key != null && value != null) {
						conf.set(key, value);
						isPresentOneProperty = true;
					}
				}
			}
			return isPresentOneProperty;
		} catch (FileNotFoundException e) {
			logger.error("File properties \"" + propertyFilePath + "\" not found");
		} catch (IOException e) {
			logger.error("Impossible to read properties file \"" + propertyFilePath + "\"");
		}
		return false;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public FileSystem getFs() {
		return fs;
	}

	public void setFs(FileSystem fs) {
		this.fs = fs;
	}

	public boolean exists(String sourcePath) {
		try {
			return fs.exists(new Path(sourcePath));
		} catch (IllegalArgumentException e) {
			logger.error("Impossible to verify the existence of file" + e);
			return false;
		} catch (IOException e) {
			logger.error("Impossible to verify the existence of file" + e);
			return false;
		}
	}

}
