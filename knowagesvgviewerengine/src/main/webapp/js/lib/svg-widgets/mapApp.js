//this mapApp object helps to convert clientX/clientY coordinates to the coordinates of the group where the element is within
//normally one can just use .getScreenCTM(), but ASV3 does not implement it, 95% of the code in this function is for ASV3!!!
//credits: Kevin Lindsey for his example at http://www.kevlindev.com/gui/utilities/viewbox/ViewBox.js
function mapApp() {
	if (!document.documentElement.getScreenCTM) {
		this.resetFactors();
		//add resize event to document element
		document.documentElement.addEventListener("SVGResize",this,false);
		document.documentElement.addEventListener("SVGScroll",this,false);
		document.documentElement.addEventListener("SVGZoom",this,false);
	}
	this.navigator = "Batik";
	if (window.navigator) {
		if (window.navigator.appName.match(/Adobe/gi)) {
			this.navigator = "Adobe";
		}
		if (window.navigator.appName.match(/Netscape/gi)) {
			this.navigator = "Mozilla";
		}
		if (window.navigator.appName.match(/Opera/gi)) {
			this.navigator = "Opera";
		}
		if (window.navigator.appName.match(/Safari/gi)) {
			this.navigator = "Safari";
		}
	}
}

mapApp.prototype.handleEvent = function(evt) {
	if (evt.type == "SVGResize" || evt.type == "SVGScroll" || evt.type == "SVGZoom") {
		this.resetFactors();
	}
}

mapApp.prototype.resetFactors = function() {
	if (!document.documentElement.getScreenCTM) {
		var svgroot = document.documentElement;
		this.viewBox = new ViewBox(svgroot);
		var trans = svgroot.currentTranslate;
		var scale = svgroot.currentScale;
		this.m = this.viewBox.getTM();
		//undo effects of zoom and pan
		this.m = this.m.scale( 1/scale );
		this.m = this.m.translate(-trans.x, -trans.y);
	}
}

mapApp.prototype.calcCoord = function(evt,ctmNode) {
	var svgPoint = document.documentElement.createSVGPoint();
	svgPoint.x = evt.clientX;
	svgPoint.y = evt.clientY;
	if (!document.documentElement.getScreenCTM) {
		//undo the effect of transformations
		if (ctmNode) {
			var matrix = getTransformToRootElement(ctmNode);
		}
		else {
			var matrix = getTransformToRootElement(evt.target);			
		}
  	svgPoint = svgPoint.matrixTransform(matrix.inverse().multiply(this.m));
	}
	else {
		//case getScreenCTM is available
		if (ctmNode) {
			var matrix = ctmNode.getScreenCTM();
		}
		else {
			var matrix = evt.target.getScreenCTM();		
		}
  	svgPoint = svgPoint.matrixTransform(matrix.inverse());
	}
  //undo the effect of viewBox and zoomin/scroll
	return svgPoint;
}

mapApp.prototype.calcInvCoord = function(svgPoint) {
	if (!document.documentElement.getScreenCTM) {
		var matrix = getTransformToRootElement(document.documentElement);
	}
	else {
		var matrix = document.documentElement.getScreenCTM();
	}
	svgPoint = svgPoint.matrixTransform(matrix);
	return svgPoint;
}

/*************************************************************************/

/*****
*
*   ViewBox.js
*
*   copyright 2002, Kevin Lindsey
*
*****/

ViewBox.VERSION = "1.0";


/*****
*
*   constructor
*
*****/
function ViewBox(svgNode) {
    if ( arguments.length > 0 ) {
        this.init(svgNode);
    }
}


/*****
*
*   init
*
*****/
ViewBox.prototype.init = function(svgNode) {
    var viewBox = svgNode.getAttributeNS(null, "viewBox");
    var preserveAspectRatio = svgNode.getAttributeNS(null, "preserveAspectRatio");
    
    if ( viewBox != "" ) {
        var params = viewBox.split(/\s*,\s*|\s+/);

        this.x      = parseFloat( params[0] );
        this.y      = parseFloat( params[1] );
        this.width  = parseFloat( params[2] );
        this.height = parseFloat( params[3] );
    } else {
        this.x      = 0;
        this.y      = 0;
        this.width  = innerWidth;
        this.height = innerHeight;
    }
    
    this.setPAR(preserveAspectRatio);
    var dummy = this.getTM(); //to initialize this.windowWidth/this.windowHeight
};


/*****
*
*   getTM
*
*****/
ViewBox.prototype.getTM = function() {
    var svgRoot      = document.documentElement;
    var matrix       = document.documentElement.createSVGMatrix();
		//case width/height contains percent
    this.windowWidth = svgRoot.getAttributeNS(null,"width");
    if (this.windowWidth.match(/%/) || this.windowWidth == null) {
    	if (this.windowWidth == null) {
    		if (window.innerWidth) {
    			this.windowWidth = window.innerWidth;
    		}
    		else {
    			this.windowWidth = svgRoot.viewport.width;
    		}
    	}
    	else {
    		var factor = parseFloat(this.windowWidth.replace(/%/,""))/100;
    		if (window.innerWidth) {
    			this.windowWidth = window.innerWidth * factor;
    		}
    		else {
    			this.windowWidth = svgRoot.viewport.width * factor;
    		}
    	}
    }
    else {
    	this.windowWidth = parseFloat(this.windowWidth);
    }
    this.windowHeight = svgRoot.getAttributeNS(null,"height");
    if (this.windowHeight.match(/%/) || this.windowHeight == null) {
    	if (this.windowHeight == null) {
    		if (window.innerHeight) {
    			this.windowHeight = window.innerHeight;
    		}
    		else {
    			this.windowHeight = svgRoot.viewport.height;
    		}
    	}
    	else {
    		var factor = parseFloat(this.windowHeight.replace(/%/,""))/100;
    		if (window.innerHeight) {
    			this.windowHeight = window.innerHeight * factor;
    		}
    		else {
    			this.windowHeight = svgRoot.viewport.height * factor;
    		}
    	}
    }
    else {
    	this.windowHeight = parseFloat(this.windowHeight);
    }
    var x_ratio = this.width  / this.windowWidth;
    var y_ratio = this.height / this.windowHeight;

    matrix = matrix.translate(this.x, this.y);
    if ( this.alignX == "none" ) {
        matrix = matrix.scaleNonUniform( x_ratio, y_ratio );
    } else {
        if ( x_ratio < y_ratio && this.meetOrSlice == "meet" ||
             x_ratio > y_ratio && this.meetOrSlice == "slice"   )
        {
            var x_trans = 0;
            var x_diff  = this.windowWidth*y_ratio - this.width;

            if ( this.alignX == "Mid" )
                x_trans = -x_diff/2;
            else if ( this.alignX == "Max" )
                x_trans = -x_diff;
            
            matrix = matrix.translate(x_trans, 0);
            matrix = matrix.scale( y_ratio );
        }
        else if ( x_ratio > y_ratio && this.meetOrSlice == "meet" ||
                  x_ratio < y_ratio && this.meetOrSlice == "slice"   )
        {
            var y_trans = 0;
            var y_diff  = this.windowHeight*x_ratio - this.height;

            if ( this.alignY == "Mid" )
                y_trans = -y_diff/2;
            else if ( this.alignY == "Max" )
                y_trans = -y_diff;
            
            matrix = matrix.translate(0, y_trans);
            matrix = matrix.scale( x_ratio );
        }
        else
        {
            // x_ratio == y_ratio so, there is no need to translate
            // We can scale by either value
            matrix = matrix.scale( x_ratio );
        }
    }

    return matrix;
}


/*****
*
*   get/set methods
*
*****/

/*****
*
*   setPAR
*
*****/
ViewBox.prototype.setPAR = function(PAR) {
    // NOTE: This function needs to use default values when encountering
    // unrecognized values
    if ( PAR ) {
        var params = PAR.split(/\s+/);
        var align  = params[0];

        if ( align == "none" ) {
            this.alignX = "none";
            this.alignY = "none";
        } else {
            this.alignX = align.substring(1,4);
            this.alignY = align.substring(5,9);
        }

        if ( params.length == 2 ) {
            this.meetOrSlice = params[1];
        } else {
            this.meetOrSlice = "meet";
        }
    } else {
        this.align  = "xMidYMid";
        this.alignX = "Mid";
        this.alignY = "Mid";
        this.meetOrSlice = "meet";
    }
};