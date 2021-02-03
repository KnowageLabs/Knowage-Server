<template>

   <div class="layout-menu-container">
      <InfoDialog v-model:visibility="display"></InfoDialog>
         <div class="menu-scroll-content">
            <div>
               <div class="profile">
                  <button class="p-link" @click="toggleProfile">
                     <img alt="Profile" class="profile-image" src="https://i.pravatar.cc/50" >
                     <span class="profile-name">Isabel Oliviera</span>
                        <i class="pi pi-fw pi-chevron-down"></i>
                     <span class="profile-role">Marketing</span>
                  </button>
                  
               </div>
               <transition name="slide-down">
                  <ul class="layout-menu profile-menu" v-show="showProfileMenu">
                     <li v-for="(item, i) of fixedMenu" :key="i" role="menuitem">
                        <router-link v-if="item.to" :to="item.to" exact>
                           <i :class="item.icon"></i>
                           <span>{{item.label}}</span>
                        </router-link>
                     </li>
                     <li role="menuitem">
                        <router-link :to="{name:'about'}" exact>
                           <i class="fas fa-sign-out-alt"></i>
                           <span>Logout</span>
                        </router-link>
                     </li>
                  </ul>
               </transition>
            </div>
            <div>
                <ul class="layout-menu">
                     <li v-for="(item, i) of customMenu" :key="i" role="menuitem">
                        <a :href="item.url" >
                           <i :class="item.icon"></i>
                           <span>{{item.label}}</span>
                        </a>
                     </li>
                  </ul>
            </div>

         </div>
   </div>

</template>

<script lang="ts">
   import { defineComponent } from 'vue'
   import InfoDialog from '@/components/InfoDialog.vue'
   import axios from 'axios'

   export default defineComponent({
      name: 'Knmenu',
      props: {
         model: Array
      },
      components: {
         InfoDialog,
      },
      data() {
         return {
            showProfileMenu: false,
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
         },
         toggleProfile() {
            this.showProfileMenu = !this.showProfileMenu
         }
      },
      created() {
         this.showProfileMenu = false
         axios.get('/knowage/restful-services/1.0/menu/enduser?curr_country=US&curr_language=en')
            .then((response) => {
               this.fixedMenu = updateMenuModel(response.data.fixedMenu)
               console.log('fixedMenu',this.fixedMenu)
               this.customMenu = updateMenuModel(response.data.customMenu[0].menu)
               console.log('customMenu',this.customMenu)
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
.slide-down-enter-active, .slide-down-leave-active {
   overflow: hidden;
   transition: max-height 1s ease-in-out;
  max-height: 500px;
}

.slide-down-enter-from, .slide-down-leave-to {
  max-height: 0; 
}
   .layout-menu-container {
      z-index: 100;
      width: 58px;
      top: 0;
      background-color: #43749E;
      height: 100%;
      position: fixed;
      .profile {
         height: 60px;
         padding: 8px;
         box-shadow: 0 2px 5px 0 rgb(0,0,0);
         & > button {
            cursor: pointer;
            width: 100%;
            font-size: 14px;
            font-family: Roboto, "Helvetica Neue", Arial, sans-serif;
            .profile-image {
               width: 45px;
               height: 45px;
               float: right;
               margin-left: 4px;
               border-radius: 50%;
               border: 2px solid #CF0854;
            }
            .profile-name, .profile-role, i {
               display: none;
            }
         }
      }
      .profile-menu {
         border-bottom: 1px solid lighten(#43749E, 10%);
         li {
            &:first-child{
               padding-top: 16px;
            }
         }
      }
      .layout-menu {
         margin: 0;
         padding: 0;
         list-style: none;
         & > li {
            position: relative;
            & > a {
               text-align: center;
               padding: 15px;
               color: white;
               display: block;
                width: 100%;
               transition: background-color .3s, border-left-color .3s;
               overflow: hidden;
               border-left: 4px solid transparent;
               outline: none;
               cursor: pointer;
               user-select: none;
               span {
                  display: none;
               }
               &:hover {
                  background-color: lighten(#43749E, 10%);
               }
            }
         }
      }
   }
   .p-tieredmenu{
      padding: 0;
      border:none;
      border-radius: 0;
   }
</style>