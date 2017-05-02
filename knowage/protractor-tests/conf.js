
exports.config = {
  allScriptsTimeout: 99999,

  // The address of a running selenium server.
  seleniumAddress: 'http://localhost:4444/wd/hub',

  // Capabilities to be passed to the webdriver instance.
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

  framework: 'jasmine',

  // Spec patterns are relative to the current working directly when
  // protractor is called.
  specs: ['test/e2e/**/*Test*.js'],

};
