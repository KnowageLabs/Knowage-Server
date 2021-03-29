import { shallowMount } from '@vue/test-utils'
import { createStore } from 'vuex'
import languageDialog from './languageDialog.vue'
import flushPromises from 'flush-promises'
import axios from 'axios'
import router from '@/App.routes.js'
import Button from 'primevue/button'

const myLanguages = ['it_IT', 'en_US', 'fr_FR', 'zh_CN#Hans']
const defaultLocale = 'en_US'

const mockWrapper = shallowMount(languageDialog, {
	propsData: {
		visibility: false,
		languages: []
	},
	global: {
		components: { Button },
		mocks: {
			$t: (msg) => msg,
			$router: router,
			$i18n: {
				locale: defaultLocale,
				fallbackLocale: defaultLocale
			},
			localObject: { locale: defaultLocale }
		}
	}
})

describe('languageDialog', () => {
	test('is loaded empty', () => {
		expect(mockWrapper.vm.languages.length).toBe(0)
		expect(mockWrapper.vm.visibility).toBe(false)
	})
})

jest.mock('axios', () => ({
	get: jest.fn(() => Promise.resolve({ data: ['it_IT', 'en_US', 'fr_FR', 'zh_CN#Hans'] }))
}))

describe('languageDialog', () => {
	test('language dialog is not populated', async () => {
		expect(mockWrapper.vm.languages.length).toBe(0)
		/* mockWrapper.vm.$emit('update:visibility', true) */
		await mockWrapper.setProps({ visibility: true })
		expect(mockWrapper.vm.visibility).toBe(true)

		expect(axios.get).toHaveBeenCalledWith('/knowage/restful-services/2.0/languages')
		await flushPromises()
		expect(mockWrapper.vm.languages.length).toBe(['it_IT', 'en_US', 'fr_FR', 'zh_CN#Hans'].length)

		for (var idx in mockWrapper.vm.languages) {
			if (mockWrapper.vm.languages[idx].locale === mockWrapper.vm.localObject.locale) {
				expect(mockWrapper.vm.languages[idx].disabled).toBe(true)
			} else {
				expect(mockWrapper.vm.languages[idx].disabled).toBe(false)
			}
		}
	})
})

describe('languageDialog', () => {
	it('Test click event', async () => {
		mockWrapper.vm.changeLanguage({ locale: 'it_IT', disabled: false })
		expect(mockWrapper.vm.$i18n.locale).toBe('it_IT')
	})
})
