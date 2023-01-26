import{o as d,c,a as u,K as r,G as o}from"./index-1e676509.js";var p={name:"InputSwitch",emits:["click","update:modelValue","change","input","focus","blur"],props:{modelValue:{type:null,default:!1},trueValue:{type:null,default:!0},falseValue:{type:null,default:!1},disabled:{type:Boolean,default:!1},inputId:{type:String,default:null},inputClass:{type:String,default:null},inputStyle:{type:null,default:null},inputProps:{type:null,default:null},"aria-labelledby":{type:String,default:null},"aria-label":{type:String,default:null}},data(){return{focused:!1}},methods:{onClick(e){if(!this.disabled){const t=this.checked?this.falseValue:this.trueValue;this.$emit("click",e),this.$emit("update:modelValue",t),this.$emit("change",e),this.$emit("input",t),this.$refs.input.focus()}e.preventDefault()},onFocus(e){this.focused=!0,this.$emit("focus",e)},onBlur(e){this.focused=!1,this.$emit("blur",e)}},computed:{containerClass(){return["p-inputswitch p-component",{"p-inputswitch-checked":this.checked,"p-disabled":this.disabled,"p-focus":this.focused}]},checked(){return this.modelValue===this.trueValue}}};const h={class:"p-hidden-accessible"},f=["id","checked","disabled","aria-checked","aria-labelledby","aria-label"],m=u("span",{class:"p-inputswitch-slider"},null,-1);function b(e,t,i,n,l,s){return d(),c("div",{class:o(s.containerClass),onClick:t[2]||(t[2]=a=>s.onClick(a))},[u("div",h,[u("input",r({ref:"input",id:i.inputId,type:"checkbox",role:"switch",class:i.inputClass,style:i.inputStyle,checked:s.checked,disabled:i.disabled,"aria-checked":s.checked,"aria-labelledby":e.ariaLabelledby,"aria-label":e.ariaLabel,onFocus:t[0]||(t[0]=a=>s.onFocus(a)),onBlur:t[1]||(t[1]=a=>s.onBlur(a))},i.inputProps),null,16,f)]),m],2)}function y(e,t){t===void 0&&(t={});var i=t.insertAt;if(!(!e||typeof document=="undefined")){var n=document.head||document.getElementsByTagName("head")[0],l=document.createElement("style");l.type="text/css",i==="top"&&n.firstChild?n.insertBefore(l,n.firstChild):n.appendChild(l),l.styleSheet?l.styleSheet.cssText=e:l.appendChild(document.createTextNode(e))}}var k=`
.p-inputswitch {
    position: relative;
    display: inline-block;
}
.p-inputswitch-slider {
    position: absolute;
    cursor: pointer;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
}
.p-inputswitch-slider:before {
    position: absolute;
    content: '';
    top: 50%;
}
`;y(k);p.render=b;export{p as s};
//# sourceMappingURL=inputswitch.esm-80317a38.js.map
