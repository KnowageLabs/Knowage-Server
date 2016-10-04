exports.config = {
  seleniumAddress: 'http://localhost:4444/wd/hub',
  specs: ['angularTest.js'],
 capabilities: {
	    'browserName': 'phantomjs',
	    //'phantomjs.binary.path': require('C:\Users\dpirkovic\AppData\Roaming\npm\node_modules\phantomjs-prebuild\phantom\bin\phantomjs.exe').path
	  },
  onPrepare: function() {
	  // The require statement must be down here, since jasmine-reporters
	  // needs jasmine to be in the global and protractor does not guarantee
	  // this until inside the onPrepare function.
	  var jasmineReporters = require('jasmine-reporters');
	    jasmine.getEnv().addReporter(
	        new jasmineReporters.JUnitXmlReporter('xmloutput', true, true)
	    );
	}
};