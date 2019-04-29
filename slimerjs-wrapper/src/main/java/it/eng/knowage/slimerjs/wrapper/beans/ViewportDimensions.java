package it.eng.knowage.slimerjs.wrapper.beans;

/**
 * Browser viewport dimensions used in rendering the page
 */
public class ViewportDimensions {
	private final int width;
	private final int height;

	public static final ViewportDimensions VIEW_1280_1024 = new ViewportDimensions(1280, 1024);
	public static final ViewportDimensions VIEW_1600_1200 = new ViewportDimensions(1600, 1200);
	public static final ViewportDimensions VIEW_1920_1080 = new ViewportDimensions(1920, 1080);

	public ViewportDimensions(int width, int height) {
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
