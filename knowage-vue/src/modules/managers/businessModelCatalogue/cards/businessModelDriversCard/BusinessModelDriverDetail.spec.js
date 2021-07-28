import { mount } from '@vue/test-utils'
import axios from 'axios'
import BusinessModelDriverDetail from './BusinessModelDriverDetail.vue'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import InputText from 'primevue/inputtext'
import KnHint from '@/components/UI/KnHint.vue'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Listbox from 'primevue/listbox'
import Toolbar from 'primevue/toolbar'

const mockedDriver = {
    id: 1,
    label: 'Product category',
    parameter: { id: 28, label: 'CORR_PRD_CAT', name: 'CORR_PRD_CAT', description: 'CORR_PRD_CAT' },
    parameterUrlName: 'category',
    priority: 2
}

jest.mock('axios')

axios.get.mockImplementation(() => Promise.resolve({ data: [] }))

const factory = () => {
    return mount(BusinessModelDriverDetail, {
        props: {
            selectedDriver: null,
            driverOptions: [],
            businessModelDrivers: []
        },
        global: {
            stubs: {
                Card,
                Button,
                Checkbox,
                Dialog,
                Dropdown,
                InputSwitch,
                InputText,
                KnHint,
                KnValidationMessages,
                Listbox,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Business Model Management drivers', () => {
    it('shows a small hint in detail if no drivers are selected', () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedDriver).toBe(null)
        expect(wrapper.find('[data-test="driver-hint"]').exists()).toBe(true)
    })
    it('shows a filled detail if one of the drivers is selected', async () => {
        const wrapper = factory()

        await wrapper.setProps({ selectedDriver: mockedDriver })

        const labelInput = wrapper.find('[data-test="label-input"]')
        const parameterUrlNameInput = wrapper.find('[data-test="parameterUrlName-input"]')

        expect(wrapper.vm.driver).toStrictEqual(mockedDriver)
        expect(labelInput.wrapperElement._value).toBe('Product category')
        expect(parameterUrlNameInput.wrapperElement._value).toBe('category')
    })
    it('shows an empty detail if one of add button is clicked', async () => {
        const wrapper = factory()

        await wrapper.setProps({ selectedDriver: { parameter: { id: 1 } } })

        const labelInput = wrapper.find('[data-test="label-input"]')
        const parameterUrlNameInput = wrapper.find('[data-test="parameterUrlName-input"]')

        expect(wrapper.vm.driver).toStrictEqual({ parameter: { id: 1 } })
        expect(labelInput.wrapperElement._value).toBeFalsy()
        expect(parameterUrlNameInput.wrapperElement._value).toBeFalsy()
    })
})
