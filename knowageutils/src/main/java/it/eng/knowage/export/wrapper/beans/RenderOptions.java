package it.eng.knowage.export.wrapper.beans;

import it.eng.knowage.export.wrapper.enums.RenderFormat;

public class RenderOptions {
	private static final RenderOptions EMPTY = new RenderOptions(null, null, null, null, null, null);

	public static final RenderOptions defaultOptions() {

		ViewportDimensions defaulViewportDimensions = ViewportDimensions.builder().build();

		return EMPTY.withDimensions(defaulViewportDimensions).withRenderFormat(RenderFormat.PNG).withJavaScriptExecutionDetails(60000L, 5000L)
				.withZoomFactor(2.0);
	}

	private final PaperSize paperSize;
	private final ViewportDimensions dimensions;
	private final RenderFormat renderFormat;
	private final Long jsRenderingWait;
	private final Long jsExitingWait;
	private final Double zoomFactor;

	private RenderOptions(PaperSize paperSize, ViewportDimensions dimensions, RenderFormat renderFormat, Long jsRenderingWait, Long jsExitingWait,
			Double zoomFactor) {
		this.paperSize = paperSize;
		this.dimensions = dimensions;
		this.renderFormat = renderFormat;
		this.jsRenderingWait = jsRenderingWait;
		this.jsExitingWait = jsExitingWait;
		this.zoomFactor = zoomFactor;
	}

	public PaperSize getPaperSize() {
		return paperSize;
	}

	public ViewportDimensions getDimensions() {
		return dimensions;
	}

	public RenderFormat getRenderFormat() {
		return renderFormat;
	}

	public Long getJsRenderingWait() {
		return jsRenderingWait;
	}

	public Long getJsExitingWait() {
		return jsExitingWait;
	}

	public Double getZoomFactor() {
		return zoomFactor;
	}

	public RenderOptions withPaperSize(PaperSize paperSize) {
		return new RenderOptions(paperSize, dimensions, renderFormat, jsRenderingWait, jsExitingWait, zoomFactor);
	}

	public RenderOptions withDimensions(ViewportDimensions dimensions) {
		return new RenderOptions(paperSize, dimensions, renderFormat, jsRenderingWait, jsExitingWait, zoomFactor);
	}

	public RenderOptions withRenderFormat(RenderFormat renderFormat) {
		return new RenderOptions(paperSize, dimensions, renderFormat, jsRenderingWait, jsExitingWait, zoomFactor);
	}

	public RenderOptions withJavaScriptExecutionDetails(Long jsRenderingWait, Long jsExitingWait) {
		return new RenderOptions(paperSize, dimensions, renderFormat, jsRenderingWait, jsExitingWait, zoomFactor);
	}

	public RenderOptions withZoomFactor(Double zoomFactor) {
		return new RenderOptions(paperSize, dimensions, renderFormat, jsRenderingWait, jsExitingWait, zoomFactor);
	}
}
