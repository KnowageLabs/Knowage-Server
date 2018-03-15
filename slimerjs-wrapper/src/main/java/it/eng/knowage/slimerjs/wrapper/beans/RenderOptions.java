package it.eng.knowage.slimerjs.wrapper.beans;

import it.eng.knowage.slimerjs.wrapper.enums.RenderFormat;

public class RenderOptions {
	public static final RenderOptions EMPTY = new RenderOptions(null, null, null, null, null, null, null, null, null, null, null);
	public static final RenderOptions DEFAULT = EMPTY.withDimensions(ViewportDimensions.VIEW_1600_1200).withRenderFormat(RenderFormat.PNG)
			.withJavaScriptExecutionDetails(10000L, 10000L).withSlimerJSOptions(SlimerJSOptions.DEFAULT.withDiskCache(true))
			.withCustomHeaders(CustomHeaders.EMPTY).withZoomFactor(2.0);

	private final SlimerJSOptions options;
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

	private RenderOptions(SlimerJSOptions options, PaperSize paperSize, ViewportDimensions dimensions, Margin margin, BannerInfo headerInfo,
			BannerInfo footerInfo, RenderFormat renderFormat, CustomHeaders customHeaders, Long jsRenderingWait, Long jsExitingWait, Double zoomFactor) {
		this.options = options;
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

	public SlimerJSOptions getOptions() {
		return options;
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

	public RenderOptions withSlimerJSOptions(SlimerJSOptions options) {
		return new RenderOptions(options, paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withPaperSize(PaperSize paperSize) {
		return new RenderOptions(options, paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withDimensions(ViewportDimensions dimensions) {
		return new RenderOptions(options, paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withMargin(Margin margin) {
		return new RenderOptions(options, paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withHeaderInfo(BannerInfo headerInfo) {
		return new RenderOptions(options, paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withFooterInfo(BannerInfo footerInfo) {
		return new RenderOptions(options, paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withRenderFormat(RenderFormat renderFormat) {
		return new RenderOptions(options, paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withCustomHeaders(CustomHeaders customHeaders) {
		return new RenderOptions(options, paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withJavaScriptExecutionDetails(Long jsRenderingWait, Long jsExitingWait) {
		return new RenderOptions(options, paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}

	public RenderOptions withZoomFactor(Double zoomFactor) {
		return new RenderOptions(options, paperSize, dimensions, margin, headerInfo, footerInfo, renderFormat, customHeaders, jsRenderingWait, jsExitingWait,
				zoomFactor);
	}
}
