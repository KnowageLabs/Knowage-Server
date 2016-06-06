
'use strict';
 
/* https://github.com/angular/protractor/blob/master/docs/getting-started.md */
 
describe('olap app', function() {
 
  beforeEach(function () {
    
  });
 
  it('should go to olap', function() {
	  
   browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=5');
  });
  it('should drilldown', function() {
	  
  browser.driver.findElement(by.css('img')).click();
   
  });
});

