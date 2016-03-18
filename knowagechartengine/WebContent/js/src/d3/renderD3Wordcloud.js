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


function renderWordCloud(chartConf,catchSVG){

	var maxic = 0;
	var minValue=0;
	
	for (var i=0; i<chartConf.data[0].length; i++){
		
		if (chartConf.data[0][i].value > maxic){
			
			maxic = chartConf.data[0][i].value;
			
		}		
		
        if (chartConf.data[0][i].value < minValue){
			
			minValue = chartConf.data[0][i].value;
			
		}
		
	}    
    
	/**
	 * Normalize height and/or width of the chart if the dimension type for that dimension is
	 * "percentage". This way the chart will take the appropriate percentage of the screen's
	 * particular dimension (height/width).
	 * 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	if (chartConf.chart.heightDimType == "percentage")
	{
    	var heightNormalized = chartConf.chart.height ? window.innerHeight*Number(chartConf.chart.height)/100 : window.innerHeight;
	}
	else
	{
		var heightNormalized = chartConf.chart.height ? Number(chartConf.chart.height) : window.innerHeight;
	}	
	
	if (chartConf.chart.widthDimType == "percentage")
	{
		var widthNormalized = chartConf.chart.width ? window.innerWidth*Number(chartConf.chart.width)/100 : window.innerWidth;
	}
	else
	{
		var widthNormalized = chartConf.chart.width ? Number(chartConf.chart.width) : window.innerWidth;
	}
	
		(function() {
               
			function cloud() {
				var size = [widthNormalized, heightNormalized-(Number(removePixelsFromFontSize(chartConf.title.style.fontSize))
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

		var maxfontsize=chartConf.chart.maxFontSize;
		var minfontsize=chartConf.chart.minFontSize;
		var fill = d3.scale.category20();
        
		var wordFontSize= d3.scale.linear().domain([minValue,maxic]).range([minfontsize,maxfontsize]);
		
		var layout=d3.layout.cloud().size([widthNormalized, heightNormalized-(Number(removePixelsFromFontSize(chartConf.title.style.fontSize))
				+Number(removePixelsFromFontSize(chartConf.subtitle.style.fontSize)))*1.6])
		.words(chartConf.data[0].map(function(d) {
			 wordSize= wordFontSize(d.value);
			 return {text: d.name, size: wordSize};
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
			var randomId= Math.round((Math.random())*10000);
		
			d3.select("body")		
			.append("div").attr("id","main"+randomId)
			.attr("class","d3-container")
			.style("margin","auto")	// Center chart horizontally (Danilo Ristovski)
			.style("height",heightNormalized)
			.style("width",widthNormalized)			
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
				d3.select("#main"+randomId).append("div")
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
            .attr("width", widthNormalized)
			.attr("height", heightNormalized-(Number(removePixelsFromFontSize(chartConf.title.style.fontSize))
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
	    
		d3.selectAll(".tooltip")
		.style("position","absolute")
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
						
//					paramethers=fetchParamethers(d);
//					var selectParam={
//							categoryName:paramethers.categoryName,
//							categoryValue:paramethers.categoryValue,
//							serieName:paramethers.serieName,
//							serieValue:paramethers.serieValue,
//							groupingCategoryName:paramethers.groupingCategoryName,
//							groupingCategoryValue:paramethers.groupingCategoryValue	
//					};
					var	selectParam=fetchSelectionParamethers(d);
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
				
			});
			
			wordArea.on('mouseover',function(d){
				var tooltipText;
				for(j=0;j<chartConf.data[0].length;j++){
					if(chartConf.data[0][j].name===d.text){
						
						tooltipText=chartConf.data[0][j].value;
					}
				}
				tooltip.transition().duration(50).style("opacity","1");
    				
				var ttText = tooltip.text("  "+chartConf.tooltip.prefix+" "+tooltipText.toFixed(chartConf.tooltip.precision)+" "+chartConf.tooltip.postfix+"  ");
			  				
				/**
				 * Call the function that enables positioning of the 
				 * tooltip according to its dimensions.
				 */			
				var chartHeight = Number(heightNormalized);
				var chartWidth = Number(widthNormalized);
				
				positionTheTooltip(chartHeight,chartWidth,ttText);							
				
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
			for(j=0;j<chartConf.data[0].length;j++){
				if(chartConf.data[0][j].name===d.text){
					//param.categoryValue=chartConf.data[0][j].name;
					//param.categoryName= chartConf.data[0][j].categoryName;
					//param.serieValue=chartConf.data[0][j].value;
					for(k=0;k<chartConf.data[0][j].categoryName.length;k++){
						param[chartConf.data[0][j].categoryName[k]]=chartConf.data[0][j].name;
						
					}
					
					
				}
			}
			
			
			return param;
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