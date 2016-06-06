describe('drill butons', function(){
	afterEach(function(){
		browser.sleep(2345);
	}); 

	it('should open olap page', function(){
  		browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4');
      element(by.css('[src="../img/plus.gif"]')).click();
  	});

  it('should open sidenav', function(){
      element(by.css('[ng-click="toggleRight()"]')).click();
    });

  it('should enable sorting', function(){
      browser.driver.findElement(by.id('ena-dis-sorting-btn')).click();
    });

  it('should close sidenav',function(){
      element(by.css('[ng-click="closeSideNav(\'right\')"]')).click();
  });

  it('should click on sort btn',function(){
      element(by.css('[src="../img/noSortRows.png"]')).click(); 
      element.all(by.css('[class="x-pivot-cell x-pivot-header-column"]')).then(function(count){
        for(var i=0; i<count.length-1; i++){
          var j= i+1;
          expect(element(by.css('[ordinal="'+j+'"]')).getText()).toBeLessThan(element(by.css('[ordinal="'+i+'"]')));
        }
      });
    });

  it('should open sidenav', function(){
      element(by.css('[ng-click="toggleRight()"]')).click();
    });
  
  it('should open dialog for sorting settings', function(){
      browser.driver.findElement(by.id('sort-set-btn')).click();      
    });

  it('should change sorting type',function(){
      element.all(by.repeater('d in sortingModes')).get(2).click();
      //element(by.model('sortingCount')).sendKeys('');
      element(by.model('sortingCount')).clear();
      browser.sleep(456);
      element(by.model('sortingCount')).sendKeys('2');
      browser.sleep(456);
      element(by.css('[ng-click="saveSortingSettings()"]')).click();
    });
});