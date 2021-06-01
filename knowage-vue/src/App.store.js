import { createStore } from 'vuex'

// Create a new store instance.
const store = createStore({
	state() {
		return {
			user: {},
			error: {},
			info: {},
			warning: {},
			downloads: {},
			locale: {},
			news: {}
		}
	},
	mutations: {
		setUser(state, user) {
			state.user = user
		},
		setError(state, error) {
			state.error = { title: error.title, msg: error.msg }
		},
		setInfo(state, info) {
			state.info = { title: info.title, msg: info.msg }
		},
		setWarning(state, warning) {
			state.warning = { title: warning.title, msg: warning.msg }
		},
		setLocale(state, locale) {
			state.locale = locale
		},
		setDownloads(state, hasDownload) {
			state.downloads = hasDownload
		},
		updateAlreadyDownloadedFiles(state) {
			if (state.downloads.count.total > state.downloads.count.alreadyDownloaded) state.downloads.count.alreadyDownloaded++
		},
		setNews(state, hasNews) {
			state.news = hasNews
		}
	}
})

export default store
