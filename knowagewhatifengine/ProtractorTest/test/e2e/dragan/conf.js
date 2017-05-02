exports.config = {
  seleniumAddress: 'http://localhost:4444/wd/hub',
  specs: ['angularTest.js'],
 capabilities: {
	    'browserName': 'phantomjs',
	    'phantomjs.binary.path': '/home/spagobi/continuousintegration/software/node_modules/phantomjs-prebuilt/bin/phantomjs'
	  },
  onPrepare: function() {
	  // The require statement must be down here, since jasmine-reporters
	  // needs jasmine to be in the global and protractor does not guarantee
	  // this until inside the onPrepare function.
	  var jasmineReporters = require('/home/spagobi/continuousintegration/software/node_modules/jasmine-reporters');
	    jasmine.getEnv().addReporter(
	        new jasmineReporters.JUnitXmlReporter({
	        	consolidateAll: false,
	            filePrefix: 'Test-'
	        })
	    );
	},
	  getPageTimeout : 5000 
};