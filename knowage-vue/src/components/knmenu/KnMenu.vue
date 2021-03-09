<template>

   <div class="layout-menu-container">
      <InfoDialog v-model:visibility="display"></InfoDialog>
      <LanguageDialog v-model:visibility="languageDisplay"></LanguageDialog>
         <div class="menu-scroll-content">
            <div>
               <div class="profile">
                  <button class="p-link" @click="toggleProfile" v-tooltip="user && user.fullName">
                     <img alt="Profile" class="profile-image" :src="getGravatarSrc(user)" >
                     <span v-if="user" class="profile-name">{{user.fullName}}</span>
                        <i class="pi pi-fw pi-chevron-down"></i>
                     <span class="profile-role">Marketing</span>
                  </button>
               </div>
               <transition name="slide-down">
                  <ul class="layout-menu profile-menu" v-show="showProfileMenu">
                     <template v-for="(item, i) of fixedMenu" :key="i">
                        <template v-if="item">
                           <KnMenuItem :item="item" @click="itemClick"></KnMenuItem>
                        </template>
                     </template>
                  </ul>
               </transition>
            </div>
            <div>
               <ul class="layout-menu">
                  <KnAdminMenu :model="adminMenu"></KnAdminMenu>
                   <template v-for="(item, i) of userMenu" :key="i">
                     <KnMenuItem :item="item" @click="itemClick"></KnMenuItem>
                  </template>
                  <template v-for="(item, i) of customMenu" :key="i">
                     <KnMenuItem :item="item" @click="itemClick"></KnMenuItem>
                  </template>
               </ul>
            </div>

         </div>
   </div>

</template>

<script lang="ts">
   import { defineComponent } from 'vue'
   import InfoDialog from '@/components/infoDialog/InfoDialog.vue'
   import KnMenuItem from '@/components/knmenu/KnMenuItem.vue'
   import KnAdminMenu from '@/components/knmenu/KnAdminMenu.vue'
   import LanguageDialog from '@/components/languageDialog/languageDialog.vue'
   import { getGravatar } from '@/helpers/gravatarHelper'
   import {  mapState } from 'vuex'
   import auth  from '@/helpers/authHelper'

   export default defineComponent({
      name: 'Knmenu',
      components: {
         InfoDialog,
         KnMenuItem,
         KnAdminMenu,
         LanguageDialog
      },
      data() {
         return {
            showProfileMenu: false,
            customMenu: new Array<MenuItem>(),
            userMenu: new Array<MenuItem>(),
            fixedMenu: new Array<MenuItem>(),
            adminMenu: new Array<MenuItem>(),
            display: false,
            languageDisplay: false
         }
      },
      methods:{
         info(){
            this.display = !this.display
         },
         logout(){
            auth.logout()
         },
         roleSelection(){
            alert('ciao')
         },
         languageSelection(){
            this.languageDisplay = !this.languageDisplay  
         },
         itemClick(event){
            const item = event.item;
            if (item.command) {
                  this[item.command]();
            }
            if (item.to && event.navigate) {
                event.navigate(event.originalEvent);
            }
         },
         toggleProfile() {
            this.showProfileMenu = !this.showProfileMenu
         },
         getGravatarSrc(user){
            if(user && user.attributes && user.attributes.email) return getGravatar(user.attributes.email)
            else return getGravatar('knowage@eng.it')
         }
      },
      created() {
         this.axios.get('/knowage/restful-services/3.0/menu/enduser?curr_country=IT&curr_language=it')
            .then(response => {
               this.customMenu = response.data.customMenu
               this.adminMenu = response.data.adminMenu
               this.fixedMenu = response.data.fixedMenu
               this.userMenu = response.data.userMenu
            }).catch(error => console.error(error))
      },
      computed: {
			...mapState({
				user: 'user'
			})
		}
   })

   interface MenuItem {
      label: string;
      url?: string;
      to?: string;
      iconCls?: string;
      items?: Array<MenuItem> | Array<Array<MenuItem>>;
   }

   /*function updateMenuModel(oldModel: Array<any>) : Array<MenuItem> {
      oldModel = oldModel.map(obj =>{
         let item:MenuItem = {label:obj.tooltip || obj.title || obj.text}
         if(obj.icon){
            item.icon = obj.icon.className
         }else item.icon = (obj.menu || obj.items) ? 'fas fa-list' : 'fas fa-file'
         if(obj.linkType === 'execUrl') item.url = obj.firstUrl
         else if(obj.linkType === 'execDirectUrl') item.url = obj.firstUrl || obj.src
         else if(obj.linkType) item.to = obj.linkType
         if(obj.menu) item.items = updateMenuModel(obj.menu)
         if(obj.items) item.items = updateMenuModel(obj.items)
         return item
      })
      return oldModel
   }*/

   
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
         box-shadow: -3px 0px 4px 0 black;
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
               background-color: white;
            }
            .profile-name, .profile-role, i {
               display: none;
            }
         }
      }
      .profile-menu {
         border-bottom: 1px solid lighten(#43749E, 10%);
      }

      .layout-menu {
         margin: 0;
         padding: 0;
         list-style: none;
         li {
            &:first-child{
               padding-top: 10px;
            }
         }
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
