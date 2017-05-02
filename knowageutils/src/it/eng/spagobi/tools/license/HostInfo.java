package it.eng.spagobi.tools.license;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.apache.log4j.Logger;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.license4j.HardwareID;

public class HostInfo implements DataSerializable {

	static private Logger logger = Logger.getLogger(HostInfo.class);

	private int actualProcessors = 0;
	private static String hardwareFingerprint;
	private String hardwareId;

	static {
		hardwareFingerprint = HardwareID.getHardwareIDFromHostName() + HardwareID.getHardwareIDFromVolumeSerialNumber()
				+ Runtime.getRuntime().availableProcessors();
	}

	public HostInfo() {
		logger.debug("IN");
		try {
			actualProcessors = Runtime.getRuntime().availableProcessors();
		} catch (NumberFormatException e) {
			logger.error("Could not retrieve nunmber of processors ", e);
		}

		hardwareFingerprint = HardwareID.getHardwareIDFromHostName() + HardwareID.getHardwareIDFromVolumeSerialNumber()
				+ Runtime.getRuntime().availableProcessors();

		hardwareId = getHardwareID();
		logger.debug("OUT");
	}

	public static void setHardwareFingerprint(String hardwareFingerprint) {
		HostInfo.hardwareFingerprint = hardwareFingerprint;
	}

	public void setHardwareId(String hardwareId) {
		this.hardwareId = hardwareId;
	}

	public static String getHardwareID() {
		logger.debug("IN");
		String hardwareID = "";
		logger.debug("Converting to binary contents");
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(hardwareFingerprint.getBytes(StandardCharsets.UTF_8));
			hardwareID = javax.xml.bind.DatatypeConverter.printHexBinary(hash);
		} catch (Exception e) {
			// do nothing
		}
		logger.debug("OUT");
		return hardwareID;
	}

	public int getActualProcessors() {
		return actualProcessors;
	}

	public void setActualProcessors(int actualProcessors) {
		this.actualProcessors = actualProcessors;
	}

	public String getHardwareFingerprint() {
		return hardwareFingerprint;
	}

	public String getHardwareId() {
		return hardwareId;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(actualProcessors);
		out.writeUTF(hardwareFingerprint);
		out.writeUTF(hardwareId);

	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		actualProcessors = in.readInt();
		hardwareFingerprint = in.readUTF();
		hardwareId = in.readUTF();
	}

}
