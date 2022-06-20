import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Dialog from 'primevue/dialog'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import GlossaryDefinition from './GlossaryDefinition.vue'
import InputText from 'primevue/inputtext'
import Listbox from 'primevue/listbox'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import PrimeVue from 'primevue/config'

const mockedWords = [
    {
        WORD_ID: 1,
        WORD: 'Customer'
    },
    {
        WORD_ID: 2,
        WORD: 'Inventory'
    },
    {
        WORD_ID: 3,
        WORD: 'Product Sales'
    }
]

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listWords?Page=1&ItemPerPage=`:
                return Promise.resolve({ data: mockedWords })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(GlossaryDefinition, {
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Button,
                Card,
                Dialog,
                FabButton,
                GlossaryDefinitionInfoDialog: true,
                GlossaryDefinitionWordEdit: true,
                InputText,
                Listbox,
                ProgressBar,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $confirm,
                $store,
                $http
            }
        }
    })
}

describe('Glossary Definition loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('the list shows an hint component when loaded empty', async () => {
        axios.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory()
        await flushPromises()
        expect(wrapper.vm.wordsList.length).toBe(0)
        expect(wrapper.find('[data-test="words-list"]').html()).toContain('common.info.noDataFound')
    })
})
describe('Glossary Definition', () => {
    it('shows a prompt when user click on a word delete button to delete it', async () => {
        const wrapper = factory()
        await flushPromises()

        const deleteButton = wrapper.find('[data-test="delete-button"]')

        await deleteButton.trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)
    })
    it('shows and empty form dialog when clicking on the add button', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="new-button"]').trigger('click')
        expect(wrapper.vm.editWordDialogVisible).toBe(true)
    })
    it('shows the detail in a dialog when clicking on a word', async () => {
        const wrapper = factory()

        await flushPromises()
        await wrapper.find('[data-test="info-button"]').trigger('click')
        expect(wrapper.vm.infoDialogVisible).toBe(true)
    })
})
