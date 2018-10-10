
var HtmlReporter = require('protractor-beautiful-reporter');
exports.config = {
		
		 

		directConnect: true,
			baseUrl:'http://localhost:8080',
		    multiCapabilities: [
		    	{
		    		browserName: 'firefox',
		    		'moz:firefoxOptions': {
		    		    args: [ "--headless", "--disable-gpu", "--window-size=1920,1080" ]
		    		  }
		    	}, 
//		    	{
//		    		browserName: 'chrome',
//		    		chromeOptions: {
//					   //  'args': [ "--headless", "--disable-gpu", "--window-size=1920,1080" ]
//				  }
//		    	}
		    ],
		  
		  

		  // Framework to use. Jasmine is recommended.
		  framework: 'jasmine2',

		  // Spec patterns are relative to the current working directory when
		  // protractor is called.
		  
		  suites:{
			  knowage:'specs/knowage/**/*login*Spec.js'
		  },

		  // Options to be passed to Jasmine.
		  jasmineNodeOpts: {
		    defaultTimeoutInterval: 260000
		  },
		  onPrepare: function() {
		      // Add a screenshot reporter and store screenshots to `/tmp/screenshots`:
//		      jasmine.getEnv().addReporter(new HtmlReporter({
//		         baseDirectory: 'e2e/protractor/testresults',
//		          
//		      }).getJasmine2Reporter());
			  var jasmineReporters = require('jasmine-reporters');
			    jasmine.getEnv().addReporter(new jasmineReporters.JUnitXmlReporter({
			        consolidateAll: true,
			        savePath: 'e2e/protractor/testresults',
			        filePrefix: 'testresults'
			    }));
		   }

};