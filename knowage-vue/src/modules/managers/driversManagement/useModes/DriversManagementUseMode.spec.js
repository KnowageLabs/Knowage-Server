import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Card from 'primevue/card'
import DriversManagementUseMode from './DriversManagementUseMode.vue'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import Listbox from 'primevue/listbox'
import Tooltip from 'primevue/tooltip'

const mockedModes = [
    {
        useID: 1,
        id: 656,
        idLov: 547,
        idLovForDefault: 547,
        idLovForMax: null,
        name: 'test 1',
        label: 'test 1',
        description: 'test 1',
        associatedRoles: [],
        associatedChecks: [],
        selectionType: 'LIST',
        multivalue: true,
        manualInput: 0,
        maximizerEnabled: false,
        valueSelection: 'map_in',
        selectedLayer: 'usa_states_file',
        selectedLayerProp: 'test property',
        defaultFormula: null,
        options: null
    },
    {
        useID: 2,
        id: 656,
        idLov: 547,
        idLovForDefault: 547,
        idLovForMax: null,
        name: 'test 2',
        label: 'test 2',
        description: 'test 2',
        associatedRoles: [],
        associatedChecks: [],
        selectionType: 'LIST',
        multivalue: true,
        manualInput: 0,
        maximizerEnabled: false,
        valueSelection: 'map_in',
        selectedLayer: 'usa_states_file',
        selectedLayerProp: 'test property',
        defaultFormula: null,
        options: null
    }
]

const factory = () => {
    return mount(DriversManagementUseMode, {
        props: {
            propModes: [...mockedModes]
        },
        global: {
            directives: {
                tooltip: Tooltip
            },
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                Card,
                InputText,
                Listbox,
                UseModeDetail: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}
describe('Drivers Management Use modes', () => {
    it('the list shows an hint component when loaded empty', async () => {
        const wrapper = factory()

        await wrapper.setProps({ propModes: [] })

        expect(wrapper.vm.modes.length).toBe(0)
        expect(wrapper.find('[data-test="usemodes-list"]').html()).toContain('common.info.noDataFound')
    })
    it('shows the detail when clicking on a item', async () => {
        const wrapper = factory()
        await flushPromises()
        const openButton = wrapper.find('[data-test="list-item"]')

        await openButton.trigger('click')

        expect(wrapper.find('[data-test="modes-form"]').exists()).toBe(true)
        expect(wrapper.vm.selectedUseMode).toStrictEqual({
            useID: 1,
            id: 656,
            idLov: 547,
            idLovForDefault: 547,
            idLovForMax: null,
            name: 'test 1',
            label: 'test 1',
            description: 'test 1',
            associatedRoles: [],
            associatedChecks: [],
            selectionType: 'LIST',
            multivalue: true,
            manualInput: 0,
            maximizerEnabled: false,
            valueSelection: 'map_in',
            selectedLayer: 'usa_states_file',
            selectedLayerProp: 'test property',
            defaultFormula: null,
            options: null
        })
    })
})
