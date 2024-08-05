package it.eng.knowage.utils.zip;

public class SonarZipUtils {
	int thresholdEntries = 10000;
	int thresholdSize = 1000000000;
	double thresholdRatio = 10;
	int totalSizeArchive = 0;
	int totalEntryArchive = 0;
	
	int nBytes = -1;
	byte[] buffer = new byte[2048];
	int totalSizeEntry = 0;

	public void inizializer() {
		totalEntryArchive ++;
		nBytes = -1;
		buffer = new byte[2048];
		totalSizeEntry = 0;
	}
	
	public int getThresholdEntries() {
		return thresholdEntries;
	}

	public void setThresholdEntries(int thresholdEntries) {
		this.thresholdEntries = thresholdEntries;
	}

	public int getThresholdSize() {
		return thresholdSize;
	}

	public void setThresholdSize(int thresholdSize) {
		this.thresholdSize = thresholdSize;
	}

	public double getThresholdRatio() {
		return thresholdRatio;
	}

	public void setThresholdRatio(double thresholdRatio) {
		this.thresholdRatio = thresholdRatio;
	}

	public int getTotalSizeArchive() {
		return totalSizeArchive;
	}

	public void setTotalSizeArchive(int totalSizeArchive) {
		this.totalSizeArchive = totalSizeArchive;
	}

	public int getTotalEntryArchive() {
		return totalEntryArchive;
	}

	public void setTotalEntryArchive(int totalEntryArchive) {
		this.totalEntryArchive = totalEntryArchive;
	}

	public int getnBytes() {
		return nBytes;
	}

	public void setnBytes(int nBytes) {
		this.nBytes = nBytes;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public int getTotalSizeEntry() {
		return totalSizeEntry;
	}

	public void setTotalSizeEntry(int totalSizeEntry) {
		this.totalSizeEntry = totalSizeEntry;
	}	
}
