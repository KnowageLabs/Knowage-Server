<template>

   <div class="layout-menu-container">
      <InfoDialog v-model:visibility="display"></InfoDialog>
         <div class="menu-scroll-content">
            <div class="primnav">
               <ul>
                  <PanelMenu :model="fixedMenu" />
               </ul>
            </div>
            <TieredMenu :model="myMenu"></TieredMenu>
            <TieredMenu :model="fixedMenu"></TieredMenu>
            <TieredMenu  :model="customMenu"></TieredMenu >
         </div>
   </div>

</template>

<script lang="ts">
   import { defineComponent } from 'vue'
   //import MegaMenu from 'primevue/megamenu';
   import InfoDialog from '@/components/InfoDialog.vue'
   import TieredMenu from 'primevue/tieredmenu'
   import PanelMenu from 'primevue/panelmenu'
   import axios from 'axios'

   export default defineComponent({
      name: 'Knmenu',
      props: {
         model: Array
      },
      components: {
         TieredMenu,
         InfoDialog,
         PanelMenu
      },
      data() {
         return {
            fixedMenu: new Array<MenuItem>(),
            customMenu: new Array<MenuItem>(),
            display: false,
            myMenu: [{label:'home', icon:'pi pi-fw pi-home', to:{name:'home'}},{label:'gallery', icon:'pi pi-fw pi-home', to:{name:'galleryManagement'}},{label:'about', icon:'pi pi-fw pi-star', to:{name:'about'}},{label:'info',icon:'pi pi-fw pi-info', command: () => {
               this.toggleInfo()
            }},{label:'logout', icon:'pi pi-fw pi-sign-out', url:'http://localhost:8080/knowage/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE'}]
         }
      },
      methods:{
         toggleInfo(){
            this.display = !this.display
         }
      },
      created() {
         axios.get('/knowage/restful-services/1.0/menu/enduser?curr_country=US&curr_language=en')
            .then((response) => {
               this.fixedMenu = updateMenuModel(response.data.fixedMenu)
               console.log('fixedMenu',this.fixedMenu)
               this.customMenu = updateMenuModel(response.data.customMenu[0].menu)
               console.log('customMenu',response.data.userMenu)
            },(error) => console.error(error))
      }
   })

   interface MenuItem {
      label: string;
      url?: string;
      to?: string;
      icon: string;
      items?: Array<MenuItem> | Array<Array<MenuItem>>;
   }

   function updateMenuModel(oldModel: any) : Array<MenuItem> {
      oldModel = oldModel.map(obj =>{
         let item:MenuItem = {
            label:obj.tooltip || obj.title || obj.text,
            icon : 'pi pi-fw pi-flag'
            }
         if(obj.linkType === 'execUrl') item.url = obj.firstUrl
         else if(obj.linkType === 'execDirectUrl') item.url = obj.firstUrl || obj.src
         else if(obj.linkType) item.to = obj.linkType
         if(obj.menu) item.items = updateMenuModel(obj.menu)
         if(obj.items) item.items = updateMenuModel(obj.items)
         return item
      })
      return oldModel
   }

   
</script>

<style lang="scss" scoped>
   .layout-menu-container {
      z-index: 100;
      top: 0;
      background-color: #363a41;
      height: 100%;
      position: fixed;
   }
   .p-tieredmenu{
      padding: 0;
      border:none;
      border-radius: 0;
   }
</style>