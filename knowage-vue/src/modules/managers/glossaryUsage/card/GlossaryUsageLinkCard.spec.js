import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Chip from 'primevue/chip'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import GlossaryUsageLinkCard from './GlossaryUsageLinkCard.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedItems = [
    { author: 'demo_admin', description: 'All Kpi', id: 1, itemType: 'document', name: 'KPI_LIST' },
    { author: 'demo_admin', description: '', id: 2, itemType: 'document', name: 'CHOCOLATE_RATINGS' },
    { author: 'demo_admin', description: '"SALES"', id: 3, itemType: 'document', name: 'SALES' }
]

const mockedWords = {
    '1': [
        { WORD_ID: 1, WORD: 'Customer' },
        { WORD_ID: 2, WORD: 'Product Store' },
        { WORD_ID: 3, WORD: 'Product Sales' }
    ]
}

const factory = (items, words) => {
    return mount(GlossaryUsageLinkCard, {
        props: {
            items: items,
            words: words
        },
        global: {
            directives: {
                tooltip() {}
            },
            stubs: { Button, Card, Chip, Column, DataTable, InputText, ProgressBar, Toolbar },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Glossary Usage Card Editing', () => {
    it('shows an hint if no element is available', () => {
        const wrapper = factory([], [])

        expect(wrapper.vm.items.length).toBe(0)
        expect(wrapper.vm.words.length).toBe(0)
        expect(wrapper.html()).toContain('common.info.noDataFound')
    })
    it('shows a populated datatable when elements are available', () => {
        const wrapper = factory(mockedItems, mockedWords)

        expect(wrapper.vm.items.length).toBe(3)
        expect(wrapper.vm.words['1'].length).toBe(3)

        expect(wrapper.html()).toContain('KPI_LIST')
        expect(wrapper.html()).toContain('CHOCOLATE_RATINGS')
        expect(wrapper.html()).toContain('SALES')
    })
    it('shows the elements assigned words if present', () => {
        const wrapper = factory(mockedItems, mockedWords)
        wrapper.vm.expandedRows = mockedItems

        expect(wrapper.vm.items.length).toBe(3)
        expect(wrapper.vm.words['1'].length).toBe(3)

        expect(wrapper.vm.associatedWords[1]).toStrictEqual(mockedWords['1'])
        expect(wrapper.html()).toContain('KPI_LIST')
    })
})
