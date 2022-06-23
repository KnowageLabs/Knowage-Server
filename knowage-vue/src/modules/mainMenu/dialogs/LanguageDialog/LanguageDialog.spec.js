import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import LanguageDialog from './LanguageDialog.vue'
import flushPromises from 'flush-promises'
import axios from 'axios'
import router from '@/App.routes.js'
import Button from 'primevue/button'

const defaultLocale = 'en_US'

vi.mock('axios')

const $http = { get: axios.get.mockImplementation(() => Promise.resolve({ data: mockedLanguagesArray })) }

const wrapper = mount(LanguageDialog, {
    propsData: {
        visibility: false,
        languages: []
    },
    global: {
        components: { Button },
        plugins: [PrimeVue],
        mocks: {
            $t: (msg) => msg,
            $router: router,
            $i18n: {
                locale: defaultLocale,
                fallbackLocale: defaultLocale,
                messages: { en_US: { test: 'TEST' } }
            },
            localObject: { locale: defaultLocale },
            $http
        }
    }
})

const mockedLanguagesArray = ['it_IT', 'en_US', 'fr_FR', 'zh_CN#Hans']

describe('LanguageDialog', () => {
    test('languages array is not populated', () => {
        expect(wrapper.vm.languages.length).toBe(0)
    })

    test('language service has been called with', async () => {
        await wrapper.setProps({ visibility: true })
        expect($http.get).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/languages')
    })

    test('languages array is populated', async () => {
        await axios.get
        await flushPromises()
        expect(wrapper.vm.languages.length).toBe(mockedLanguagesArray.length)
    })

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
    })
})
