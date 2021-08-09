<template>
    <Toast></Toast>
    <ConfirmDialog></ConfirmDialog>
    <KnOverlaySpinnerPanel :visibility="loading" />
    <div class="layout-wrapper-content">
        <MainMenu></MainMenu>

        <div class="layout-main">
            <router-view />
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
import axios from 'axios'
import WEB_SOCKET from '@/services/webSocket.js'

export default defineComponent({
    components: {
        ConfirmDialog,
        KnOverlaySpinnerPanel,
        MainMenu,
        Toast
    },

    beforeMount() {
        axios
            .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/currentuser')
            .then((response) => {
                store.commit('setUser', response.data)

                let responseLocale = response.data.locale
                let storedLocale = responseLocale
                if (localStorage.getItem('locale')) {
                    storedLocale = localStorage.getItem('locale')
                }
                localStorage.setItem('locale', storedLocale)
                localStorage.setItem('token', response.data.userUniqueIdentifier)
                store.commit('setLocale', storedLocale)
                this.$i18n.locale = storedLocale

                if (responseLocale != storedLocale) {
                    let language = this.$i18n
                    let splittedLanguage = language.locale.split('_')

                    let url = '/knowage/servlet/AdapterHTTP?'
                    url += 'ACTION_NAME=CHANGE_LANGUAGE'
                    url += '&LANGUAGE_ID=' + splittedLanguage[0]
                    url += '&COUNTRY_ID=' + splittedLanguage[1].toUpperCase()
                    url += '&SCRIPT_ID=' + (splittedLanguage.length > 2 ? splittedLanguage[2].replaceAll('#', '') : '')
                    url += '&THEME_NAME=sbi_default'

                    this.$emit('update:loading', true)
                    axios.get(url).then(
                        () => {
                            store.commit('setLocale', language.locale)
                            localStorage.setItem('locale', language.locale)
                            this.$i18n.locale = language.locale

                            this.closeDialog()
                            this.$router.go(0)
                            this.$forceUpdate()
                        },
                        (error) => console.error(error)
                    )

                    this.$emit('update:loading', false)
                }
            })
            .catch(function(error) {
                if (error.response) {
                    console.log(error.response.data)
                    console.log(error.response.status)
                    console.log(error.response.headers)
                }
            })
    },
    created() {
        this.newsDownloadHandler()
    },
    mounted() {
        this.newsDownloadHandler()
    },
    methods: {
        /* 			closeDialog() {
				this.$emit('update:visibility', false)
			}, */
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
                this.update(event)
            }
            WEB_SOCKET.onmessage = function(event) {
                this.update(event)
            }
        }
    },
    computed: {
        ...mapState({
            error: 'error',
            info: 'info',
            user: 'user',
            loading: 'loading'
        })
    },
    watch: {
        error(newError) {
            this.$toast.add({
                severity: 'error',
                summary: newError.title,
                detail: newError.msg,
                life: typeof newError.duration == 'undefined' ? 5000 : newError.duration
            })
        },
        info(newInfo) {
            this.$toast.add({
                severity: 'info',
                summary: newInfo.title,
                detail: newInfo.msg,
                life: typeof newInfo.duration == 'undefined' ? 5000 : newInfo.duration
            })
        },
        loading(newLoading) {
            this.loading = newLoading
        },
        user() {
            /* if (!oldUser.userId && oldUser != newUser)  */
        }
    }
})
</script>

<style lang="scss">
body {
    padding: 0;
    margin: 0;
    font-family: 'Roboto';
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
