/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
  
 
 function drawDial(options) {

var renderTo = options.renderTo,
    value = options.value,
    centerX = options.centerX,
    centerY = options.centerY,
    min = options.min,
    max = options.max,
    minAngle = options.minAngle,
    maxAngle = options.maxAngle,
    tickInterval = options.tickInterval,
    ranges = options.ranges,
    pivotLength = options.pivotLength,
    backgroundRadius = options.backgroundRadius,
    arcMinRadius = options.arcMinRadius,
    arcMaxRadius = options.arcMaxRadius,
    textRadius = options.textRadius,
    renderX = options.renderX,
    renderY = options.renderY
    ;
    
var renderer = new Highcharts.Renderer(
    document.getElementById(renderTo),
    renderX,
    renderY
);
var rangeElements = new Array();
var maxValueElement;
var tickPathElement;
var tickTextElement;
var circleElement;

// internals
var angle,
    pivot;

function valueToAngle(value) {

    return (maxAngle - minAngle) / (max - min) * value + minAngle;
}

function setValue(value) {
	
	var valueToSet = value;
	var tickBorder ="#000";
	var valueText ="#000";
	if(max < value){
		value = max;
		tickBorder ='#c63310';
	}else if(min > value){
		value = min;
		tickBorder ='#c63310';
	}
    // the pivot
    angle = valueToAngle(value);
    
    var path = [
         'M',
         centerX, centerY,
         'L',
         centerX + pivotLength * Math.cos(angle), centerY + pivotLength * Math.sin(angle)
     ];//arrow

		if(pivot != undefined){
			pivot.destroy();
		}
        pivot = renderer.path(path)
        .attr({
            stroke: tickBorder,
            'stroke-width': 3
        })
        .add();

}
function setRanges(ranges){
	for(i=0; i<rangeElements.length; i++){
		rangeElements[i].destroy();
	}
	rangeElements = new Array();
	// ranges
	for(i=0; i<ranges.length; i++){
		var rangesOptions = ranges[i];
		if(rangesOptions.from != undefined && rangesOptions.from !== null &&
				rangesOptions.to != undefined && rangesOptions.to !== null){
			var rangeElement =renderer.arc(
			        centerX,
			        centerY,
			        arcMaxRadius,
			        arcMinRadius,
			        valueToAngle(rangesOptions.from),
			        valueToAngle(rangesOptions.to)
			    )
			    .attr({
			        fill: rangesOptions.color
			    })
			    .add();
			rangeElements.push(rangeElement);
		}else{
			continue;
		}

	}

}
function setMax(maximumn){
	if(maxValueElement != undefined){
		maxValueElement.destroy();
	}
	max = maximumn;
	maxValueElement = renderer.arc(centerX, centerY, backgroundRadius, 0, minAngle, maxAngle)
    .attr({
        fill: {
            linearGradient: [0, 0, min, max],
            stops: [
                [0, '#FFF'],
                [1, '#DDD']
            ]
        },
        stroke: 'silver',
        'stroke-width': 1
    })
    .add();
}
// background area

maxValueElement = setMax(max);

// ranges
setRanges(ranges);

function setTicks(maxim, tickInt){
	tickInterval = tickInt;
	max = maxim
	for (var i = min; i <= max; i += tickInterval) {
	    
	    angle = valueToAngle(i);
	    
	    // draw the tick marker
	    tickPathElement = renderer.path([
	            'M',
	            centerX + arcMaxRadius * Math.cos(angle), centerY + arcMaxRadius * Math.sin(angle),
	            'L',
	            centerX + arcMinRadius * Math.cos(angle), centerY + arcMinRadius * Math.sin(angle)
	        ])
	        .attr({
	            stroke: 'silver',
	            'stroke-width': 2
	        })
	        .add();
	    
	    // draw the text
	    tickTextElement = renderer.text(
	            i,
	            centerX + textRadius * Math.cos(angle),
	            centerY + textRadius * Math.sin(angle)
	        )
	        .attr({
	            align: 'center'
	        })
	        .add();
	    
	}
}
setTicks(max, tickInterval);
// the initial value
setValue(value);

// center disc
function setCircle(){
	if(circleElement != undefined){
		circleElement.destroy();
	}
	circleElement = renderer.circle(centerX, centerY, 10)
	    .attr({
	        fill: '#4572A7',
	        stroke: 'black',
	        'stroke-width': 1
	    })
	    .add();
}

setCircle();

return {
    setValue: setValue,
    setMax: setMax,
    setTicks : setTicks,
    setRanges: setRanges,
    setCircle: setCircle
};

}