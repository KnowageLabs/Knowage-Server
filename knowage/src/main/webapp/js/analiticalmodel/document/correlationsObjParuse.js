/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */  
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