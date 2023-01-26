import{U as f,o as u,c as r,a as d,B as c,C as m,G as p,J as x,t as b,K as y}from"./index-1e676509.js";var g={name:"Chips",emits:["update:modelValue","add","remove","focus","blur"],props:{modelValue:{type:Array,default:null},max:{type:Number,default:null},separator:{type:String,default:null},addOnBlur:{type:Boolean,default:null},allowDuplicate:{type:Boolean,default:!0},placeholder:{type:String,default:null},disabled:{type:Boolean,default:!1},inputId:{type:String,default:null},inputClass:{type:String,default:null},inputStyle:{type:null,default:null},inputProps:{type:null,default:null},"aria-labelledby":{type:String,default:null},"aria-label":{type:String,default:null}},data(){return{id:f(),inputValue:null,focused:!1,focusedIndex:null}},methods:{onWrapperClick(){this.$refs.input.focus()},onInput(t){this.inputValue=t.target.value,this.focusedIndex=null},onFocus(t){this.focused=!0,this.focusedIndex=null,this.$emit("focus",t)},onBlur(t){this.focused=!1,this.focusedIndex=null,this.addOnBlur&&this.addItem(t,t.target.value,!1),this.$emit("blur",t)},onKeyDown(t){const e=t.target.value;switch(t.code){case"Backspace":e.length===0&&this.modelValue&&this.modelValue.length>0&&(this.focusedIndex!==null?this.removeItem(t,this.focusedIndex):this.removeItem(t,this.modelValue.length-1));break;case"Enter":e&&e.trim().length&&!this.maxedOut&&this.addItem(t,e,!0);break;case"ArrowLeft":e.length===0&&this.modelValue&&this.modelValue.length>0&&this.$refs.container.focus();break;case"ArrowRight":t.stopPropagation();break;default:this.separator&&this.separator===","&&t.key===","&&this.addItem(t,e,!0);break}},onPaste(t){if(this.separator){let e=(t.clipboardData||window.clipboardData).getData("Text");if(e){let n=this.modelValue||[],s=e.split(this.separator);s=s.filter(a=>this.allowDuplicate||n.indexOf(a)===-1),n=[...n,...s],this.updateModel(t,n,!0)}}},onContainerFocus(){this.focused=!0},onContainerBlur(){this.focusedIndex=-1,this.focused=!1},onContainerKeyDown(t){switch(t.code){case"ArrowLeft":this.onArrowLeftKeyOn(t);break;case"ArrowRight":this.onArrowRightKeyOn(t);break;case"Backspace":this.onBackspaceKeyOn(t);break}},onArrowLeftKeyOn(){this.inputValue.length===0&&this.modelValue&&this.modelValue.length>0&&(this.focusedIndex=this.focusedIndex===null?this.modelValue.length-1:this.focusedIndex-1,this.focusedIndex<0&&(this.focusedIndex=0))},onArrowRightKeyOn(){this.inputValue.length===0&&this.modelValue&&this.modelValue.length>0&&(this.focusedIndex===this.modelValue.length-1?(this.focusedIndex=null,this.$refs.input.focus()):this.focusedIndex++)},onBackspaceKeyOn(t){this.focusedIndex!==null&&this.removeItem(t,this.focusedIndex)},updateModel(t,e,n){this.$emit("update:modelValue",e),this.$emit("add",{originalEvent:t,value:e}),this.$refs.input.value="",this.inputValue="",n&&t.preventDefault()},addItem(t,e,n){if(e&&e.trim().length){let s=this.modelValue?[...this.modelValue]:[];(this.allowDuplicate||s.indexOf(e)===-1)&&(s.push(e),this.updateModel(t,s,n))}},removeItem(t,e){if(this.disabled)return;let n=[...this.modelValue];const s=n.splice(e,1);this.focusedIndex=null,this.$refs.input.focus(),this.$emit("update:modelValue",n),this.$emit("remove",{originalEvent:t,value:s})}},computed:{maxedOut(){return this.max&&this.modelValue&&this.max===this.modelValue.length},containerClass(){return["p-chips p-component p-inputwrapper",{"p-disabled":this.disabled,"p-focus":this.focused,"p-inputwrapper-filled":this.modelValue&&this.modelValue.length||this.inputValue&&this.inputValue.length,"p-inputwrapper-focus":this.focused}]},focusedOptionId(){return this.focusedIndex!==null?`${this.id}_chips_item_${this.focusedIndex}`:null}}};const w=["aria-labelledby","aria-label","aria-activedescendant"],I=["id","aria-label","aria-setsize","aria-posinset"],k={class:"p-chips-token-label"},V=["onClick"],C={class:"p-chips-input-token",role:"option"},B=["id","disabled","placeholder"];function O(t,e,n,s,a,i){return u(),r("div",{class:p(i.containerClass)},[d("ul",{ref:"container",class:"p-inputtext p-chips-multiple-container",tabindex:"-1",role:"listbox","aria-orientation":"horizontal","aria-labelledby":t.ariaLabelledby,"aria-label":t.ariaLabel,"aria-activedescendant":a.focused?i.focusedOptionId:void 0,onClick:e[5]||(e[5]=l=>i.onWrapperClick()),onFocus:e[6]||(e[6]=(...l)=>i.onContainerFocus&&i.onContainerFocus(...l)),onBlur:e[7]||(e[7]=(...l)=>i.onContainerBlur&&i.onContainerBlur(...l)),onKeydown:e[8]||(e[8]=(...l)=>i.onContainerKeyDown&&i.onContainerKeyDown(...l))},[(u(!0),r(c,null,m(n.modelValue,(l,o)=>(u(),r("li",{key:`${o}_${l}`,id:a.id+"_chips_item_"+o,role:"option",class:p(["p-chips-token",{"p-focus":a.focusedIndex===o}]),"aria-label":l,"aria-selected":!0,"aria-setsize":n.modelValue.length,"aria-posinset":o+1},[x(t.$slots,"chip",{value:l},()=>[d("span",k,b(l),1)]),d("span",{class:"p-chips-token-icon pi pi-times-circle",onClick:h=>i.removeItem(h,o),"aria-hidden":"true"},null,8,V)],10,I))),128)),d("li",C,[d("input",y({ref:"input",id:n.inputId,type:"text",class:n.inputClass,style:n.inputStyle,disabled:n.disabled||i.maxedOut,placeholder:n.placeholder,onFocus:e[0]||(e[0]=l=>i.onFocus(l)),onBlur:e[1]||(e[1]=l=>i.onBlur(l)),onInput:e[2]||(e[2]=(...l)=>i.onInput&&i.onInput(...l)),onKeydown:e[3]||(e[3]=l=>i.onKeyDown(l)),onPaste:e[4]||(e[4]=l=>i.onPaste(l))},n.inputProps),null,16,B)])],40,w)],2)}function v(t,e){e===void 0&&(e={});var n=e.insertAt;if(!(!t||typeof document=="undefined")){var s=document.head||document.getElementsByTagName("head")[0],a=document.createElement("style");a.type="text/css",n==="top"&&s.firstChild?s.insertBefore(a,s.firstChild):s.appendChild(a),a.styleSheet?a.styleSheet.cssText=t:a.appendChild(document.createTextNode(t))}}var D=`
.p-chips {
    display: -webkit-inline-box;
    display: -ms-inline-flexbox;
    display: inline-flex;
}
.p-chips-multiple-container {
    margin: 0;
    padding: 0;
    list-style-type: none;
    cursor: text;
    overflow: hidden;
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    -webkit-box-align: center;
        -ms-flex-align: center;
            align-items: center;
    -ms-flex-wrap: wrap;
        flex-wrap: wrap;
}
.p-chips-token {
    cursor: default;
    display: -webkit-inline-box;
    display: -ms-inline-flexbox;
    display: inline-flex;
    -webkit-box-align: center;
        -ms-flex-align: center;
            align-items: center;
    -webkit-box-flex: 0;
        -ms-flex: 0 0 auto;
            flex: 0 0 auto;
}
.p-chips-input-token {
    -webkit-box-flex: 1;
        -ms-flex: 1 1 auto;
            flex: 1 1 auto;
    display: -webkit-inline-box;
    display: -ms-inline-flexbox;
    display: inline-flex;
}
.p-chips-token-icon {
    cursor: pointer;
}
.p-chips-input-token input {
    border: 0 none;
    outline: 0 none;
    background-color: transparent;
    margin: 0;
    padding: 0;
    -webkit-box-shadow: none;
            box-shadow: none;
    border-radius: 0;
    width: 100%;
}
.p-fluid .p-chips {
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
}
`;v(D);g.render=O;export{g as s};
//# sourceMappingURL=chips.esm-693c3c01.js.map
