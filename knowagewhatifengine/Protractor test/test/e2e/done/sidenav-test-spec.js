describe('my app', function() {
 
  beforeEach(function () {
	 
  });

  it('should open olap page', function(){
  	browser.get('/knowagewhatifengine/restful-services/start-standalone?SBI_EXECUTION_ID=4'); 
  	element(by.css('[ng-click="toggleRight()"]')).click();
  	browser.sleep(1234);
  });

  it('should chage data repr', function(){
  	var perspectiveBtnParams = ['both','chart','table'];
  	
  	for(var i=0; i<perspectiveBtnParams.length;i++){
      element(by.css('[ng-click="changeDataRepr(\'' + perspectiveBtnParams[i] +'\')"]')).click();
      browser.sleep(1234);
    }
  });

  it('should close right sidenav', function(){
	element(by.css('[ng-click="closeSideNav(\'right\')"]'))
  });
});