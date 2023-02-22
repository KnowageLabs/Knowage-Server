import mainStore from '@/App.store'

export default {
    install: (app) => {
        const store = mainStore()
        app.config.globalProperties.$internationalization = (key) => {
            const options = store.$state.internationalization

            const el = options.find((item) => item.label === key)
            return el ? el.message : key
        }
    }
}
