//slider properties

//function slider(id,parentNode,x1,y1,value1,x2,y2,value2,startVal,sliderStyles,invisSliderWidth,sliderSymb,functionToCall,mouseMoveBool) {

function slider(x1,y1,value1,x2,y2,value2,startVal,sliderGroupId,sliderColor,visSliderWidth,invisSliderWidth,sliderSymb,functionToCall,mouseMoveBool) {
	this.x1 = x1;
	this.y1 = y1;
	this.value1 = value1;
	this.x2 = x2;
	this.y2 = y2;
	this.value2 = value2;
	this.startVal = startVal;
	this.value = startVal;
	this.sliderGroupId = sliderGroupId;
	alert(this.sliderGroupId);
	this.sliderGroup = document.getElementById(this.sliderGroupId);
	this.sliderColor = sliderColor;
	this.visSliderWidth = visSliderWidth;
	this.invisSliderWidth = invisSliderWidth;
	this.sliderSymb = sliderSymb;
	this.functionToCall = functionToCall;
	this.mouseMoveBool = mouseMoveBool;
	this.length = toPolarDist((this.x2 - this.x1),(this.y2 - this.y1));
	this.direction = toPolarDir((this.x2 - this.x1),(this.y2 - this.y1));
	this.sliderLine = null;
	this.createSlider();
	this.slideStatus = 0;
}

//create slider
slider.prototype.createSlider = function() {
	this.sliderLine = document.createElementNS(svgNS,"line");
	this.sliderLine.setAttributeNS(null,"x1",this.x1);
	this.sliderLine.setAttributeNS(null,"y1",this.y1);
	this.sliderLine.setAttributeNS(null,"x2",this.x2);
	this.sliderLine.setAttributeNS(null,"y2",this.y2);
	this.sliderLine.setAttributeNS(null,"stroke",this.sliderColor);
	this.sliderLine.setAttributeNS(null,"stroke-width",this.invisSliderWidth);
	this.sliderLine.setAttributeNS(null,"opacity","0");
	this.sliderLine.setAttributeNS(null,"stroke-linecap","square");
	this.sliderLine.setAttributeNS(null,"id",this.sliderGroupId+"_invisibleSliderLine");
	this.sliderLine.addEventListener("mousedown",this,false);
	alert(this.sliderGroup);
	this.sliderGroup.appendChild(this.sliderLine);
	var mySliderLine = document.createElementNS(svgNS,"line");
	mySliderLine.setAttributeNS(null,"x1",this.x1);
	mySliderLine.setAttributeNS(null,"y1",this.y1);
	mySliderLine.setAttributeNS(null,"x2",this.x2);
	mySliderLine.setAttributeNS(null,"y2",this.y2);
	mySliderLine.setAttributeNS(null,"stroke",this.sliderColor);
	mySliderLine.setAttributeNS(null,"stroke-width",this.visSliderWidth);
	mySliderLine.setAttributeNS(null,"id",this.sliderGroupId+"_visibleSliderLine");
	mySliderLine.setAttributeNS(null,"pointer-events","none");
	this.sliderGroup.appendChild(mySliderLine);
	mySliderSymb = document.createElementNS(svgNS,"use");
	mySliderSymb.setAttributeNS(xlinkNS,"xlink:href","#"+this.sliderSymb);
	var myStartDistance = this.length - ((this.value2 - this.startVal) / (this.value2 - this.value1)) * this.length;
	var myPosX = this.x1 + toRectX(this.direction,myStartDistance);
	var myPosY = this.y1 + toRectY(this.direction,myStartDistance);
	var myTransformString = "translate("+myPosX+","+myPosY+") rotate(" + Math.round(this.direction / Math.PI * 180) + ")";
	mySliderSymb.setAttributeNS(null,"transform",myTransformString);
	mySliderSymb.setAttributeNS(null,"id",this.sliderGroupId+"_sliderSymbol");
	this.sliderGroup.appendChild(mySliderSymb);
}

//remove all slider elements
slider.prototype.removeSlider = function() {
    var mySliderSymb = document.getElementById(this.sliderGroup+"_sliderSymbol");
	this.sliderGroup.removeChild(mySliderSymb);
    var mySliderLine = document.getElementById(this.sliderGroup+"_visibleSliderLine");
	this.sliderGroup.removeChild(mySliderLine);
    var mySliderLine = document.getElementById(this.sliderGroup+"_invisibleSliderLine");
	this.sliderGroup.removeChild(mySliderLine);
}

//handle events
slider.prototype.handleEvent = function(evt) {
	this.drag(evt);
}

//drag slider
slider.prototype.drag = function(evt) {
	if (evt.type == "mousedown" || (evt.type == "mousemove" && this.slideStatus == 1)) {
		//get coordinate in slider coordinate system
		var coordPoint = myMapApp.calcCoord(evt,this.sliderLine);
		//draw normal line for first vertex
		var ax = this.x2 - this.x1;
		var ay = this.y2 - this.y1;
		//normal vector 1
		var px1 = parseFloat(this.x1) + ay * -1;
		var py1 = parseFloat(this.y1) + ax;
		//normal vector 2
		var px2 = parseFloat(this.x2) + ay * -1;
		var py2 = parseFloat(this.y2) + ax;
				
		if (leftOfTest(coordPoint.x,coordPoint.y,this.x1,this.y1,px1,py1) == 0 && leftOfTest(coordPoint.x,coordPoint.y,this.x2,this.y2,px2,py2) == 1) {
			if (evt.type == "mousedown" && (evt.detail == 1 || evt.detail == 0)) {
				this.slideStatus = 1;
				document.documentElement.addEventListener("mousemove",this,false);
				document.documentElement.addEventListener("mouseup",this,false);
			}
			myNewPos = intersect2lines(this.x1,this.y1,this.x2,this.y2,coordPoint.x,coordPoint.y,coordPoint.x + ay * -1,coordPoint.y + ax);
			var myPercentage = toPolarDist(myNewPos['x'] - this.x1,myNewPos['y'] - this.y1) / this.length;
			this.value = this.value1 + myPercentage * (this.value2 - this.value1);
		}
		else {
			var myNewPos = new Array();
			if (leftOfTest(coordPoint.x,coordPoint.y,this.x1,this.y1,px1,py1) == 0 && leftOfTest(coordPoint.x,coordPoint.y,this.x2,this.y2,px2,py2) == 0) {
				//more than max
				this.value = this.value2;
				myNewPos['x'] = this.x2;
				myNewPos['y'] = this.y2;
			}
			if (leftOfTest(coordPoint.x,coordPoint.y,this.x1,this.y1,px1,py1) == 1 && leftOfTest(coordPoint.x,coordPoint.y,this.x2,this.y2,px2,py2) == 1) {
				//less than min
				this.value = this.value1;
				myNewPos['x'] = this.x1;
				myNewPos['y'] = this.y1;
			}
		}
		var myTransformString = "translate("+myNewPos['x']+","+myNewPos['y']+") rotate(" + Math.round(this.direction / Math.PI * 180) + ")";
		document.getElementById(this.sliderGroupId+"_sliderSymbol").setAttributeNS(null,"transform",myTransformString);
		this.getValue();
	}
	if (evt.type == "mouseup" && (evt.detail == 1 || evt.detail == 0)) {
		if (this.slideStatus == 1) {
			this.slideStatus = 2;
			document.documentElement.removeEventListener("mousemove",this,false);
			document.documentElement.removeEventListener("mouseup",this,false);
			this.getValue();
		}
		this.slideStatus = 0;
	}
}

//this code is executed, after the slider is released
//you can use switch/if to detect which slider was used (use this.sliderGroup) for that
slider.prototype.getValue = function() {
	if (this.slideStatus == 1 && this.mouseMoveBool == true) {
		if (typeof(this.functionToCall) == "function") {
			this.functionToCall("change",this.sliderGroupId,this.value);
		}
		if (typeof(this.functionToCall) == "object") {
			this.functionToCall.getSliderVal("change",this.sliderGroupId,this.value);
		}
		if (typeof(this.functionToCall) == undefined) {
			return;
		}
	}
	if (this.slideStatus == 2) {
		if (typeof(this.functionToCall) == "function") {
			this.functionToCall("release",this.sliderGroupId,this.value);
		}
		if (typeof(this.functionToCall) == "object") {
			this.functionToCall.getSliderVal("release",this.sliderGroupId,this.value);
		}
		if (typeof(this.functionToCall) == undefined) {
			return;
		}
	}
}	

//this is to set the value from other scripts
slider.prototype.setValue = function(value) {
	var myPercAlLine = (value - this.value1) / (this.value2 - this.value1);
	this.value = myPercAlLine;
	var myPosX = this.x1 + toRectX(this.direction,this.length * myPercAlLine);
	var myPosY = this.y1 + toRectY(this.direction,this.length * myPercAlLine);
	var myTransformString = "translate("+myPosX+","+myPosY+") rotate(" + Math.round(this.direction / Math.PI * 180) + ")";
	document.getElementById(this.sliderGroupId+"_sliderSymbol").setAttributeNS(null,"transform",myTransformString);
}
