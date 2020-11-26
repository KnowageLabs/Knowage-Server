package it.eng.knowage.export.wrapper.beans;

import it.eng.knowage.export.wrapper.enums.SizeUnit;

/**
 * Defines the size of paper for printed formats such as PDF
 */
public class PaperSize {
	public enum Orientation {
		PORTRAIT, LANDSCAPE
	}

	public static final PaperSize Letter = new PaperSize(8.5f, SizeUnit.in, 11, SizeUnit.in, Orientation.PORTRAIT);
	public static final PaperSize Legal = new PaperSize(11f, SizeUnit.in, 14, SizeUnit.in, Orientation.PORTRAIT);
	public static final PaperSize Tabloid = new PaperSize(11f, SizeUnit.in, 17, SizeUnit.in, Orientation.PORTRAIT);
	public static final PaperSize A3 = new PaperSize(297, SizeUnit.mm, 420, SizeUnit.mm, Orientation.PORTRAIT);
	public static final PaperSize A4 = new PaperSize(210, SizeUnit.mm, 297, SizeUnit.mm, Orientation.PORTRAIT);
	public static final PaperSize A5 = new PaperSize(148, SizeUnit.mm, 210, SizeUnit.mm, Orientation.PORTRAIT);

	private final float width;
	private final SizeUnit widthUnit;
	private final float height;
	private final SizeUnit heightUnit;
	private final Orientation orientation;

	public PaperSize(float width, SizeUnit widthUnit, float height, SizeUnit heightUnit, Orientation orientation) {
		if (widthUnit == null || heightUnit == null || orientation == null) {
			throw new NullPointerException();
		}
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException();
		}
		this.width = width;
		this.widthUnit = widthUnit;
		this.height = height;
		this.heightUnit = heightUnit;
		this.orientation = orientation;
	}

	public String getWidth() {
		return Float.toString(width) + widthUnit.name();
	}

	public String getHeight() {
		return Float.toString(height) + heightUnit.name();
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public PaperSize withOrientation(Orientation orientation) {
		if (orientation == null) {
			throw new NullPointerException();
		}
		if (this.orientation.equals(orientation)) {
			return this;
		}
		return new PaperSize(height, heightUnit, width, widthUnit, orientation);
	}
}
