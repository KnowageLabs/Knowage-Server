<template>
  <div class="layout-menu-container" id="kn-main-menu" ref="mainMenu">
    <InfoDialog v-model:visibility="display"></InfoDialog>
    <LanguageDialog v-model:visibility="languageDisplay"></LanguageDialog>
    <RoleDialog v-model:visibility="roleDisplay" :mandatory="mandatoryRole()"></RoleDialog>
    <DownloadsDialog v-model:visibility="downloadsDisplay"></DownloadsDialog>
    <NewsDialog v-model:visibility="newsDisplay"></NewsDialog>
    <LicenseDialog v-model:visibility="licenseDisplay" v-if="user && user.isSuperadmin && isEnterprise"></LicenseDialog>
    <MainMenuAdmin :openedPanelEvent="adminMenuOpened" :model="technicalUserFunctionalities" v-if="technicalUserFunctionalities && technicalUserFunctionalities.length > 0" @click="itemClick"></MainMenuAdmin>
    <q-menu ref="menu" :target="menuTargetElem" anchor="top right" self="top left">
      <MainMenuTieredMenu :items="selectedCustomMenu" @link="itemClick"></MainMenuTieredMenu>
    </q-menu>

    <div class="menu-scroll-content">
      <div class="profile" ref="menuProfile">
        <button class="p-link" @click="toggleProfile" v-tooltip="user && user.fullName">
          <img alt="Profile" class="profile-image" :src="getProfileImage(user)" />
          <span v-if="user" class="profile-name">{{ user.fullName }}</span>
          <i class="pi pi-fw pi-chevron-down"></i>
          <span class="profile-role">Marketing</span>
        </button>
      </div>
      <transition name="slide-down">
        <ul class="layout-menu profile-menu" v-show="showProfileMenu" ref="menuProfileSlide">
          <template v-for="(item, i) of commonUserFunctionalities" :key="i">
            <template v-if="item">
              <MainMenuItem :item="item" @click="itemClick"></MainMenuItem>
            </template>
          </template>
        </ul>
      </transition>
      <ScrollPanel :style="{ height: menuDimensions }">
        <ul class="layout-menu">
          <li role="menu" @click="toggleAdminMenu" v-if="technicalUserFunctionalities && technicalUserFunctionalities.length > 0">
            <span :class="['p-menuitem-icon', 'fas fa-cog']"></span>
          </li>
          <template v-for="(item, i) of allowedUserFunctionalities" :key="i">
            <MainMenuItem :item="item" @click="itemClick" :badge="getBadgeValue(item)" @mouseover="toggleMenu($event, item)"></MainMenuItem>
          </template>
          <template v-for="(item, i) of dynamicUserFunctionalities" :key="i">
            <MainMenuItem :item="item" @click="itemClick" @mouseover="toggleMenu($event, item)" :internationalize="true"></MainMenuItem>
          </template>
        </ul>
      </ScrollPanel>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import InfoDialog from "@/modules/mainMenu/dialogs/InfoDialog.vue";
import MainMenuItem from "@/modules/mainMenu/MainMenuItem.vue";
import MainMenuAdmin from "@/modules/mainMenu/MainMenuAdmin.vue";
import LanguageDialog from "@/modules/mainMenu/dialogs/LanguageDialog/LanguageDialog.vue";
import LicenseDialog from "@/modules/mainMenu/dialogs/LicenseDialog/LicenseDialog.vue";
import NewsDialog from "@/modules/mainMenu/dialogs/NewsDialog/NewsDialog.vue";
import RoleDialog from "@/modules/mainMenu/dialogs/RoleDialog.vue";
import { mapState } from "vuex";
import auth from "@/helpers/commons/authHelper";
import { AxiosResponse } from "axios";
import DownloadsDialog from "@/modules/mainMenu/dialogs/DownloadsDialog/DownloadsDialog.vue";
import { IMenuItem } from "@/modules/mainMenu/MainMenu";
import MainMenuTieredMenu from "@/modules/mainMenu/MainMenuTieredMenu.vue";
import ScrollPanel from "primevue/scrollpanel";

export default defineComponent({
  name: "Knmenu",
  components: {
    InfoDialog,
    MainMenuAdmin,
    MainMenuTieredMenu,
    MainMenuItem,
    LanguageDialog,
    LicenseDialog,
    NewsDialog,
    RoleDialog,
    DownloadsDialog,
    ScrollPanel,
  },
  data() {
    return {
      adminMenuOpened: false,
      menuDimensions: 0,
      showProfileMenu: false,
      dynamicUserFunctionalities: new Array<IMenuItem>(),
      allowedUserFunctionalities: new Array<IMenuItem>(),
      commonUserFunctionalities: new Array<IMenuItem>(),
      technicalUserFunctionalities: new Array<IMenuItem>(),
      tieredMenuClass: "largeScreen",
      display: false,
      languageDisplay: false,
      roleDisplay: false,
      downloadsDisplay: false,
      newsDisplay: false,
      licenseDisplay: false,
      selectedCustomMenu: {},
      hoverTimer: false as any,
      menuTargetElem: "" as any,
    };
  },
  emits: ["update:visibility", "menuItemSelected", "openMenu"],
  props: ["closeMenu"],
  methods: {
    info() {
      this.display = !this.display;
    },
    mandatoryRole() {
      if (this.configurations && this.configurations["KNOWAGE.MANDATORY-ROLE"] && this.user?.roles?.length > 1 && !this.user.defaultRole) {
        this.roleDisplay = true;
        return true;
      }
      return false;
    },
    logout() {
      auth.logout(localStorage.getItem("public") ? true : undefined);
    },
    roleSelection() {
      this.roleDisplay = !this.roleDisplay;
    },
    downloadsSelection() {
      this.downloadsDisplay = !this.downloadsDisplay;
    },
    isItemToDisplay(item) {
      if (item.conditionedView) {
        if (item.conditionedView === "downloads" && this.downloads && this.downloads.count.total > 0) return true;
        if (item.conditionedView === "news" && this.news && this.news.count.total > 0) return true;
        if (item.conditionedView === "roleSelection" && this.user && this.user.roles && this.user.roles.length > 1) return true;
        return false;
      } else {
        return true;
      }
    },
    languageSelection() {
      this.languageDisplay = !this.languageDisplay;
    },
    checkTimer() {
      clearTimeout(this.hoverTimer);
      this.hoverTimer = setTimeout(() => {
        // @ts-ignore
        this.$refs.menu.hide();
      }, process.env.VUE_APP_MENU_FADE_TIMER);
    },
    newsSelection() {
      console.log("ALLOWED: ", this.allowedUserFunctionalities);
      this.newsDisplay = !this.newsDisplay;
    },
    licenseSelection() {
      this.licenseDisplay = !this.licenseDisplay;
    },
    itemClick(event) {
      const item = event.item ? event.item : event;
      if (item.command) {
        this[item.command]();
      } else if (item.to) {
        if (event.navigate) {
          event.navigate(event.originalEvent);
          this.$emit("menuItemSelected", item);
        } else location.replace(this.getHref(item));
      } else if (item.url && (!item.target || item.target === "insideKnowage")) this.$router.push({ name: "externalUrl", params: { url: item.url } });
      if (this.adminMenuOpened) this.adminMenuOpened = false;
      this.hideItemMenu();
    },
    getHref(item) {
      let to = item.to;
      if (to) {
        to = to.replace(/\\\//g, "/");
        if (to.startsWith("/")) to = to.substring(1);
        return process.env.VUE_APP_PUBLIC_PATH + to;
      } else return to;
    },
    toggleProfile() {
      this.showProfileMenu = !this.showProfileMenu;
    },
    toggleAdminMenu(event) {
      this.adminMenuOpened = this.adminMenuOpened === false ? event : false;
    },
    getProfileImage(user) {
      if (user && user.organizationImageb64) return user.organizationImageb64;
      return require("@/assets/images/commons/logo_knowage.svg");
    },
    updateNewsAndDownload() {
      for (var idx in this.allowedUserFunctionalities) {
        let menu = this.allowedUserFunctionalities[idx] as any;
        if (menu.conditionedView) {
          if (menu.conditionedView === "downloads") {
            menu.visible = this.downloads.count.total > 0;
          } else if (menu.conditionedView === "news") {
            menu.visible = this.news.count.total > 0;
          }
          menu.badge = this.getBadgeValue(menu);
        }
      }
    },
    getBadgeValue(item) {
      if (item.conditionedView === "downloads") {
        if (Object.keys(this.downloads).length !== 0) return this.downloads.count.total - this.downloads.count.alreadyDownloaded;
      } else if (item.conditionedView === "news") {
        if (Object.keys(this.news).length !== 0) return this.news.count.unread;
      }
      return 0;
    },
    findHomePage(dynMenu) {
      for (const item of dynMenu) {
        const hasUrl = 'to' in item || 'url' in item

        if (hasUrl) return item

        if (item.items?.length) {
            const found = this.findHomePage(item.items)
            if (found) return found
        }
      }
      return null
    },
    toggleMenu(event, item) {
      this.hideItemMenu();
      if (item.items) {
        this.$emit("openMenu");
        clearTimeout(this.hoverTimer);
        this.menuTargetElem = document.querySelector(`li[role="menu"][label="${item.label}"]`);
        this.selectedCustomMenu = item.items;
        // @ts-ignore
        this.$refs.menu.show(event);
        this.$store.commit("toggleMenuOpened", true);
      }
    },
    hideItemMenu() {
      // @ts-ignore
      this.$refs.menu.hide();
    },
    getDimensions() {
      if (this.$refs && this.$refs.mainMenu)
        //@ts-ignore
        this.menuDimensions = this.$refs.mainMenu.getBoundingClientRect().height - this.$refs.menuProfile.getBoundingClientRect().height - this.$refs.menuProfileSlide.getBoundingClientRect().height + "px";
    },
    cleanTo(item): any {
      return item.to.replace(/\\\//g, "/");
    },

    async loadMenu(recursive: Boolean = false) {
      window.addEventListener("resize", this.getDimensions);
      this.$store.commit("setLoading", true);
      let localObject = { locale: this.$i18n.fallbackLocale.toString() };
      if (Object.keys(this.locale).length !== 0) localObject = { locale: this.locale };
      if (localStorage.getItem("locale")) {
        localObject = { locale: localStorage.getItem("locale") || this.$i18n.fallbackLocale.toString() };
      }
      localObject.locale = localObject.locale.replaceAll("_", "-");
      // script handling
      let splittedLocale = localObject.locale.split("-");
      if (splittedLocale.length > 2) {
        localObject.locale = splittedLocale[0] + "-" + splittedLocale[2].replaceAll("#", "") + "-" + splittedLocale[1];
      }
      await this.$http
        .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + "3.0/menu/enduser?locale=" + encodeURIComponent(localObject.locale))
        .then((response: AxiosResponse<any>) => {
          this.technicalUserFunctionalities = response.data.technicalUserFunctionalities;

          this.setConditionedVisibility(response.data.allowedUserFunctionalities);

          this.dynamicUserFunctionalities = response.data.dynamicUserFunctionalities.sort((el1, el2) => {
            return el1.prog - el2.prog;
          });
          if (this.dynamicUserFunctionalities.length > 0) {
            let homePage = this.findHomePage(this.dynamicUserFunctionalities) || {};
            if (homePage && Object.keys(homePage).length !== 0) {
              if (!this.stateHomePage.label) {
                this.$store.commit("setHomePage", homePage);
              }
            }else this.$store.commit("setHomePage", {loading:false})
          }else this.$store.commit("setHomePage", {loading:false})
          let responseCommonUserFunctionalities = response.data.commonUserFunctionalities;
          for (var index in responseCommonUserFunctionalities) {
            let item = responseCommonUserFunctionalities[index];
            item.visible = this.isItemToDisplay(item);
            if (parseInt(index) == 0 && this.stateHomePage?.to) item.to = this.stateHomePage.to.replaceAll("\\/", "/");
            this.commonUserFunctionalities.push(item);
          }
          this.updateNewsAndDownload();
        })
        .catch(() => {
          if (recursive) this.logout();
          else this.loadMenu(true);
        })
        .finally(() => {
          this.$store.commit("setLoading", false);
          this.getDimensions();
        });
    },
    setConditionedVisibility(responseAllowedUserFunctionalities) {
      this.allowedUserFunctionalities = [];
      for (var idx in responseAllowedUserFunctionalities) {
        let item = responseAllowedUserFunctionalities[idx];
        item.visible = this.isItemToDisplay(item);
        this.allowedUserFunctionalities.push(item);
      }
    },
    deleteTimer() {
      clearTimeout(this.hoverTimer);
    },
  },

  async mounted() {
    await this.loadMenu();
  },
  unmounted() {
    window.removeEventListener("resize", this.getDimensions);
  },
  computed: {
    ...mapState({
      user: "user",
      downloads: "downloads",
      locale: "locale",
      news: "news",
      stateHomePage: "homePage",
      isEnterprise: "isEnterprise",
      licenses: "licenses",
      configurations: "configurations",
      menuOpened: "menuOpened",
    }),
  },
  watch: {
    news() {
      let orig = JSON.parse(JSON.stringify(this.allowedUserFunctionalities));
      this.setConditionedVisibility(orig);
    },
    closeMenu(newProp) {
      //@ts-ignore
      if (newProp) this.$refs.menu.hide();
    },
    menuOpened(newProp) {
      if (newProp === false) {
        // @ts-ignore
        this.$refs.menu.hide();
      }
    },
  },
});
</script>

<style lang="scss" scoped>
.slide-down-enter-active,
.slide-down-leave-active {
  overflow: hidden;
  transition: max-height 0.6s ease-in-out;
  max-height: 500px;
}
.slide-down-enter-from,
.slide-down-leave-to {
  max-height: 0;
}
.p-scrollpanel:deep(.p-scrollpanel-content) {
  padding: 0 0 18px 0;
}
@-moz-document url-prefix() {
  .p-scrollpanel:deep(.p-scrollpanel-content) {
    padding: 0 18px 18px 0;
  }
}
.itemSection {
  cursor: pointer;
}
.layout-menu-container {
  z-index: 9000;
  width: var(--kn-mainmenu-width);
  top: 0;
  background-color: var(--kn-mainmenu-background-color);
  height: 100%;
  position: fixed;
  .menu-scroll-content {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
  .profile {
    height: 60px;
    padding: 8px;
    box-shadow: var(--kn-mainmenu-profile-box-shadow);
    & > button {
      cursor: pointer;
      width: 100%;
      font-size: 14px;
      font-family: var(--kn-font-family);
      .profile-image {
        width: 45px;
        height: 45px;
        float: right;
        margin-left: 4px;
        border-radius: var(--kn-mainmenu-avatar-border-radius);
        border: 2px solid var(--kn-mainmenu-highlight-color);
        background-color: var(--kn-mainmenu-avatar-background-color);
      }
      .profile-name,
      .profile-role,
      i {
        display: none;
      }
    }
  }
  .profile-menu {
    border-bottom: 1px solid var(--kn-mainmenu-hover-background-color);
  }
  .layout-menu {
    margin: 0;
    padding: 0;
    list-style: none;
    li {
      &:first-child {
        padding-top: 10px;
      }
    }
    & > li {
      position: relative;
      & > a {
        text-align: center;
        padding: 15px;
        color: var(--kn-mainmenu-icon-color);
        display: block;
        width: 100%;
        transition: background-color 0.3s, border-left-color 0.3s;
        overflow: hidden;
        border-left: 4px solid transparent;
        outline: none;
        cursor: pointer;
        user-select: none;
        span {
          display: none;
        }
        &:hover {
          background-color: var(--kn-mainmenu-hover-background-color);
        }
      }
      & > span {
        text-decoration: none;
        text-align: center;
        padding: 15px;
        padding-left: 12px;
        color: var(--kn-mainmenu-icon-color);
        display: block;
        width: 100%;
        transition: background-color 0.3s, border-left-color 0.3s;
        overflow: hidden;
        border-left: 4px solid transparent;
        outline: none;
        cursor: pointer;
        user-select: none;
        &:hover {
          background-color: var(--kn-mainmenu-hover-background-color);
        }
        &.router-link-active {
          border-left: 3px solid var(--kn-mainmenu-highlight-color);
        }
      }
    }
    &.scrollable {
      overflow-y: auto;
      overflow-x: hidden;
    }
  }
}
</style>
