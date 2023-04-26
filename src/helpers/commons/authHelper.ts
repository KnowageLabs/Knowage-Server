import mainStore from '../../App.store'

export default {
    logout() {
        const store = mainStore()
        localStorage.clear()
        store.setUser({})
        let url = window.location.origin
        window.location.href = url + '/knowage/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION&LIGHT_NAVIGATOR_DISABLED=TRUE&NEW_SESSION=TRUE'
    },
    handleUnauthorized() {
        const store = mainStore()
        localStorage.clear()
        store.setUser({})
        let url = window.location.origin
        window.location.href = url + '/knowage/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE'
    }
}
