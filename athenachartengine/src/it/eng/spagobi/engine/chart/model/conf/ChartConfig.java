package it.eng.spagobi.engine.chart.model.conf;

public class ChartConfig {

	/*
	 * - Velocity Model properties vmPath="/chart/templates/{name}/" vmName="{type}_chart.vm"
	 *
	 * - Library Initializer properties libIniPath="/chartlib/" libIniNAme="{name}Initializer.jspf"
	 */

	private final String type;
	private final String name;
	private final String vmPath;
	private final String vmName;
	private final String libIniPath;
	private final String libIniName;

	public ChartConfig(String type, String name, String vmPath, String vmName, String libIniPath, String libIniName) {
		this.type = type;
		this.name = name;
		this.vmPath = isEmpty(vmPath) ? "/chart/templates/" + name + "/" : vmPath;
		this.vmName = isEmpty(vmName) ? type + "_chart.vm" : vmName;
		this.libIniPath = isEmpty(libIniPath) ? "chartlib/" : libIniPath;
		this.libIniName = isEmpty(libIniName) ? name + "Initializer.jspf" : libIniName;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getVelocityModelPath() {
		return this.vmPath + this.vmName;
	}

	public String getLibraryInitializerPath() {
		return this.libIniPath + this.libIniName;
	}

	public String getLibIniName() {
		return libIniName;
	}

	private boolean isEmpty(String s) {
		return s == null || s.equals("");
	}

}