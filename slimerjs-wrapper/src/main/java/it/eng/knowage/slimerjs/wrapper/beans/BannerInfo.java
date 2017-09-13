package it.eng.knowage.slimerjs.wrapper.beans;

import it.eng.knowage.slimerjs.wrapper.enums.SizeUnit;

/**
 * Defines information for a header or a footer, including the JavaScript generator function that takes a pageNum and
 * numPages and returns HTML for the page header or footer
 */
public class BannerInfo {
    public static final BannerInfo EMPTY = new BannerInfo(0, SizeUnit.px, "function (pageNum, numPages) { return ''; }");

    private final float height;
    private final SizeUnit heightUnit;
    private final String generatorFunction;

    /**
     * @param height            of the header or footer
     * @param heightUnit        unit of the height in previous parameter
     * @param generatorFunction string containing JavaScript function with signature: String function(int pageNum, int numPages)
     */
    public BannerInfo(float height, SizeUnit heightUnit, String generatorFunction) {
        if (heightUnit == null || generatorFunction == null) {
            throw new NullPointerException();
        }
        this.generatorFunction = generatorFunction;
        this.height = height;
        this.heightUnit = heightUnit;
    }

    public String getHeight() {
        return Float.toString(height) + heightUnit.name();
    }

    public String getGeneratorFunction() {
        return generatorFunction;
    }
}
