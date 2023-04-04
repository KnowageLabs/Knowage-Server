<template>
    <div id="kn-main-menu" ref="mainMenu" class="layout-menu-container">
        <InfoDialog v-model:visibility="display"></InfoDialog>
        <LanguageDialog v-model:visibility="languageDisplay"></LanguageDialog>
        <RoleDialog v-model:visibility="roleDisplay"></RoleDialog>
        <DownloadsDialog v-model:visibility="downloadsDisplay"></DownloadsDialog>
        <NewsDialog v-model:visibility="newsDisplay"></NewsDialog>
        <LicenseDialog v-if="user && user.isSuperadmin && isEnterprise" v-model:visibility="licenseDisplay"></LicenseDialog>
        <MainMenuAdmin v-if="technicalUserFunctionalities && technicalUserFunctionalities.length > 0" :opened-panel-event="adminMenuOpened" :model="technicalUserFunctionalities" @click="itemClick"></MainMenuAdmin>
        <TieredMenu ref="menu" :class="['kn-tieredMenu', tieredMenuClass]" :model="selectedCustomMenu" :popup="true" @blur="hideItemMenu" @mouseleave="checkTimer">
            <template #item="{ item }">
                <router-link v-if="item.to" class="p-menuitem-link" :to="cleanTo(item)" exact @click="itemClick(item)">
                    <span v-if="item.descr" v-tooltip.top="item.descr" class="p-menuitem-text kn-truncated">{{ $internationalization($t(item.descr)) }}</span>
                    <span v-else v-tooltip.top="$internationalization($t(item.label))" class="p-menuitem-text kn-truncated">{{ $internationalization($t(item.label)) }}</span>
                    <span v-if="item.items" class="p-submenu-icon pi pi-angle-right kn-truncated"></span>
                </router-link>
                <a v-else class="p-menuitem-link" :target="item.target" role="menuitem" :tabindex="item.disabled ? null : '0'" @click="itemClick(item)">
                    <span v-if="item.descr" v-tooltip.top="item.descr" class="p-menuitem-text kn-truncated">{{ $internationalization($t(item.descr)) }}</span>
                    <span v-else v-tooltip.top="$internationalization($t(item.label))" class="p-menuitem-text kn-truncated">{{ $internationalization($t(item.label)) }}</span>
                    <span v-if="item.items" class="p-submenu-icon pi pi-angle-right kn-truncated"></span>
                </a>
            </template>
        </TieredMenu>

        <div class="menu-scroll-content">
            <div ref="menuProfile" class="profile">
                <button v-tooltip="user && user.fullName" class="p-link" @click="toggleProfile">
                    <img alt="Profile" class="profile-image" :src="getProfileImage(user)" />
                    <span v-if="user" class="profile-name">{{ user.fullName }}</span>
                    <i class="pi pi-fw pi-chevron-down"></i>
                    <span class="profile-role">Marketing</span>
                </button>
            </div>
            <transition name="slide-down">
                <ul v-show="showProfileMenu" ref="menuProfileSlide" class="layout-menu profile-menu">
                    <template v-for="(item, i) of commonUserFunctionalities" :key="i">
                        <template v-if="item">
                            <MainMenuItem :item="item" @click="itemClick"></MainMenuItem>
                        </template>
                    </template>
                </ul>
            </transition>
            <ScrollPanel :style="{ height: menuDimensions }">
                <ul class="layout-menu">
                    <li v-if="technicalUserFunctionalities && technicalUserFunctionalities.length > 0" role="menu" @click="toggleAdminMenu">
                        <span :class="['p-menuitem-icon', 'fas fa-cog']"></span>
                    </li>
                    <template v-for="(item, i) of allowedUserFunctionalities" :key="i">
                        <MainMenuItem :item="item" :badge="getBadgeValue(item)" @click="itemClick" @mouseover="toggleMenu($event, item)"></MainMenuItem>
                    </template>
                    <template v-for="(item, i) of dynamicUserFunctionalities" :key="i">
                        <MainMenuItem :item="item" :internationalize="true" @click="itemClick" @mouseover="toggleMenu($event, item)"></MainMenuItem>
                    </template>
                </ul>
            </ScrollPanel>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import InfoDialog from '@/modules/mainMenu/dialogs/InfoDialog.vue'
import MainMenuItem from '@/modules/mainMenu/MainMenuItem.vue'
import MainMenuAdmin from '@/modules/mainMenu/MainMenuAdmin.vue'
import LanguageDialog from '@/modules/mainMenu/dialogs/LanguageDialog/LanguageDialog.vue'
import LicenseDialog from '@/modules/mainMenu/dialogs/LicenseDialog/LicenseDialog.vue'
import NewsDialog from '@/modules/mainMenu/dialogs/NewsDialog/NewsDialog.vue'
import RoleDialog from '@/modules/mainMenu/dialogs/RoleDialog.vue'
import { mapState, mapActions } from 'pinia'
import auth from '@/helpers/commons/authHelper'
import { AxiosResponse } from 'axios'
import DownloadsDialog from '@/modules/mainMenu/dialogs/DownloadsDialog/DownloadsDialog.vue'
import { IMenuItem } from '@/modules/mainMenu/MainMenu'
import TieredMenu from 'primevue/tieredmenu'
import ScrollPanel from 'primevue/scrollpanel'
import mainStore from '../../App.store'

export default defineComponent({
    name: 'knmenu',
    components: {
        InfoDialog,
        MainMenuAdmin,
        MainMenuItem,
        LanguageDialog,
        LicenseDialog,
        NewsDialog,
        RoleDialog,
        DownloadsDialog,
        TieredMenu,
        ScrollPanel
    },
    emits: ['update:visibility', 'menuItemSelected'],
    data() {
        return {
            adminMenuOpened: false,
            menuDimensions: 0,
            showProfileMenu: false,
            dynamicUserFunctionalities: new Array<IMenuItem>(),
            allowedUserFunctionalities: new Array<IMenuItem>(),
            commonUserFunctionalities: new Array<IMenuItem>(),
            technicalUserFunctionalities: new Array<IMenuItem>(),
            tieredMenuClass: 'largeScreen',
            display: false,
            languageDisplay: false,
            roleDisplay: false,
            downloadsDisplay: false,
            newsDisplay: false,
            licenseDisplay: false,
            selectedCustomMenu: {},
            hoverTimer: false as any,
            publicPath: import.meta.env.VITE_PUBLIC_PATH
        }
    },
    async mounted() {
        await this.loadMenu()
    },
    unmounted() {
        window.removeEventListener('resize', this.getDimensions)
    },
    methods: {
        ...mapActions(mainStore, ['setHomePage', 'setLoading']),
        info() {
            this.display = !this.display
        },
        logout() {
            auth.logout()
        },
        roleSelection() {
            this.roleDisplay = !this.roleDisplay
        },
        downloadsSelection() {
            this.downloadsDisplay = !this.downloadsDisplay
        },
        isItemToDisplay(item) {
            if (item.conditionedView) {
                if (item.conditionedView === 'downloads' && this.downloads && this.downloads.count.total > 0) return true
                if (item.conditionedView === 'news' && this.news && this.news.count.total > 0) return true
                if (item.conditionedView === 'roleSelection' && this.user && this.user.roles && this.user.roles.length > 1) return true
                return false
            } else {
                return true
            }
        },
        languageSelection() {
            this.languageDisplay = !this.languageDisplay
        },
        checkTimer() {
            clearTimeout(this.hoverTimer)
            this.hoverTimer = setTimeout(() => {
                // @ts-ignore
                this.$refs.menu.hide()
            }, import.meta.env.VITE_MENU_FADE_TIMER)
        },
        newsSelection() {
            this.newsDisplay = !this.newsDisplay
        },
        licenseSelection() {
            this.licenseDisplay = !this.licenseDisplay
        },
        itemClick(event) {
            const item = event.item ? event.item : event
            if (item.command) {
                this[item.command]()
            } else if (item.to && event.navigate) {
                event.navigate(event.originalEvent)
                this.$emit('menuItemSelected', item)
            } else if (item.url && (!item.target || item.target === 'insideKnowage')) this.$router.push({ name: 'externalUrl', params: { url: item.url } })
            if (this.adminMenuOpened) this.adminMenuOpened = false
        },
        getHref(item) {
            let to = item.to
            if (to) {
                to = to.replace(/\\\//g, '/')
                if (to.startsWith('/')) to = to.substring(1)
                return import.meta.env.VITE_PUBLIC_PATH + to
            }
        },
        toggleProfile() {
            this.showProfileMenu = !this.showProfileMenu
        },
        toggleAdminMenu(event) {
            this.adminMenuOpened = this.adminMenuOpened === false ? event : false
        },
        getProfileImage(user) {
            if (user && user.organizationImageb64) return user.organizationImageb64
            return this.publicPath + '/images/commons/logo_knowage.svg'
        },
        updateNewsAndDownload() {
            for (const idx in this.allowedUserFunctionalities) {
                const menu = this.allowedUserFunctionalities[idx] as any
                if (menu.conditionedView) {
                    if (menu.conditionedView === 'downloads') {
                        menu.visible = this.downloads.count.total > 0
                    } else if (menu.conditionedView === 'news') {
                        menu.visible = this.news.count.total > 0
                    }
                    menu.badge = this.getBadgeValue(menu)
                }
            }
        },
        getBadgeValue(item) {
            if (item.conditionedView === 'downloads') {
                if (Object.keys(this.downloads).length !== 0) return this.downloads.count.total - this.downloads.count.alreadyDownloaded
            } else if (item.conditionedView === 'news') {
                if (Object.keys(this.news).length !== 0) return this.news.count.unread
            }
            return 0
        },
        findHomePage(dynMenu) {
            const toRet = undefined
            for (const idx in dynMenu) {
                const menu = dynMenu[idx]
                if (this.user.sessionRole) {
                    if (menu.roles.includes(this.user.sessionRole) && (menu.to || menu.url)) return menu
                } else {
                    for (let i = 0; i < this.user.roles.length; i++) {
                        const element = this.user.roles[i]
                        if (menu.roles.includes(element) && (menu.to || menu.url)) {
                            return menu
                        }
                    }
                }
            }
            return toRet
        },
        toggleMenu(event, item) {
            if (item.items) {
                clearTimeout(this.hoverTimer)
                this.selectedCustomMenu = item.items
                if (event.target.getBoundingClientRect().bottom + Object.keys(this.selectedCustomMenu).length * 40 > window.innerHeight) {
                    this.tieredMenuClass = 'smallScreen'
                } else this.tieredMenuClass = 'largeScreen'
                // @ts-ignore
                this.$refs.menu.show(event)
            } else {
                // @ts-ignore
                this.$refs.menu.hide()
            }
        },
        hideItemMenu() {
            // @ts-ignore
            this.$refs.menu.hide()
        },
        getDimensions() {
            if (this.$refs && this.$refs.mainMenu)
                //@ts-ignore
                this.menuDimensions = this.$refs.mainMenu.getBoundingClientRect().height - this.$refs.menuProfile.getBoundingClientRect().height - this.$refs.menuProfileSlide.getBoundingClientRect().height + 'px'
        },
        cleanTo(item): any {
            return item.to.replace(/\\\//g, '/')
        },
        async loadMenu(recursive = false) {
            window.addEventListener('resize', this.getDimensions)
            this.setLoading(true)
            let localObject = { locale: this.$i18n.fallbackLocale.toString() }
            if (Object.keys(this.locale).length !== 0) localObject = { locale: this.locale }
            if (localStorage.getItem('locale')) {
                localObject = { locale: localStorage.getItem('locale') || this.$i18n.fallbackLocale.toString() }
            }
            localObject.locale = localObject.locale.replaceAll('_', '-')
            // script handling
            const splittedLocale = localObject.locale.split('-')
            if (splittedLocale.length > 2) {
                localObject.locale = splittedLocale[0] + '-' + splittedLocale[2].replaceAll('#', '') + '-' + splittedLocale[1]
            }
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '3.0/menu/enduser?locale=' + encodeURIComponent(localObject.locale))
                .then((response: AxiosResponse<any>) => {
                    this.technicalUserFunctionalities = response.data.technicalUserFunctionalities
                    this.setConditionedVisibility(response.data.allowedUserFunctionalities)
                    this.dynamicUserFunctionalities = response.data.dynamicUserFunctionalities.sort((el1, el2) => {
                        return el1.prog - el2.prog
                    })
                    if (this.dynamicUserFunctionalities.length > 0) {
                        const homePage = this.findHomePage(this.dynamicUserFunctionalities) || {}
                        if (homePage && Object.keys(homePage).length !== 0) {
                            if (!this.stateHomePage.label) {
                                this.setHomePage(homePage)
                            }
                        }
                    }
                    const responseCommonUserFunctionalities = response.data.commonUserFunctionalities
                    for (const index in responseCommonUserFunctionalities) {
                        const item = responseCommonUserFunctionalities[index]
                        item.visible = this.isItemToDisplay(item)
                        if (parseInt(index) == 0 && this.stateHomePage?.to) item.to = this.stateHomePage.to.replaceAll('\\/', '/')
                        this.commonUserFunctionalities.push(item)
                    }
                    this.updateNewsAndDownload()
                })
                .catch(() => {
                    if (recursive) this.logout()
                    else this.loadMenu(true)
                })
                .finally(() => {
                    this.setLoading(false)
                    this.getDimensions()
                })
        },
        setConditionedVisibility(responseAllowedUserFunctionalities) {
            this.allowedUserFunctionalities = []
            for (const idx in responseAllowedUserFunctionalities) {
                const item = responseAllowedUserFunctionalities[idx]
                item.visible = this.isItemToDisplay(item)
                this.allowedUserFunctionalities.push(item)
            }
        }
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user',
            downloads: 'downloads',
            locale: 'locale',
            news: 'news',
            stateHomePage: 'homePage',
            isEnterprise: 'isEnterprise',
            licenses: 'licenses'
        })
    },
    watch: {
        news() {
            const orig = JSON.parse(JSON.stringify(this.allowedUserFunctionalities))
            this.setConditionedVisibility(orig)
        }
    }
})
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
.p-tieredmenu {
    padding: 0;
    border: none;
    border-radius: 0;
}
</style>
