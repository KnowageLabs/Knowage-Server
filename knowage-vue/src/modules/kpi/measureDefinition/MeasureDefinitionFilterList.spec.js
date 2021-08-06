import { mount } from '@vue/test-utils'
import Listbox from 'primevue/listbox'
import MeasureDefinitionFilterList from './MeasureDefinitionFilterList.vue'
import Toolbar from 'primevue/toolbar'

const mockedFilter = [
    {
        id: 1,
        name: 'Test'
    },
    {
        id: 2,
        name: 'Description'
    },
    {
        id: 3,
        name: 'A'
    },
    {
        id: 4,
        name: 'W'
    }
]

const factory = () => {
    return mount(MeasureDefinitionFilterList, {
        props: {
            list: mockedFilter
        },
        global: {
            stubs: {
                Listbox,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Filter List', () => {
    it('sorts the list if the sort icon is clicked', async () => {
        const wrapper = factory()

        expect(wrapper.vm.filters[0]).toStrictEqual(mockedFilter[0])

        await wrapper.find('[data-test="sort-icon"]').trigger('click')

        expect(wrapper.vm.filters[0]).toStrictEqual({
            id: 3,
            name: 'A'
        })

        await wrapper.find('[data-test="sort-icon"]').trigger('click')

        expect(wrapper.vm.filters[0]).toStrictEqual({
            id: 4,
            name: 'W'
        })
    })
    it('clicking on an list item will emit event with correct value', async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="list-item-1"]').trigger('click')

        expect(wrapper.emitted()).toHaveProperty('selected')
        expect(wrapper.emitted().selected[0][0]).toStrictEqual({ type: undefined, value: 'Test' })
    })
})
