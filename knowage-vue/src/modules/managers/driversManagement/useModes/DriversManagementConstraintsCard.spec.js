import { mount } from '@vue/test-utils'
import Column from 'primevue/column'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import ConstraintsCard from './DriversManagementConstraintsCard.vue'
import InputText from 'primevue/inputtext'

const mockedConstraints = [
    {
        checkId: 224,
        valueTypeId: 49,
        name: 'Internet Address',
        label: 'CK-FIX-01',
        description: 'Control if parameter is an Internet Address',
        valueTypeCd: 'INTERNET ADDRESS',
        firstValue: '',
        secondValue: ''
    },
    {
        checkId: 225,
        valueTypeId: 50,
        name: 'Numeric',
        label: 'CK-FIX-02',
        description: 'Control if  a parameter is Numeric',
        valueTypeCd: 'NUMERIC',
        firstValue: '',
        secondValue: ''
    },
    {
        checkId: 226,
        valueTypeId: 51,
        name: 'Alfanumeric',
        label: 'CK-FIX-03',
        description: 'Control if  a parameter is Alfanumeric',
        valueTypeCd: 'ALFANUMERIC',
        firstValue: '',
        secondValue: ''
    }
]

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const factory = () => {
    return mount(ConstraintsCard, {
        props: {
            constraints: [...mockedConstraints]
        },
        global: {
            plugins: [],
            stubs: {
                Column,
                Card,
                DataTable,
                InputText
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Drivers Management Use modes', () => {
    it('filters constraints when entering a roles text search', async () => {
        const wrapper = factory()

        const valuesList = wrapper.find('[data-test="values-list"]')
        const searchInput = valuesList.find(['[data-test="filter-input"]'])

        expect(valuesList.html()).toContain('Internet Address')
        expect(valuesList.html()).toContain('Numeric')
        expect(valuesList.html()).toContain('Alfanumeric')

        await searchInput.setValue('Internet Address')
        await valuesList.trigger('filter')

        expect(valuesList.html()).toContain('Internet Address')
        expect(valuesList.html()).not.toContain('Numeric')
        expect(valuesList.html()).not.toContain('Alfanumeric')

        await searchInput.setValue('Numeric')
        await valuesList.trigger('filter')

        expect(valuesList.html()).not.toContain('Internet Address')
        expect(valuesList.html()).toContain('Numeric')
        expect(valuesList.html()).toContain('Alfanumeric')

        await searchInput.setValue('Alfanumeric')
        await valuesList.trigger('filter')

        expect(valuesList.html()).not.toContain('Internet Address')
        expect(valuesList.html()).not.toContain('Numeric')
        expect(valuesList.html()).toContain('Alfanumeric')
    })
})
