function renderWordCloud(chartConf){
	
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


		var fill = d3.scale.category20();

		d3.layout.cloud().size([800, 700])
		.words(chartConf.data[0].map(function(d) {
			return {text: d.name, size: 10 + d.count*25};
		}))
		.padding(chartConf.chart.padding)
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
			.style("font-family", chartConf.chart.font.type);
			
			if (chartConf.data[0].length < 1)
			{
				var emptyMsgFontSize = parseInt(chartConf.emptymessage.style.fontSize);
				var emptyMsgDivHeight = parseInt(chartConf.emptymessage.height);
				var emptyMsgTotal = emptyMsgDivHeight+emptyMsgFontSize/2;
				
				// Set title
				d3.select("#main").append("div")
					.style("height",emptyMsgTotal)
					.style("position",chartConf.emptymessage.position)
					.style("left",chartConf.emptymessage.paddingLeft)
					.style("color",chartConf.emptymessage.style.fontColor)
					.style("text-align",chartConf.emptymessage.style.textAlign)
		    		.style("font-family",chartConf.emptymessage.style.fontType)
		    		.style("font-style",chartConf.emptymessage.style.fontStyle)
		    		.style("font-size",emptyMsgFontSize)
					.text(chartConf.emptymessage.text);	
			}
			else
			{
				var titleFontSize = parseInt(chartConf.title.style.fontSize);
				var titleDivHeight = parseInt(chartConf.title.height);
				var titleTotal = titleDivHeight+titleFontSize/2;
				
				// Set title
				d3.select("#main").append("div")
					.style("height",titleTotal)
					.style("position",chartConf.title.position)
					.style("left",chartConf.title.paddingLeft)
					.style("color",chartConf.title.style.fontColor)
					.style("text-align",chartConf.title.style.textAlign)
		    		.style("font-family",chartConf.title.style.fontType)
		    		.style("font-style",chartConf.title.style.fontStyle)
		    		.style("font-size",titleFontSize)
					.text(chartConf.title.text);	
				
				var subtitleFontSize = parseInt(chartConf.subtitle.style.fontSize);
				var subtitleDivHeight = parseInt(chartConf.subtitle.height);
				var subtitleTotal = subtitleDivHeight+subtitleFontSize/2;
				
				// Set subtitle
				d3.select("#main").append("div")
					.style("height",subtitleTotal)
					.style("position",chartConf.subtitle.position)
					.style("left",chartConf.subtitle.paddingLeft)
					.style("color",chartConf.subtitle.style.fontColor)
					.style("text-align",chartConf.subtitle.style.textAlign)
		    		.style("font-family",chartConf.subtitle.style.fontType)
		    		.style("font-style",chartConf.subtitle.style.fontStyle)
		    		.style("font-size",subtitleFontSize)
					.text(chartConf.subtitle.text);
			      
			d3.select("body").append("svg")
			.attr("width", 800)
			.attr("height", 700)
			.append("g")
			.attr("transform", "translate(400,350)")
			.selectAll("text")
			.data(words)
			.enter().append("text")
			.style("font-size", function(d) { return d.size + "px"; })
			.style("font-family",chartConf.chart.font)
			.style("fill", function(d, i) { return fill(i); })
			.attr("text-anchor", "middle")
			.attr("transform", function(d) {
				return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
			})
			.text(function(d) { return d.text; });
			}	
		}
		
	}
	
	function renderSunburst(jsonObject)
	{
		/* ME: The part that we need to place into HTML in order to attach 
		 * given data to them - we are going to create it through D3 notation */
		
		// Dimensions of sunburst.
	    /* ME: Dimensions of the window in which is chart going to be placed.
	     * Hence, radius of the circular Sunburst chart is going to be half of
	     * the lesser dimension of that window. */
//		var width = 750;
//		var height = 600;
	    var width = parseInt(jsonObject.chart.width);
		var height = parseInt(jsonObject.chart.height);
		var radius = Math.min(width, height) / 2;	
		
		// Breadcrumb dimensions: width, height, spacing, width of tip/tail.
		//var b = { w: 50, h: 30, s: 3, t: 10 };
		var bcWidth = parseInt(jsonObject.toolbar.width);
		var bcHeight = parseInt(jsonObject.toolbar.height);
		var bcSpacing = parseInt(jsonObject.toolbar.spacing);
		var bcTipTail = parseInt(jsonObject.toolbar.tail);
		
		//var toolbarPadding = parseInt(jsonObject.toolbar.padding);
		
		var topPadding = parseInt(jsonObject.chart.topPadding);
		var bottomPadding = parseInt(jsonObject.chart.bottomPadding);
		
	    var tipFontSize = parseInt(jsonObject.tip.style.fontSize);
	    var tipWidth = parseInt(jsonObject.tip.width);
	        
	    //var sequenceHeight = parseInt(jsonObject.toolbar.padding) + parseInt(bcHeight);
		
		var b = { 
					w: bcWidth, 	h: bcHeight, 
					s: bcSpacing, 	t: bcTipTail 
				};
	    
		/* ME: Create necessary part of the HTML DOM - the one that code need to
		 * position chart on the page (D3 notation) */
		d3.select("body")
			.append("div").attr("id","main")
			.style("font-family", jsonObject.chart.style.font.type);
		
		if (jsonObject.data[0].length < 1)
		{
			var emptyMsgFontSize = parseInt(jsonObject.emptymessage.style.fontSize);
			var emptyMsgDivHeight = parseInt(jsonObject.emptymessage.height);
			var emptyMsgTotal = emptyMsgDivHeight+emptyMsgFontSize/2;
			
			// Set title
			d3.select("#main").append("div")
				.style("height",emptyMsgTotal)
				.style("position",jsonObject.emptymessage.position)
				.style("left",jsonObject.emptymessage.paddingLeft)
				.style("color",jsonObject.emptymessage.style.fontColor)
				.style("text-align",jsonObject.emptymessage.style.textAlign)
	    		.style("font-family",jsonObject.emptymessage.style.fontType)
	    		.style("font-style",jsonObject.emptymessage.style.fontStyle)
	    		.style("font-size",emptyMsgFontSize)
				.text(jsonObject.emptymessage.text);	
		}
		else
		{
			var titleFontSize = parseInt(jsonObject.title.style.fontSize);
			var titleDivHeight = parseInt(jsonObject.title.height);
			var titleTotal = titleDivHeight+titleFontSize/2;
			
			// Set title
			d3.select("#main").append("div")
				.style("height",titleTotal)
				.style("position",jsonObject.title.position)
				.style("left",jsonObject.title.paddingLeft)
				.style("color",jsonObject.title.style.fontColor)
				.style("text-align",jsonObject.title.style.textAlign)
	    		.style("font-family",jsonObject.title.style.fontType)
	    		.style("font-style",jsonObject.title.style.fontStyle)
	    		.style("font-size",titleFontSize)
				.text(jsonObject.title.text);	
			
			var subtitleFontSize = parseInt(jsonObject.subtitle.style.fontSize);
			var subtitleDivHeight = parseInt(jsonObject.subtitle.height);
			var subtitleTotal = subtitleDivHeight+subtitleFontSize/2;
			
			// Set subtitle
			d3.select("#main").append("div")
				.style("height",subtitleTotal)
				.style("position",jsonObject.subtitle.position)
				.style("left",jsonObject.subtitle.paddingLeft)
				.style("color",jsonObject.subtitle.style.fontColor)
				.style("text-align",jsonObject.subtitle.style.textAlign)
	    		.style("font-family",jsonObject.subtitle.style.fontType)
	    		.style("font-style",jsonObject.subtitle.style.fontStyle)
	    		.style("font-size",subtitleFontSize)
				.text(jsonObject.subtitle.text);
				
			var tipPadding = 0;
			
		    if (jsonObject.toolbar.position=="top")
			{   
		    	tipPadding = height/2+bcHeight/2-tipFontSize+topPadding;
				d3.select("#main").append("div").attr("id","sequence");		
			}
		    else
		    {
		    	tipPadding = height/2-tipFontSize+topPadding;
		    }
		    
		    //d3.select("#main").append("div").attr("style","height: " + topPadding + "px;");	    
		    d3.select("#main").append("div").style("height",topPadding);
		    
		    var distLeft = (height/2+(width-height)/2-tipWidth/2);
		    
		    d3.select("#main")
		    	.append("div").attr("id","chart")    	
		    	.append("div").attr("id","explanation")
		    		.style("visibility","hidden")
		    		.style("color",jsonObject.tip.style.fontColor)
		    		.style("position",jsonObject.tip.position)
		    		.style("top",tipPadding+titleTotal+subtitleTotal)
		    		.style("left",distLeft)
		    		.style("width",tipWidth)
		    		.style("text-align",jsonObject.tip.style.textAlign)
		    		.style("font-family",jsonObject.tip.style.fontType)
		    		.style("font-style",jsonObject.tip.style.fontStyle)
		    		.style("font-size",tipFontSize)
		    	.append("span").attr("id","percentage");
		    
		    //d3.select("#main").append("div").attr("style","height: " + bottomPadding + "px;"); 
		    d3.select("#main").append("div").style("height",bottomPadding); 
		    
		    if (jsonObject.toolbar.position=="bottom")
			{   	   	
				d3.select("#main").append("div").attr("id","sequence");	
				d3.select("#main").append("div").style("height",jsonObject.legend.padding); 
			}
		    
		    d3.select("body")
		    	.append("div").attr("id","sidebar")
		    	.append("input").attr("type","checkbox").attr("id","togglelegend");
		    
		    // We can also customize this (font size)
		    /*d3.select("#sidebar").append("text").html("Legend" + "</br>")
		    	.attr("style","font-size: 13px;");*/	    	
	    	d3.select("#sidebar").append("text").html("Legend" + "</br>")
	    		.style("font-size",13);
		    
		    /*d3.select("#sidebar")
		    	.append("div").attr("id","legend").attr("style","visibility: hidden;");*/
	    	d3.select("#sidebar")
		    	.append("div").attr("id","legend").style("visibility","hidden");
		    
		/* !!! WE SHOULD CHANGE THE STATIC TEXT INSIDE THE CHART - it should
		 * !!! depend on query that user is going to provide in order to create
		 * !!! a proper dataset */
		    d3.select("#explanation")
		    	.append("text").html("</br>" + jsonObject.tip.text);
		}
		
		/* ME: Collect all possible colors into one array - PREDEFINED set of colors
		 * (the ones that we are going to use in case configuration for the
		 * current user (customized) is not set already) */	
		var children = new Array();	
		
		children = children.concat(d3.scale.category10().range());
		children = children.concat(d3.scale.category20().range());
		children = children.concat(d3.scale.category20b().range());
		children = children.concat(d3.scale.category20c().range());
		
		/* ME: Map that will contain key-value pairs. Key is going to be name of each 
		 * individual element of the result (of the request for dataset). Value 
		 * will be the color that is going to be assigned to each element. */
		var colors = {}; 
		
		// Total size of all segments; we set this later, after loading the data.
		var totalSize = 0; 
		
		/* ME: Put inside first "div" element the Suburst chart, i.e. SVG DOM
		 * element that will represent it. SVG window will be with previously
		 * defined dimensions ("width" and "height"). */
		
	// ??? I'm not sure what appending of the SVG's subelement ("svg:g") really does ???
		var vis = d3.select("#chart").append("svg:svg")
		    .attr("width", width)
		    .attr("height", height)
		    .append("svg:g")
		    .attr("id", "container")
		    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");	
		
	// ???
		var partition = d3.layout.partition()
		    .size([2 * Math.PI, radius * radius])
		    .value(function(d) { return d.size; });
		
		/* ME: This part is counting angular data for some particular 
		 * element of dataset. */
		var arc = d3.svg.arc()
		    .startAngle(function(d) { return d.x; })
		    .endAngle(function(d) { return d.x + d.dx; })
		    .innerRadius(function(d) { return Math.sqrt(d.y); })
		    .outerRadius(function(d) { return Math.sqrt(d.y + d.dy); });
		 
		/* ME: We now get hierarchy of root data (first level of the chart) - 
		 * data ordered by their presence in total ammount (100% of the sum). 
		 * E.g. if we have this distribution of data for particular query:
		 * USA: 78%, Canada: 12%, Mexico: 8%, No country: 2%, the "children"
		 * array (sequence) inside "json" variable will be in descending order: 
		 * USA, Canada, Mexico, No country. */
		var json = buildHierarchy(jsonObject.data[0]);
	  
//		console.log("JSON retrieved from buildHierarchy()...");
//		console.log(json);  
		
		createVisualization(json);
		
		// Main function to draw and set up the visualization, once we have the data.
		function createVisualization(json) 
		{	
			// Basic setup of page elements.
			/* ME: Set the initial configuration of the breadcrumb - 
			 * defining dimensions of the trail, color of the text 
			 * and position of it within the chart (top (default), bottom) */	
			initializeBreadcrumbTrail();
			//drawLegend();
			
			/* ME: Toggles the legend depending on whether checkbox is 
			 * checked. It calls toggleLegend() method. */
			d3.select("#togglelegend").on("click", toggleLegend);
			
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

			var path = vis.data([json]).selectAll("path")
				.data(nodes)
				.enter().append("svg:path")
				.attr("display", function(d) { return d.depth ? null : "none"; })
				.attr("d", arc)
				.attr("fill-rule", "evenodd")
				.style
				(
						"fill", 
						
						function(d) 
						{   	    	
							  /* Go through the array of key-value pairs (elements of the chart and their color)
							   * and check if there is unique element-color mapping. */
							  if (colors[d.name] == undefined && d.name != "root")
							  {
								  var numberOfColor = Math.floor(Math.random()*children.length);
								  colors[d.name] = children[numberOfColor];
							  }
						
							  return colors[d.name];	  
						}
				)					
				.style("opacity", 1)
				.on("mouseover", mouseover);

				drawLegend();
			
				// Add the mouseleave handler to the bounding circle.
				d3.select("#container").on("mouseleave", mouseleave);

				// Get total size of the tree = value of root node from partition.
				totalSize = path.node().__data__.value;
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
		
		  d3.select("#percentage")
		      .text(percentageString);
		
		  d3.select("#explanation")
		      .style("visibility", "");
		
		  var sequenceArray = getAncestors(d);
		  updateBreadcrumbs(sequenceArray, percentageString);
		
		  // Fade all the segments.
		  /*d3.selectAll("path")
		      .style("opacity", 0.8);*/
		  
		  d3.selectAll("path")
	      .style("opacity", jsonObject.chart.style.opacMouseOver);
		
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
		
		/* ME: this function will put the breadcrumb trail that will
		 * be positioned at the position where DOM element with given
		 * ID (#sequence) resides. */
	/* !!! This part should be configurable - currently, breadcrumb trail
	 * is posiotioned on the top of the chart, but it should be possible
	 * to let the user to choose where he would like to place this element.
	 * Predefine implementation put this element on the top, because 
	 * given ID is attached to the top "div" element of the HTML DOM 
	 * structure. */
		function initializeBreadcrumbTrail() 
		{
			// Add the svg area.
			/* ME: Adds the new SVG DOM element to the current structure -
			 * it appends new SVG to the very first "div" element in order
			 * to present breadcrumb. It specifies its dimensions (width,
			 * height */		
			
			
			var trail = d3.select("#sequence")
				.append("svg:svg")
				.attr("width", width)
				.attr("height", bcHeight)
				.attr("id", "trail");
			  
			// Add the label at the end, for the percentage.
			/* ME: Append to the newly created SVG element text subelement 
			 * that will contain value of the percentage that covered sequence
			 * represent. Here, predefined color of percentage text is black
			 * (#000). ("#000") = ("black") */
			trail
				.append("svg:text")
				.attr("id", "endlabel")
				.style("fill", jsonObject.toolbar.style.percFontColor);
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
		
		  entering.append("svg:polygon")
		      .attr("points", breadcrumbPoints)
		      .style("fill", function(d) { return colors[d.name]; });
		
		  entering.append("svg:text")
		      .attr("x", (b.w + b.t) / 2)
		      .attr("y", b.h / 2)
		      .attr("dy", "0.35em")
		      .attr("text-anchor", "middle")
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
//			var li = { w: 120, h: 30, s: 3, r: 3 };
			var li = 
					{ 
						w: parseInt(jsonObject.legend.width), h: parseInt(jsonObject.legend.height), 
						s: parseInt(jsonObject.legend.spacing), r: parseInt(jsonObject.legend.radius) 
					};

			var numOfColorElems = Object.keys(colors).length;
			
			var legend = d3.select("#legend").append("svg:svg")
			.attr("width", li.w)
			.attr("height", numOfColorElems * (li.h + li.s));
		
//			console.log("bbbbb");
//			console.log(legend.selectAll("g")
//					.data(d3.entries(colors))
//					.enter());
			
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
			
//			console.log(g);

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
		  
		  /* ME: Total number of data received when requesting dataset. */
		  var dataLength = jsonObject.length;
		  
//		  console.log("Received JSON object:");
//		  console.log(jsonObject);
		  
		  for (var i = 0; i < dataLength; i++) 
		  {
		    //var sequence = jsonObject[i].column_1;
		   // var size =+ jsonObject[i].column_2;
			  
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
		    var parts = sequence.split("-");
		    
		    var currentNode = root;
		    
		    for (var j = 0; j < parts.length; j++) 
		    {
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
		    	} 
		    	
		    	else 
		    	{
				 	// Reached the end of the sequence; create a leaf node.
				 	childNode = {"name": nodeName, "size": size};
				 	children.push(childNode);
		    	}
		    
		    } 	// inner for loop
		    
		  }		// outter for loop
		  
		  return root;
		  
		};
	}