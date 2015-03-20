/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  function correlationManagerObj(){
  this.correlations = new Array();
  function addCorrelationFunct(corr) {
    this.correlations[this.correlations.length] = corr;
  }
  this.addCorrelation = addCorrelationFunct;
  function getCorrelationFunct(index) {
    return this.correlations[index];
  }
  this.getCorrelation = getCorrelationFunct;
  function setCorrelationFunct(index, corr) {
    this.correlations[index] = corr;
  }
  this.setCorrelation = setCorrelationFunct;
  function deleteCorrelationFunct(index) {
    var prog = 0;
    var tmpCorr = new Array();
    for(i=0; i<this.correlations.length; i++) {
      if(i!=index) {
        tmpCorr[prog] = this.correlations[i];
        prog = prog + 1;
      }
    }
    this.correlations=tmpCorr; 
  }
  this.deleteCorrelation = deleteCorrelationFunct;
  function setPreConditionFunct(index, precondval) {
    this.correlations[index].preCond = precondval; 
  }
  this.setPreCondition = setPreConditionFunct;
  function setPostConditionFunct(index, postcondval) {
    this.correlations[index].postCond = postcondval; 
  }
  this.setPostCondition = setPostConditionFunct;
  function setLogicOperatorFunct(index, logop) {
    this.correlations[index].logicOper = logop; 
  }
  this.setLogicOperator = setLogicOperatorFunct;
  this.correlationExist = correlationExistFunct;
  function correlationExistFunct(idPFath, fOp) {
     var found = false;
     for(i=0; i<this.correlations.length; i++) {
        var corr = this.correlations[i];
        if( (corr.idParFather==idPFath) && (corr.condition==fOp) ) {
          found = true;
        }
     }
     return found; 
  }
}