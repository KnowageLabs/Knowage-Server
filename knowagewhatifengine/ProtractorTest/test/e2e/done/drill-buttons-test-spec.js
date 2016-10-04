describe('drill butons', function(){
	afterEach(function(){
		browser.sleep(2345);
	});

	it('should open olap page and drill down', function(){
  		browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4');
  	  	browser.sleep(1234);
  	});

  	it('should open filter dialog and search',function(){
  		browser.driver.findElement(by.id('Region-filter')).click();
  		browser.sleep(1234);
      element(by.model('searchText')).sendKeys('guadalajara');
      browser.sleep(567);
      element(by.model('showSiblings')).click();
      browser.sleep(567);
      element(by.css('[ng-click="searchFilter()"]')).click();
  	});

    it('should select region', function(){
      //tdl-value-{{item.name}}
      browser.driver.findElement(by.id('tdl-value-Guadalajara')).click();
    });

    it('save selected dimension', function(){
      element(by.css('[ng-click="filterDialogSave()"]')).click();
    });

    it('should click on only data in table', function(){
      //class="x-pivot-cell x-pivot-header-column"
      element(by.css('[class="x-pivot-cell x-pivot-header-column"]')).click();
    });

    it('should open sidenav', function(){
      element(by.css('[ng-click="toggleRight()"]')).click();
    });

    it('should click on drill trough button', function(){
      
      browser.driver.findElement(by.id('drill-trough-btn')).click();
    });

    it('should close drill trough dialog', function(){
       element(by.css('[ng-click="closeDialog()"]')).click();
    });

});
