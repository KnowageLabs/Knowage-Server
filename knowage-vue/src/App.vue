<template>
  <Toast></Toast>
  <ConfirmDialog></ConfirmDialog>
  <div class="layout-wrapper-content">
    <Knmenu :model="menu"></Knmenu>

    <div class="layout-main">
      <router-view />
    </div>
  </div>
</template>

<script>
import ConfirmDialog from 'primevue/confirmdialog'
import Knmenu from '@/components/knmenu/KnMenu'
import Toast from 'primevue/toast'
import { defineComponent } from 'vue'
import store from '@/App.store'
import { mapState } from 'vuex'
import { concatLocale } from '@/helpers/localeHelper'

export default defineComponent({
  components: {
    ConfirmDialog,
    Knmenu,
    Toast
  },
  created() {
    this.axios.get('/knowage/restful-services/3.0/users/current').then(
      (response) => {
        store.commit('setUser', response.data)

        let storedLocale = response.data.locale
        if (localStorage.getItem('locale')) {
          storedLocale = JSON.parse(localStorage.getItem('locale'))
        }
        localStorage.setItem('locale', JSON.stringify(storedLocale))
        localStorage.setItem('token', response.data.token)
        store.commit('setLocale', storedLocale)
        this.$i18n.locale = concatLocale(storedLocale)
      },
      (error) => console.error(error)
    )
  },
  methods: {
    newsDownloadHandler() {
      console.log('Starting connection to WebSocket Server')
      var uri = process.env.VUE_APP_WEBSOCKET_URL

      this.connection = new WebSocket(uri)
      this.connection.onmessage = function(event) {
        if (event.data) {
          let json = JSON.parse(event.data)
          if (json.news) {
            console.log('Total news', json.news.count.total)
            console.log('Unread news', json.news.count.unread)
            store.commit('setNews', json.news.count.total > 0)
          }

          store.commit('setDownload', json.downloads.count > 0)
        }
      }

      this.connection.onopen = function(event) {
        console.log('Connected')
        console.log(event)
      }
    }
  },
  computed: {
    ...mapState({
      error: 'error',
      user: 'user'
    })
  },
  watch: {
    error(newError) {
      if (newError.visible) {
        this.$toast.add({
          severity: 'error',
          summary: 'Error Message',
          detail: newError.msg,
          life: 3000
        })
      }
    },
    user(newUser, oldUser) {
      if (!oldUser.userId && oldUser != newUser) this.newsDownloadHandler()
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
.layout-main {
  margin-left: 58px;
  flex: 1;
}
</style>
