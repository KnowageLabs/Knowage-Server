import { mount } from '@vue/test-utils'
import axios from 'axios'
import Button from 'primevue/button'
import FabButton from '@/components/UI/KnFabButton.vue'
import flushPromises from 'flush-promises'
import GlossaryDefinition from './GlossaryDefinition.vue'
import Listbox from 'primevue/listbox'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

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

axios.get.mockImplementation((url) => {
    switch (url) {
        case process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listWords?Page=1&ItemPerPage=`:
            return Promise.resolve({ wordsList: mockedWords })
        default:
            return Promise.resolve({ data: [] })
    }
})

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(GlossaryDefinition, {
        global: {
            stubs: {
                Button,
                FabButton,
                Listbox,
                ProgressBar,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $confirm,
                $store
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
