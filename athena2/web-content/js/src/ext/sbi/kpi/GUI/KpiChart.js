/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
function Gauge(placeholderName, configuration)
{
	this.placeholderName = placeholderName;

	var self = this; // some internal d3 functions do not "like" the "this" keyword, hence setting a local variable

	this.configure = function(configuration)
	{
		this.config = configuration;

		this.config.size = this.config.size * 0.9;

		this.config.raduis = this.config.size * 0.97 / 2;
		this.config.cx = this.config.size / 2;
		this.config.cy = this.config.size / 2;

		this.config.min = configuration.min || 0; 
		this.config.max = configuration.max || 100; 
		this.config.range = this.config.max - this.config.min;

		this.config.majorTicks = configuration.majorTicks || 5;
		this.config.minorTicks = configuration.minorTicks || 2;
		this.config.ranges = configuration.ranges;
		this.renderTo = configuration.renderTo;

	}

	this.render = function()
	{
		this.body = d3.select("#" + this.renderTo)
							.append("svg:svg")
	   						.attr("class", "gauge")
	   						.attr("width", this.config.size)
	   						.attr("height", this.config.size);

		this.body.append("svg:circle")
					.attr("cx", this.config.cx)						
					.attr("cy", this.config.cy)								
					.attr("r", this.config.raduis)
					.style("fill", "#ccc")
					.style("stroke", "#000")
					.style("stroke-width", "0.5px");

		this.body.append("svg:circle")							
					.attr("cx", this.config.cx)						
					.attr("cy", this.config.cy)								
					.attr("r", 0.9 * this.config.raduis)
					.style("fill", "#fff")
					.style("stroke", "#e0e0e0")
					.style("stroke-width", "2px");

		for (var index in this.config.ranges)
		{
			this.drawBand(this.config.ranges[index].from, this.config.ranges[index].to, this.config.ranges[index].color);
		}
		if (undefined != this.config.label)
		{
			var fontSize = Math.round(this.config.size / 9);
			this.body.append("svg:text")								
						.attr("x", this.config.cx)
						.attr("y", this.config.cy / 2 + fontSize / 2)			 			
						.attr("dy", fontSize / 2)
						.attr("text-anchor", "middle")
						.text(this.config.label)
						.style("font-size", fontSize + "px")
						.style("fill", "#333")
						.style("stroke-width", "0px");	
		}

		var fontSize = Math.round(this.config.size / 16);		
		var majorDelta = this.config.range / (this.config.majorTicks - 1);
		for (var major = this.config.min; major <= this.config.max; major += majorDelta)
		{
			var minorDelta = majorDelta / this.config.minorTicks;
			for (var minor = major + minorDelta; minor < Math.min(major + majorDelta, this.config.max); minor += minorDelta)
			{
				var point1 = this.valueToPoint(minor, 0.75);
				var point2 = this.valueToPoint(minor, 0.85);

				this.body.append("svg:line")
							.attr("x1", point1.x)
							.attr("y1", point1.y)
							.attr("x2", point2.x)
							.attr("y2", point2.y)
							.style("stroke", "#666")
							.style("stroke-width", "1px");
			}

			var point1 = this.valueToPoint(major, 0.7);
			var point2 = this.valueToPoint(major, 0.85);	

			this.body.append("svg:line")
						.attr("x1", point1.x)
						.attr("y1", point1.y)
						.attr("x2", point2.x)
						.attr("y2", point2.y)
						.style("stroke", "#333")
						.style("stroke-width", "2px");

			if (major == this.config.min || major == this.config.max)
			{
				var point = this.valueToPoint(major, 0.63);

				this.body.append("svg:text")
				 			.attr("x", point.x)
				 			.attr("y", point.y)			 			
				 			.attr("dy", fontSize / 3)
				 			.attr("text-anchor", major == this.config.min ? "start" : "end")
				 			.text(major)
				 			.style("font-size", fontSize + "px")
							.style("fill", "#333")
							.style("stroke-width", "0px");
			}
		}		

		var pointerContainer = this.body.append("svg:g").attr("class", "pointerContainer");		


	}

	this.redraw = function(value)
	{
		this.drawPointer(value);
	}

	this.drawBand = function(start, end, color)
	{
		if (0 >= end - start) return;
		try{
			this.body.append("svg:path")
						.style("fill", color)
						.attr("d", d3.svg.arc()
							.startAngle(this.valueToRadians(start))
							.endAngle(this.valueToRadians(end))
							.innerRadius(0.65 * this.config.raduis)
							.outerRadius(0.85 * this.config.raduis))
						.attr("transform", function() { return "translate(" + self.config.cx + ", " + self.config.cy + ") rotate(270)" });
		}catch(err){
			if(!Ext.isIE) {
				console.log(err);
			}
		}
	}

	this.drawPointer = function(value)
	{
		var valueToSet =''+value+'';
		if(valueToSet.indexOf('.') != -1){
			valueToSet = parseFloat(valueToSet).toFixed(2);
		}else{
			valueToSet = Math.round(valueToSet);
		}
		
		var tickColor ='#57a8d7';//light blue
		var tickBorder ='#155ba4';//dark blue
		var valueText ="#000";
		if(this.config.max < value){
			value = this.config.max;
			tickColor ='#e5340b';//light red
			tickBorder ="#c63310";//dark red			
		}else if(this.config.min > value){
			value = this.config.min;
			tickColor ='#e5340b';//light red
			tickBorder ="#c63310";//dark red	
		}
/*		var delta = this.config.range / 13;

		var head = this.valueToPoint(value, 0.85);
		var head1 = this.valueToPoint(value - delta, 0.12);
		var head2 = this.valueToPoint(value + delta, 0.12);

		var tailValue = value -  (this.config.range * (1/(270/360)) / 2);
		var tail = this.valueToPoint(tailValue, 0.28);
		var tail1 = this.valueToPoint(tailValue - delta, 0.12);
		var tail2 = this.valueToPoint(tailValue + delta, 0.12);

		var data = [head, head1, tail2, tail, tail1, head2, head];

		var line = d3.svg.line()
							.x(function(d) { return d.x })
							.y(function(d) { return d.y })
							.interpolate("linear");*/
		
		
		var pointerContainer = this.body.select(".pointerContainer");	

		//var pointer = pointerContainer.selectAll("path").data([data]);
				
		pointerContainer.append('svg:path').attr('class', 'needle').attr('d', this.mkNeedle(value, 70)).style("fill", tickColor).style("stroke", tickBorder).style("fill-opacity", 0.7);	
		
		pointerContainer.append("svg:circle")								
		.attr("cx", this.config.cx)						
		.attr("cy", this.config.cy)								
		.attr("r", 0.12 * this.config.raduis)
		.style("fill", tickColor).style("stroke", tickBorder)
		.style("opacity", 1);
/*		pointer.enter()
				.append("svg:path")
					.attr("d", line)
					//.attr("d", function(d) { return line.tension(d)(data); })
					.style("fill", tickColor)
					.style("stroke", tickBorder)
					.style("fill-opacity", 0.7);
		
		pointer.style("fill", tickColor).style("stroke", tickBorder);
		
		pointer.transition()
					.attr("d", line);*/


		var fontSize = Math.round(this.config.size / 10);
		
		pointerContainer.selectAll("text")
							.data([valueToSet])
								.text(valueToSet)
							.enter()
								.append("svg:text")
									.attr("x", this.config.cx)
									.attr("y", this.config.size - this.config.cy / 4 - fontSize)			 			
									.attr("dy", fontSize / 2)
									.attr("text-anchor", "middle")
									.text(valueToSet)
									.style("font-size", fontSize + "px")
									.style("fill", "#000")
									.style("stroke-width", "0px");
	}

	this.valueToDegrees = function(value)
	{
		return value / this.config.range * 270 - 45;
	}

	this.valueToRadians = function(value)
	{
		return this.valueToDegrees(value) * Math.PI / 180;
	}

	this.valueToPoint = function(value, factor)
	{
		var point = 
		{
			x: this.config.cx - this.config.raduis * factor * Math.cos(this.valueToRadians(value)),
			y: this.config.cy - this.config.raduis * factor * Math.sin(this.valueToRadians(value))
		}

		return point;
	}
		  
    this.mkNeedle = function(value, len) {
        var centerX, centerY, leftX, leftY, rightX, rightY, thetaRad, topX, topY;
  	    thetaRad = this.valueToRadians(value);
        centerX = 0;
        centerY = 0;
  	  this.len= 
        topX = this.config.cx - len * Math.cos(thetaRad);
        topY = this.config.cy - len * Math.sin(thetaRad);
        leftX = this.config.cx - (0.12 * this.config.raduis) * Math.cos(thetaRad - Math.PI / 2);
        leftY = this.config.cy - (0.12 * this.config.raduis) * Math.sin(thetaRad - Math.PI / 2);
        rightX = this.config.cx - (0.12 * this.config.raduis) * Math.cos(thetaRad + Math.PI / 2);
        rightY = this.config.cy - (0.12 * this.config.raduis) * Math.sin(thetaRad + Math.PI / 2);
        return "M " + leftX + " " + leftY + " L " + topX + " " + topY + " L " + rightX + " " + rightY;
  			
      };
	// initialization
	this.configure(configuration);	
}
