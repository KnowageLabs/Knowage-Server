/**
 * Function needed for cleaning the already rendered chart (if the one exists
 * on the page). This operation is mandatory since we want to rerender the 
 * chart when resizing the window (panel). 
 * (danilo.ristovski@mht.net)
 */
function cleanChart()
{
	/**
	 * Select everything that body of the page contains (every child node)
	 * and remove it from the page.
	 */
	d3.select("body").selectAll("*").remove();
}

/**
 * Convert RGB to HSL.
 * 
 * @param r
 * @param g
 * @param b
 * @returns {Array}
 */
function rgbToHsl(r, g, b)
{
	r /= 255, g /= 255, b /= 255;
	var max = Math.max(r, g, b), min = Math.min(r, g, b);
	var h, s, l = (max + min) / 2;
	
	if(max == min){
	    h = s = 0; // achromatic
	}else{
	    var d = max - min;
	    s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
	    switch(max){
	        case r: h = (g - b) / d + (g < b ? 6 : 0); break;
	        case g: h = (b - r) / d + 2; break;
	        case b: h = (r - g) / d + 4; break;
	    }
	    h /= 6;
	}
	
	return [h, s, l];
}

/**
 * Function for extracting the font size from the string value that contains
 * also the 'px' substring. Function is called whenever we need pure numeric
 * value of the size (especially for purposes of dynamic resizing of the chart).
 * 
 * @param fontSize String value of the size of the font
 * @returns Pure numeric value for size of the font
 * 
 * (danilo.ristovski@mht.net)
 */
function removePixelsFromFontSize(fontSize)
{
	var indexOfPx = fontSize.indexOf('px');
	
	if (indexOfPx > 0)
	{
		return fontSize.substring(0,indexOfPx);
	}
	else
	{
		return fontSize;
	}
}

function renderWordCloud(chartConf){
    console.log(chartConf);
	var maxic = 0;
	
	for (var i=0; i<chartConf.data[0].length; i++){
		
		if (chartConf.data[0][i].value > maxic){
			
			maxic = chartConf.data[0][i].value;
			
		}
		
	}
	
		(function() {

			function cloud() {
				var size = [256, 256],
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
							d.x = (size[0] * (Math.random() + .5)) >> 1;
							d.y = (size[1] * (Math.random() + .5)) >> 1;
							cloudSprite(d, data, i);
							if (d.hasText && place(board, d, bounds)) {
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

		var maxfontsize=chartConf.chart.maxFontSize;
		var fill = d3.scale.category20();

		d3.layout.cloud().size([chartConf.chart.width, chartConf.chart.height])
		.words(chartConf.data[0].map(function(d) {
			 return {text: d.name, size: d.value/(maxic/maxfontsize)};
		}))
		.padding(chartConf.chart.wordPadding)
		.rotate(function() {
			var angle = (Math.random() * 2) * 90;
			while ((angle < chartConf.chart.minAngle || angle > chartConf.chart.maxAngle)){
				angle = (Math.random() * 2) * 90;
			}
			return angle })
		.font("Impact")
		.fontSize(function(d) { return d.size; })
		.on("end", draw)
		.start();

		function draw(words) {
			
			d3.select("body")
			.append("div").attr("id","main")
			.style("height",chartConf.chart.height)
			.style("width",chartConf.chart.width)
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
				
				// Set title
				d3.select("#main").append("div")
					.style("height",emptyMsgTotal)
					.style("position",chartConf.emptymessage.position)
					.style("left",chartConf.emptymessage.paddingLeft)
					.style("color",chartConf.emptymessage.style.fontColor)
					.style("text-align",chartConf.emptymessage.style.textAlign)
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
				d3.select("#main").append("div")
					.style("color",chartConf.title.style.color)
					.style("text-align",chartConf.title.style.align)
		    		.style("font-family",chartConf.title.style.fontFamily)
		    		.style("font-size",chartConf.title.style.fontSize)
		    		.style("font-style",titleFontStyle)
		    		.style("font-weight",titleFontWeight)
		    		.style("text-decoration", titleTextDecoration)
					.text(chartConf.title.text);	
								
				// Set subtitle
				d3.select("#main").append("div")					
					.style("color",chartConf.subtitle.style.color)
					.style("text-align",chartConf.subtitle.style.align)
		    		.style("font-family",chartConf.subtitle.style.fontFamily)
		    		.style("font-style", subtitleFontStyle)
		    		.style("font-weight", subtitleFontWeight)
		    		.style("text-decoration", subtitleTextDecoration)
		    		.style("font-size",chartConf.subtitle.style.fontSize)
					.text(chartConf.subtitle.text);
			      
			 /* Previous:
			 	
			 	d3.select("#main")
					.append("div").attr("id","chart")
					.append("svg")
					.attr("width", chartConf.chart.width)
					...			 
			  */
			      
			d3.select("#main")
			.append("div").attr("id","chart")
			.append("svg")
			.attr("width", chartConf.chart.width)
			.attr("height", chartConf.chart.height-(Number(removePixelsFromFontSize(chartConf.title.style.fontSize))
					+Number(removePixelsFromFontSize(chartConf.subtitle.style.fontSize)))*1.6)
			.append("g")
			.attr("transform", "translate("+(chartConf.chart.width/2-40)+","+(chartConf.chart.height/2-10)+")")
			.selectAll("text")
			.data(words)
			.enter().append("text")
			.style("font-size", function(d) { return d.size + "px"; })
			.style("font-family",chartConf.chart.style.fontFamily)
			.style("font-style", chartConf.chart.style.fontStyle)
    		.style("font-weight", chartConf.chart.style.fontWeight)
    		.style("text-decoration", chartConf.chart.style.textDecoration)
			.style("fill", function(d, i) { return fill(i); })
			.attr("text-anchor", "middle")
			.attr("transform", function(d) {
				return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
			})
			.text(function(d) { return d.text; })
			.on('click', function(d){
				if(chartConf.crossNavigation.hasOwnProperty('crossNavigationDocumentName')){
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
					console.log(navigParams)
					handleCrossNavigationTo(navigParams);
				}
				
			});
			
			}	
		}
		
		function fetchParamethers(d){
			
			var param={
					"categoryName" : null,
					"categoryValue":null,
					"serieName":null,
					"serieValue":null,
					"groupingCategoryName":null,
					"groupingCategoryValue":null
			};
			for(j=0;j<chartConf.data[0].length;j++){
				if(chartConf.data[0][j].name===d.text){
					param.categoryValue=chartConf.data[0][j].name;
					param.serieValue=chartConf.data[0][j].value;
				}
			}
			return param;
		}
	}
    
	function renderSunburst(jsonObject)
	{
		console.log(jsonObject);
		/*The part that we need to place into HTML (JSP) in order to attach 
		 * given data to them - we are going to create it through D3 notation */
				
		/* Check if configurable (from the Designer point of view)
		 * parameters are defined through the Designer. If not set
		 * the predefined values, instead. */			

//		console.log(d3.select("body").selectAll("*").remove());
//		d3.selectAll("*").remove();
		
//		var chartHeight = (jsonObject.chart.height != '$chart.height') ? parseInt(jsonObject.chart.height) : 400 ;
//		var chartHeight = null;
//		var chartFontFamily = (jsonObject.chart.style.fontFamily != '$chart.style.fontFamily') ? jsonObject.chart.style.fontFamily : "" ;
//		var chartFontSize = (jsonObject.chart.style.fontSize != '$chart.style.fontSize') ? jsonObject.chart.style.fontSize : "" ;
//		var chartFontWeight = (jsonObject.chart.style.fontWeight != '$chart.style.fontWeight') ? jsonObject.chart.style.fontWeight : "" ;
//		var chartBackgroundColor = (jsonObject.chart.style.backgroundColor != '$chart.style.backgroundColor') ? jsonObject.chart.style.backgroundColor : "" ;
		var chartOpacityOnMouseOver = (jsonObject.chart.opacMouseOver != '$chart.style.opacMouseOver') ? parseInt(jsonObject.chart.opacMouseOver) : 100 ;
		
		/* 'topPadding':	padding (empty space) between the breadcrumb 
		 * 					(toolbar) and the top of the chart when the
		 * 					toolbar is possitioned on the top of the chart. 
		 * 'bottomPadding':	padding (empty space) between the bottom of the 
		 * 					chart and the top of the breadcrumb (toolbar) 
		 * 					when the toolbar is possitioned on the top 
		 * 					of the chart. */
		var topPadding = 30;
		var bottomPadding = 30;
		
		// Breadcrumb dimensions: width, height, spacing, width of tip/tail.
		var bcWidth = 2.5*parseInt(jsonObject.toolbar.style.width);
		var bcHeight = parseInt(jsonObject.toolbar.style.height);
		var bcSpacing = parseInt(jsonObject.toolbar.style.spacing);
		var bcTail = parseInt(jsonObject.toolbar.style.tail);
		
		/* Dimensions of the Sunburst chart. */
	    /* Dimensions of the window in which chart is going to be placed.
	     * Hence, radius of the circular Sunburst chart is going to be half of
	     * the lesser dimension of that window. */				
	    //var width = parseInt(jsonObject.chart.width);
	    var width = jsonObject.chart.width;
	    var height = 0;
	    
	    if (jsonObject.toolbar.style.position=="bottom")
    	{
	    	height = jsonObject.chart.height 
			- (Number(removePixelsFromFontSize(jsonObject.title.style.fontSize)) 
					+ Number(removePixelsFromFontSize(jsonObject.subtitle.style.fontSize))
					)*1.4 - bottomPadding - topPadding - bcHeight;
    	}
	    else
    	{
	    	height = jsonObject.chart.height 
						- (Number(removePixelsFromFontSize(jsonObject.title.style.fontSize)) 
								+ Number(removePixelsFromFontSize(jsonObject.subtitle.style.fontSize))
					)*1.4 - topPadding*2 - bottomPadding - bcHeight;
    	}
		
//	    var height = jsonObject.chart.height;
//		/* Manage chart position on the screen (in the window) depending on
//		 * the resizing of it, so the chart could be in the middle of it. */
//		window.onresize = function() 
//		{
//		    width = document.getElementById("chart").getBoundingClientRect().width;
//		    d3.select("#container").attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
//		};	
		
		var radius = Math.min(width,height)/2;	
		
		var chartOrientation = (width > height) ? "horizontal" : "vertical";		
		
	    var tipFontSize = parseInt(jsonObject.tip.style.fontSize);
	    var tipWidth = parseInt(jsonObject.tip.style.width);
		
	    // Parameters (dimensions) for the toolbar (breadcrumb)
		var b = 
		{ 
			w: bcWidth, 	h: bcHeight, 
			s: bcSpacing, 	t: bcTail 
		};
		
		/*var chartDivWidth=width;
		var chartDivHeight=height;
		
		if(jsonObject.title.style!="" || jsonObject.subtitle.style!=""){
			chartDivHeight-=jsonObject.title.style
		}*/
		
		
		/* Create necessary part of the HTML DOM - the one that code need to
		 * position chart on the page (D3 notation) */
		
		/**
		 * Add this root DIV so when we specify some font properties for the chart
		 * it can be applied on every chart element that has some elements that are
		 * using font properties, if they are not specified. For example, user defines
		 * font family for the chart, but not for the title. In this case we will 
		 * apple font family of the whole chart on the title DIV element, as well as
		 * on other DIV elements.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		d3.select("body")
			.append("div").attr("id","main")
			.style("height", jsonObject.chart.height)
			.style("width", jsonObject.chart.width)
			.style("font-family", jsonObject.chart.style.fontFamily)
			.style("font-size", jsonObject.chart.style.fontSize)
			.style("font-style",jsonObject.chart.style.fontStyle)
    		.style("font-weight",jsonObject.chart.style.fontWeight)
    		.style("text-decoration",jsonObject.chart.style.textDecoration)
			.style("background-color",jsonObject.chart.style.backgroundColor);
		
		// If there is no data in the recieved JSON object - print empty message
		if (jsonObject.data[0].length < 1)
		{
			var emptyMsgFontSize = parseInt(jsonObject.emptymessage.style.fontSize);
			//var emptyMsgTotal = emptyMsgDivHeight+emptyMsgFontSize/2;
			var emptyMsgTotal = emptyMsgFontSize;
			
			// Set empty text on the chart
			d3.select("#main").append("div")
				.style("color",jsonObject.emptymessage.style.color)
				.style("text-align",jsonObject.emptymessage.style.align)
	    		.style("font-family",jsonObject.emptymessage.style.fontFamily)
	    		.style("font-style",jsonObject.emptymessage.style.fontStyle)
	    		.style("font-weight",jsonObject.emptymessage.style.fontWeight)
	    		.style("text-decoration",jsonObject.emptymessage.style.textDecoration)
	    		.style("font-size",emptyMsgFontSize)
				.text(jsonObject.emptymessage.text);	
		}
		else
		{			
			// Set title on the chart
			d3.select("#main").append("div")
				.attr("id","title")  
				.style("color",jsonObject.title.style.color)
				.style("text-align",jsonObject.title.style.align)
	    		.style("font-family",jsonObject.title.style.fontFamily)
	    		.style("font-style",jsonObject.title.style.fontStyle)
	    		.style("font-weight",jsonObject.title.style.fontWeight)
	    		.style("text-decoration",jsonObject.title.style.textDecoration)
	    		.style("font-size",jsonObject.title.style.fontSize)
				.text(jsonObject.title.text);	
			
			// Set subtitle on the chart
			d3.select("#main").append("div")
				.attr("id","subtitle")  
				.style("color",jsonObject.subtitle.style.color)
				.style("text-align",jsonObject.subtitle.style.align)
	    		.style("font-family",jsonObject.subtitle.style.fontFamily)
	    		.style("font-style",jsonObject.subtitle.style.fontStyle)
	    		.style("font-weight",jsonObject.subtitle.style.fontWeight)
	    		.style("text-decoration",jsonObject.subtitle.style.textDecoration)
	    		.style("font-size",jsonObject.subtitle.style.fontSize)
				.text(jsonObject.subtitle.text);
		    
			/* Put the topPadding on the the top of the chart, even if toolbar
			 * is set on the bottom of the chart (because of the clearer and 
			 * more structured view). */
		    d3.select("#main").append("div").style("height",topPadding);
		    
		    /* Get the data about the height of the title, subtitle and toolbar 
		     * already placed on the chart. */
		    var titleHeight = d3.select("#title")[0][0].getBoundingClientRect().height;
		    var subtitleHeight = d3.select("#subtitle")[0][0].getBoundingClientRect().height;
		    var breadCrumbHeight = parseInt(jsonObject.toolbar.style.height);
		    
		    /* Sum of heights of all of the DOM elements above the chart's center:
		     * title, subtitle, toolbar (breadcrumb), padding between the toolbar 
		     * and the chart, half of the height of the chart. */
		    var sumOfHeightsAboveChartCenter = parseInt(titleHeight + subtitleHeight + height/2 + topPadding);		    	
		    
		    if (jsonObject.toolbar.style.position=="top")
			{   
		    	sumOfHeightsAboveChartCenter = parseInt(sumOfHeightsAboveChartCenter + breadCrumbHeight);		    	
	    		d3.select("#main").append("div").attr("id","sequence");	
	    		d3.select("#main").append("div").style("height",topPadding);
			}
		    
		    d3.select("#main").append("div").attr("id","chart");	        
	    	
	    	if (jsonObject.toolbar.style.position=="bottom")
			{   
		    	/* Add padding between the bottom of the chart and the top of 
		    	 * the toolbar (breadcrumb). */	  
	    		d3.select("#main").append("div").style("height",bottomPadding);
		    	d3.select("#main").append("div").attr("id","sequence");				
			}
	    	
	    	if (jsonObject.toolbar.style.position=="top")
			{   
	    		d3.select("#main").append("div").style("height",bottomPadding);
			}
		       
		}
		
		/* Collect all possible colors into one array - PREDEFINED set of colors
		 * (the ones that we are going to use in case configuration for the
		 * current user (customized) is not set already) */	
		var children = new Array();	
		
		children = children.concat(d3.scale.category10().range());
		children = children.concat(d3.scale.category20().range());
		children = children.concat(d3.scale.category20b().range());
		children = children.concat(d3.scale.category20c().range());
		
		/* Map that will contain key-value pairs. Key is going to be name of each 
		 * individual element of the result (of the request for dataset). Value 
		 * will be the color that is going to be assigned to each element. */
		var colors = {}; 
		
		var colorArrangement = new Array();
		
		// Total size of all segments; we set this later, after loading the data.
		var totalSize = 0; 
		
		/* Put inside first "div" element the Suburst chart, i.e. SVG DOM
		 * element that will represent it. SVG window will be with previously
		 * defined dimensions ("width" and "height"). */		
		var vis = d3.select("#chart").append("svg:svg")
		    .attr("width", width)
		    .attr("height", height)
		    .append("svg:g")
		    .attr("id", "container")
		    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");	
		
		var partition = d3.layout.partition()
		    .size([2 * Math.PI, radius * radius])
		    .value(function(d) { return d.size; });
		
		/* Counting angular data for some particular element of dataset. */
		var arc = d3.svg.arc()
		    .startAngle(function(d) { return d.x; })
		    .endAngle(function(d) { return d.x + d.dx; })
		    .innerRadius(function(d) { return Math.sqrt(d.y); })
		    .outerRadius(function(d) { return Math.sqrt(d.y + d.dy); });
		 
		/* Get hierarchy of root data (first level of the chart) - 
		 * data ordered by their presence in total ammount (100% of the sum). 
		 * E.g. if we have this distribution of data for particular query:
		 * USA: 78%, Canada: 12%, Mexico: 8%, No country: 2%, the "children"
		 * array (sequence) inside "json" variable will be in descending order: 
		 * USA, Canada, Mexico, No country. */
		var json = buildHierarchy(jsonObject.data[0]);
		
		createVisualization(json);

		/**
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		function getGradientColorsHSL(fromH,fromS,fromL,toH,toS,toL,numberOfLayers) 
		{		
			var i, colors = [],
	
			deltaH = (toH - fromH) / numberOfLayers,
			deltaS = (toS - fromS) / numberOfLayers,
			deltaL = (toL - fromL) / numberOfLayers;
	
			for (i = 0; i <= numberOfLayers; i++) 
			{		        	
				colors.push( d3.hsl(fromH + deltaH * i, fromS + deltaS * i, fromL + deltaL * i) );
			}

				return colors;
		}
		
		// TODO: remove - not needed
//		function rgbToHsl(r, g, b){
//		    r /= 255, g /= 255, b /= 255;
//		    var max = Math.max(r, g, b), min = Math.min(r, g, b);
//		    var h, s, l = (max + min) / 2;
//
//		    if(max == min){
//		        h = s = 0; // achromatic
//		    }else{
//		        var d = max - min;
//		        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
//		        switch(max){
//		            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
//		            case g: h = (b - r) / d + 2; break;
//		            case b: h = (r - g) / d + 4; break;
//		        }
//		        h /= 6;
//		    }
//
//		    return [h, s, l];
//		}
		
		// Main function to draw and set up the visualization, once we have the data.
		function createVisualization(json) 
		{				
			// Basic setup of page elements.
			/* Set the initial configuration of the breadcrumb - 
			 * defining dimensions of the trail, color of the text 
			 * and position of it within the chart (top (default), bottom) */	
			initializeBreadcrumbTrail();
			
			// Bounding circle underneath the sunburst, to make it easier to detect
			// when the mouse leaves the parent g.
			vis.append("svg:circle")
				.attr("r", radius)
				.style("opacity", 0);

			// For efficiency, filter nodes to keep only those large enough to see.
			var nodes = partition.nodes(json).filter
			(
				function(d) 
				{
					return (d.dx > 0.005); // 0.005 radians = 0.29 degrees
				}
			);
			
			// NEW
			
			/* Dark colors for the chart's first layer items */
//			var storeFirstLayerColor = 
//			[
//			 	"#CC0000", 	// red
//			 	"#003D00", 	// green
//			 	"#151B54", 	// blue	
//			 	"#CC3399",	// purple
//			 	"#808080",	// gray
//			 	"#FF9900"	// orange			 			 	
//		 	];
			
			var storeFirstLayerColor = new Array();
			var allColorsLayered = new Array();
			
			var allColorsUserPicked = jsonObject.colors;
			
			// START: TODO: maybe not necessary (danristo)
			var differentLayersArray = new Array();
			var numberOfLayers = -1;
			
			for (j=0; j<nodes.length; j++)
			{
				if (differentLayersArray.indexOf(nodes[j].layer) < 0)
				{
					differentLayersArray.push(nodes[j].layer);
					numberOfLayers++;
				}
			}
			// END : maybe not necessary
						
			for (p=0; p<allColorsUserPicked.length; p++)
			{
				storeFirstLayerColor.push(allColorsUserPicked[p]);
				
				var layeringColorRaw = allColorsUserPicked[p];	// start color			
				var layeringColorRGB = d3.rgb(layeringColorRaw);			
				var layeringColorHSL = layeringColorRGB.hsl();			
							
				// number of layers: 20
				var yyyHSL = getGradientColorsHSL(layeringColorHSL.h,layeringColorHSL.s,layeringColorHSL.l,0,0,1,15);
				var yyyRGB = new Array();
				//console.log(yyyHSL);
				
				for (w=0; w<yyyHSL.length; w++)
				{
					var uuu = yyyHSL[w];
					//console.log(uuu);
					//console.log(uuu.rgb());
					yyyRGB.push(uuu.rgb());
				}
				
				allColorsLayered.push(yyyRGB);
			}
			
			//console.log(storeFirstLayerColor);
			//console.log(allColorsLayered);
			
			
//			var storeColors = 
//			[
//			 	"red", "green", "blue", "orange", "purple"
//			 ];
//			
//			var varietiesOfMainColors = 
//			{
//				red: 	["#CC0000", "#FF4747", "#FF7A7A", "#FF9595", "#FFAAAA", "#FFBBBB", "#FFC9C9", "#FFD4D4", "#FFDDDD", "#FFE4E4", "#FFECEC"],
//				green: 	["#003D00", "#003100", "#194619", "#305830", "#456945", "#587858", "#698669", "#789278", "#869D86", "#92A792", "#9DB09D"],
//				blue: 	["#151B54", "#2C3265", "#414674", "#545882", "#65698E", "#747899", "#8286A3", "#8E92AC", "#999DB4", "#A3A7BC", "#BDC0CF"],
//				orange: ["#E68A00", "#FF9900", "#EBA133", "#FFAD33", "#FFC266", "#FFCC80", "#FFD699", "#FFE0B2", "#FFEBCC", "#FFF5E6", "#FFE3BA"],
//				purple: ["#7A297A", "#8A2E8A", "#993399", "#A347A3", "#AD5CAD", "#B870B8", "#C285C2", "#CC99CC", "#D6ADD6", "#E0C2E0", "#EBD6EB"],
//			};
			
//			var rbgRedColor = d3.rgb("#CC0000");
//			var rgbRedWhiteColor = d3.rgb("#FFECEC");
//			
//			var hslRedColor = rbgRedColor.hsl();
//			var hslRedWhiteColor = rgbRedWhiteColor.hsl();
			
//			var gradientColors = getGradientColorsHSL([hslRedColor,hslRedColor,hslRedWhiteColor,10]);
//			console.log(gradientColors);
			
//			var  baseColor = Ext.draw.Color.create(args[0]);
//		     var  from =args[1];
//		     var  to =args[2];
//		     var  number =args[3];
			
			var rootParentsNodes = getRootParentNodes(nodes);
			var counter = 0;
			
			var path = vis.data([json]).selectAll("path")
				.data(nodes)
				.enter().append("svg:path")
				.attr("display", function(d) { return (d.depth && (d.name!=""||d.name)) ? null : "none"; })
				.attr("d", arc)
				.attr('stroke', (jsonObject.chart.style.backgroundColor && 
									jsonObject.chart.style.backgroundColor!="" && 
										jsonObject.chart.style.backgroundColor!=undefined) ? 
												jsonObject.chart.style.backgroundColor : "#FFFFFF")	// color bewtween arcs (danristo)
				.attr('stroke-width', '2')	// spacing (width, padding) bewtween arcs (danristo)
				.attr("fill-rule", "evenodd")
				.style
				(
						"fill", 
						
						function(d,i) 
						{   	    								
							  /* Go through the array of key-value pairs (elements of the chart and their color)
							   * and check if there is unique element-color mapping. */
//							  if (colors[d.name] == undefined && d.name != "root")
//							  {
//								  var numberOfColor = Math.floor(Math.random()*children.length);
//								  colors[d.name] = children[numberOfColor];								  
//							  }
							
							//console.log(d);
							
							if(d.name!=null && d.name!="")
							{
							  /* If current node is not a root */
							  if (d.name != "root")
							  {								  
								  /* If current node's parent name is root
								   * (if this node is part of the first layer) */
								  
								  if (d.parent.name=="root")
								  {									  
//									  colors[d.name] = varietiesOfMainColors[storeColors[counter]][0];	
									  //console.log(allColorsLayered[counter][0]); //ok
									  colors[d.name] = allColorsLayered[counter][0];
//									  colors[d.name] = varietiesOfMainColors[counter][0];		
									  counter++;
								  }		
								  else
								  {
									  //console.log(d.firstLayerParent);
//									  console.log(rootParentsNodes.indexOf(d.firstLayerParent));
									  //console.log(storeColors[rootParentsNodes.indexOf(d.firstLayerParent)]);
//									  colors[d.name] = varietiesOfMainColors[storeColors[rootParentsNodes.indexOf(d.firstLayerParent)]][d.layer+1];
									  //console.log(allColorsLayered[rootParentsNodes.indexOf(d.firstLayerParent)][d.layer+1]);
									  colors[d.name] = allColorsLayered[rootParentsNodes.indexOf(d.firstLayerParent)][d.layer+1];
								  }
								  
								  d['color'] = colors[d.name];
								  
								  //colorArrangement[i] = colors[d.name];
								 // console.log(colors[d.name]);
								  return colors[d.name];
							  }							  	
							}
							else
							{
								 console.log("RRRRR");
								  console.log(d);
								  return "invisible";
							  }		
							  
//							colorArrangement[i] = colors[d.name];
//							  
//							return colors[d.name];	
						}
				)					
				.style("opacity", 1)
				.on("mouseover", mouseover)
				.on("click",clickFunction);

				drawLegend();
			
				// Add the mouseleave handler to the bounding circle.
				d3.select("#container").on("mouseleave", mouseleave);

				// Get total size of the tree = value of root node from partition.
				totalSize = path.node().__data__.value;
		 };
		
		 /**
		  * danristo
		  */
		 function getRootParentNodes(nodes)
		 {				 
			var arrayOfParents = [];
				
			 for (var i=0; i<nodes.length; i++)
			 {
				 if (nodes[i].parent && nodes[i].parent.name=="root")
				 {
					 arrayOfParents.push(nodes[i].name);
				 }
			 }
			 
			 return arrayOfParents;
		 };
		 
		// Fade all but the current sequence, and show it in the breadcrumb trail.
		function mouseover(d) 
		{	
		  var percentage = (100 * d.value / totalSize).toPrecision(3);
		  var percentageString = percentage + "%";
		  
		  if (percentage < 0.1) 
		  {
		    percentageString = "< 0.1%";
		  }
		  
		  /* If we already have move mouse over the chart, remove
		   * previous content for the "explanation", i.e. move the
		   * previous text inside the chart.  */
		  if (d3.select("#explanation")[0][0] != null)
		  {
			  d3.select("#explanation").remove();
			  d3.select("#percentage").remove();
		  }	  
		  
		  d3.select("#chart")   	
	    	.append("div").attr("id","explanation")
	    	.append("span").attr("id","percentage");
		  
		  d3.select("#explanation")
		  	.append("text").html("</br>" + jsonObject.tip.text)
	    	.style("text-align","center")
	    	.style("font-family",jsonObject.tip.style.fontFamily)
	  		.style("font-style",jsonObject.tip.style.fontStyle ? jsonObject.tip.style.fontStyle : "none")
    		.style("font-weight",jsonObject.tip.style.fontWeight ? jsonObject.tip.style.fontWeight : "none")
    		.style("text-decoration",jsonObject.tip.style.textDecoration ? jsonObject.tip.style.textDecoration : "none")
	  		.style("font-size",tipFontSize); 
		
		  d3.select("#percentage")
		  	.text(percentageString)		  	
		      .style("font-family",jsonObject.tip.style.fontFamily)
	    		.style("font-style",jsonObject.tip.style.fontStyle ? jsonObject.tip.style.fontStyle : "none")
	    		.style("font-weight",jsonObject.tip.style.fontWeight ? jsonObject.tip.style.fontWeight : "none")
	    		.style("text-decoration",jsonObject.tip.style.textDecoration ? jsonObject.tip.style.textDecoration : "none")
	    		.style("font-size",tipFontSize)
	    		.style("vertical-align","middle");		  
	    			
		  var percentageHeight = document.getElementById('percentage').getBoundingClientRect().height;
		  var explanationHeight = document.getElementById('explanation').getBoundingClientRect().height;	
		  		    
		  d3.select("#explanation")
		  	.style("color",jsonObject.tip.style.color)
			.style("position","absolute")
			.style("left",width/2-tipWidth/2)
			.style("width",tipWidth)
			.style("text-align","center");
		    		
		  /* When width of the text area (rectangle) is set, count 
		   * the height of the invisible text rectangle in which 
		   * text will fit. */
		  var textRectangleHight = d3.select("#explanation")[0][0].clientHeight;		  
		  var distanceFromTheTop = sumOfHeightsAboveChartCenter - textRectangleHight/2;
		    
		  /* When text area in the middle of the chart is determined,
		   * set the distance of the invisible text rectangle from
		   * the top of the window to the top of that rectangle.*/
		  d3.select("#explanation")
    		.style("top",distanceFromTheTop);    				    
		    
		  d3.select("#explanation")
		      .style("visibility", "");
		
		  var sequenceArray = getAncestors(d);
		  updateBreadcrumbs(sequenceArray, percentageString);
		  
		  var opacMouseOver = chartOpacityOnMouseOver;
		  opacMouseOver = opacMouseOver/100; // normalize value from interval [1,100] to %
		  
		  d3.selectAll("path")
	      	.style("opacity", opacMouseOver);
		
		  // Then highlight only those that are an ancestor of the current segment.
		  vis.selectAll("path")
		      .filter(function(node) {
		                return (sequenceArray.indexOf(node) >= 0);
		              })
		      .style("opacity", 1);
		}
		
		// Restore everything to full opacity when moving off the visualization.
		function mouseleave(d) {
		
		  // Hide the breadcrumb trail
		  d3.select("#trail")
		      .style("visibility", "hidden");
		
		  // Deactivate all segments during transition.
		  d3.selectAll("path").on("mouseover", null);
		
		  // Transition each segment to full opacity and then reactivate it.
		  d3.selectAll("path")
		      .transition()
		      .duration(1000)
		      .style("opacity", 1)
		      .each("end", function() {
		              d3.select(this).on("mouseover", mouseover);
		            });
		
		  d3.select("#explanation")
		      .style("visibility", "hidden");
		}
		
		// Given a node in a partition layout, return an array of all of its ancestor
		// nodes, highest first, but excluding the root.
		function getAncestors(node) {
		  var path = [];
		  var current = node;
		  while (current.parent) {
		    path.unshift(current);
		    current = current.parent;
		  }
		  return path;
		}
		
		/* Put the breadcrumb trail that will
		 * be positioned at the position where DOM element with given
		 * ID (#sequence) resides. */
		function initializeBreadcrumbTrail() 
		{
			// Add the svg area.
			/* Adds the new SVG DOM element to the current structure -
			 * it appends new SVG to the very first "div" element in order
			 * to present breadcrumb. It specifies its dimensions (width,
			 * height */		
			
			var trail = d3.select("#sequence")
				.append("svg:svg")
				.attr("width", width)
				.attr("height", bcHeight)
				.attr("id", "trail");
			  
			// Add the label at the end, for the percentage.
			/* Append to the newly created SVG element text subelement 
			 * that will contain value of the percentage that covered sequence
			 * represent. Here, predefined color of percentage text is black
			 * (#000). ("#000") = ("black") */
			trail
				.append("svg:text")
				.attr("id", "endlabel")
				.style("fill", jsonObject.toolbar.style.percFontColor)
				.style("font-family", jsonObject.toolbar.style.fontFamily)
				.style("font-style",jsonObject.toolbar.style.fontStyle ? jsonObject.toolbar.style.fontStyle : "none")
	    		.style("font-weight",jsonObject.toolbar.style.fontWeight ? jsonObject.toolbar.style.fontWeight : "none")
	    		.style("text-decoration",jsonObject.toolbar.style.textDecoration ? jsonObject.toolbar.style.textDecoration : "none")
				.style("font-size", jsonObject.toolbar.style.fontSize);
		}
		
		// Generate a string that describes the points of a breadcrumb polygon.
		function breadcrumbPoints(d, i) {
		  var points = [];
		  points.push("0,0");
		  points.push(b.w + ",0");
		  points.push(b.w + b.t + "," + (b.h / 2));
		  points.push(b.w + "," + b.h);
		  points.push("0," + b.h);
		  if (i > 0) { // Leftmost breadcrumb; don't include 6th vertex.
		    points.push(b.t + "," + (b.h / 2));
		  }
		  return points.join(" ");
		}
		
		// Update the breadcrumb trail to show the current sequence and percentage.
		function updateBreadcrumbs(nodeArray, percentageString) {
		
		  // Data join; key function combines name and depth (= position in sequence).
		  var g = d3.select("#trail")
		      .selectAll("g")
		      .data(nodeArray, function(d) { return d.name + d.depth; });
		
		  // Add breadcrumb and label for entering nodes.
		  var entering = g.enter().append("svg:g");
		
		  /* TODO: see how could possible be realized that breadcrumb items
		   * get different color for different levels, even if the same name. */
		  entering.append("svg:polygon")
		      .attr("points", breadcrumbPoints)
		      .style("fill", function(d) { return d.color; });
		  
		  entering.append("svg:text")
		      .attr("x", (b.w + b.t) / 2)
		      .attr("y", b.h / 2)
		      .attr("dy", "0.35em")
		      .attr("text-anchor", "middle")
		      .style("font-family",jsonObject.toolbar.style.fontFamily)
		      .style("font-size",jsonObject.toolbar.style.fontSize)
		      .style("font-style",jsonObject.toolbar.style.fontStyle ? jsonObject.toolbar.style.fontStyle : "none")
		      .style("font-weight",jsonObject.toolbar.style.fontWeight ? jsonObject.toolbar.style.fontWeight : "none")
		      .style("text-decoration",jsonObject.toolbar.style.textDecoration ? jsonObject.toolbar.style.textDecoration : "none")
		      .style("text-shadow", "0px 0px 5px #FFFFFF")
		      .text(function(d) { return d.name; });
		
		  // Set position for entering and updating nodes.
		  g.attr("transform", function(d, i) {
		    return "translate(" + i * (b.w + b.s) + ", 0)";
		  });
		
		  // Remove exiting nodes.
		  g.exit().remove();
		  
		  // Now move and update the percentage at the end.
		  d3.select("#trail").select("#endlabel")
		      .attr("x", (nodeArray.length + 0.5) * (b.w + b.s))
		      .attr("y", b.h / 2)
		      .attr("dy", "0.35em")
		      .attr("text-anchor", "middle")
		      .text(percentageString);
		
		  // Make the breadcrumb trail visible, if it's hidden.
		  d3.select("#trail")
		      .style("visibility", "");
		
		}
		
		function drawLegend() 
		{	
			// Dimensions of legend item: width, height, spacing, radius of rounded rect.
//			var li = 
//					{ 
//						w: parseInt(jsonObject.legend.style.width), h: parseInt(jsonObject.legend.style.height), 
//						s: parseInt(jsonObject.legend.style.spacing), r: parseInt(jsonObject.legend.style.radius) 
//					};
			
			var li = 
			{ 
				w: 200, h: 50, 
				s: 10, r: 15 
			};

			var numOfColorElems = Object.keys(colors).length;
			
			var legend = d3.select("#legend").append("svg:svg")
			.attr("width", li.w)
			.attr("height", numOfColorElems * (li.h + li.s));
					
			var g = legend.selectAll("g")
				.data(d3.entries(colors))
				.enter().append("svg:g")
				.attr
				(	
						"transform", 
						
						function(d, i) 
						{
							return "translate(0," + i * (li.h + li.s) + ")";
						}
				);

			g.append("svg:rect")
			.attr("rx", li.r)
			.attr("ry", li.r)
			.attr("width", li.w)
			.attr("height", li.h)
			.style("fill", function(d) { return d.value; });

			g.append("svg:text")
			.attr("x", li.w / 2)
			.attr("y", li.h / 2)
			.attr("dy", "0.35em")
			.attr("text-anchor", "middle")
			.text(function(d) { return d.key; });
		}
		
		/* ME: This function will be called whenever we click on "Legend" 
		 * checkbox (whether its already checked or it is not). It toggles
		 * legends visibility. */
		function toggleLegend() 
		{		
			var legend = d3.select("#legend");
			
			if (legend.style("visibility") == "hidden") 
			{
				legend.style("visibility", "");
			} 
			else 
			{
				legend.style("visibility", "hidden");
			}
		}
		
		// Take a 2-column CSV and transform it into a hierarchical structure suitable
		// for a partition layout. The first column is a sequence of step names, from
		// root to leaf, separated by hyphens. The second column is a count of how 
		// often that sequence occurred.
		function buildHierarchy(jsonObject) 
		{
		  var root = { "name": "root", "children": [] };
		  
		  /* Total number of data received when requesting dataset. */
		  var dataLength = jsonObject.length;
		  
		  var counter = 0;
		  for (var i = 0; i < dataLength; i++) 
		  {
		    //var sequence = jsonObject[i].column_1;
		   // var size =+ jsonObject[i].column_2;
			  //console.log(i);
			  var sequence = jsonObject[i].sequence;
			  var size =+ jsonObject[i].value;
			  		
		    if (isNaN(size)) 
		    { 
		    	// e.g. if this is a header row
		    	continue;
		    }
		    
		    /* ME: Split single parts within received data in order
		     * to create visualization of levels that represent those
		     * data. */
		    var parts = sequence.split("_SEP_");
		    
		    var currentNode = root;		    
		    
		    for (var j = 0; j < parts.length; j++) 
		    {
		    	currentNode["layer"] = j-1;
	    		currentNode["firstLayerParent"] = parts[0];	    
	    		
		    	var children = currentNode["children"];
		    	var nodeName = parts[j];
		    	var childNode;
		    	
		    	if (j + 1 < parts.length) 
		    	{
		    		// Not yet at the end of the sequence; move down the tree.
		    		var foundChild = false;
		    		
		    		for (var k = 0; k < children.length; k++) 
		    		{				    			
		    			if (children[k]["name"] == nodeName) 
		    			{
		    				childNode = children[k];
		    				foundChild = true;
		    				break;
	    				}
	    			}
		    		
		    		// If we don't already have a child node for this branch, create it.
		    		if (!foundChild) 
		    		{		    			
		    			childNode = {"name": nodeName, "children": []};		    	
		    			children.push(childNode);
		    		}
	    		
		    		currentNode = childNode;
		    		
		    		currentNode["firstLayerParent"] = parts[0];
		    		currentNode["layer"] = j-1;
		    	} 
		    	
		    	else 
		    	{
				 	// Reached the end of the sequence; create a leaf node.
				 	childNode = {"name": nodeName, "size": size};
				 	childNode["layer"] = j;
				 	childNode["firstLayerParent"] = parts[0];
				 	children.push(childNode);
		    	}	    		
		    
		    } 	// inner for loop
		    
		  }		// outter for loop
		  
		  return root;
		  
		};
		
		function clickFunction(d){
			if(jsonObject.crossNavigation.hasOwnProperty('crossNavigationDocumentName')){
				paramethers=crossNavigationParams(d);
				var navigParams={
					crossNavigationDocumentName:jsonObject.crossNavigation.crossNavigationDocumentName,
					crossNavigationDocumentParams:jsonObject.crossNavigation.crossNavigationDocumentParams,
					categoryName:paramethers.categoryName,
					categoryValue:paramethers.categoryValue,
					serieName:paramethers.serieName,
					serieValue:paramethers.serieValue,
					groupingCategoryName:paramethers.groupingCategoryName,
					groupingCategoryValue:paramethers.groupingCategoryValue
				};
				handleCrossNavigationTo(navigParams);
			}
			
			
		}
		
		function crossNavigationParams(d){
			var par={
				"categoryName":null,
				"categoryValue":null,
				"serieName":null,
				"serieValue":null,
				"groupingCategoryName":null,
				"groupingCategoryValue":null
			};
			par.categoryValue=d.name;
			par.serieValue=d.value;
			return par;
		}
	}	
	
	/**
	 * The rendering function for the PARALLEL chart.
	 * 
	 * @param data JSON containing data (parameters) about the chart 
	 */
	function renderParallelChart(data){
	
	var records = data.data[0];

	if(records.length>0){

		if (records.length>data.limit.maxNumberOfLines){

			var limitcolumn = data.limit.serieFilterColumn;

			records.sort(function(obj1, obj2) {
				return obj1[limitcolumn] - obj2[limitcolumn];
			});
		
		
		var len = records.length;
		
		var max = data.limit.maxNumberOfLines;
		
		if (data.limit.orderTopMinBottomMax === 'top'){
			
			var slicedData = records.slice(len-max,len);
			
			records = slicedData;
		}
		else if (data.limit.orderTopMinBottomMax === 'bottom'){
			
			var slicedData = records.slice(0,max);
			
			records = slicedData;
		}}

		var groupcolumn = data.chart.group;

		var group = Ext.decode(data.chart.groups);
		var column = Ext.decode(data.chart.serie);

		var groups = [];

		var columns = [];

		for (var i = 0; i< group.length; i++){

			groups.push(group[i][i]);
		}

		for (var i = 0; i<column.length;i++){

			columns.push(column[i][i]);
		}

		function pickColors(colors, n){ // picks n different colors from colors
			var selected=[];
			while(selected.length < n){   
				var c= colors[Math.floor(Math.random()*colors.length)];
				if(selected.indexOf(c)==-1){
					selected.push(c);
				}

			}
			return selected;
		}

		var allTableData;

        var colors = [];

		var colorsResponse = data.chart.colors;
	
	 var colorsResponseDec = Ext.decode(colorsResponse);
     
     for (var i = 0; i< colorsResponseDec.length; i++){

			colors.push(colorsResponseDec[i][i]);
		
		}

     var myColors=d3.scale.ordinal().domain(groups).range(colors);

	    var brushWidth = data.axis.brushWidth;

		var brushx = -Number(brushWidth)/2;

		var m = [40, 40, 40, 100],
		w = data.chart.width - m[1] - m[3],
		h = data.chart.height - m[0] - m[2];

		/**
		 * Configuration that we get directly from the VM (needed for displaying
		 * the full (complete) chart when resizing. The biggest problems are
		 * legend's width and table's height parameters.
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		var legendWidth = data.legend.width;
		var tableRowElements = data.table.numberOfRows;
		var tablePaginationHeight = data.table.heightPageNavigator;
		var divHeightAfterTable = data.table.afterTableDivHeight;	

		/**
		 * This is the part when we set the width of the chart itself (the width between axes
		 * on edges of the chart).
		 * 
		 * @modifiedBy: danristo (danilo.ristovski@mht.net)
		 */
		var x = d3.scale.ordinal().domain(columns).rangePoints([0, w-legendWidth]),
		y = {};

		var line = d3.svg.line(),
		axis = d3.svg.axis().orient("left"),
		foreground;

		/**
		 * Add this root DIV so when we specify some font properties for the chart
		 * it can be applied on every chart element that has some elements that are
		 * using font properties, if they are not specified. For example, user defines
		 * font family for the chart, but not for the title. In this case we will 
		 * apple font family of the whole chart on the title DIV element, as well as
		 * on other DIV elements.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		
		d3.select("body")
			.append("div").attr("id","main")
			.style("height",data.chart.height)
			.style("width",data.chart.width)
			.style("background-color",data.chart.style.backgroundColor)
			.style("font-family", data.chart.style.fontFamily)
			.style("font-size",  data.chart.style.fontSize)
			.style("font-style",data.chart.style.fontStyle)
			.style("font-weight",data.chart.style.fontWeight)
			.style("text-decoration",data.chart.style.textDecoration);
				
		// Set title
		d3.select("#main").append("div")
		.style("color",data.title.style.color)
		.style("text-align",data.title.style.align)
		.style("font-family",data.title.style.fontFamily)
		.style("font-style",data.title.style.fontStyle)
		.style("font-weight",data.title.style.fontWeight)
		.style("text-decoration",data.title.style.textDecoration)
		.style("font-size",data.title.style.fontSize)
		.text(data.title.text);

		// Set subtitle
		d3.select("#main").append("div")
		.style("color",data.subtitle.style.color)
		.style("text-align",data.subtitle.style.align)
		.style("font-family",data.subtitle.style.fontFamily)
		.style("font-style",data.subtitle.style.fontStyle)
		.style("font-weight",data.subtitle.style.fontWeight)
		.style("text-decoration",data.subtitle.style.textDecoration)
		.style("font-size",data.subtitle.style.fontSize)
		.text(data.subtitle.text);

		var groupsHeight=groups.length*20+60;
		var svgHeight;
		if(groupsHeight > (h + m[0] + m[2])){
			svgHeight=groupsHeight;
		}else{
			svgHeight=h + m[0] + m[2];
		}
		
		d3.select("#main").append("div").attr("id","chart").style("width",data.chart.width).style("height",data.chart.height - (Number(removePixelsFromFontSize(data.title.style.fontSize))+Number(removePixelsFromFontSize(data.subtitle.style.fontSize)))*1.2 - 180-20);
		
		var heightTotal = h + m[0] + m[2];
		
		var svg = d3.select("#chart")
		.append("div")
		.style("float","left")
			.style("width",data.chart.width-legendWidth)
			// "...-180" for table height plus pagination height (150+30)
			// "...-20" for bottom padding of the pagination  
			.style("height", data.chart.height - (Number(removePixelsFromFontSize(data.title.style.fontSize))+Number(removePixelsFromFontSize(data.subtitle.style.fontSize)))*1.2 - 180-20)
		.append("svg:svg")
		//.style("font-size",18)
			.style("width", data.chart.width-legendWidth)
			// "...-180" for table height plus pagination height (150+30)
			// "...-20" for bottom padding of the pagination  
			.style("height", data.chart.height - (Number(removePixelsFromFontSize(data.title.style.fontSize))+Number(removePixelsFromFontSize(data.subtitle.style.fontSize)))*1.2 - 180-20)
		.append("svg:g")
		.attr("transform", "translate(" + m[3] + "," + m[0] + ")");

		columns.forEach(function(d){
			records.forEach(function(p) {p[d] = +p[d]; });

			/**
			 * This is the part when we set the height of the chart itself.
			 * 
			 * @modifiedBy: danristo (danilo.ristovski@mht.net)
			 */
			y[d] = d3.scale.linear()
			.domain(d3.extent(records, function(p) {return p[d]; }))
			// "...-180" for table height plus pagination height (150+30)
			// "...-m[0]" for translation of the chart from the top downwards
			// "...-20" for bottom padding of the pagination 
			// "...-20" for enabling text on labels (serie values) to be visible
			.range([data.chart.height - (Number(removePixelsFromFontSize(data.title.style.fontSize))+Number(removePixelsFromFontSize(data.subtitle.style.fontSize)))*1.4- 180-20-m[0]-20, 0]);

			y[d].brush = d3.svg.brush()
			.y(y[d])
			.on("brush", brush);

		});
		//counting height of svg to depend on groups number
		var gr=JSON.parse(data.chart.groups);
		
		var svgHeight=Number(removePixelsFromFontSize(data.legend.title.style.fontSize))+8+gr.length*(Number(removePixelsFromFontSize(data.legend.element.style.fontSize))+8)+30;
	
   
		var legend=d3.select("#chart").append("div")
		         .style("float","right")
		         .style("width",legendWidth)
		         // "...-180" for table height plus pagination height (150+30)
		         // "...-20" for bottom padding of the pagination 
		         .style("height",data.chart.height-(Number(removePixelsFromFontSize(data.title.style.fontSize))+Number(removePixelsFromFontSize(data.subtitle.style.fontSize)))*1.2- 180-20)
		         .style("overflow","auto")
		         .append("svg:svg")
		         //.style("font-size",10)
		         // "...-180" for table height plus pagination height (150+30)
		         // "...-20" for bottom padding of the pagination 
		         .attr("height",svgHeight)
		         .style("width",legendWidth-25)
		         .append("svg:g")
		         .attr("transform", "translate("+0 + "," + m[0] + ")");
		
		legend.append("svg:g")
		.attr("transform",  "translate("+ (30) +"," + 0 + ")" )
		//.style("height",Number(removePixelsFromFontSize(data.legend.title.style.fontSize))+5)
		.append("svg:text")
		.style("fill",data.title.style.color)
		.style("font-family",data.legend.title.style.fontFamily)
		.style("font-size",data.legend.title.style.fontSize)
		.style("font-style",data.legend.title.style.fontStyle)
		.style("font-weight",data.legend.title.style.fontWeight)
		.style("text-decoration",data.legend.title.style.textDecoration)
		.attr("x", 20)
		.attr("y",-10)
		.attr("dy", ".31em")
		.text(groupcolumn);


		 legend.selectAll("g.legend")
		.data(groups)
		.enter().append("svg:g")
		.attr("class", "legend")
		//.attr("height",Number(removePixelsFromFontSize(data.legend.element.style.fontSize)))
		.attr("transform", function(d, i) {
			return "translate("+ 20 +"," + (i*(Number(removePixelsFromFontSize(data.legend.element.style.fontSize))+8) +Number(removePixelsFromFontSize(data.legend.title.style.fontSize))+8) + ")"; 
			});

		legend.selectAll("g.legend").append("svg:rect")
		//.attr("class", String)
		.style({"stroke":function(d) { return myColors(d); }, "stroke-width":"3px", "fill": function(d) { return myColors(d); }})
		.attr("x", 0)
		.attr("y", -6)
		.attr("width", 10)
		.attr("height", 10);

		legend.selectAll("g.legend").append("svg:text")
		.style("font-family",data.legend.element.style.fontFamily)
		.style("font-size",data.legend.element.style.fontSize)
		.style("font-style",data.legend.element.style.fontStyle)
		.style("font-weight",data.legend.element.style.fontWeight)
		.style("text-decoration",data.legend.element.style.textDecoration)
		.attr("x", 20)
		.attr("dy", ".31em")
		.text(function(d) {	
			return d; });

		//tooltip
		var tooltip=d3.select("#chart")
		.append("div")
		.attr("class","tooltip")
		.style("opacity","0");
		
		d3.selectAll(".tooltip")
		.style("position","absolute")
		.style("text-align","center")
		.style("min-width",data.tooltip.minWidth)
		.style("max-width",data.tooltip.maxWidth)
		.style("min-height",data.tooltip.minHeight)
		.style("max-height",data.tooltip.maxHeight)
		.style("padding",data.tooltip.padding)
		.style("font-size",data.tooltip.fontSize)
		.style("font-family",data.tooltip.fontFamily)
		.style("border",data.tooltip.border+"px solid black")	// @modifiedBy: danristo (danilo.ristovski@mht.net)
		.style("border-radius",data.tooltip.borderRadius+"px")
		.style("pointer-events","none");
		
		foreground = svg.append("svg:g")
		.attr("class","foreground")
		.style({"fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
		.selectAll("path")
		.data(records)
		.enter().append("svg:path")
		.attr("visible","true")
		.attr("d", path)
		.style("stroke", function(d) {return myColors(d[groupcolumn])});		
		
		/**
		 * This part is responsible for determining if the TOOLTIP should 
		 * be displayed on the chart. Current criteria for this issue is:
		 * if the number of all records that can be displayed at once (the 
		 * maximum number of them) is bigger than 'maxNumOfRecsForDispTooltip'
		 * do not display TOOLTIP for the lines (records) when mouse over.
		 * Otherwise, display TOOLTIP whenever mouse is over particular line
		 * its value. 		 
		 * @authors Lazar Kostic (koslazar), Ana Tomic (atomic)
		 * @commentedBy Danilo Ristovski (danristo)
		 */
		
		/**
		 * 'maxNumOfRecsForDispTooltip'	-	the maximum number of records that chart
		 * 									displays within which we can have (display)
		 * 									the TOOLTIP (if number of records of the 
		 * 									chart is bigger than this value, TOOLTIP
		 * 									will not be rendered).
		 */
		var maxNumOfRecsForDispTooltip = 20;
		
		if (records.length <= maxNumOfRecsForDispTooltip){

			foreground.on("mouseover",function(d){
				
				if(allTableData){

					for (var i=0; i<allTableData.length; i++)
					{
						if(d[data.chart.tooltip] === allTableData[i][data.chart.tooltip])
						{				
							/**
							 * Convert the RGB background color of the tooltip to its HSL pair in order
							 * to determine its darkness, i.e. its light level. If the color of the
							 * background (that depends on the color of the line over which the mouse is
							 * positioned) is too dark, we will put the white text of the tooltip. Otherwise,
							 * the color of the text will be black. 
							 * 
							 * NOTE: The threshold can be changed. Value of 0.4 is set as an example and the
							 * consequence of empirical approach.
							 * 
							 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
							 */
							var rgbColorForTooltipBckgnd = d3.rgb(myColors(d[groupcolumn]));
							var hslColorForTooltipBckgnd = 
								rgbToHsl(rgbColorForTooltipBckgnd.r, rgbColorForTooltipBckgnd.g, rgbColorForTooltipBckgnd.b);						
							var degreeOfLightInColor = hslColorForTooltipBckgnd[2];
							
							var darknessThreshold = 0.4;
							
							var tooltipBckgndColor = null;
							
							if (degreeOfLightInColor < darknessThreshold)
							{
								tooltipBckgndColor = "#FFFFFF";
							}
							else
							{
								tooltipBckgndColor = "#000000";
							}
	
							tooltip.transition().duration(50).style("opacity","1");
							
							tooltip.style("background", myColors(d[groupcolumn]));
							
							tooltip.text(d[data.chart.tooltip])	
								/**
								 * Set the color of the text, determined on the base of the level
								 * of light (darkness) of the tooltip background color.
								 * 
								 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
								 */
								.style("color",tooltipBckgndColor)		
								//.style("text-shadow", "1px 1px 2px #FFFFFF")	// @addedBy: danristo (danilo.ristovski@mht.net)			
								.style("left", (d3.event.pageX) + "px")     
								.style("top", (d3.event.pageY - 25) + "px");
						}

					}

				}
				else{

					tooltip.transition().duration(50).style("opacity","1");
					tooltip.style("background",myColors(d[groupcolumn]));
					tooltip.text(d[data.chart.tooltip])
					.style("left", (d3.event.pageX) + "px")     
					.style("top", (d3.event.pageY - 25) + "px");

				}

			})
			.on("mouseout",function(d){
				tooltip.transition()
				.duration(200)
				.style("opacity","0");
			});

		}
		

		var g = svg.selectAll(".column")
		.data(columns)
		.enter().append("svg:g")
		.attr("class", "column")
		.style({"font-family":data.chart.font})
		
		/**
		 * Added so to follow the main font style of the chart: e.g. if the font style
		 * of the chart (main font style) is 'underline', then all elements on the 
		 * PARALLEL chart should have this style (undeline) as the base.
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		.style("font-size",data.chart.style.fontSize)
		.style("font-style",data.chart.style.fontStyle)
		.style("font-weight",data.chart.style.fontWeight)
		.style("text-decoration",data.chart.style.textDecoration)
		
		.attr("transform", function(d) {return "translate(" + x(d) + ")"; })
		.call(d3.behavior.drag()
				.origin(function(d) { return {x: x(d)}; })
				.on("dragstart", dragstart)
				.on("drag", drag)
				.on("dragend", dragend));

		// Axis
		g.append("svg:g")
		.attr("class","axis")
		.each(function(d) { d3.select(this).call(axis.scale(y[d])); })
		.attr("fill",data.yAxis.labels.style.color)
		.style("font-family",data.yAxis.labels.style.fontFamily)
		.style("font-size",data.yAxis.labels.style.fontSize)
		.style("font-style",data.yAxis.labels.style.fontStyle)
		.style("font-weight",data.yAxis.labels.style.fontWeight)
		.style("text-decoration",data.yAxis.labels.style.textDecoration)
		.append("svg:text")
		.attr("text-anchor", "middle")
		.attr("y", -data.axis.axisColNamePadd)
		.attr("fill",data.xAxis.labels.style.color)
		.style("font-family",data.xAxis.labels.style.fontFamily)
		.style("font-size",data.xAxis.labels.style.fontSize)
		.style("font-style",data.xAxis.labels.style.fontStyle)
		.style("font-weight",data.xAxis.labels.style.fontWeight)
		.style("text-decoration",data.xAxis.labels.style.textDecoration)
		.text(String)
		.style({"cursor":"move"});

		g.selectAll(".axis line, .axis path").style({"fill":"none","stroke": data.axis.axisColor,"shape-rendering": "crispEdges"});

		// Add a brush for each axis.
		g.append("svg:g")
		.style({"fill-opacity":" .3","stroke":data.axis.brushColor,"shape-rendering":" crispEdges"})
		.each(function(d) { d3.select(this).call(y[d].brush); })
		.selectAll("rect")
		.attr("x", brushx)
		.attr("width", brushWidth);

	}

	else{

		// Set title
		d3.select("body").append("div")
		.style("color",data.emptymessage.style.color)
		.style("text-align",data.emptymessage.style.align)
		.style("font-family",data.emptymessage.style.fontFamily)
		.style("font-style",data.emptymessage.style.fontWeight)
		.style("font-size",data.emptymessage.style.fontSize)
		.text(data.emptymessage.text);	

	}
	
	//cross navigation
	foreground.on("click",clickLine);
	
	function clickLine(d){
		if(data.crossNavigation.hasOwnProperty('crossNavigationDocumentName')){
			paramethers=crossNavigationParamethers(d);
			var navigParams={
				crossNavigationDocumentName:data.crossNavigation.crossNavigationDocumentName,
				crossNavigationDocumentParams:data.crossNavigation.crossNavigationDocumentParams,
				categoryName:paramethers.categoryName,
				categoryValue:paramethers.categoryValue,
				serieName:paramethers.serieName,
				serieValue:paramethers.serieValue,
				groupingCategoryName:paramethers.groupingCategoryName,
				groupingCategoryValue:paramethers.groupingCategoryValue
			};
			handleCrossNavigationTo(navigParams);
		}
	   
	}
	
	function crossNavigationParamethers(d){
		 var params={
				    "categoryName" : null,
					"categoryValue":null,
					"serieName":null,
					"serieValue":null,
					"groupingCategoryName":null,
					"groupingCategoryValue":null
				   };	
				   var category=data.chart.tooltip;
				   params.categoryName=category;
				   params.categoryValue=d[category];
				   var groupCategory=data.chart.group;
				   params.groupingCategoryName=groupCategory;
				   params.groupingCategoryValue=d[groupCategory];
				   
				   return params;
	}

	// TABLE
	var initialTableData=records;

	var allTableData=initialTableData; // all records or filtered records
	var currentTableData=allTableData.slice(0,5); // up to 5 recoords
	var firstDisplayed=1;
	var lastDisplayed=0;
	
	if(allTableData.length > 5){
		lastDisplayed=5;
	}else{
		lastDisplayed=allTableData.length;
	}

	var tableDiv = d3.select("#chart")
						.append("div").attr("id","tableDiv")
						.style("width",data.chart.width-legendWidth)						
						.style("padding-bottom",10)
						.style("padding-top",20);
	
	var table = tableDiv.append("div").attr("id","tDiv").attr("align","center")
	                .attr("width", data.chart.width-legendWidth)
	                .append("table")
					.style("width", data.chart.width-legendWidth)
					
					/**
					 * The next style parameter setting allow us to reset font stylization provided 
					 * for the whole chart (independency of the table element over whole chart). 
					 * This way we can e.g. reset the text decoration (that the whole chart has) and
					 * provide that table does not have the one. This is important since (for now)
					 * table gets font customization from the legend's elements.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */ 
					//.style("display", "block") 
					
					/**
					 * For now, table text elements will use the font customization provided for
					 * legend's elements.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					.style("font-family", data.legend.element.style.fontFamily)
					.style("font-size", data.legend.element.style.fontSize)
					.style("font-style", data.legend.element.style.fontStyle)
					.style("font-weight", data.legend.element.style.fontWeight)
					.style("text-decoration", data.legend.element.style.textDecoration)
					.style("padding-right",m[1])
					.style("padding-left",m[3]);
	
	var paginationBar = tableDiv.append("div").attr("id","pBar")
	                        .attr("align","center")
							//.style("padding-left",w/2+m[3]/2-150)
							.style("padding-top",10)
	                        .style("padding-left",m[3])
	                        .style("padding-right",m[1]);
	
	var prevButton = paginationBar.append("button")
						.text("<< Prev")
						.on("click", function(){ return showPrev(); });
	
	var paginationText = paginationBar.append("label")
							.html("&nbsp;&nbsp;" + firstDisplayed + " to " + lastDisplayed + " of " + allTableData.length + "&nbsp;&nbsp;")
							/**
							 * The next style parameter setting allow us to reset font stylization provided 
							 * for the whole chart (independency of the table element over whole chart). 
							 * This way we can e.g. reset the text decoration (that the whole chart has) and
							 * provide that table does not have the one. This is important since (for now)
							 * table gets font customization from the legend's elements.
							 * 
							 * @author: danristo (danilo.ristovski@mht.net)
							 */ 
							.style("display", "inline-block") 
							
							/**
							 * For now, table text elements will use the font customization provided for
							 * legend's elements.
							 * 
							 * @author: danristo (danilo.ristovski@mht.net)
							 */
							.style("font-family",data.legend.element.style.fontFamily)
							.style("font-size",data.legend.element.style.fontSize)
							.style("font-style",data.legend.element.style.fontStyle)
							.style("font-weight",data.legend.element.style.fontWeight)
							.style("text-decoration",data.legend.element.style.textDecoration);
	
	var nextButton = paginationBar.append("button")
						.text("Next >>")
						.on("click", function(){ return showNext(); });

	if(firstDisplayed===1){
		prevButton.attr("disabled","true");	
	}

	if(lastDisplayed===allTableData.length){
		nextButton.attr("disabled","true");
	}

	//columns for table
	var tableColumns=[];
	tableColumns.push(groupcolumn);
	tableColumns.push(data.chart.tooltip);
	tableColumns=tableColumns.concat(columns);

	
	//table header
	table.append("thead")
	      .style("background-color","silver") 
	      .style("border","1px solid black")
	      .attr("border-collapse","collapse")
	     .append("tr")
	     .style("width", data.chart.width-legendWidth)
	     .style("height","30px")
	     .selectAll("th")
	     .data(tableColumns).enter()
	     .append("th")
	     .text(function(d){return d;});
	
	//table body
	table.append("tbody")
	     .selectAll("tr")
	.data(currentTableData)
	     .enter()
	     .append("tr")
	     .style("width", data.chart.width-legendWidth)
	     .style("background-color",function(d,i){
	    	 if(i%2==1)return "lightgray";
	     })
	     .attr("class","tdata")
	.on("mouseover",function(d){
		d3.select(this).style("outline","solid dimgray");
		return selectSingleLine(d);})
	     .on("mouseout",function(d){
		d3.select(this).style("outline","none");
	    	foreground.style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
	 		.style({"stroke":function(d) { return myColors(d[groupcolumn]);}});
           })
	     .selectAll("td")
	     .data(function(row){
	    	 return tableColumns.map(function(column) {
	                return {column: column, value: row[column]};
	            });
	     }).enter()
	       .append("td")
	.on("click",function(d){return filterTable(d,allTableData);})
	       .text(function(d){return d.value})
	       .style("text-align","center");
	       

	function dragstart(d) {
		i = columns.indexOf(d);
	}

	function drag(d) {
		x.range()[i] = d3.event.x;
		columns.sort(function(a, b) { return x(a) - x(b); });
		g.attr("transform", function(d) { return "translate(" + x(d) + ")"; });
		foreground.attr("d", path);
	}

	function dragend(d) {
		x.domain(columns).rangePoints([0, w-legendWidth]);
		var t = d3.transition().duration(500);
		t.selectAll(".column").attr("transform", function(d) { 
			return "translate(" + x(d) + ")"; });
		t.selectAll(".foreground path").attr("d", path);

	}

	// Returns the path for a given data point.
	function path(d) {
		return line(columns.map(function(p) { 
			return [x(p), y[p](d[p])]; }));
	}

	// Handles a brush event, toggling the display of foreground lines.
	function brush() {
		var actives = columns.filter(function(p) { return !y[p].brush.empty(); }),
		extents = actives.map(function(p) { return y[p].brush.extent(); });
		foreground.classed("fade", function(d) {

			return !actives.every(function(p, i) {

				return extents[i][0] <= d[p] && d[p] <= extents[i][1];
			});
		});

		foreground.classed("notfade", function(d) {
			return actives.every(function(p, i) {
				return extents[i][0] <= d[p] && d[p] <= extents[i][1];
			})
		});

		var allRows=records;
		
		filteredRows=allRows.filter(function(d) {
         
			return actives.every(function(p, i) {

				return extents[i][0] <= d[p] && d[p] <= extents[i][1];
			});
		});
		
		nextButton.attr("disabled",null);
		prevButton.attr("disabled",null);

		allTableData=filteredRows;
		
		currentTableData=allTableData.slice(0,5);
		firstDisplayed=1;
		if(allTableData.length > 5){
			lastDisplayed=5;
		}else{
			lastDisplayed=allTableData.length;
		}

		if(firstDisplayed===1){
			prevButton.attr("disabled","true");	
		}

		if(lastDisplayed===allTableData.length){
			nextButton.attr("disabled","true");
		}
		
		paginationText.html("&nbsp;&nbsp;" + firstDisplayed + " to " + lastDisplayed + " of " + allTableData.length + "&nbsp;&nbsp;")
					/**
					 * The next style parameter setting allow us to reset font stylization provided 
					 * for the whole chart (independency of the table element over whole chart). 
					 * This way we can e.g. reset the text decoration (that the whole chart has) and
					 * provide that table does not have the one. This is important since (for now)
					 * table gets font customization from the legend's elements.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */ 
					.style("display", "inline-block") 
					
					/**
					 * For now, table text elements will use the font customization provided for
					 * legend's elements.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					.style("font-family",data.legend.element.style.fontFamily)
					.style("font-size",data.legend.element.style.fontSize)
					.style("font-style",data.legend.element.style.fontStyle)
					.style("font-weight",data.legend.element.style.fontWeight)
					.style("text-decoration",data.legend.element.style.textDecoration);
		
		var dummy=[];
		d3.select("table").select("tbody").selectAll("tr").data(dummy).exit().remove();
		
		 d3.select("table")
		   .select("tbody")
		   .selectAll("tr")
		.data(currentTableData)
		   .enter()
		   .append("tr")
	       .style("background-color",function(d,i){
	    	 if(i%2==1)return "lightgray";
	        })
		    .attr("class","tdata") 
		.on("mouseover",function(d){ 
			d3.select(this).style("outline","solid dimgray");
			return selectSingleLine(d);
			})
	     .on("mouseout",function(d){
			d3.select(this).style("outline","none");
	    	d3.selectAll(".notfade").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
	 		.style({"stroke":function(d) { return myColors(d[groupcolumn]);}});
           })
	     .selectAll("td")
	     .data(function(row){
	    	 return tableColumns.map(function(column) {
	                return {column: column, value: row[column]};
	            });
	     }).enter()
	       .append("td")
		.on("click",function(d){return filterTable(d,filteredRows);})
	       .text(function(d){return d.value})
		   .style("text-align","center");
		 
		
		d3.selectAll(".fade").style({"stroke": "#000","stroke-opacity": ".02"}); 
		d3.selectAll(".notfade").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
		.style({"stroke" :function(d) { return myColors(d[groupcolumn]);}});


	}

	function selectSingleLine(selectedRow){
		foreground.attr("visible", function(d){
          return (d===selectedRow)?"true":"false";
      });
		
	

		d3.select(".foreground").selectAll("[visible=false]").style({"stroke": "#000","stroke-opacity": ".02"}); 
		d3.select(".foreground").selectAll("[visible=true]").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
		.style({"stroke" :function(d) { return myColors(d[groupcolumn]);}});
	}

	function filterTable(selectedCell,coollectionToFilter){
		
		nextButton.attr("disabled",null);
		prevButton.attr("disabled",null);

		var filteredData=coollectionToFilter.filter(function(d){return d[selectedCell.column]===selectedCell.value;});		

		allTableData=filteredData;
		currentTableData=allTableData.slice(0,5);
		firstDisplayed=1;
		if(allTableData.length > 5){
			lastDisplayed=5;
		}else{
			lastDisplayed=allTableData.length;
		}
		if(firstDisplayed===1){
			prevButton.attr("disabled","true");	
		}

		if(lastDisplayed===allTableData.length){
			nextButton.attr("disabled","true");
		}
		paginationText.html("&nbsp;&nbsp;" + firstDisplayed + " to " + lastDisplayed + " of " + allTableData.length + "&nbsp;&nbsp;")
					/**
					 * The next style parameter setting allow us to reset font stylization provided 
					 * for the whole chart (independency of the table element over whole chart). 
					 * This way we can e.g. reset the text decoration (that the whole chart has) and
					 * provide that table does not have the one. This is important since (for now)
					 * table gets font customization from the legend's elements.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */ 
					.style("display", "inline-block") 
					
					/**
					 * For now, table text elements will use the font customization provided for
					 * legend's elements.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					.style("font-family",data.legend.element.style.fontFamily)
					.style("font-size",data.legend.element.style.fontSize)
					.style("font-style",data.legend.element.style.fontStyle)
					.style("font-weight",data.legend.element.style.fontWeight)
					.style("text-decoration",data.legend.element.style.textDecoration);

		var dummy=[];
		d3.select("table").select("tbody").selectAll("tr").data(dummy).exit().remove();

		d3.select("table")
		.select("tbody")
		.selectAll("tr")
		.data(currentTableData)
		.enter()
		.append("tr")
		.style("background-color",function(d,i){
			if(i%2==1)return "lightgray";
		})
		.attr("class","tdata") 
		.on("mouseover",function(d){
			d3.select(this).style("outline","solid dimgray");
			return selectSingleLine(d);
			})
		.on("mouseout",function(d){
  
			d3.select(this).style("outline","none");
			foreground.attr("visible", function(d){
				return (d[selectedCell.column]===selectedCell.value)?"true":"false";
			});

			d3.selectAll(".fade").attr("visible","false");
			d3.selectAll(".notfade").attr("visible", function(d){
				return (d[selectedCell.column]===selectedCell.value)?"true":"false";
			});

			d3.select(".foreground").selectAll("[visible=false]").style({"stroke": "#000","stroke-opacity": ".02"}); 
			d3.select(".foreground").selectAll("[visible=true]").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
			.style({"stroke" :function(d) { return myColors(d[groupcolumn]);}});

			//foreground.selectAll(".fade").style({"stroke": "#000","stroke-opacity": ".02"});
		})
		.selectAll("td")
		.data(function(row){
			return tableColumns.map(function(column) {
				return {column: column, value: row[column]};
			});
		}).enter()
		.append("td")
		.text(function(d){return d.value})
		.style("text-align","center");



	}

	function updateTable(){
		var dummy=[];
		d3.select("table").select("tbody").selectAll("tr").data(dummy).exit().remove();

		d3.select("table")
		.select("tbody")
		.selectAll("tr")
		.data(currentTableData)
		.enter()
		.append("tr")
		.style("background-color",function(d,i){
			if(i%2==1)return "lightgray";
		})
		.attr("class","tdata") 
		.on("mouseover",function(d){ 			
			d3.select(this).style("outline","solid dimgray");
			return selectSingleLine(d);})
		.on("mouseout",function(){
			d3.select(this).style("outline","none");
			foreground.attr("visible",function(d){
				return (allTableData.indexOf(d)!=-1)?"true":"false";
			});

			d3.select(".foreground").selectAll("[visible=false]").style({"stroke": "#000","stroke-opacity": ".02"}); 
			d3.select(".foreground").selectAll("[visible=true]").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
			.style({"stroke" :function(d) { return myColors(d[groupcolumn]);}});


		})
		.selectAll("td")
		.data(function(row){
			return tableColumns.map(function(column) {
				return {column: column, value: row[column]};
			});
		}).enter()
		.append("td")
		.on("click",function(d){return filterTable(d,allTableData);})
		.text(function(d){return d.value})
		.style("text-align","center");

		foreground.attr("visible",function(d){
			return (allTableData.indexOf(d)!=-1)?"true":"false";
		});

	d3.select(".foreground").selectAll("[visible=false]").style({"stroke": "#000","stroke-opacity": ".02"}); 
	d3.select(".foreground").selectAll("[visible=true]").style({ "fill": "none", "stroke-opacity": ".5","stroke-width": "2px"})
	.style({"stroke" :function(d) { return myColors(d[groupcolumn]);}});
	}

	function showNext(){
		prevButton.attr("disabled",null);
		firstDisplayed=firstDisplayed+5;
		lastDisplayed=lastDisplayed+5;
		if(lastDisplayed>allTableData.length){
			lastDisplayed=allTableData.length;
		}



		currentTableData=[];
		currentTableData=allTableData.slice(firstDisplayed-1,lastDisplayed);



		if(lastDisplayed === allTableData.length){
			nextButton.attr("disabled","true");
		}



		paginationText.html("&nbsp;&nbsp;" + firstDisplayed + " to " + lastDisplayed + " of " + allTableData.length + "&nbsp;&nbsp;")
			/**
			 * The next style parameter setting allow us to reset font stylization provided 
			 * for the whole chart (independency of the table element over whole chart). 
			 * This way we can e.g. reset the text decoration (that the whole chart has) and
			 * provide that table does not have the one. This is important since (for now)
			 * table gets font customization from the legend's elements.
			 * 
			 * @author: danristo (danilo.ristovski@mht.net)
			 */ 
			.style("display", "inline-block") 
			
			/**
			 * For now, table text elements will use the font customization provided for
			 * legend's elements.
			 * 
			 * @author: danristo (danilo.ristovski@mht.net)
			 */
			.style("font-family",data.legend.element.style.fontFamily)
			.style("font-size",data.legend.element.style.fontSize)
			.style("font-style",data.legend.element.style.fontStyle)
			.style("font-weight",data.legend.element.style.fontWeight)
			.style("text-decoration",data.legend.element.style.textDecoration);
		
		updateTable();	


	}

	function showPrev(){
		nextButton.attr("disabled",null);
		firstDisplayed=firstDisplayed-5;
		if(lastDisplayed===allTableData.length){
			if(allTableData.length%5!=0){
				lastDisplayed=lastDisplayed-(allTableData.length%5);
			}else{
				lastDisplayed=lastDisplayed-5;	
			}
		}
		else{
			lastDisplayed=lastDisplayed-5;
		}



		currentTableData=[];
		currentTableData=allTableData.slice(firstDisplayed-1,lastDisplayed);

		if(firstDisplayed===1){
			prevButton.attr("disabled","true");	
		}

		paginationText.html("&nbsp;&nbsp;" + firstDisplayed + " to " + lastDisplayed + " of " + allTableData.length + "&nbsp;&nbsp;")
				/**
				 * The next style parameter setting allow us to reset font stylization provided 
				 * for the whole chart (independency of the table element over whole chart). 
				 * This way we can e.g. reset the text decoration (that the whole chart has) and
				 * provide that table does not have the one. This is important since (for now)
				 * table gets font customization from the legend's elements.
				 * 
				 * @author: danristo (danilo.ristovski@mht.net)
				 */ 
				.style("display", "inline-block") 
				
				/**
				 * For now, table text elements will use the font customization provided for
				 * legend's elements.
				 * 
				 * @author: danristo (danilo.ristovski@mht.net)
				 */
				.style("font-family",data.legend.element.style.fontFamily)
				.style("font-size",data.legend.element.style.fontSize)
				.style("font-style",data.legend.element.style.fontStyle)
				.style("font-weight",data.legend.element.style.fontWeight)
				.style("text-decoration",data.legend.element.style.textDecoration);
		
		
		updateTable();	

	}
}
	
/**
 * Javascript function that serves for rendering the CHORD chart
 * @param jsonData Input data in JSON format, needed for rendering the chart on the client side
 */
function renderChordChart(jsonData)
{
	console.log(jsonData);
	/**
	 *  'opacityMouseOver' - value for the opacity of the item (row) that is covered by the mouse pointer and all the items 
	 *  that are linked (connected) to that row (item)
	 *  
	 *  'opacityMouseOutAndDefault' - value of the opacity of all graphical items (arcs and stripes) when non of the items 
	 *  (rows) is selected by the mouse pointer or when the mouse pointer leaves an item
	 */
	 // TODO: Maybe customizable ???
	var opacityMouseOutAndDefault = 0.6;
	var opacityMouseOver = 0.1;
	
	/**
	 * 'allFieldsObject' - object that contains information about the data that we got from the server. Particularly we get
	 * data about the rows and columns that our matrix (will) contain
	 * 
	 * 'allFieldsArray' - array of all rows/columns items (fields) sorted in alphabetically ascending order that matrix (will)
	 *  contain. This way can sort all rows and columns of the future matrix in the same, alphabetically ascending, order
	 */
	var allFieldsObject = jsonData.data[0].metaData.fields;
	var allFieldsArray = new Array();
	 
	 /**
  	  * 'columnsPairedWithRows' - contains data about which columns are linked to the particular row, that is, which column is in
  	  * the intersection with the particular row and what is the value of their intersection (the value of the matrix field). We 
  	  * will need this data to see outgoing items (columns to which particular row is connected). 
  	  * 
  	  * 'rowsPairedWithColumns' - containes data about which rows are connected to the particular column (we will need this data
  	  * in pair woth the previous one - columnsPairedWithRows). We will need this to see incoming items (which rows (items) are
  	  * connected to the particular item (in this case, column))
  	  */
  	 var columnsPairedWithRows = new Array();	
  	 var rowsPairedWithColumns = new Array();
	
	/**
	 * TODO: NOT USED: Useful when filtering is enabled (FILTER tags attribute 'value' is set to 'true')
	 */
	function contains(a, obj) {
	    for (var i = 0; i < a.length; i++) {
	        if (a[i].value === obj) {
	            return true;
	        }
	    }
	    return false;
   }

	/**
	 * TODO: NOT USED: Useful when filtering is enabled (FILTER tags attribute 'value' is set to 'true')
	 */
   function getIndex(a, obj) {
	    for (var i = 0; i < a.length; i++) {
	        if (a[i].value === obj) {
	            return a[i].index;
	        }
	    }
	    return -1;
   }

   /**
    * Returns an array of tick angles and labels, given a group.
    */
   // TODO: Customize text that goes next to the ticks (here it is 'k') ???
   function groupTicks(d)     
   {   	
		var k = (d.endAngle - d.startAngle) / d.value;
		
		return d3.range(0, d.value, 1000).map(function(v, i) {			
			return {
				startAngle: d.startAngle,
				endAngle: d.endAngle,
				angle: v * k + d.startAngle,
				label: i % 5 ? null : v / 1000 + "k"
				};
			});
   }
	
   /**
    * 'deselectClickedItem' - indicator if user clicked on some item on the chart - if false, prevent mouse over 
    * and mouse out events. If true every item on the chart should be deselected - full colored
    * 
    * 'indexOfItemSelected' - index of the item of the chart that is selected. We need this parameter to take care
    * if we should
    */
    var deselectClickedItem = undefined;
    var indexOfItemSelected = -1;
    var previouslySelected = false;
    
    var selectedSomeItem = false;
    var indexOfSelectedItem = -1;
    var enableMouseOver = true;
    var enableMouseOut = true;
    var enableOtherSelect = false;
        
    /**
     * Returns an event handler for fading a given chord group.
     */
	function fadeMouseOver()
	{	
		return function(g, i) {
			
			setParamsClickAndMouseOver(g,i,false);
			
			/**
			 * With filtering we are getting pairs of stripes: one member of the pair is the source item (the row) whose 
			 * outgoing stripe(s) we need to leave as default color (darker); the second member of the pair is the target
			 * item's stripe (both linking two items: source and target) that is coming into the source item (the row). 
			 * We also need to leave this, target item's stripe as default color (darker).
			 * 
			 * The same logic, just in other direction is for this part inside the 'fadeMouseOut' function - those stripes
			 * (that we mentioned in previous paragraph) need to be reset as default color (dark) except those that are the
			 * subject of our discussion: the soure and target item's stripe (links between them).
			 */
			if (enableMouseOver)
			{		
				svg.selectAll(".chord path")
				 	.filter(function(d) { return d.source.index != i && d.target.index != i; })
				 	.transition()
				 	.style("opacity", opacityMouseOver);				
			}
		}
	}
	
	function fadeMouseOut()
	{
		return function(g, i) 
		{		
			setParamsClickAndMouseOver(g,i,false);
			
			if (enableMouseOut)
			{
				svg.selectAll(".chord path")
				 	.filter(function(d) { return d.source.index != i && d.target.index != i; })
				 	.transition()
				 	.style("opacity", opacityMouseOutAndDefault);
			}			
		};
	}

	function clickOnItem()
	{   
		
		return function(g, i) 
		{	
		    
			setParamsClickAndMouseOver(g,i,true);
				
			/**
			 * Reset all stripes to the default (mouse out, darker) color.
			 */
			svg.selectAll(".chord path")
			 	.filter(function(d) { return true; })
			 	.transition()
			 	.style("opacity", opacityMouseOutAndDefault);
				
			/**
			 * Find the one (stripes that link target and source items) that we need to leave as default 
			 * (darker, mouse out) color. Other stripes will be shadowed (lighter (mouse over) color).
			 */
			svg.selectAll(".chord path")
				.filter(function(d) { return d.source.index != i && d.target.index != i; })
			 	.transition()
			 	.style("opacity", opacityMouseOver);
		    
			if(jsonData.crossNavigation.hasOwnProperty('crossNavigationDocumentName')){
				paramethers=crossNavigationParamethers(jsonData.data[0].rows[i]);
				var navigParams={
					crossNavigationDocumentName:jsonData.crossNavigation.crossNavigationDocumentName,
					crossNavigationDocumentParams:jsonData.crossNavigation.crossNavigationDocumentParams,
					categoryName:paramethers.categoryName,
					categoryValue:paramethers.categoryValue,
					serieName:paramethers.serieName,
					serieValue:paramethers.serieValue,
					groupingCategoryName:paramethers.groupingCategoryName,
					groupingCategoryValue:paramethers.groupingCategoryValue
				};
				handleCrossNavigationTo(navigParams);
			}
			
		};
	}
	
	function crossNavigationParamethers(d){
		
		var param={
			"categoryName":null,
			"categoryValue":null,
			"serieName":null,
			"serieValue":null,
			"groupingCategoryName":null,
			"groupingCategoryValue":null
		};
		param.categoryValue=d.column_0;
		serie=0;
		
		for(property in d){
			if(property != "column_0"){
				serie=serie+d[property];
			}
		}
	   param.serieValue=serie;	
		
	   return param;
	}
	
	
	/**
	 * Set parameters that are necessary for the events that we are listening to: the mouse over and click event.
	 * According to these parameters we are going to control when should we enable or disable listening to the 
	 * mouse over and/or mouse out events.
	 */
	function setParamsClickAndMouseOver(d,i,isClick)
	{
		
		/**
		 * The function responsible for processing the 'click' event listener's call is calling this function.
		 * This function is not called by the 'fadeMouseOver'/'fadeMouseOut' functions that are responsible
		 * for processing the 'mouseover'/'mouseout' event listener's call.
		 */
		if (isClick)
		{			 
			enableMouseOver = false;
			 
			/**
			 * No item is selected (clicked) on the chart - select it and freeze the chart.
			 */
			if (selectedSomeItem == false)
			{
				selectedSomeItem = true;		
				enableMouseOut = false;		
				indexOfSelectedItem = i;	
				
				/**
				 * TODO:
				 * 		Temporary function for printing out the items (source and target) that are the
				 * 		result of the selection (clicking) operation on the chart's item.
				 */
				printTheResultWhenSelecting(i);
			}
			/**
			 * Some item is already selected (the chart is still freeze).
			 */
			else 
			{
				/**
				 * The item that is now clicked (selected) is already selected, hence we need to deselect it and
				 * unfreeze the chart.
				 */
				if (indexOfSelectedItem == i)
				{
					selectedSomeItem = false;	
					enableMouseOut = true;		
					indexOfSelectedItem = -1;	
				}
				/**
				 * The item that we have now clicked (selected) is different for the one that is alreday selected,
				 * hence select the newly clicked (selected) item and keep the chart freeze.
				 */
				else
				{
					selectedSomeItem = true;	
					enableMouseOut = false;	
					indexOfSelectedItem = i;	
					
					printTheResultWhenSelecting(i);
				}
			}
		}
		/**
		 * This function is called by the 'fadeMouseOver'/'fadeMouseOut' functions that are responsible
		 * for processing the 'mouseover'/'mouseout' event listener's call.
		 */
		else 
		{
			/**
			 * if-block: 
			 * 		None of items (arcs) is selected (clicked) - the chart us 'unfreeze'
			 * else-block:
			 * 		Some item (arc) is selected (clicked) - the chart is 'freeze'
			 */
			if (selectedSomeItem == false)		
			{
				enableMouseOver = true;		
				enableMouseOut = true; 		
			}
			else
			{
				enableMouseOver = false;	
				enableMouseOut = false;		
			}				 
		}
	}
	
	function printTheResultWhenSelecting(i)
	{
		// With which columns is this (selected, clicked) row paired 
		console.log(columnsPairedWithRows[i]);
		// Which columns are paired with this (selected, clicked) row 
	   	console.log(rowsPairedWithColumns[i]);
	}
	

	/**
	 * We will specify this value in order to leave enough space for labels that
	 * are going to surround the chart in order to 
	 * (danilo.ristovski@mht.net)
	 */
	//var spaceForLabels = 20;
	

	/* TODO: Enable and customize empty DIV of specified height in order to make some space between the subtitle and
	 * the chart (values on ticks) ??? */
	// var emptySplitDivHeight = 10;
	
	var emptySplitDivHeight = 0;
	
	/**
	 * Width and height of the chart
	 */
	var width = jsonData.chart.width;
	var height = jsonData.chart.height;
	
	var chartDivWidth=width;
	var chartDivHeight=height;
	
	if(jsonData.title.text!="" || jsonData.subtitle.text!=""){
		emptySplitDivHeight=10;
		chartDivHeight-=Number(removePixelsFromFontSize(jsonData.title.style.fontSize))*1.2;
		chartDivHeight-=Number(removePixelsFromFontSize(jsonData.subtitle.style.fontSize))*1.2;
		chartDivHeight-=emptySplitDivHeight*1.2;
		
	}
	
	var heightForChartSvg = jsonData.chart.height-(Number(removePixelsFromFontSize(jsonData.title.style.fontSize))
							 + Number(removePixelsFromFontSize(jsonData.subtitle.style.fontSize))
							 +emptySplitDivHeight)*1.2;
	
	
	var innerRadius = Math.min(width, height) * .35;
    var outerRadius = innerRadius * 1.1;
    
    /**
     * Number of row/column elements of the squared martix
     */
    var elemSize = jsonData.data[0].results;
	
	var fill = d3.scale.ordinal()
    			.domain(d3.range(elemSize))
				.range(jsonData.colors);
	
	d3.select("body")
		.append("div").attr("id","main")
		// Set the real height of the entire chart (the one that user specified)
		.style("height",height)	
		.style("width",width)
		.style("background-color",jsonData.chart.style.backgroundColor)
		.style("font-style",jsonData.chart.style.fontStyle)
		.style("font-weight",jsonData.chart.style.fontWeight)
		.style("text-decoration",jsonData.chart.style.textDecoration)
		.style("font-size",jsonData.chart.style.fontSize);
	
	// Set title
	d3.select("#main").append("div")
		.style("color",jsonData.title.style.color)
		.style("text-align",jsonData.title.style.align)
		.style("font-family",jsonData.title.style.fontFamily)
		.style("font-style",jsonData.title.style.fontStyle)
		.style("font-weight",jsonData.title.style.fontWeight)
		.style("text-decoration",jsonData.title.style.textDecoration)
		.style("font-size",jsonData.title.style.fontSize)
		.text(jsonData.title.text);

	// Set subtitle
	d3.select("#main").append("div")
		.style("color",jsonData.subtitle.style.color)
		.style("text-align",jsonData.subtitle.style.align)
		.style("font-family",jsonData.subtitle.style.fontFamily)
		.style("font-style",jsonData.subtitle.style.fontStyle)
		.style("font-weight",jsonData.subtitle.style.fontWeight)
		.style("text-decoration",jsonData.subtitle.style.textDecoration)
		.style("font-size",jsonData.subtitle.style.fontSize)
		.text(jsonData.subtitle.text);
		
	d3.select("#main").append("div").style("height", emptySplitDivHeight);
	
	d3.select("#main").append("div").attr("id","chartD3");
	
	var svg = d3.select("#chartD3").append("div")
	 			.attr("class","chart")	 			
	 			.style("width",width)
	 			.style("height",chartDivHeight)
	 			.attr("align","center")
				.append("svg:svg")
				.attr("width",width)				
				.attr("height",heightForChartSvg)	
				.attr("viewBox","-125 -125 "+(Number(width)+250)+" "+ (Number(heightForChartSvg)+250))
				.attr( "preserveAspectRatio","xMidYMid meet")
				.style("background-color",jsonData.chart.style.backgroundColor)	
				.append("svg:g")
				.attr("transform", "translate(" + width / 2 + "," + ((Number(heightForChartSvg)) / 2) + ")");
	
	/**
	 * [START] Data processing part
	 */
	var rows = jsonData.data[0].rows;
	//var source,target,value;
	
	var matrix = new Array(elemSize);
	
	for (var i = 0; i < matrix.length;i++)
	{
		matrix[i] = new Array(elemSize);
	}	
	 	
	// use dataset as-is		
	 for (i = 0; i < rows.length; i++) 
	 {		 
		 for (j = 0; j < elemSize; j++) 
		 {
			var column = 'column_'+(j+1);			
			matrix[i][j] = parseFloat(rows[i][column]);
		 };	
  	 };  	 
  	 
  	 // Variable that will let us render this chart part, but preventing listening for the mouseover event
  	 var arcs = null;
 
  	/**
  	  * Which column is paired with which row (row is the initial point of view)
  	  * - 	array of objects that are composed of row attribute and its array of 
  	  * 	columns that are intersected with it and have value != 0
  	  */
  	 
  	 for (var i=0; i<allFieldsObject.length; i++)
	 {
  		 var tempObject = {};
  		 var arrayOfColumnsAndValues = new Array();
  		 
  		 var rowName = allFieldsObject[i].header;  		 
  		 allFieldsArray.push(rowName);
  		 
  		 tempObject.row = allFieldsObject[i].header;
  		 
  		 for (var j=0; j<allFieldsObject.length; j++)
		 {
  			 var tempSubArrayObjects = {};
  			 
  			 if (rows[i]["column_"+(j+1)] != 0)
			 {
  				tempSubArrayObjects.column = allFieldsObject[j].header;
  				tempSubArrayObjects.value = rows[i]["column_"+(j+1)];
  				
  				arrayOfColumnsAndValues.push(tempSubArrayObjects);
			 }  			
		 }
  		 
  		 tempObject.pairedWith = arrayOfColumnsAndValues;
  		 
  		 columnsPairedWithRows.push(tempObject);
	 }

  	 // draw the graph before getting data for populating table in the legend (no loosing time)
	 drawGraph(matrix); 
  	 
  	 for (var i=0; i<allFieldsObject.length; i++)
	 {
  		 var columnName = allFieldsObject[i].header;
  		 
  		 var tempObject = {};
  		 
  		 var tempArray = new Array();
  		 
  		 tempObject.column = columnName;
  		 
  		 // go through all rows for this (i-th) column
  		 for (var j=0; j<allFieldsObject.length; j++)
		 {
  			 var rowName = allFieldsObject[j].header;
  			 var tempArraysObject = {};
  			 
  			 /**
  			  * "... && rowName!=columnName" => we do not need information about intersection of this columns with the row of the same name (i,i), 
  			  * since we got this information when populating the 'columnsPairedWithRows' array. We do must not duplicate the data.
  			  */
  			 // TODO: Should I leave the rows that we already found as intersected with the actual column? Maybe for two separate tables?
  			 if (rows[j]["column_"+(i+1)] != 0 && rowName!=columnName)
			 {
  				 tempArraysObject.row = rowName;
  				 tempArraysObject.value = rows[j]["column_"+(i+1)];
  				 
  				 tempArray.push(tempArraysObject);
			 }  				
		 }
  		 
  		 tempObject.pairedWith = tempArray; 
  		 
  		 rowsPairedWithColumns.push(tempObject);
	 }  	 
  	 
  	 // since we have all the data, enable fading of stripes and rendering the table in legend
  	 arcs.on("mouseover", fadeMouseOver())	
  	 	 .on("mouseout", fadeMouseOut());
  	   	 
  	 arcs.on
  	 (
		 "click", clickOnItem()
		 
  	 );
	 
	 function drawGraph(matrix)
	 {
		 /**
		  * The chord layout is designed to work in conjunction with the chord shape and the arc shape. 
		  * The layout is used to generate data objects which describe the chords, serving as input to 
		  * the chord shape. The layout also generates descriptions for the groups, which can be used as 
		  * input to the arc shape. 
		  * 
		  * Data is specified by setting the associated matrix. 
		  * 
		  * IMPORTANT: 	The input matrix must be a square matrix of numbers.
		  * IMPORTANT: 	Each column i in the matrix corresponds to the same group as row i; the cell ij 
		  * 			corresponds to the relationship from group i to group j.
		  * 
		  * (from: https://github.com/mbostock/d3/wiki/Chord-Layout)
		  */
		 var chord = d3.layout.chord()
		  .padding(.05)	// TODO: Customize ???
		  .sortSubgroups(d3.descending)
		  .matrix(matrix);		
		 
		 // draws circles and defines the effect on the passage mouse
		var arcs1 = svg.append("svg:g").selectAll("path")
			.data(chord.groups)
			.enter();
			
		arcs =	arcs1.append("svg:path")
			.style("fill", function(d) { return fill(d.index); })
			.style("stroke", function(d) { return fill(d.index); })
			.attr("d", d3.svg.arc().innerRadius(innerRadius).outerRadius(outerRadius));			

		 var ticks1 = svg.append("svg:g").selectAll("g")
			.data(chord.groups)
			.enter();
			
		var	ticks = ticks1.append("svg:g").selectAll("g")
			.data(groupTicks)
			.enter().append("svg:g")
			.attr("transform", function(d) {
				return "rotate(" + (d.angle * 180 / Math.PI - 90) + ")"
				   + "translate(" + outerRadius + ",0)";
				});
		
		/**
		 * Customization for category labels (desciptions over arcs of the CHORD chart).
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		var literalLabelsFontCustom = jsonData.xAxis.labels.style;
		
		ticks1.append("svg:text")
		  .each(function(d,i) {  d.angle = (d.startAngle + d.endAngle) / 2; })
		   .attr("text-anchor", function(d) { return d.angle > Math.PI ? "end" : null; })
		  .attr("transform", function(d) {
				return "rotate(" + (d.angle * 180 / Math.PI - 90) + ")"
				+ "translate(" + (innerRadius + 60) + ")"
				+ (d.angle > Math.PI ? "rotate(180)" : "");
		  })
		  .attr("fill", literalLabelsFontCustom.color)
		  .style("font-family",literalLabelsFontCustom.fontFamily)	
		  .style("font-style",literalLabelsFontCustom.fontStyle)
		  .style("font-size",literalLabelsFontCustom.fontSize)
		  .style("font-weight",literalLabelsFontCustom.fontWeight)
		  .style("text-decoration",literalLabelsFontCustom.textDecoration)
		  .text(function(d,i) { return allFieldsArray[i];})		  

		 //aggiunge le lineette "graduate"		 
		 ticks.append("svg:line")
			.attr("x1", "1")
			.attr("y1", "0")
			.attr("x2", "5")
			.attr("y2", "0")
			.style("stroke", "#FF0000");	// TODO: Customize the color of ticks ???

		/**
		 * Customization for serie labels (ticks on arcs of the CHORD chart).
		 * 
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		var tickLabelsFontCustom = jsonData.yAxis.labels.style;
		
		 //aggiunge le label unit� di misura
		ticks.append("svg:text")
			.attr("x", "8")
			.attr("dy", ".35em")
			.attr("transform", function(d) { return d.angle > Math.PI ? "rotate(180)translate(-16)" : null; })
			.style("text-anchor", function(d) { return d.angle > Math.PI ? "end" : null; })
			.attr("fill", tickLabelsFontCustom.color)
			.style("font-family",tickLabelsFontCustom.fontFamily)	
			.style("font-style",tickLabelsFontCustom.fontStyle)
			.style("font-size",tickLabelsFontCustom.fontSize)
			.style("font-weight",tickLabelsFontCustom.fontWeight)
			.style("text-decoration",tickLabelsFontCustom.textDecoration)
			.text(function(d) { return d.label; });
			
		 //disegna le fasce da un'area ad un altra
		 svg.append("svg:g")
			.attr("class", "chord")
			.selectAll("path")
			.data(chord.chords)
			.enter().append("svg:path")
			.attr("d", d3.svg.chord().radius(innerRadius))
			.style("fill", function(d) { return fill(d.target.index); })
			.style("opacity", opacityMouseOutAndDefault) 
			.style("stroke", "#000")	// TODO: Customize ??
			.style("stroke-width", ".5px");	// TODO: Customize ??		 
		}
	 
	 /**
	 * [END] Data processing part
	 */
}