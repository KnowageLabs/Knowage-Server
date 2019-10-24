package it.eng.knowage.slimerjs.wrapper.beans;

import it.eng.knowage.slimerjs.wrapper.enums.RenderFormat;

public class RenderOptions {
	private static final RenderOptions EMPTY = new RenderOptions(null, null, null, null, null, null, null, null, null, null);

	public static final RenderOptions defaultOptions() {

		ViewportDimensions defaulViewportDimensions = ViewportDimensions.builder().build();

		return EMPTY.withDimensions(defaulViewportDimensions).withRenderFormat(RenderFormat.PNG).withJavaScriptExecutionDetails(60000L, 5000L)
				.withCustomHeaders(CustomHeaders.EMPTY).withZoomFactor(2.0);
	}

	private final PaperSize paperSize;
	private final ViewportDimensions dimensions;
	private final Margin margin;
	private final BannerInfo headerInfo;
	private final BannerInfo footerInfo;
	private final RenderFormat renderFormat;
	private final CustomHeaders customHeaders;
	private final Long jsRenderingWait;
	private final Long jsExitingWait;
	private final Double zoomFactor;

	private RenderOptions(PaperSize paperSize, ViewportDimensions dimensions, Margin margin, BannerInfo headerInfo, BannerInfo footerInfo,
			RenderFormat renderFormat, CustomHeaders customHeaders, Long jsRenderingWait, Long jsExitingWait, Double zoomFactor) {
		this.paperSize = paperSize;
		this.dimensions = dimensions;
		this.margin = margin;
		this.headerInfo = headerInfo;
		this.footerInfo = footerInfo;
		this.renderFormat = renderFormat;
		this.customHeaders = customHeaders;
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

	public Margin getMargin() {
		return margin;
	}

	public BannerInfo getHeaderInfo() {
		return headerInfo;
	}

	public BannerInfo getFooterInfo() {
		return footerInfo;
	}

	public RenderFormat getRenderFormat() {
		return renderFormat;
	}

	public CustomHeaders getCustomHeaders() {
		return customHeaders;
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
		return new RenderOptions(paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withDimensions(ViewportDimensions dimensions) {
		return new RenderOptions(paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withMargin(Margin margin) {
		return new RenderOptions(paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withHeaderInfo(BannerInfo headerInfo) {
		return new RenderOptions(paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withFooterInfo(BannerInfo footerInfo) {
		return new RenderOptions(paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withRenderFormat(RenderFormat renderFormat) {
		return new RenderOptions(paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withCustomHeaders(CustomHeaders customHeaders) {
		return new RenderOptions(paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withJavaScriptExecutionDetails(Long jsRenderingWait, Long jsExitingWait) {
		return new RenderOptions(paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withZoomFactor(Double zoomFactor) {
		return new RenderOptions(paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}
}
