import { shallowMount } from '@vue/test-utils'
import LanguageDialog from './LanguageDialog.vue'
import flushPromises from 'flush-promises'
import axios from 'axios'
import router from '@/App.routes.js'
import Button from 'primevue/button'

const defaultLocale = 'en_US'

const mockWrapper = shallowMount(LanguageDialog, {
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

jest.mock('axios', () => ({
	get: jest.fn(() => Promise.resolve({ data: ['it_IT', 'en_US', 'fr_FR', 'zh_CN#Hans'] }))
}))

describe('LanguageDialog', () => {

	test('languages array is not populated', () => {
		expect(mockWrapper.vm.languages.length).toBe(0)		//1)
    });

    test('language service has been called with', async () => {
		await mockWrapper.setProps({ visibility: true })
        expect(axios.get).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/languages')		//2)
		await flushPromises()
    });

    test('language dialog is not populated', async () => {
		await mockWrapper.setProps({ visibility: true })
		expect(axios.get).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/languages')		//2)
		await flushPromises()
		expect(mockWrapper.vm.languages.length).toBe(4)	//3)
    });

    test('language dialog is not populated', async () => {
		await mockWrapper.setProps({ visibility: true })
		expect(axios.get).toHaveBeenCalledWith(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/languages')		//2)
		await flushPromises()
		for (var idx in mockWrapper.vm.languages) {												//
			if (mockWrapper.vm.languages[idx].locale === mockWrapper.vm.localObject.locale) {	//
				expect(mockWrapper.vm.languages[idx].disabled).toBe(true)						//
			} else {																			//4)
				expect(mockWrapper.vm.languages[idx].disabled).toBe(false)						//
			}																					//
		}																						//
	});
})
