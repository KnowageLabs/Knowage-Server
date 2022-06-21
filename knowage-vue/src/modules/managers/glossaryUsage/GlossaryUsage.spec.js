import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import GlossaryUsageHint from './GlossaryUsageHint.vue'
import GlossaryUsage from './GlossaryUsage.vue'
import flushPromises from 'flush-promises'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import Tree from 'primevue/tree'

const mockedGlossaryList = [
    { GLOSSARY_ID: 45, GLOSSARY_NM: 'MARKET ANALYSIS' },
    { GLOSSARY_ID: 46, GLOSSARY_NM: 'MOCK GLOSSARY' }
]

const mockedContent = [
    { CONTENT_ID: 199, CONTENT_NM: 'PRODUCTS', HAVE_WORD_CHILD: true, HAVE_CONTENTS_CHILD: false },
    { CONTENT_ID: 198, CONTENT_NM: 'STORE', HAVE_WORD_CHILD: true, HAVE_CONTENTS_CHILD: false },
    { CONTENT_ID: 200, CONTENT_NM: 'UNITS', HAVE_WORD_CHILD: true, HAVE_CONTENTS_CHILD: false }
]

const mockedSearchContent = {
    GlossSearch: {
        GLOSSARY_ID: 45,
        GLOSSARY_NM: 'MARKET ANALYSIS',
        SBI_GL_CONTENTS: [{ CONTENT_ID: 199, CONTENT_NM: 'PRODUCTS', HAVE_WORD_CHILD: true, HAVE_CONTENTS_CHILD: false, CHILD: [{ WORD_ID: 262, WORD: 'Customer' }] }]
    },
    Status: 'OK'
}

const mockedWords = [
    { WORD_ID: 1, WORD: 'Customer' },
    { WORD_ID: 2, WORD: 'Product Store' },
    { WORD_ID: 3, WORD: 'Product Sales' }
]

jest.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/glossary/listGlossary':
                return Promise.resolve({ data: mockedGlossaryList })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/glossary/listContents?GLOSSARY_ID=45&PARENT_ID=1':
                return Promise.resolve({ data: mockedContent })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

const factory = () => {
    return mount(GlossaryUsage, {
        global: {
            stubs: { Button, Card, Dropdown, InputText, GlossaryUsageDetail: true, GlossaryUsageHint, ProgressBar, Toolbar, Tree },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

jest.useFakeTimers()
jest.spyOn(global, 'setTimeout')

describe('Glossary Usage loading', () => {
    it('the list shows an hint component when loaded empty', () => {
        const wrapper = factory()

        expect(wrapper.vm.glossaryList.length).toBe(0)
        expect(wrapper.find('[data-test="no-glossary-found-hint"]').exists()).toBe(true)
    })
})

describe('Glossary Usage Tree', () => {
    it('shows an hint component when no glossary is selected', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.selectedGlossaryId).toBe(null)
        expect(wrapper.find('[data-test="no-glossary-selected-tree-hint"]').exists()).toBe(true)
    })
    it('filters the tree when entering a text search', async () => {
        const wrapper = factory()

        wrapper.vm.selectedGlossaryId = 45

        await flushPromises()

        wrapper.vm.listContents(45, { id: 1 })

        await flushPromises()

        let glossaryTree = wrapper.find('[data-test="glossary-tree"]')
        const inputSearch = wrapper.find('[data-test="search-input"]')

        await inputSearch.setValue('Customer')
        wrapper.vm.searchWord = 'Customer'

        await wrapper.vm.createGlossaryTree(mockedSearchContent)
        await nextTick()

        glossaryTree = wrapper.find('[data-test="glossary-tree"]')
        expect(glossaryTree.html()).toContain('PRODUCTS')
        expect(glossaryTree.html()).toContain('Customer')
        expect(glossaryTree.html()).not.toContain('STORE')
        expect(glossaryTree.html()).not.toContain('UNITS')
    })
})

describe('Glossary Usage Navigation', () => {
    it('shows an hint if no glossary is selected', async () => {
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.selectedGlossaryId).toBe(null)
        expect(wrapper.find('[data-test="no-glossary-selected-hint"]').exists()).toBe(true)
    })
    it('filters the glossary tree when an element in the card is selected', async () => {
        const wrapper = factory()

        wrapper.vm.selectedGlossaryId = 45
        wrapper.vm.setFilteredWords(mockedWords)

        await flushPromises()

        expect(wrapper.html()).toContain('Customer')
        expect(wrapper.html()).toContain('Product Store')
        expect(wrapper.html()).toContain('Product Sales')
    })
})
