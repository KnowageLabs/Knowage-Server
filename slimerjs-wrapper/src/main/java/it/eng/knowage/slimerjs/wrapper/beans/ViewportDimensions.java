package it.eng.knowage.slimerjs.wrapper.beans;

import it.eng.spagobi.commons.SingletonConfig;

/**
 * Browser viewport dimensions used in rendering the page
 */
public class ViewportDimensions {

	public static final String CONFIG_NAME_FOR_VIEWPORT_WIDTH = "internal.exporting.pdf.viewportWidth";
	public static final String CONFIG_NAME_FOR_VIEWPORT_HEIGHT = "internal.exporting.pdf.viewportHeight";

	public static class Builder {
		private int width;
		private int height;

		public Builder() {
			String widthValueAsStr = SingletonConfig.getInstance().getConfigValue(CONFIG_NAME_FOR_VIEWPORT_WIDTH);
			String heighthValueAsStr = SingletonConfig.getInstance().getConfigValue(CONFIG_NAME_FOR_VIEWPORT_HEIGHT);

			width = Integer.parseInt(widthValueAsStr);
			height = Integer.parseInt(heighthValueAsStr);
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

		public ViewportDimensions build() {
			return new ViewportDimensions(width, height);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private final int width;
	private final int height;

	private ViewportDimensions(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public String getWidth() {
		return Integer.toString(width);
	}

	public String getHeight() {
		return Integer.toString(height);
	}

	public ViewportDimensions withHeight(int height) {
		return new ViewportDimensions(width, height);
	}

	public ViewportDimensions withWidth(int width) {
		return new ViewportDimensions(width, height);
	}
}
