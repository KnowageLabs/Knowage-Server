package it.eng.knowage.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigSingleton {

	private static final String LIB_FOLDER = "libFolder";
	private static final String WORKSPACE_FOLDER = "workspaceFolder";
	private static final String MYSQL_TABLE_NAMES = "MYSQL_TABLE_NAMES";
	private static ConfigSingleton _this;
	private static Map<String, Object> map = new HashMap<>();

	private ConfigSingleton() {
	}

	public static ConfigSingleton getInstance() {
		if (_this == null) {
			_this = new ConfigSingleton();
		}
		return _this;
	}

	public boolean enableTestsOnMySql() {
		return TestConstants.enableTestsOnMySql;
	}

	public File workspaceFolder() {
		return (File) (map.get(WORKSPACE_FOLDER) != null ? map.get(WORKSPACE_FOLDER) : TestConstants.workspaceFolder);
	}

	public File libFolder() {
		return (File) (map.get(LIB_FOLDER) != null ? map.get(LIB_FOLDER) : TestConstants.libFolder);
	}

	public String[] MYSQL_TABLE_NAMES() {
		return (String[]) (map.get(MYSQL_TABLE_NAMES) != null ? map.get(MYSQL_TABLE_NAMES) : TestConstants.tables);
	}

}
