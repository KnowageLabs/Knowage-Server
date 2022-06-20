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
</template>

<script>
import ConfirmDialog from 'primevue/confirmdialog'
import KnOverlaySpinnerPanel from '@/components/UI/KnOverlaySpinnerPanel.vue'
import MainMenu from '@/modules/mainMenu/MainMenu'
import Toast from 'primevue/toast'
import { defineComponent } from 'vue'
import store from '@/App.store'
import { mapState } from 'vuex'
import WEB_SOCKET from '@/services/webSocket.js'
import themeHelper from '@/helpers/themeHelper/themeHelper'
import { primeVueDate, getLocale } from '@/helpers/commons/localeHelper'

export default defineComponent({
    components: { ConfirmDialog, KnOverlaySpinnerPanel, MainMenu, Toast },

    data() {
        return {
            themeHelper: new themeHelper(),
            selectedMenuItem: null,
            menuItemClickedTrigger: false
        }
    },

    async beforeCreate() {
        await this.$http
            .get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/currentuser')
            .then((response) => {
                let currentUser = response.data
                if (localStorage.getItem('sessionRole')) {
                    currentUser.sessionRole = localStorage.getItem('sessionRole')
                } else if (currentUser.defaultRole) currentUser.sessionRole = currentUser.defaultRole

                store.dispatch('initializeUser', currentUser)

                let responseLocale = response.data.locale
                let storedLocale = responseLocale
                if (localStorage.getItem('locale')) {
                    storedLocale = localStorage.getItem('locale')
                }
                localStorage.setItem('locale', storedLocale)
                localStorage.setItem('token', response.data.userUniqueIdentifier)

                store.commit('setLocale', storedLocale)
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

                this.$store.commit('setLoading', true)
                this.$http.get(url).then(
                    () => {
                        store.commit('setLocale', language.locale)
                        localStorage.setItem('locale', language.locale)
                        this.$i18n.locale = language.locale
                    },
                    (error) => console.error(error)
                )
                this.$store.commit('setLoading', false)
            })
            .catch(function(error) {
                if (error.response) {
                    console.log(error.response.data)
                    console.log(error.response.status)
                    console.log(error.response.headers)
                }
            })
        await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/user-configs').then((response) => {
            store.commit('setConfigurations', response.data)
        })
        if (this.isEnterprise) {
            if (Object.keys(this.defaultTheme.length === 0)) store.commit('setDefaultTheme', await this.themeHelper.getDefaultKnowageTheme())

            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/license').then((response) => {
                store.commit('setLicenses', response.data)
            })
            if (Object.keys(this.theme).length === 0) {
                this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `thememanagement/current`).then((response) => {
                    store.commit('setTheme', response.data.config)
                    this.themeHelper.setTheme(response.data.config)
                })
            } else {
                this.themeHelper.setTheme(this.theme)
            }
        }
    },
    mounted() {
        this.onLoad()
    },

    methods: {
        closeDialog() {
            this.$emit('update:visibility', false)
        },
        async onLoad() {
            await this.$http
                .get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/export/dataset')
                .then((response) => {
                    let totalDownloads = response.data.length
                    let alreadyDownloaded = response.data.filter((x) => x.alreadyDownloaded).length

                    let json = { downloads: { count: { total: 0, alreadyDownloaded: 0 } } }
                    json.downloads.count.total = totalDownloads
                    json.downloads.count.alreadyDownloaded = alreadyDownloaded

                    store.commit('setDownloads', json.downloads)

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
            let currentLocale = localStorage.getItem('locale') ? localStorage.getItem('locale') : store.state.locale
            let currLanguage = ''
            if (currentLocale && Object.keys(currentLocale).length > 0) currentLocale = currentLocale.replaceAll('_', '-')
            else currentLocale = 'en-US'

            let splittedLanguage = currentLocale.split('-')
            currLanguage += splittedLanguage[0] + '-'
            if (splittedLanguage.length > 2) currLanguage += splittedLanguage[2].replaceAll('#', '') + '-'
            currLanguage += splittedLanguage[1].toUpperCase()

            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/i18nMessages/internationalization?currLanguage=' + currLanguage).then((response) => store.commit('setInternationalization', response.data))
        },
        newsDownloadHandler() {
            console.log('Starting connection to WebSocket Server')

            WEB_SOCKET.update = function(event) {
                if (event.data) {
                    let json = JSON.parse(event.data)
                    if (json.news) {
                        store.commit('setNews', json.news)
                    }
                    if (json.downloads) {
                        store.commit('setDownloads', json.downloads)
                    }
                }
            }
            WEB_SOCKET.onopen = function(event) {
                if (event.data) {
                    let json = JSON.parse(event.data)
                    if (json.news) {
                        store.commit('setNews', json.news)
                    }
                    if (json.downloads) {
                        store.commit('setDownloads', json.downloads)
                    }
                }
            }
            WEB_SOCKET.onmessage = function(event) {
                if (event.data) {
                    let json = JSON.parse(event.data)
                    if (json.news) {
                        store.commit('setNews', json.news)
                    }
                    if (json.downloads) {
                        store.commit('setDownloads', json.downloads)
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
        ...mapState({
            error: 'error',
            info: 'info',
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
                life: typeof newError.duration == 'undefined' ? import.meta.env.VUE_APP_TOAST_DURATION : newError.duration
            })
        },
        info(newInfo) {
            this.$toast.add({
                severity: 'info',
                summary: newInfo.title ? this.$t(newInfo.title) : '',
                detail: newInfo.msg ? this.$t(newInfo.msg) : '',
                baseZIndex: typeof newInfo.baseZIndex == 'undefined' ? 0 : newInfo.baseZIndex,
                life: typeof newInfo.duration == 'undefined' ? import.meta.env.VUE_APP_TOAST_DURATION : newInfo.duration
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
    margin-left: 58px;
    flex: 1;
}
</style>
