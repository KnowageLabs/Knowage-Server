package it.eng.knowage.slimerjs.wrapper.beans;

import it.eng.knowage.slimerjs.wrapper.enums.SizeUnit;

/**
 * Used to specify the margins of a page
 */
public class Margin {
    private final float top;
    private final SizeUnit topUnit;
    private final float bottom;
    private final SizeUnit bottomUnit;
    private final float left;
    private final SizeUnit leftUnit;
    private final float right;
    private final SizeUnit rightUnit;

    public static final Margin ZERO = new Margin(0, SizeUnit.px);

    /**
     * Uses one margin around the whole page
     *
     * @param margin margin
     * @param unit   unit
     */
    public Margin(float margin, SizeUnit unit) {
        this(margin, unit, margin, unit, margin, unit, margin, unit);
    }

    /**
     * Uses the top margin to populate both the top and bottom margin, and the left margin to populate both the left
     * and right margins
     *
     * @param topBottomMargin margin for top and bottom of page
     * @param topBottomUnit   unit for top and bottom margins
     * @param leftRightMargin margin for left and right margin of page
     * @param leftRightUnit   unit for left and right margins
     */
    public Margin(float topBottomMargin, SizeUnit topBottomUnit, float leftRightMargin, SizeUnit leftRightUnit) {
        this(topBottomMargin, topBottomUnit, topBottomMargin, topBottomUnit, leftRightMargin, leftRightUnit, leftRightMargin, leftRightUnit);
    }

    /**
     * Main constructor that takes all the margin inputs
     *
     * @param top        top margin
     * @param topUnit    top margin unit
     * @param bottom     bottom margin
     * @param bottomUnit bottom margin unit
     * @param left       left margin
     * @param leftUnit   left margin unit
     * @param right      right margin
     * @param rightUnit  right margin unit
     */
    public Margin(float top, SizeUnit topUnit, float bottom, SizeUnit bottomUnit,
                  float left, SizeUnit leftUnit, float right, SizeUnit rightUnit) {
        if (topUnit == null || bottomUnit == null || leftUnit == null || rightUnit == null) {
            throw new NullPointerException();
        }
        this.top = top;
        this.topUnit = topUnit;
        this.bottom = bottom;
        this.bottomUnit = bottomUnit;
        this.right = right;
        this.rightUnit = rightUnit;
        this.left = left;
        this.leftUnit = leftUnit;
    }

    public String getTop() {
        return Float.toString(top) + topUnit.name();
    }

    public String getBottom() {
        return Float.toString(bottom) + bottomUnit.name();
    }

    public String getLeft() {
        return Float.toString(left) + leftUnit.name();
    }

    public String getRight() {
        return Float.toString(right) + rightUnit.name();
    }
}
