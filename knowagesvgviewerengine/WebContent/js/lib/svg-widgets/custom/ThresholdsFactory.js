ThresholdsFactory = {};

ThresholdsFactory.QUANTILE = 'quantile';
ThresholdsFactory.PERCENTAGE = 'perc';
ThresholdsFactory.UNIFORM = 'uniform';
ThresholdsFactory.STATIC = 'static';

ThresholdsFactory.getThresholds = function( measure ){

   
    var results;    
    var conf;
    
    
    
    if(measure === undefined || measure.threshold_calculator_conf  === undefined) return;
    
    conf = measure.threshold_calculator_conf;
    
    ThresholdsFactory.fixLbUbVAlues(measure);
    
    if(conf.type === ThresholdsFactory.QUANTILE) {
  	   results = ThresholdsFactory.getQuantileThresholds( measure, conf.params );
  	} else if (conf.type === ThresholdsFactory.PERCENTAGE) {
  	   results = ThresholdsFactory.getPercTrasholds( measure, conf.params  );
  	} else if (conf.type === ThresholdsFactory.UNIFORM) {
  	   results = ThresholdsFactory.getUniformTrasholds( measure, conf.params  );
  	} else if (conf.type === ThresholdsFactory.STATIC) {
  	    results =  ThresholdsFactory.getStaticThresholds( measure, conf.params );
  	} else {
  	  results = ThresholdsFactory.getQuantileTrasholds( measure );
  	}
  	
  	
    
  	measure.thresholds = results.thresholds;
  	measure.num_group = results.num_group;  
};

ThresholdsFactory.getStaticThresholds = function( measure, params ) { 
   var results;
		  
	 results = {};
   results.thresholds = params.ranges;
	 results.num_group = results.thresholds.length -1;
	 
	 if(measure.upper_bound < results.thresholds[results.thresholds.length - 1]) {
    measure.upper_bound = results.thresholds[results.thresholds.length - 1];
   }
   
   if(measure.lower_bound > results.thresholds[0]) {
    measure.lower_bound = results.thresholds[0];
   }
	 

	 return results;
};


ThresholdsFactory.getPercTrasholds = function( measure, params ) { 
		
		  var results;
		  
		  results = {};
      results.thresholds= new Array();
		  results.num_group = -1;
    
			var range;
      
      range = measure.upper_bound - measure.lower_bound;
			
			
			var ranges = params.ranges;
		  
      results.thresholds[0] = measure.lower_bound;
      //alert(typeof measure.lower_bound)
			for(var j = 0; j < ranges.length-1; j++) {

				var groupSize = parseFloat(range / 100.0) * parseFloat(ranges[j]);
				results.thresholds[j+1] = parseFloat(results.thresholds[j]) + parseFloat(groupSize);
				
			}
			
			results.thresholds[ ranges.length ] = measure.upper_bound;	
			
			results.num_group = ranges.length;
			
			return results;
};
		
ThresholdsFactory.getUniformThresholds = function( measure, params ) { 
		                    
      var ranges = new Array();
      		
			var perc = 100 / params.num_group;
			for(var j = 0; j < params.num_group; j++) {
				ranges[j] = perc;
			}
			
			params = {ranges: ranges};
			
			return setPercTrasholds( measure, params );
		}
		
ThresholdsFactory.getQuantileThresholds = function( measure, params ) {
				
				var results;
		  
  		  results = {};
        results.thresholds = new Array();
  		  results.num_group = params.num_group;
		  
        var MIN = measure.ordered_values[0];
        var MAX = measure.ordered_values[measure.ordered_values.length-1];
        
        
        var a = new Array();
        var i = 0;
        for(j = 0; j < measure.ordered_values.length; j++) {
          if(i == 0 || a[i-1] != measure.ordered_values[j]) {
            
            a[i++] = measure.ordered_values[j];
          }          
        }
        measure.ordered_values = a;
        
				var diff_value_num = 0;	
				var start_index = -1;	
				if(MIN >= measure.lower_bound && MAX <= measure.upper_bound) {
					diff_value_num = measure.ordered_values.length;
					start_index = 0;
				} else {
					for(j = 0; j < measure.ordered_values.length; j++) {						
			   			if(measure.ordered_values[j] >= measure.lower_bound && measure.ordered_values[j] <= measure.upper_bound) {
			   				start_index = (start_index == -1?j:start_index);
			   				diff_value_num++;
			   			}
		   		}
		   	}
		   	
				if(diff_value_num < results.num_group) results.num_group = diff_value_num;
				var blockSize = Math.floor( diff_value_num / results.num_group );
			
			  results.thresholds[0] = measure.lower_bound;
				for(j = 1; j < results.num_group; j++){
				  //alert(([start_index + (j*blockSize)) + ' ' +  measure.ordered_values);
					results.thresholds[j] = measure.ordered_values[start_index + (j*blockSize)];
				}
				results.thresholds[results.num_group] = measure.upper_bound;
				
				return results;
};


ThresholdsFactory.fixLbUbVAlues = function( measure ) {	   		
	   
			var MIN = measure.ordered_values[0];
			var MAX = measure.ordered_values[ measure.ordered_values.length-1 ];
				
			if(measure.lower_bound === 'none') {
			    measure.lower_bound = MIN;
		  }
		   		
      if(measure.upper_bound === 'none') {
		     measure.upper_bound = MAX;
		  }
		   		
		  if(measure.lower_bound > measure.upper_bound) {
		   	 var t = measure.upper_bound;
		   	 measure.upper_bound = measure.lower_bound;
		     measure.lower_bound = t;
		  }
		   		
		  if(measure.lower_bound < MIN) {
		  	measure.lower_bound = MIN;
		  }
		   	
		  if(measure.upper_bound >  MAX) {
		  	measure.upper_bound = MAX;
		  }
};





// ======================================================================================================================
/*

				if(measure.threshold_calculator_conf.type === "quantile") {
					setQuantileTrasholds( i );
				} else if (measure.threshold_calculator_conf.type === "perc") {
					setPercTrasholds( i );
				} else if (measure.threshold_calculator_conf.type === "uniform") {
					setUniformTrasholds( i );
				} else if (measure.threshold_calculator_conf.type === "static") {
				  measure.thresholds = measure.threshold_calculator_conf.params.ranges;
					measure.num_group = measure.thresholds.length -1;
				} else {
					setQuantileTrasholds( i );
				}
				
				
				
				
			}
		}
		
		function setPercTrasholds( measureIndex ) { 
		
      var measure = sbi.geo.conf.measures[measureIndex];		
      
			var range = measure.upper_bound - measure.lower_bound;
			
			measure.thresholds = new Array();
			var ranges = measure.threshold_calculator_conf.params.ranges;
		  
      measure.thresholds[0] = measure.lower_bound;
		   
			for(var j = 0; j < ranges.length-1; j++) {
				var groupSize = (range / 100.0) * ranges[j];
				measure.thresholds[j+1] = measure.thresholds[j] + groupSize;
			}
			
			measure.thresholds[ ranges.length ] = measure.lower_bound;	
			
			measure.num_group = ranges.length.length;
		}
		
		function setUniformTrasholds( measureIndex ) { 
		  
      var measure = sbi.geo.conf.measures[measureIndex];
      measure.num_group = measure.threshold_calculator_conf.params.num_group;	
      
      
      var ranges = new Array();
      
		
			var perc = 100 / measure.num_group;
			for(var j = 0; j < measure.num_group; j++) {
				ranges[j] = perc;
			}
			
			measure.threshold_calculator_conf.type = 'perc';
			measure.threshold_calculator_conf.params = {ranges: ranges};
			
			setPercTrasholds( measureIndex );
		}
		
		function setQuantileTrasholds( measureIndex ) {
				
				var measure = sbi.geo.conf.measures[measureIndex];		
        var MIN = measure.ordered_values[0];
        var MAX = measure.ordered_values[measure.ordered_values.length-1];
        
        
        measure.thresholds = new Array();
        measure.num_group = measure.threshold_calculator_conf.params.num_group;
				
				var diff_value_num = 0;	
				var start_index = -1;	
				if(MIN >= measure.lower_bound && MAX <= measure.upper_bound) {
					diff_value_num = measure.ordered_values.length;
					start_index = 0;
				} else {
					for(j = 0; j < measure.ordered_values.length; j++) {						
			   			if(measure.ordered_values[j] >= measure.lower_bound && measure.ordered_values[j] <= measure.upper_bound) {
			   				start_index = (start_index == -1?j:start_index);
			   				diff_value_num++;
			   			}
		   		}
		   	}
		   			   		
				if(diff_value_num < measure.num_group) measure.num_group = diff_value_num;
				var blockSize = Math.floor( diff_value_num / measure.num_group );
			
				measure.thresholds[0] = measure.lower_bound;
				for(j = 1; j < measure.num_group; j++){
					measure.thresholds[j] = measure.ordered_values[start_index + (j*blockSize)];
				}
				measure.thresholds[measure.num_group] = measure.upper_bound;
		}
*/