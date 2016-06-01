ColourRangesFactory = {};

ColourRangesFactory.GRADIENT = 'gradient';
ColourRangesFactory.STATIC = 'static';

ColourRangesFactory.getColourRanges = function( measure ) {
    
    var results;    
    var conf;
    
    if(measure === undefined || measure.colourrange_calculator_conf  === undefined) return;
    
    conf = measure.colourrange_calculator_conf;
		
		if(conf.type === ColourRangesFactory.GRADIENT) {
			results = ColourRangesFactory.getGradientColourRange( measure, conf.params );
		} else if (conf.type === ColourRangesFactory.STATIC) {
			results = ColourRangesFactory.getStaticColourRange( measure, conf.params );
		} else {
			results = ColourRangesFactory.getGradientColourRange( measure, conf.params );
		}	
	
		measure.colours = results;
};

ColourRangesFactory.getStaticColourRange = function( measure, params ) { 

     return params.ranges;
};

		
ColourRangesFactory.getGradientColourRange = function( measure, params ) { 

      var results;
			
      var A = new Array();
			var Grad = new Array();
			var new_rA;
			var new_gA;
			var new_bA;
			var shade;
			
		
			var colour = params.colour;
			
			results = new Array();
			//measure.colours = new Array();	
			
			// splitting color components, conversion to decimal, and storing in array
			A["r"] = parseInt(colour.substring(1,3),16);
			A["g"] = parseInt(colour.substring(3,5),16);
			A["b"] = parseInt(colour.substring(5),16);
			RGB = [A["r"],A["g"],A["b"]].sort(function(a,b) {return parseInt(a) - parseInt(b)}); 
			
      for(comp in A) {
				if(A[comp] == RGB[2]) Grad[comp] = parseInt((240 - A[comp]) / (measure.num_group - 1));	
				else if(A[comp] == RGB[1]) Grad[comp] = parseInt((230 - A[comp]) / (measure.num_group - 1));
				else  Grad[comp] = parseInt((220 - A[comp]) / (measure.num_group - 1));
			}
			
					
		
		  results[0] = colour;
		   		    
			for(var j = 1; j < measure.num_group; j++) {
				new_rA = A["r"] + Grad["r"] * j;
				new_gA = A["g"] + Grad["g"] * j;
				new_bA = A["b"] + Grad["b"] * j;
		
				shade = "#" + new_rA.toString(16) + new_gA.toString(16) + new_bA.toString(16);	
				results[j] = shade;		
			}
			results.reverse();
			
			return results;
	
};

/*
function setGradientColourRange( measureIndex ) { 
			var A = new Array();
			var Grad = new Array();
			var new_rA;
			var new_gA;
			var new_bA;
			var shade;
			
			var measure = sbi.geo.conf.measures[measureIndex];
			var colour = measure.colourrange_calculator_conf.params.colour;
			
			// splitting color components, conversion to decimal, and storing in array
			A["r"] = parseInt(colour.substring(1,3),16);
			A["g"] = parseInt(colour.substring(3,5),16);
			A["b"] = parseInt(colour.substring(5),16);
			RGB = [A["r"],A["g"],A["b"]].sort(function(a,b) {return parseInt(a) - parseInt(b)}); 
			for(comp in A) {
				if(A[comp] == RGB[2]) Grad[comp] = parseInt((240 - A[comp]) / (measure.num_group - 1));	
				else if(A[comp] == RGB[1]) Grad[comp] = parseInt((230 - A[comp]) / (measure.num_group - 1));
				else  Grad[comp] = parseInt((220 - A[comp]) / (measure.num_group - 1));
			}
			
			measure.colours = new Array();			
		
		  measure.colours[0] = colour;
		   		    
			for(var j = 1; j < measure.num_group; j++) {
				new_rA = A["r"] + Grad["r"] * j;
				new_gA = A["g"] + Grad["g"] * j;
				new_bA = A["b"] + Grad["b"] * j;
				//alert(new_rA + " " + new_gA + " " + new_bA);
				shade = "#" + new_rA.toString(16) + new_gA.toString(16) + new_bA.toString(16);	
				measure.colours[j] = shade;		
			}
			measure.colours.reverse();
	
		}	
    */			
