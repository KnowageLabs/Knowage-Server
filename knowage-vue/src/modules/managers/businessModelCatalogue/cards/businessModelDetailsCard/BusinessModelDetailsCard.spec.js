import { mount } from '@vue/test-utils'
import BusinessModelDetailsCard from './BusinessModelDetailsCard.vue'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Toolbar from 'primevue/toolbar'
import Tooltip from 'primevue/tooltip'

const mockedBusinessModel = {
    id: 1,
    dataSourceId: 4,
    dataSourceLabel: 'foodmart_mysql',
    description: 'Model with 3 drivers',
    drivers: [],
    metamodelDrivers: null,
    modelLocked: false,
    modelLocker: null,
    name: 'MODEL_WITH_3_DRIVERS',
    smartView: true,
    tablePrefixLike: 'tablePrefix',
    tablePrefixNotLike: 'tablePrefixNotLike'
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn(),
    replace: jest.fn()
}

const factory = () => {
    return mount(BusinessModelDetailsCard, {
        props: {
            selectedBusinessModel: {},
            domainCategories: [],
            datasourcesMeta: [],
            user: {}
        },
        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                Card,
                Checkbox,
                Button,
                Dialog,
                Dropdown,
                InputText,
                InputSwitch,
                GenerateDatamartCard: true,
                KnInputFile: true,
                KnValidationMessages,
                Toolbar,
                Tooltip
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $router
            }
        }
    })
}

describe('Business Model Detail', () => {
    it('shows filled input fields when business model is passed', async () => {
        const wrapper = factory()

        await wrapper.setProps({ selectedBusinessModel: mockedBusinessModel })

        const nameInput = wrapper.find('[data-test="name-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        expect(wrapper.vm.businessModel).toStrictEqual(mockedBusinessModel)
        expect(nameInput.wrapperElement._value).toBe('MODEL_WITH_3_DRIVERS')
        expect(descriptionInput.wrapperElement._value).toBe('Model with 3 drivers')
    })

    it('emits correct value on input change', async () => {
        const wrapper = factory()

        const nameInput = wrapper.find('[data-test="name-input"]')
        const descriptionInput = wrapper.find('[data-test="description-input"]')

        await nameInput.setValue('test name')
        expect(wrapper.emitted().fieldChanged[0][0].value).toBe('test name')

        await descriptionInput.setValue('test description')
        expect(wrapper.emitted().fieldChanged[1][0].value).toBe('test description')
    })

    it('name field is not editable if the form is not new', async () => {
        const wrapper = factory()

        expect(wrapper.find('[data-test="name-input"]').element.disabled).toBe(false)

        await wrapper.setProps({ selectedBusinessModel: mockedBusinessModel })

        expect(wrapper.vm.businessModel.id).toBeTruthy()
        expect(wrapper.find('[data-test="name-input"]').element.disabled).toBe(true)
    })

    it("shows metaweb button if the 'enable metaweb' is selected", async () => {
        const wrapper = factory()

        await wrapper.setProps({ selectedBusinessModel: mockedBusinessModel })
        await wrapper.find('[data-test="metaweb-switch"]').trigger('click')

        expect(wrapper.vm.metaWebVisible).toBe(true)
        expect(wrapper.find('[data-test="metaweb-button"]').exists()).toBe(true)
    })

    it("hides metaweb button if the 'enable metaweb' is not selected", async () => {
        const wrapper = factory()

        await wrapper.setProps({ selectedBusinessModel: mockedBusinessModel })
        expect(wrapper.vm.metaWebVisible).toBe(false)
        expect(wrapper.find('[data-test="metaweb-button"]').exists()).toBe(false)
    })
})
