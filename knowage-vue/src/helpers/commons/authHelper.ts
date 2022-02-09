import store from '@/App.store'

export default {
    logout() {
        localStorage.clear()
        store.commit('setUser', {})
        let url = window.location.origin
        window.location.href = url + '/knowage/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE'
    },
    handleUnauthorized() {
        localStorage.clear()
        store.commit('setUser', {})
        let url = window.location.origin
        window.location.href = url + '/knowage/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE'
    }
}
