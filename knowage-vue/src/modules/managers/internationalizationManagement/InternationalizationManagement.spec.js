import { mount } from '@vue/test-utils'
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

const mockedLanguages = [
    {
        iso3code: 'ita',
        language: 'Italian(ITA)',
        languageTag: 'it-IT'
    },
    {
        defaultLanguage: true,
        iso3code: 'eng',
        language: 'English(USA)',
        languageTag: 'en-US'
    },
    {
        iso3code: 'fra',
        language: 'French(FRA)',
        languageTag: 'fr-FR'
    }
]

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

const factory = () => {
    return mount(InternationalizationManagement, {
        attachToDocument: true,
        global: {
            plugins: [],
            stubs: { Button, InputText, ProgressBar, Toolbar, Message, Checkbox, DataTable, Column, TabPanel, TabView },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Internationalization Management loading', () => {
    it('show progress bar when loading', async () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
})

describe('Internationalization Management', () => {
    it("starts with a default language selected depending on user's locale", () => {
        const wrapper = factory()
        wrapper.vm.loading = false
        wrapper.vm.languages = mockedLanguages

        expect(wrapper.find('[role="presentation"]:nth-child(1)').html()).toContain('aria-selected="true"')
    })
    it('shows as many tabs as the available languages', () => {
        const wrapper = factory()
        wrapper.vm.languages = mockedLanguages
        expect(wrapper.vm.languages.length).toBe(3)

        expect(wrapper.find('[role="presentation"]:nth-child(3)').html()).toContain('French(FRA)')
    })
    it('enables label and message code if the language is the default one', async () => {
        const wrapper = factory()
        wrapper.vm.loading = false
        wrapper.vm.languages = mockedLanguages
        wrapper.vm.messages = mockedMessages

        expect(wrapper.vm.languages.length).toBe(3)
        expect(wrapper.vm.messages.length).toBe(3)
        await wrapper.find('.p-tabview-nav li:nth-child(2)').trigger('click')

        expect(wrapper.find('[data-test="messages-table"]').html()).toContain('common.label')
        expect(wrapper.find('[data-test="messages-table"]').html()).not.toContain('managers.internationalizationManagement.table.defaultMessage')
        expect(wrapper.find('[data-test="messages-table"]').html()).toContain('managers.internationalizationManagement.table.messageCode')
    })
    it('disables label and default message code if the language is not the default one', async () => {
        const wrapper = factory()
        wrapper.vm.loading = false
        wrapper.vm.languages = mockedLanguages
        wrapper.vm.messages = mockedMessages
        wrapper.vm.selectedLanguage = nonDefaultLanguage

        expect(wrapper.vm.languages.length).toBe(3)
        expect(wrapper.vm.messages.length).toBe(3)
        await wrapper.find('.p-tabview-nav li:nth-child(2)').trigger('click')

        expect(wrapper.find('[data-test="messages-table"]').html()).toContain('managers.internationalizationManagement.table.defaultMessage')
    })
    it('shows only blank fields if the show only blank fields option is selected', async () => {
        const wrapper = factory()
        wrapper.vm.loading = false
        wrapper.vm.languages = mockedLanguages
        wrapper.vm.messages = mockedMessages

        expect(wrapper.vm.languages.length).toBe(3)
        expect(wrapper.vm.messages.length).toBe(3)
        await wrapper.find('.p-tabview-nav li:nth-child(2)').trigger('click')
        await wrapper.find('.p-checkbox-icon').trigger('click')

        expect(wrapper.find('[data-test="messages-table"]').html()).toContain('common.info.noDataFound')
    })
})

describe('Internationalization Management Search', () => {
    it('filters the list of the current language if a text is provided', async () => {
        const wrapper = factory()
        wrapper.vm.loading = false
        wrapper.vm.languages = mockedLanguages
        wrapper.vm.messages = mockedMessages

        await wrapper.find('.p-tabview-nav li:nth-child(1)').trigger('click')

        const messageList = wrapper.find('[data-test="messages-table"]')
        const searchInput = messageList.find('[data-test="filterInput"]')

        expect(messageList.html()).toContain('test11')
        expect(messageList.html()).toContain('test22')

        await searchInput.setValue('test22')

        expect(messageList.html()).not.toContain('test11')
        expect(messageList.html()).toContain('test22')
    })
    it('returns no data if the text is not present', async () => {
        const wrapper = factory()
        wrapper.vm.loading = false
        wrapper.vm.languages = mockedLanguages
        wrapper.vm.messages = mockedMessages

        await wrapper.find('.p-tabview-nav li:nth-child(1)').trigger('click')

        const messageList = wrapper.find('[data-test="messages-table"]')
        const searchInput = messageList.find('[data-test="filterInput"]')

        expect(messageList.html()).toContain('test11')
        expect(messageList.html()).toContain('test22')

        await searchInput.setValue('data that doesnt exist')

        expect(messageList.html()).toContain('common.info.noDataFound')
    })
})
