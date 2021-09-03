import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import GlossaryUsageNavigationCard from './GlossaryUsageNavigationCard.vue'
import Toolbar from 'primevue/toolbar'

const mockedItems = [
    { id: 1, label: 'KPI_CARD' },
    { id: 2, label: 'CUSTOMER RETENTION' },
    { id: 3, label: 'HR VIEW' }
]

const $store = {
    state: { user: {} },
    commit: jest.fn()
}

const factory = (items) => {
    return mount(GlossaryUsageNavigationCard, {
        props: {
            items: items
        },
        global: {
            stubs: { Button, Card, Column, DataTable, InputText, Toolbar },
            mocks: {
                $t: (msg) => msg,
                $store
            }
        }
    })
}

describe('Glossary Usage Navigation', () => {
    it('filters the card content when entering a text search', async () => {
        const wrapper = factory(mockedItems)
        const inputSearch = wrapper.find('[data-test="search-input"]')

        expect(wrapper.html()).toContain('KPI_CARD')
        expect(wrapper.html()).toContain('CUSTOMER RETENTION')
        expect(wrapper.html()).toContain('HR VIEW')

        await inputSearch.setValue('KPI_CARD')

        expect(wrapper.html()).toContain('KPI_CARD')
        expect(wrapper.html()).not.toContain('CUSTOMER RETENTION')
        expect(wrapper.html()).not.toContain('HR VIEW')
    })
    it('shows a dialog with the element detail when an item in a card is selected', async () => {
        const wrapper = factory(mockedItems)

        await wrapper.find('[data-test="info-button-1"]').trigger('click')

        expect(wrapper.emitted()).toHaveProperty('infoClicked')
        expect(wrapper.emitted().infoClicked[0][0]).toStrictEqual(mockedItems[0])
    })
})
