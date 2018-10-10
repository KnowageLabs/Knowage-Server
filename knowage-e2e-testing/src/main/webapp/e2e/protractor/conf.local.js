
var HtmlReporter = require('protractor-beautiful-reporter');
exports.config = {
		
		 

			directConnect: true,
			baseUrl:'http://localhost:8080',
			allScriptsTimeout: 11000,

		    multiCapabilities: [
//		    	{
//		    		browserName: 'firefox',
//		    		// allows different specs to run in parallel.
//		    	    // If this is set to be true, specs will be sharded by file
//		    	    // (i.e. all files to be run by this set of capabilities will run in parallel).
//		    	    // Default is false.
//		    		shardTestFiles: true,
//		    		//maxInstances: 2,
//		    		
//		    		'moz:firefoxOptions': {
//		    		   // args: [ "--headless", "--disable-gpu", "--window-size=1920,1080" ]
//		    		  }
//		    	}, 
		    	{
		    		browserName: 'chrome',
		    		// allows different specs to run in parallel.
		    	    // If this is set to be true, specs will be sharded by file
		    	    // (i.e. all files to be run by this set of capabilities will run in parallel).
		    	    // Default is false.
		    		shardTestFiles: true, 
		    		chromeOptions: {
					   //  'args': [ "--headless", "--disable-gpu", "--window-size=1920,1080" ]
				  }
		    	}
		    ],

		  // Framework to use. Jasmine is recommended.
		  framework: 'jasmine2',

		  // Spec patterns are relative to the current working directory when
		  // protractor is called.
		  suites:{
			  knowage:'specs/knowage/**/e*Spec.js'
		  },

		  // Options to be passed to Jasmine.
		  jasmineNodeOpts: {
		    defaultTimeoutInterval: 260000
		  },
		  onPrepare: function() {
		      // Add a screenshot reporter and store screenshots to `/tmp/screenshots`:
		      jasmine.getEnv().addReporter(new HtmlReporter({
		         baseDirectory:  'e2e/protractor/testresults',
		          
		      }).getJasmine2Reporter());
		   }

};