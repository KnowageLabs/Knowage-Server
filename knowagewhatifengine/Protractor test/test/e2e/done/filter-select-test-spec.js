describe('filter test', function(){
  var oldValue;
	beforeEach(function () {

  	});

	afterEach(function(){
		browser.sleep(2345);
	});

  	it('should open olap page', function(){
  		browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4');
      element(by.css('[ordinal="0"]')).getText().then(function(c){
        oldValue = c;
      });
  	});

  	it('open Region filter element',function(){
      browser.driver.findElement(by.id('Region-filter')).click();
      //console.log(oldValue);
    }); 		

  	it('should open first nodes dialog',function(){
  		element(by.css('[ng-click="expandTreeAsync(item)"]')).click();
  		browser.sleep(1234);
  		element.all(by.model('item.children')).first().element(by.css('[ng-click="expandTreeAsync(item)"]')).click();
  	});

    it('should select some node',function(){
      element.all(by.model('item.children')).first().element(by.css('[ng-click="selectFilter(item)"]')).click();
    });
  	/*it('should close nodes', function(){
  		element.all(by.model('item.children')).first().element(by.css('[ng-click="hideAsyncTree(item)"]')).click();
  	});*/

  	it('save selected dimension', function(){
  		element(by.css('[ng-click="filterDialogSave()"]')).click();
      expect(element(by.css('[ordinal="0"]')).getText()).toBeLessThan(oldValue);
  	});

});