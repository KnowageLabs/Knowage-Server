describe('filter test', function(){
	beforeEach(function () {

  	});

	afterEach(function(){
		browser.sleep(2345);
	});

  	it('should open olap page', function(){
  		browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4');
  		
  	});

  	it('should click on first filter icon in filter panel',function(){
  		element.all(
  			by.css('.bottom')).
  			first().element(by.css('.md-icon-button')).click();

  	}); 		

  	it('should open first nodes dialog',function(){
  		element(by.css('[ng-click="expandTreeAsync(item)"]')).click();
  		browser.sleep(1234);
  		element.all(by.model('item.children')).first().element(by.css('[ng-click="expandTreeAsync(item)"]')).click();
  	});

  	it('should close nodes', function(){
  		element.all(by.model('item.children')).first().element(by.css('[ng-click="hideAsyncTree(item)"]')).click();
  	});

  	it('should close dialog', function(){
  		element(by.css('[ng-click="closeFiltersDialog()"]')).click();
  	});

});