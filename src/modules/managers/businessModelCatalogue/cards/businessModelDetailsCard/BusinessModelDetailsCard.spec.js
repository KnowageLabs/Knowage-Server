import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import { createTestingPinia } from '@pinia/testing'
import BusinessModelDetailsCard from './BusinessModelDetailsCard.vue'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import InputSwitch from 'primevue/inputswitch'
import InputText from 'primevue/inputtext'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import ProgressBar from 'primevue/progressbar'
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

const $router = {
    push: vi.fn(),
    replace: vi.fn()
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
            plugins: [createTestingPinia()],
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
                KnOverlaySpinnerPanel: true,
                ProgressBar,
                Toolbar,
                Tooltip
            },
            mocks: {
                $t: (msg) => msg,
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

        await wrapper.setProps({ selectedBusinessModel: mockedBusinessModel })
        wrapper.vm.loadBusinessModel()
        await nextTick()

        expect(wrapper.vm.businessModel.id).toBeTruthy()
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
        expect(wrapper.find('[data-test="metaweb-button"]').isVisible()).toBe(false)
    })

    it('should show a generate button exiting from meta when something is changed and saved in the model', async () => {
        const wrapper = factory()

        await wrapper.setProps({ selectedBusinessModel: mockedBusinessModel, toGenerate: true })
        await wrapper.find('[data-test="metaweb-switch"]').trigger('click')

        expect(wrapper.vm.toGenerate).toBe(true)
        expect(wrapper.find('[data-test="generate-button"]').exists()).toBe(true)
    })
})
