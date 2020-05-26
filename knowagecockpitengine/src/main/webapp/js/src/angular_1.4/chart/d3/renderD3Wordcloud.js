/*In this file is used code that is distribuited uner the license:

Copyright (c) 2013, Jason Davies.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * The name Jason Davies may not be used to endorse or promote products
    derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL JASON DAVIES BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

/**
 * The rendering function for the WORDCLOUD chart.
 * @param chartConf JSON containing data (parameters) about the chart.
 * @param locale Information about the locale (language). Needed for the formatting of the series values (data labels and tooltips).
 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */
function renderWordCloud(chartConf,panel,handleCockpitSelection,locale, handleCrossNavigationTo){

	var maxic = 0;
	var minValue=0;
	var maxWordLength = 0;

	for (var i=0; i<chartConf.data[0].length; i++){

		if (chartConf.data[0][i].value > maxic){

			maxic = chartConf.data[0][i].value;

		}

        if (chartConf.data[0][i].value < minValue){

			minValue = chartConf.data[0][i].value;

		}
        if (chartConf.data[0][i].name.length > maxWordLength){

        	maxWordLength = chartConf.data[0][i].name.length;

		}

	}

	/**
	 * Normalize height and/or width of the chart if the dimension type for that dimension is
	 * "percentage". This way the chart will take the appropriate percentage of the screen's
	 * particular dimension (height/width).
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */

	if (chartConf.chart.heightDimType == "percentage") {
    	var heightNormalized = chartConf.chart.height ? panel.offsetHeight*Number(chartConf.chart.height)/100 : panel.offsetHeight;
	}
	else {
		var heightNormalized = chartConf.chart.height ? Number(chartConf.chart.height) : panel.offsetHeight;
	}

	if (chartConf.chart.widthDimType == "percentage") {
		var widthNormalized = chartConf.chart.width ? panel.offsetWidth*Number(chartConf.chart.width)/100 : panel.offsetWidth;
	}
	else {
		var widthNormalized = chartConf.chart.width ? Number(chartConf.chart.width) : panel.offsetWidth;
	}

	/**
     * Correction for width and height if the other one is fixed and bigger than the window dimension value.
     * E.g. if the height of the chart is higher than the height of the window height, the width needs to
     * be corrected, since the vertical scrollbar appears. Without this correction, the chart will be cut
     * and not entirely presented, and the horizontal scrollbar will be present as well (and it should not
     * be, since the width should just expand as much as the window is wide).
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    var widthCorrection = 0, heightCorrection = 0, overflowXHidden = "auto", overflowYHidden = "auto";

    if (!chartConf.chart.isCockpit && heightNormalized > panel.offsetHeight && widthNormalized==panel.offsetWidth) {
    	widthCorrection = 16;
    	overflowXHidden = "hidden";
    }

    if (!chartConf.chart.isCockpit && widthNormalized > panel.offsetWidth && heightNormalized==panel.offsetHeight) {
    	heightCorrection = 16;
    	overflowYHidden = "hidden";
    }

		(function() {

			function cloud() {
				var size = [widthNormalized-widthCorrection, heightNormalized-(Number(removePixelsFromFontSize(chartConf.title.style.fontSize))
						+Number(removePixelsFromFontSize(chartConf.subtitle.style.fontSize)))*1.6],
				text = cloudText,
				font = cloudFont,
				fontSize = cloudFontSize,
				fontStyle = cloudFontNormal,
				fontWeight = cloudFontNormal,
				rotate = cloudRotate,
				padding = cloudPadding,
				spiral = archimedeanSpiral,
				words = [],
		     	timeInterval = Infinity,
				event = d3.dispatch("word", "end"),
				timer = null,
				cloud = {};
                w=timeInterval;
                h=fontSize;
				cloud.start = function() {

					var board = zeroArray((size[0] >> 5) * size[1]),
					bounds = null,
					n = Math.min(words.length, chartConf.chart.maxWords),
					i = -1,
					tags = [],
					data = words.map(function(d, i) {
						d.text = text.call(this, d, i);
						d.font = font.call(this, d, i);
						d.style = fontStyle.call(this, d, i);
						d.weight = fontWeight.call(this, d, i);
						d.rotate = rotate.call(this, d, i);
						d.size = ~~fontSize.call(this, d, i);
						d.padding = padding.call(this, d, i);
						return d;
					}).sort(function(a, b) { return b.size - a.size; });

					if (timer) clearInterval(timer);
					timer = setInterval(step, 0);
					step();

					return cloud;

					function step() {
						var start = +new Date,
						d;
						while (+new Date - start < timeInterval && ++i < n && timer) {

							d = data[i];

					  var condition;

					  if(chartConf.chart.preventOverlap){
						    d.x = (size[0]) >> 1;
					        d.y = (size[1]) >> 1;

					  } else{
						  d.x = (size[0] * (Math.random() + .5)) >> 1;
		                    d.y = (size[1] * (Math.random() + .5)) >> 1;

					  }


						cloudSprite(d, data, i);
							if(chartConf.chart.preventOverlap){
								condition=d.hasText && place(board, d, bounds);

							}else{
								condition=d.hasText;
							}

							if (condition) {
								tags.push(d);
								event.word(d);
								if (bounds) cloudBounds(bounds, d);
								else bounds = [{x: d.x + d.x0, y: d.y + d.y0}, {x: d.x + d.x1, y: d.y + d.y1}];
								// Temporary hack
								d.x -= size[0] >> 1;
								d.y -= size[1] >> 1;
							}
						}
						if (i >= n) {
							cloud.stop();

							event.end(tags, bounds);
						}
					}
				}

				cloud.stop = function() {
					if (timer) {
						clearInterval(timer);
						timer = null;
					}
					return cloud;
				};

				cloud.timeInterval = function(x) {
					if (!arguments.length) return timeInterval;
					timeInterval = x == null ? Infinity : x;
					return cloud;
				};

				function place(board, tag, bounds) {

					var perimeter = [{x: 0, y: 0}, {x: size[0], y: size[1]}],
					startX = tag.x,
					startY = tag.y,
					maxDelta = Math.sqrt(size[0] * size[0] + size[1] * size[1]),
					s = spiral(size),
					dt = Math.random() < .5 ? 1 : -1,
							t = -dt,
							dxdy,
							dx,
							dy;

					while (dxdy = s(t += dt)) {
						dx = ~~dxdy[0];
						dy = ~~dxdy[1];

						if (Math.min(dx, dy) > maxDelta) break;

						tag.x = startX + dx;
						tag.y = startY + dy;


						if (tag.x + tag.x0 < 0 || tag.y + tag.y0 < 0 ||
								tag.x + tag.x1 > size[0] || tag.y + tag.y1 > size[1]) continue;

						// TODO only check for collisions within current bounds.
						if (!bounds || !cloudCollide(tag, board, size[0])) {
							if (!bounds || collideRects(tag, bounds)) {
								var sprite = tag.sprite,
								w = tag.width >> 5,
								sw = size[0] >> 5,
								lx = tag.x - (w << 4),
								sx = lx & 0x7f,
								msx = 32 - sx,
								h = tag.y1 - tag.y0,
								x = (tag.y + tag.y0) * sw + (lx >> 5),
								last;
								for (var j = 0; j < h; j++) {
									last = 0;
									for (var i = 0; i <= w; i++) {
										board[x + i] |= (last << msx) | (i < w ? (last = sprite[j * w + i]) >>> sx : 0);
									}
									x += sw;
								}
								delete tag.sprite;
								return true;
							}
						}
					}
					return false;
				}

				cloud.words = function(x) {
					if (!arguments.length) return words;
					words = x;
					return cloud;
				};

				cloud.size = function(x) {
					if (!arguments.length) return size;
					size = [+x[0], +x[1]];
					return cloud;
				};

				cloud.font = function(x) {
					if (!arguments.length) return font;
					font = d3.functor(x);
					return cloud;
				};

				cloud.fontStyle = function(x) {
					if (!arguments.length) return fontStyle;
					fontStyle = d3.functor(x);
					return cloud;
				};

				cloud.fontWeight = function(x) {
					if (!arguments.length) return fontWeight;
					fontWeight = d3.functor(x);
					return cloud;
				};

				cloud.rotate = function(x) {
					if (!arguments.length) return rotate;
					rotate = d3.functor(x);
					return cloud;

				};

				cloud.text = function(x) {
					if (!arguments.length) return text;
					text = d3.functor(x);
					return cloud;
				};

				cloud.spiral = function(x) {
					if (!arguments.length) return spiral;
					spiral = spirals[x + ""] || x;
					return cloud;
				};

				cloud.fontSize = function(x) {
					if (!arguments.length) return fontSize;
					fontSize = d3.functor(x);
					return cloud;
				};

				cloud.padding = function(x) {
					if (!arguments.length) return padding;
					padding = d3.functor(x);
					return cloud;
				};

				return d3.rebind(cloud, event, "on");
			}

			function cloudText(d) {
				return d.text;
			}

			function cloudFont() {
				return "serif";
			}

			function cloudFontNormal() {
				return "normal";
			}

			function cloudFontSize(d) {
				return Math.sqrt(d.value);
			}

			function cloudRotate() {
				return (~~(Math.random() * 6) - 3) * 30;
			}

			function cloudPadding() {
				return 1;
			}

			// Fetches a monochrome sprite bitmap for the specified text.
			// Load in batches for speed.
			function cloudSprite(d, data, di) {
				if (d.sprite) return;
				c.clearRect(0, 0, (cw << 5) / ratio, ch / ratio);
				var x = 0,
				y = 0,
				maxh = 0,
				n = data.length;
				--di;
				while (++di < n) {
					d = data[di];
					c.save();
					c.font = d.style + " " + d.weight + " " + ~~((d.size + 1) / ratio) + "px " + d.font;
					var w = c.measureText(d.text + "m").width * ratio,
					h = d.size << 1;

					if (d.rotate)
					{
						var sr = Math.sin(d.rotate * cloudRadians),
						cr = Math.cos(d.rotate * cloudRadians),
						wcr = w * cr,
						wsr = w * sr,
						hcr = h * cr,
						hsr = h * sr;
						w = (Math.max(Math.abs(wcr + hsr), Math.abs(wcr - hsr)) + 0x1f) >> 5 << 5;
						h = ~~Math.max(Math.abs(wsr + hcr), Math.abs(wsr - hcr));
					}
					else
					{
						w = (w + 0x1f) >> 5 << 5;
					}

					if (h > maxh)
						maxh = h;
					if (x + w >= (cw << 5)) {
						x = 0;
						y += maxh;
						maxh = 0;
					}
					if (y + h >= ch) break;
					c.translate((x + (w >> 1)) / ratio, (y + (h >> 1)) / ratio);
					if (d.rotate) c.rotate(d.rotate * cloudRadians);
					c.fillText(d.text, 0, 0);
					if (d.padding) c.lineWidth = 2 * d.padding, c.strokeText(d.text, 0, 0);
					c.restore();
					d.width = w;
					d.height = h;
					d.xoff = x;
					d.yoff = y;
					d.x1 = w >> 1;
					d.y1 = h >> 1;
					d.x0 = -d.x1;
					d.y0 = -d.y1;
					d.hasText = true;
					x += w;
				}
				var pixels = c.getImageData(0, 0, (cw << 5) / ratio, ch / ratio).data,
				sprite = [];
				while (--di >= 0) {
					d = data[di];
					if (!d.hasText) continue;
					var w = d.width,
					w32 = w >> 5,
					h = d.y1 - d.y0;
					// Zero the buffer
					for (var i = 0; i < h * w32; i++) sprite[i] = 0;
					x = d.xoff;
					if (x == null) return;
					y = d.yoff;
					var seen = 0,
					seenRow = -1;
					for (var j = 0; j < h; j++) {
						for (var i = 0; i < w; i++) {
							var k = w32 * j + (i >> 5),
							m = pixels[((y + j) * (cw << 5) + (x + i)) << 2] ? 1 << (31 - (i % 32)) : 0;
							sprite[k] |= m;
							seen |= m;
						}
						if (seen) seenRow = j;
						else {
							d.y0++;
							h--;
							j--;
							y++;
						}
					}
					d.y1 = d.y0 + seenRow;
					d.sprite = sprite.slice(0, (d.y1 - d.y0) * w32);
				}
			}

			// Use mask-based collision detection.
			function cloudCollide(tag, board, sw) {
				sw >>= 5;
				var sprite = tag.sprite,
				w = tag.width >> 5,
				lx = tag.x - (w << 4),
				sx = lx & 0x7f,
				msx = 32 - sx,
				h = tag.y1 - tag.y0,
				x = (tag.y + tag.y0) * sw + (lx >> 5),
				last;
				for (var j = 0; j < h; j++) {
					last = 0;
					for (var i = 0; i <= w; i++) {
						if (((last << msx) | (i < w ? (last = sprite[j * w + i]) >>> sx : 0))
								& board[x + i]) return true;
					}
					x += sw;
				}
				return false;
			}

			function cloudBounds(bounds, d) {
				var b0 = bounds[0],
				b1 = bounds[1];
				if (d.x + d.x0 < b0.x) b0.x = d.x + d.x0;
				if (d.y + d.y0 < b0.y) b0.y = d.y + d.y0;
				if (d.x + d.x1 > b1.x) b1.x = d.x + d.x1;
				if (d.y + d.y1 > b1.y) b1.y = d.y + d.y1;
			}

			function collideRects(a, b) {
				return a.x + a.x1 > b[0].x && a.x + a.x0 < b[1].x && a.y + a.y1 > b[0].y && a.y + a.y0 < b[1].y;
			}

			function archimedeanSpiral(size) {
				var e = size[0] / size[1];
				return function(t) {
					return [e * (t *= .1) * Math.cos(t), t * Math.sin(t)];
				};
			}

			function rectangularSpiral(size) {
				var dy = 4,
				dx = dy * size[0] / size[1],
				x = 0,
				y = 0;
				return function(t) {
					var sign = t < 0 ? -1 : 1;
					// See triangular numbers: T_n = n * (n + 1) / 2.
					switch ((Math.sqrt(1 + 4 * sign * t) - sign) & 3) {
					case 0:  x += dx; break;
					case 1:  y += dy; break;
					case 2:  x -= dx; break;
					default: y -= dy; break;
					}
					return [x, y];
				};
			}

			// TODO reuse arrays?
			function zeroArray(n) {
				var a = [],
				i = -1;
				while (++i < n) a[i] = 0;
				return a;
			}

			var cloudRadians = Math.PI / 180,
			cw = 1 << 11 >> 5,
			ch = 1 << 11,
			canvas,
			ratio = 1;

			if (typeof document !== "undefined") {
				canvas = document.createElement("canvas");
				canvas.width = 1;
				canvas.height = 1;
				ratio = Math.sqrt(canvas.getContext("2d").getImageData(0, 0, 1, 1).data.length >> 2);
				canvas.width = (cw << 5) / ratio;
				canvas.height = ch / ratio;
			} else {
				// Attempt to use node-canvas.
				canvas = new Canvas(cw << 5, ch);
			}

			var c = canvas.getContext("2d"),
			spirals = {
				archimedean: archimedeanSpiral,
				rectangular: rectangularSpiral
			};
			c.fillStyle = c.strokeStyle = "red";
			c.textAlign = "center";

			if (typeof module === "object" && module.exports) module.exports = cloud;
			else (d3.layout || (d3.layout = {})).cloud = cloud;
		})();

		var actualCloudSizeHeight = heightNormalized-(Number(removePixelsFromFontSize(chartConf.title.style.fontSize))
				+Number(removePixelsFromFontSize(chartConf.subtitle.style.fontSize)))*1.6;


		var actualCloudSize = Math.min(actualCloudSizeHeight, widthNormalized);
		var maxfontsize=0;
		if(maxWordLength<8){
			maxfontsize = Math.round(actualCloudSize/5);
		}
		if(8<=maxWordLength<12){
			maxfontsize= Math.round(actualCloudSize/6.67);
		}
		if(maxWordLength>=12){
			maxfontsize = Math.round(actualCloudSize/10);

		}
		if(chartConf.chart.maxFontSize!=10) maxfontsize = chartConf.chart.maxFontSize

		var minfontsize=Math.round(actualCloudSize/50);

		if(chartConf.chart.minFontSize!=5) minfontsize = chartConf.chart.minFontSize
		var fill = d3.scale.category20();

		var wordFontSize= d3.scale.linear().domain([minValue,maxic]).range([minfontsize,maxfontsize]);

		var layout=d3.layout.cloud().size([widthNormalized-widthCorrection, heightNormalized-(Number(removePixelsFromFontSize(chartConf.title.style.fontSize))
				+Number(removePixelsFromFontSize(chartConf.subtitle.style.fontSize)))*1.6])
		.words(chartConf.data[0].map(function(d) {
			 wordSize= wordFontSize(d.value);
			 return {text: d.name, size: wordSize, categoryId:d.categoriId };
		}))
		.padding(chartConf.chart.wordPadding)
		.rotate(function() {
			if(chartConf.chart.wordLayout==='horizontalAndVertical'){
				return 90*Math.round(Math.random());
			}

			if(chartConf.chart.minAngle==chartConf.chart.maxAngle){
				return chartConf.chart.minAngle;
			}
			var angle = (Math.random() * 2) * 90;
			while ((angle < chartConf.chart.minAngle || angle > chartConf.chart.maxAngle)){
				angle = (Math.random() * 2) * 90;
			}
			return angle })
		.font("Impact")
		.fontSize(function(d) { return d.size; })
		.on("end", draw);

		layout.start();

		function draw(words,e) {
			panel.innerText = '';
			var randomId= Math.round((Math.random())*10000);

			/**
			 * Create an invisible HTML form that will sit on the page where the chart (in this case, the WORDCLOUD) is rendered.
			 * This form will serve us as a media through which the data and customization for the rendered chart will be sent
			 * towards the Highcharts exporting service that will take the HTML of the WORDCLOUD chart, render it and take a snapshot
			 * that will be sent back towards the client (our browser) in a proper format (PDF or PNG) and downloaded to the local
			 * machine.
			 *
			 * This way, when the user clicks on the export option for the rendered chart, the JS code ("chartExecutionController.js")
			 * that fills the form (that we set here as a blank structure) will eventually submit it towards the Highcharts export
			 * service. The result is the exported chart. This code will catch the form by the ID that we set here.
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			d3.select(panel)
				.append("form").attr("id","export-chart-form").style("margin","0px")
				.append("input").attr("type","hidden").attr("name","options")
				.append("input").attr("type","hidden").attr("name","content")
				.append("input").attr("type","hidden").attr("name","type")
				.append("input").attr("type","hidden").attr("name","width")
				.append("input").attr("type","hidden").attr("name","constr")
				.append("input").attr("type","hidden").attr("name","async")
				.append("input").attr("type","hidden").attr("name","chartHeight")
				.append("input").attr("type","hidden").attr("name","chartWidth");


			/**
			 * The body inside of which the chart will be rendered.
			 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			d3.select(panel)
				.style("overflow-x",overflowXHidden)
				.style("overflow-y",overflowYHidden)
				.append("div").attr("id","main"+randomId)
				.attr("class","d3-container")
				.style("margin","auto")	// Center chart horizontally (Danilo Ristovski)
				.style("height",heightNormalized-10)
				.style("width",widthNormalized-widthCorrection-10)
				.style("font-family", chartConf.chart.style.fontFamily)
				.style("font-style", chartConf.chart.style.fontStyle)
	    		.style("font-weight", chartConf.chart.style.fontWeight)
	    		.style("text-decoration", chartConf.chart.style.textDecoration)
				.style("background-color", chartConf.chart.style.backgroundColor); // danristo

			if (chartConf.data[0].length < 1)
			{
				var emptyMsgFontSize = parseInt(chartConf.emptymessage.style.fontSize);
				//var emptyMsgDivHeight = parseInt(chartConf.emptymessage.height);
				//var emptyMsgTotal = emptyMsgDivHeight+emptyMsgFontSize/2;
				var emptyMsgTotal = emptyMsgFontSize;

				var emptyMsgFontWeight = null;
				var emptyMsgFontStyle = null;
				var emptyMsgTextDecoration = null;

				/**
				 * For EMPTYMESSAGE tag
				 */
				emptyMsgFontWeight = (chartConf.emptymessage.style.fontWeight == "bold" || chartConf.chart.style.fontWeight == "bold") ? "bold" : "none";

				if (chartConf.emptymessage.style.fontStyle == "italic" || chartConf.chart.style.fontStyle == "italic")
				{
					emptyMsgFontStyle = "italic";
				}
				else if (chartConf.emptymessage.style.fontStyle == "normal" || chartConf.chart.style.fontStyle == "normal")
				{
					emptyMsgFontStyle = "normal";
				}

				emptyMsgTextDecoration = (chartConf.emptymessage.style.textDecoration == "underline" || chartConf.chart.style.textDecoration == "underline") ? "underline" : "none";

				var emptyMsgAlignment = null;
				if(chartConf.emptymessage.style.align == "left") {
					emptyMsgAlignment = "flex-start";
				} else if (chartConf.emptymessage.style.align == "right") {
					emptyMsgAlignment = "flex-end";
				} else {
					emptyMsgAlignment = "center";
				}

				// Set empty message
				d3.select(panel)
					.style("display","flex")
					.style("align-items","center")
					.style("justify-content",emptyMsgAlignment)
					.style("color",chartConf.emptymessage.style.color)
		    		.style("font-family",chartConf.emptymessage.style.fontFamily)
		    		.style("font-style", emptyMsgFontStyle)
		    		.style("font-weight", emptyMsgFontWeight)
		    		.style("text-decoration", emptyMsgTextDecoration)
		    		.style("font-size",emptyMsgFontSize)
					.text(chartConf.emptymessage.text);
			}
			else
			{
				var titleFontWeight = null;
				var titleFontStyle = null;
				var titleTextDecoration = null;

				var subtitleFontWeight = null;
				var subtitleFontStyle = null;
				var subtitleTextDecoration = null;

				/**
				 * For TITLE tag
				 * @author: danristo (danilo.ristovski@mht.net)
				 */
				titleFontWeight = (chartConf.title.style.fontWeight == "bold" || chartConf.chart.style.fontWeight == "bold") ? "bold" : "none";

				if (chartConf.title.style.fontStyle == "italic" || chartConf.chart.style.fontStyle == "italic")
				{
					titleFontStyle = "italic";
				}
				else if (chartConf.title.style.fontStyle == "normal" || chartConf.chart.style.fontStyle == "normal")
				{
					titleFontStyle = "normal";
				}

				titleTextDecoration = (chartConf.title.style.textDecoration == "underline" || chartConf.chart.style.textDecoration == "underline") ? "underline" : "none";

				/**
				 * For SUBTITLE TAG
				 * @author: danristo (danilo.ristovski@mht.net)
				 */
				subtitleFontWeight = (chartConf.subtitle.style.fontWeight == "bold" || chartConf.chart.style.fontWeight == "bold") ? "bold" : "none";

				if (chartConf.subtitle.style.fontStyle == "italic" || chartConf.chart.style.fontStyle == "italic")
				{
					subtitleFontStyle = "italic";
				}
				else if (chartConf.subtitle.style.fontStyle == "normal" || chartConf.chart.style.fontStyle == "normal")
				{
					subtitleFontStyle = "normal";
				}

				subtitleTextDecoration = (chartConf.subtitle.style.textDecoration == "underline" || chartConf.chart.style.textDecoration == "underline") ? "underline" : "none";

				// Set title
				d3.select("#main"+ randomId).append("div")
					.style("color",chartConf.title.style.color)
					.style("text-align",chartConf.title.style.align)
		    		.style("font-family",chartConf.title.style.fontFamily)
		    		.style("font-size",chartConf.title.style.fontSize)
		    		.style("font-style",titleFontStyle)
		    		.style("font-weight",titleFontWeight)
		    		.style("text-decoration", titleTextDecoration)
					.text(chartConf.title.text);

				// Set subtitle
				d3.select("#main"+randomId).append("div")
					.style("color",chartConf.subtitle.style.color)
					.style("text-align",chartConf.subtitle.style.align)
		    		.style("font-family",chartConf.subtitle.style.fontFamily)
		    		.style("font-style", subtitleFontStyle)
		    		.style("font-weight", subtitleFontWeight)
		    		.style("text-decoration", subtitleTextDecoration)
		    		.style("font-size",chartConf.subtitle.style.fontSize)
					.text(chartConf.subtitle.text);



		var bacground=d3.select("#main"+randomId)
			.append("div").attr("id","chart"+randomId).attr("class","d3chartclass")
			.append("svg")
            .attr("width", widthNormalized-widthCorrection-10)
			.attr("height", heightNormalized-10-(Number(removePixelsFromFontSize(chartConf.title.style.fontSize))
					+Number(removePixelsFromFontSize(chartConf.subtitle.style.fontSize)))*1.6);


		var tooltip=d3.select("#chart"+randomId)
			.append("div")
			.attr("class","tooltip")
			.style("opacity","0");


		var tooltipBackgroundColor=(chartConf.tooltip.backgroundColor)?chartConf.tooltip.backgroundColor:"rgba(255, 255, 255, 0.85)";
		var tolltipFontStyle=chartConf.tooltip.fontStyle?chartConf.tooltip.fontStyle:'';
		var tolltipFontWeight=chartConf.tooltip.fontWeight?chartConf.tooltip.fontWeight:'';
	    var tolltipTextDecoration=chartConf.tooltip.textDecoration?chartConf.tooltip.textDecoration:'';
	    var tooltipBorderWidth=chartConf.tooltip.borderWidth?chartConf.tooltip.borderWidth:'1';
	    var tooltipBorderRadius=chartConf.tooltip.borderRadius?chartConf.tooltip.borderRadius:'3';

	    d3.select(panel).selectAll(".tooltip")
			.style("position","fixed")
			.style("text-align",chartConf.tooltip.align)
			.style("min-width",10)
			.style("max-width",1000)
			.style("min-height",10)
			.style("max-height",800)
			.style("padding",3)
			.style("color",chartConf.tooltip.fontColor)
			.style("font-size",chartConf.tooltip.fontSize)
			.style("font-family",chartConf.tooltip.fontFamily)
			.style("border",tooltipBorderWidth+"px solid")	// @modifiedBy: danristo (danilo.ristovski@mht.net)
			.style("border-color",chartConf.tooltip.fontColor)
			.style("border-radius",tooltipBorderRadius+"px")
			.style("pointer-events","none")
			.style("background-color",tooltipBackgroundColor)
			.style("font-style",tolltipFontStyle)
			.style("font-weight",tolltipFontWeight)
			.style("text-decoration",tolltipTextDecoration);

		    var wordArea=bacground
			.append("g")
			//.attr("transform", "translate("+(chartConf.chart.width/2-40)+","+(chartConf.chart.height/2-10)+")")
			.attr("transform", "translate(" + layout.size()[0] / 2 + "," + layout.size()[1] / 2 + ")")
			.selectAll("text")
			.data(words)
			.enter()
			.append("text")
			.style("font-size", "1px")
			.style("font-family",chartConf.chart.style.fontFamily)
			.style("font-style", chartConf.chart.style.fontStyle)
    		.style("font-weight", chartConf.chart.style.fontWeight)
    		.style("text-decoration", chartConf.chart.style.textDecoration)
			.style("fill", function(d, i) { return fill(i); })
			.attr("text-anchor", "middle");


			wordArea.transition().duration(1e3)
				.attr("transform", function(d) {
				return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";})
				.style("font-size", function(d) { return d.size + "px"; })
				.text(function(d) { return d.text.toLowerCase(); })


			wordArea.on('click', function(d){

				if(chartConf.chart.isCockpit==true){
					if(chartConf.chart.outcomingEventsEnabled){

					paramethers=fetchParamethers(d);
					var selectParam_cross={
							categoryName:paramethers.categoryName,
							categoryValue:paramethers.categoryValue,
							serieName:paramethers.serieName,
							serieValue:paramethers.serieValue,
							groupingCategoryName:paramethers.groupingCategoryName,
							groupingCategoryValue:paramethers.groupingCategoryValue	,
							categoryId: d.categoryId
					};
					var	selectParam=fetchSelectionParamethers(d);
					selectParam.selectParam_cross = selectParam_cross;
					handleCockpitSelection(selectParam);
					}
				}else if(chartConf.crossNavigation.hasOwnProperty('crossNavigationDocumentName')){
					paramethers=fetchParamethers(d);
					var navigParams={
						crossNavigationDocumentName:chartConf.crossNavigation.crossNavigationDocumentName,
						crossNavigationDocumentParams:chartConf.crossNavigation.crossNavigationDocumentParams,
						categoryName:paramethers.categoryName,
						categoryValue:paramethers.categoryValue,
						serieName:paramethers.serieName,
						serieValue:paramethers.serieValue,
						groupingCategoryName:paramethers.groupingCategoryName,
						groupingCategoryValue:paramethers.groupingCategoryValue
					};

					handleCrossNavigationTo(navigParams);
				}
				/**
				 * Implementation for the new Cross Navigation Definition interface.
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				else {

					/**
					 * Collect all needed data for the cross-navigation (all output parameters for the WORDCLOUD chart document) and
					 * forward them towards the cross-navigation handler. The data represents all the output parameter categories and
					 * the series item value and name pairs.
					 */
					var outputParams = fetchParamethers(d);
					handleCrossNavigationTo(outputParams);

				}

			});

			wordArea.on('mouseover',function(d){

				var tooltipText;

				for(j=0;j<chartConf.data[0].length;j++){

					if(chartConf.data[0][j].name===d.text) {

						/**
						 * The displaying of the numeric (series) values in the table of the WORDCLOUD chart is redefined, so now it considers the
						 * precision, prefix, suffix (postfix), thousands separator, formatting localization and scale factor. [JIRA 1060 and 1061]
						 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						 */
						var value = Number(chartConf.data[0][j].value);

						var text = "";
						tooltipText = chartConf.data[0][j].value;

						var prefix = chartConf.tooltip.prefix;
						var postfix = chartConf.tooltip.postfix;
						var precision = chartConf.tooltip.precision;
						var scaleFactor = chartConf.tooltip.scaleFactor;

						if (prefix) {
				 		   text += prefix +" ";
				 	   	}

				   	   	/*
			   	    		The scaling factor of the current series item can be empty (no scaling - pure (original) value) or "k" (kilo), "M" (mega),
			   	    		"G" (giga), "T" (tera), "P" (peta), "E" (exa). That means we will scale our values according to this factor and display
			   	    		these abbreviations (number suffix) along with the scaled number. Apart form the scaling factor, the thousands separator
			   	    		is included into the formatting of the number that is going to be displayed, as well as precision.
				   	    	@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			   			*/
				   		switch(scaleFactor.toUpperCase()) {

				   	  		case "EMPTY":
				   	  			text += value.toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision});
				   	  			break;

				   	  		case "K":
				   	  			text += (value/Math.pow(10,3)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision});
				   	  			text += "k";
				   	  			break;

				  			case "M":
				  				text += (value/Math.pow(10,6)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision});
				   	  			text += "M";
				   	  			break;

				  			case "G":
				   	  			text += (value/Math.pow(10,9)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision});
				   	  			text += "G";
				   	  			break;

							case "T":
								text += (value/Math.pow(10,12)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
								text += "T";
				   	  			break;

							case "P":
				   	  			text += (value/Math.pow(10,15)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
				   	  			text += "P";
				   	  			break;

							case "E":
			   					text += (value/Math.pow(10,18)).toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
			   					text += "E";
				   	  			break;

							default:
								text += value.toLocaleString(locale,{ minimumFractionDigits: precision, maximumFractionDigits: precision });
				   	  			break;

			   	  		}

			   			text += (postfix!="" ? " " : "") + postfix;

					}

				}

				tooltip.transition().duration(50).style("opacity","1");

				var ttText = tooltip.text(text);

				/**
				 * Call the function that enables positioning of the
				 * tooltip according to its dimensions.
				 */
				var chartHeight = Number(heightNormalized);
				var chartWidth = Number(widthNormalized-widthCorrection);

				positionTheTooltip(d3.event.layerX,d3.event.layerY,ttText);

//					.style("left", (d3.event.pageX) + "px")
//					.style("top", (d3.event.pageY - 25) + "px");

			});

			wordArea.on('mouseleave',function(d){

				tooltip.transition().duration(50).style("opacity","0");

			});

			}
		}

		function fetchSelectionParamethers(d){

			var param={};

			for(j=0; j<chartConf.data[0].length; j++) {

				if(chartConf.data[0][j].name === d.text) {

					//param.categoryValue=chartConf.data[0][j].name;
					//param.categoryName= chartConf.data[0][j].categoryName;
					//param.serieValue=chartConf.data[0][j].value;

					for(k=0; k<chartConf.data[0][j].categoryName.length; k++){
						param[chartConf.data[0][j].categoryName[k]] = chartConf.data[0][j].name;
					}

				}

			}

			return param;
		}

		// OLD IMPLEMENTATION: commented by danristo
//		function fetchParamethers(d){
//
//			var param={
//					"categoryName" : null,
//					"categoryValue":null,
//					"serieName":null,
//					"serieValue":null,
//					"groupingCategoryName":null,
//					"groupingCategoryValue":null
//			};
//			for(j=0;j<chartConf.data[0].length;j++){
//				if(chartConf.data[0][j].name===d.text){
//					param.categoryValue=chartConf.data[0][j].name;
//					param.serieValue=chartConf.data[0][j].value;
//				}
//			}
//			return param;
//		}

		/**
		 * Improved old implementation so it can handle the new cross-navigation handling implementation.
		 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		function fetchParamethers(d) {

			var param = {

				"categoryName": null,
				"categoryValue": null,
				"serieName": null,
				"serieValue": null,
				"groupingCategoryName": null,
				"groupingCategoryValue": null

			};

			for (j=0;j<chartConf.data[0].length;j++) {

				if(chartConf.data[0][j].name===d.text) {

					var item = chartConf.data[0][j];

					param.categoryName = item.categoryName[0];
					param.categoryValue = item.name;
					param.serieValue = item.value;
					param.serieName = item.seriesItemName;

				}

			}

			return param;
		}

	}