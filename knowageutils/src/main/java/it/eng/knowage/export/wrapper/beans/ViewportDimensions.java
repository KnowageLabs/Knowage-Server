package it.eng.knowage.export.wrapper.beans;

import it.eng.spagobi.commons.SingletonConfig;

/**
 * Browser viewport dimensions used in rendering the page
 */
public class ViewportDimensions {

	public static final String CONFIG_NAME_FOR_VIEWPORT_WIDTH = "internal.exporting.pdf.viewportWidth";
	public static final String CONFIG_NAME_FOR_VIEWPORT_HEIGHT = "internal.exporting.pdf.viewportHeight";
	public static final String CONFIG_NAME_FOR_VIEWPORT_DEVICE_SCALE_FACTOR = "internal.exporting.pdf.viewportDeviceScaleFactor";

	public static class Builder {
		private int width;
		private int height;
		private double deviceScaleFactor;
		private boolean isMultiSheet;

		public Builder() {

			String widthValueAsStr = SingletonConfig.getInstance().getConfigValue(CONFIG_NAME_FOR_VIEWPORT_WIDTH);
			String heighthValueAsStr = SingletonConfig.getInstance().getConfigValue(CONFIG_NAME_FOR_VIEWPORT_HEIGHT);
			String deviceScaleFactorValueAsStr = SingletonConfig.getInstance().getConfigValue(CONFIG_NAME_FOR_VIEWPORT_DEVICE_SCALE_FACTOR);

			width = Integer.parseInt(widthValueAsStr);
			height = Integer.parseInt(heighthValueAsStr);
			deviceScaleFactor = Double.parseDouble(deviceScaleFactorValueAsStr);
			isMultiSheet = false;
		}

		public Builder withWidth(Integer width) {
			if (width != null) {
				this.width = width;
			}
			return this;
		}

		public Builder withWidth(int width) {
			this.width = width;
			return this;
		}

		public Builder withHeight(Integer height) {
			if (height != null) {
				this.height = height;
			}
			return this;
		}

		public Builder withHeight(int height) {
			this.height = height;
			return this;
		}

		public Builder withDeviceScaleFactor(double deviceScaleFactor) {
			this.deviceScaleFactor = deviceScaleFactor;
			return this;
		}

		public Builder withIsMultiSheet(boolean isMultiSheet) {
			this.isMultiSheet = isMultiSheet;
			return this;
		}

		public ViewportDimensions build() {
			return new ViewportDimensions(width, height, deviceScaleFactor, isMultiSheet);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private final int width;
	private final int height;
	private final double deviceScaleFactor;
	private final boolean isMultiSheet;

	private ViewportDimensions(int width, int height, double deviceScaleFactor) {
		this.width = width;
		this.height = height;
		this.deviceScaleFactor = deviceScaleFactor;
		this.isMultiSheet = false;
	}

	private ViewportDimensions(int width, int height, double deviceScaleFactor, boolean isMultiSheet) {
		this.width = width;
		this.height = height;
		this.deviceScaleFactor = deviceScaleFactor;
		this.isMultiSheet = isMultiSheet;
	}

	public String getWidth() {
		return Integer.toString(width);
	}

	public String getHeight() {
		return Integer.toString(height);
	}

	public String getDeviceScaleFactor() {
		return Double.toString(deviceScaleFactor);
	}

	public String getIsMultiSheet() {
		return Boolean.toString(isMultiSheet);
	}

	public ViewportDimensions withHeight(int height) {
		return new ViewportDimensions(width, height, 0.8);
	}

	public ViewportDimensions withWidth(int width) {
		return new ViewportDimensions(width, height, 0.8);
	}

	public ViewportDimensions withDeviceScaleFactor(double deviceScaleFactor) {
		return new ViewportDimensions(width, height, deviceScaleFactor);
	}

	public ViewportDimensions withIsMultiSheet(boolean isMultiSheet) {
		return new ViewportDimensions(width, height, deviceScaleFactor, isMultiSheet);
	}

}
