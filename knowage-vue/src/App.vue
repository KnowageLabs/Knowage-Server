<template>
    <Toast></Toast>
    <ConfirmDialog></ConfirmDialog>
    <KnOverlaySpinnerPanel />
    <div class="layout-wrapper-content" :class="{ 'layout-wrapper-content-embed': documentExecution.embed }">
        <MainMenu @menuItemSelected="setSelectedMenuItem"></MainMenu>

        <div class="layout-main">
            <router-view :selectedMenuItem="selectedMenuItem" :menuItemClickedTrigger="menuItemClickedTrigger" />
        </div>
    </div>
    <KnRotate v-show="isMobileDevice"></KnRotate>
</template>

<script lang="ts">
import ConfirmDialog from 'primevue/confirmdialog'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import KnRotate from '@/components/UI/KnRotate.vue'
import MainMenu from '@/modules/mainMenu/MainMenu'
import Toast from 'primevue/toast'
import { defineComponent } from 'vue'
import mainStore from '@/App.store'
import { mapState, mapActions } from 'pinia'
import WEB_SOCKET from '@/services/webSocket.js'
import themeHelper from '@/helpers/themeHelper/themeHelper'
import { primeVueDate, getLocale } from '@/helpers/commons/localeHelper'

export default defineComponent({
    components: { ConfirmDialog, KnOverlaySpinnerPanel, KnRotate, MainMenu, Toast },

    data() {
        return {
            themeHelper: new themeHelper(),
            selectedMenuItem: null,
            isMobileDevice: false,
            menuItemClickedTrigger: false
        }
    },

    async beforeCreate() {
        await this.$http
            .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/currentuser')
            .then((response) => {
                let currentUser = response.data
                if (localStorage.getItem('sessionRole')) {
                    currentUser.sessionRole = localStorage.getItem('sessionRole')
                } else if (currentUser.defaultRole) currentUser.sessionRole = currentUser.defaultRole

                this.store.initializeUser(currentUser)

                let responseLocale = response.data.locale
                let storedLocale = responseLocale
                if (localStorage.getItem('locale')) {
                    storedLocale = localStorage.getItem('locale')
                }
                localStorage.setItem('locale', storedLocale)
                localStorage.setItem('token', response.data.userUniqueIdentifier)

                this.store.setLocale(storedLocale)
                this.$i18n.locale = storedLocale

                // @ts-ignore
                if (this.$i18n.messages[this.$i18n.locale.replaceAll('-', '_')]) {
                    // @ts-ignore
                    this.$primevue.config.locale = { ...this.$primevue.config.locale, ...this.$i18n.messages[this.$i18n.locale.replaceAll('-', '_')].locale }
                }
                this.$primevue.config.locale.dateFormat = primeVueDate(getLocale(true))

                let language = this.$i18n
                let splittedLanguage = language.locale.split('_')

                let url = '/knowage/servlet/AdapterHTTP?'
                url += 'ACTION_NAME=CHANGE_LANGUAGE'
                url += '&LANGUAGE_ID=' + splittedLanguage[0]
                url += '&COUNTRY_ID=' + splittedLanguage[1].toUpperCase()
                url += '&SCRIPT_ID=' + (splittedLanguage.length > 2 ? splittedLanguage[2].replaceAll('#', '') : '')
                url += '&THEME_NAME=sbi_default'

                this.store.setLoading(true)
                this.$http.get(url).then(
                    () => {
                        this.store.setLocale(language.locale)
                        localStorage.setItem('locale', language.locale)
                        this.$i18n.locale = language.locale
                    },
                    (error) => console.error(error)
                )
                this.store.setLoading(false)
            })
            .catch(function(error) {
                if (error.response) {
                    console.log(error.response.data)
                    console.log(error.response.status)
                    console.log(error.response.headers)
                }
            })
        await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/user-configs').then((response) => {
            this.store.setConfigurations(response.data)
        })
        if (this.isEnterprise) {
            if (Object.keys(this.defaultTheme.length === 0)) this.store.setDefaultTheme(await this.themeHelper.getDefaultKnowageTheme())

            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/license').then((response) => {
                this.store.setLicenses(response.data)
            })
            if (Object.keys(this.theme).length === 0) {
                this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `thememanagement/current`).then((response) => {
                    this.store.setTheme(response.data.config)
                    this.themeHelper.setTheme(response.data.config)
                })
            } else {
                this.themeHelper.setTheme(this.theme)
            }
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    mounted() {
        this.onLoad()
        if (/Android|iPhone/i.test(navigator.userAgent)) {
            this.isMobileDevice = true
        }
    },

    methods: {
        closeDialog() {
            this.$emit('update:visibility', false)
        },
        async onLoad() {
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/export/dataset')
                .then((response) => {
                    let totalDownloads = response.data.length
                    let alreadyDownloaded = response.data.filter((x) => x.alreadyDownloaded).length

                    let json = { downloads: { count: { total: 0, alreadyDownloaded: 0 } } }
                    json.downloads.count.total = totalDownloads
                    json.downloads.count.alreadyDownloaded = alreadyDownloaded

                    this.store.setDownloads(json.downloads)

                    this.newsDownloadHandler()
                    this.loadInternationalization()
                })
                .catch(function(error) {
                    if (error.response) {
                        console.log(error.response.data)
                        console.log(error.response.status)
                        console.log(error.response.headers)
                    }
                })
        },
        async loadInternationalization() {
            let currentLocale = localStorage.getItem('locale') ? localStorage.getItem('locale') : this.store.$state.locale
            let currLanguage = ''
            if (currentLocale && Object.keys(currentLocale).length > 0) currentLocale = currentLocale.replaceAll('_', '-')
            else currentLocale = 'en-US'

            let splittedLanguage = currentLocale.split('-')
            currLanguage += splittedLanguage[0] + '-'
            if (splittedLanguage.length > 2) currLanguage += splittedLanguage[2].replaceAll('#', '') + '-'
            currLanguage += splittedLanguage[1].toUpperCase()

            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/i18nMessages/internationalization?currLanguage=' + currLanguage).then((response) => this.store.setInternationalization(response.data))
        },
        newsDownloadHandler() {
            console.log('Starting connection to WebSocket Server')
            const store = this.store

            WEB_SOCKET.update = function(event) {
                if (event.data) {
                    let json = JSON.parse(event.data)
                    if (json.news) {
                        store.setNews(json.news)
                    }
                    if (json.downloads) {
                        store.setDownloads(json.downloads)
                    }
                }
            }
            WEB_SOCKET.onopen = function(event) {
                if (event.data) {
                    let json = JSON.parse(event.data)
                    if (json.news) {
                        this.store.setNews(json.news)
                    }
                    if (json.downloads) {
                        this.store.setDownloads(json.downloads)
                    }
                }
            }
            WEB_SOCKET.onmessage = function(event) {
                if (event.data) {
                    let json = JSON.parse(event.data)
                    if (json.news) {
                        store.setNews(json.news)
                    }
                    if (json.downloads) {
                        store.setDownloads(json.downloads)
                    }
                }
            }
        },
        setSelectedMenuItem(menuItem) {
            this.selectedMenuItem = menuItem
            this.menuItemClickedTrigger = !this.menuItemClickedTrigger
        }
    },
    computed: {
        ...mapState(mainStore, {
            error: 'error',
            info: 'info',
            warning: 'warning',
            user: 'user',
            loading: 'loading',
            isEnterprise: 'isEnterprise',
            documentExecution: 'documentExecution',
            theme: 'theme',
            defaultTheme: 'defaultTheme'
        })
    },
    watch: {
        error(newError) {
            this.$toast.add({
                severity: 'error',
                summary: newError.title ? this.$t(newError.title) : '',
                detail: newError.msg ? this.$t(newError.msg) : '',
                baseZIndex: typeof newError.baseZIndex == 'undefined' ? 0 : newError.baseZIndex,
                life: typeof newError.duration == 'undefined' ? import.meta.env.VITE_TOAST_DURATION : newError.duration
            })
        },
        info(newInfo) {
            this.$toast.add({
                severity: 'info',
                summary: newInfo.title ? this.$t(newInfo.title) : '',
                detail: newInfo.msg ? this.$t(newInfo.msg) : '',
                baseZIndex: typeof newInfo.baseZIndex == 'undefined' ? 0 : newInfo.baseZIndex,
                life: typeof newInfo.duration == 'undefined' ? import.meta.env.VITE_TOAST_DURATION : newInfo.duration
            })
        },
        warning(newWarning) {
            this.$toast.add({
                severity: 'warn',
                summary: newWarning.title ? this.$t(newWarning.title) : '',
                detail: newWarning.msg ? this.$t(newWarning.msg) : '',
                baseZIndex: typeof newWarning.baseZIndex == 'undefined' ? 0 : newWarning.baseZIndex,
                life: typeof newWarning.duration == 'undefined' ? import.meta.env.VUE_APP_TOAST_DURATION : newWarning.duration
            })
        },
        user() {
            /* if (!oldUser.userId && oldUser != newUser)  */
        }
    }
})
</script>

<style lang="scss">
html {
    font-size: var(--kn-font-size);
}
body {
    padding: 0;
    margin: 0;
    font-family: var(--kn-font-family);
}
.layout-wrapper-content {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    min-height: 100vh;
}
.layout-wrapper-content::after {
    content: '';
    display: table; /* NOTE: Display "block" does not seem to work with height: 0px. */
    height: 0px;
}
.layout-main {
    margin-left: var(--kn-mainmenu-width);
    flex: 1;
}
</style>
