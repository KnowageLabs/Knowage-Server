import { shallowMount } from '@vue/test-utils'
import LanguageDialog from './LanguageDialog.vue'
import flushPromises from 'flush-promises'
import axios from 'axios'
import router from '@/App.routes.js'
import Button from 'primevue/button'

const defaultLocale = 'en_US'

const wrapper = shallowMount(LanguageDialog, {
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

const mockedLanguagesArray = ['it_IT', 'en_US', 'fr_FR', 'zh_CN#Hans']

jest.mock('axios', () => ({
	get: jest.fn(() => Promise.resolve({ data: mockedLanguagesArray }))
}))

describe('LanguageDialog', () => {

	test('languages array is not populated', () => {
		expect(wrapper.vm.languages.length).toBe(0)
    });

    test('language service has been called with', async () => {
		await wrapper.setProps({ visibility: true })
        expect(axios.get).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/languages')
    });
	
	test('languages array is populated', async () => {
		await axios.get
		await flushPromises()
		expect(wrapper.vm.languages.length).toBe(mockedLanguagesArray.length)
	});

	test('current locale language is the only disabled', async () => {
		/* Languages is populated because previous test */
		expect(wrapper.vm.languages.length).toBeGreaterThan(0)

		for (var idx in wrapper.vm.languages) {
			if (wrapper.vm.languages[idx].locale === wrapper.vm.localObject.locale) {
				expect(wrapper.vm.languages[idx].disabled).toBe(true)
			} else {
				expect(wrapper.vm.languages[idx].disabled).toBe(false)
			}
		}
	});  
})