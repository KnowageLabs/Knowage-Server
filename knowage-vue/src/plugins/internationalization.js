import store from '@/App.store'

export default {
	install: (app) => {
		app.config.globalProperties.$internationalization = (key) => {
			let options = store.state.internationalization

			let el = options.find((item) => item.label === key)
			return el ? el.message : key
		}
	}
}
