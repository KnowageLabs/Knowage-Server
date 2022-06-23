import { flushPromises, mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import InternationalizationManagement from './InternationalizationManagement.vue'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Checkbox from 'primevue/checkbox'
import Message from 'primevue/message'

const mockedLanguages = {
    wrappedObject: [
        {
            language: 'Italian(ITA)',
            iso3code: 'ita',
            languageTag: 'it-IT'
        },
        {
            language: 'English(USA)',
            iso3code: 'eng',
            languageTag: 'en-US',
            defaultLanguage: true
        },
        {
            language: 'French(FRA)',
            iso3code: 'fra',
            languageTag: 'fr-FR'
        },
        {
            language: 'Spanish(ESP)',
            iso3code: 'spa',
            languageTag: 'es-ES'
        },
        {
            language: 'Portuguese(BRA)',
            iso3code: 'por',
            languageTag: 'pt-BR'
        },
        {
            language: 'English(GBR)',
            iso3code: 'eng',
            languageTag: 'en-GB'
        },
        {
            language: 'Chinese(CHN)',
            iso3code: 'zho',
            languageTag: 'zh-Hans-CN'
        },
        {
            language: 'German(DEU)',
            iso3code: 'deu',
            languageTag: 'de-DE'
        }
    ]
}

const nonDefaultLanguage = [
    {
        iso3code: 'ita',
        language: 'Italian(ITA)',
        languageTag: 'it-IT'
    }
]

const mockedMessages = [
    {
        id: 1,
        label: 'test11',
        languageCd: 1,
        message: 'test11'
    },
    {
        id: 2,
        label: 'test22',
        languageCd: 2,
        message: 'test22'
    },
    {
        id: 3,
        label: 'test33',
        languageCd: 3,
        message: 'test33'
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/internationalization/languages`:
                return Promise.resolve({ data: mockedLanguages })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/i18nMessages/internationalization/?currLanguage=en-US`:
                return Promise.resolve({ data: mockedMessages })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const factory = () => {
    return mount(InternationalizationManagement, {
        attachToDocument: true,
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: { Button, InputText, ProgressBar, Toolbar, Message, Checkbox, DataTable, Column, TabPanel, TabView },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Internationalization Management loading', () => {
    it('show progress bar when loading', async () => {
        const wrapper = factory()
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
})

describe('Internationalization Management', () => {
    it("starts with a default language selected depending on user's locale", async () => {
        const wrapper = factory()
        await flushPromises()
        wrapper.vm.loading = false
        wrapper.vm.languages = mockedLanguages

        expect(wrapper.find('[role="presentation"]:nth-child(1)').html()).toContain('aria-selected="true"')
    })

    it('shows as many tabs as the available languages', async () => {
        const wrapper = factory()
        await flushPromises()
        expect(wrapper.vm.languages.length).toBe(8)
        expect(wrapper.find('[role="presentation"]:nth-child(3)').html()).toContain('French(FRA)')
    })
    it('enables label and message code if the language is the default one', async () => {
        const wrapper = factory()
        await flushPromises()

        expect(wrapper.vm.languages.length).toBe(8)
        expect(wrapper.vm.messages.length).toBe(3)
        await wrapper.find('.p-tabview-nav li:nth-child(2)').trigger('click')

        expect(wrapper.find('[data-test="messages-table"]').html()).toContain('common.label')
        expect(wrapper.find('[data-test="messages-table"]').html()).not.toContain('managers.internationalizationManagement.table.defaultMessage')
        expect(wrapper.find('[data-test="messages-table"]').html()).toContain('managers.internationalizationManagement.table.messageCode')
    })
    it('disables label and default message code if the language is not the default one', async () => {
        const wrapper = factory()
        await flushPromises()
        wrapper.vm.selectedLanguage = nonDefaultLanguage

        expect(wrapper.vm.languages.length).toBe(8)
        expect(wrapper.vm.messages.length).toBe(3)
        await wrapper.find('.p-tabview-nav li:nth-child(2)').trigger('click')

        expect(wrapper.find('[data-test="messages-table"]').html()).toContain('managers.internationalizationManagement.table.defaultMessage')
    })
    it('shows only blank fields if the show only blank fields option is selected', async () => {
        const wrapper = factory()
        await flushPromises()

        expect(wrapper.vm.languages.length).toBe(8)
        expect(wrapper.vm.messages.length).toBe(3)
        await wrapper.find('.p-tabview-nav li:nth-child(2)').trigger('click')
        await wrapper.find('.p-checkbox-icon').trigger('click')

        expect(wrapper.find('[data-test="messages-table"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Internationalization Management Search', () => {
    it('filters the list of the current language if a text is provided', async () => {
        const wrapper = factory()
        await flushPromises()

        await wrapper.find('.p-tabview-nav li:nth-child(1)').trigger('click')

        const messageList = wrapper.find('[data-test="messages-table"]')
        const searchInput = messageList.find('[data-test="filterInput"]')

        expect(wrapper.find('[data-test="input-field-1"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="input-field-1"]').wrapperElement._value).toBe('test11')
        expect(wrapper.find('[data-test="input-field-2"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="input-field-2"]').wrapperElement._value).toBe('test22')

        await searchInput.setValue('test22')

        expect(wrapper.find('[data-test="input-field-1"]').exists()).toBe(false)
        expect(wrapper.find('[data-test="input-field-2"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="input-field-2"]').wrapperElement._value).toBe('test22')
    })
    it('returns no data if the text is not present', async () => {
        const wrapper = factory()
        await flushPromises()

        await wrapper.find('.p-tabview-nav li:nth-child(1)').trigger('click')

        const messageList = wrapper.find('[data-test="messages-table"]')
        const searchInput = messageList.find('[data-test="filterInput"]')

        expect(wrapper.find('[data-test="input-field-1"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="input-field-1"]').wrapperElement._value).toBe('test11')
        expect(wrapper.find('[data-test="input-field-2"]').exists()).toBe(true)
        expect(wrapper.find('[data-test="input-field-2"]').wrapperElement._value).toBe('test22')

        await searchInput.setValue('data that doesnt exist')

        expect(messageList.html()).toContain('common.info.noDataFound')
    })
})
