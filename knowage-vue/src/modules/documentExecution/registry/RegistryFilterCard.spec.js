import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import RegistryFilterCard from './RegistryFilterCard.vue'

const mockedFilter = {
    column: {
        field: 'store_type',
        editorType: 'COMBO',
        isEditable: true,
        title: 'store type',
        infoColumn: false,
        columnInfo: { dataIndex: 'column_3', defaultValue: null, header: 'store_type', multiValue: false, name: 'column_3', type: 'string' },
        isVisible: true,
        unsigned: false
    },
    field: 'store_type',
    presentation: 'MANUAL',
    static: false,
    title: 'Store type',
    visible: true
}

const factory = (filter) => {
    return mount(RegistryFilterCard, {
        props: {
            propFilter: filter,
            entity: 'it.eng.knowage.meta.stores_for_registry.Store',
            id: '1'
        },
        global: {
            plugins: [createTestingPinia()],
            stubs: {
                Dropdown,
                InputText
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Registry loading', () => {
    it('emits changed event when a filter is set and filterChaged is called', () => {
        const wrapper = factory(mockedFilter)

        expect(wrapper.vm.filter).toStrictEqual(mockedFilter)

        wrapper.vm.filter.filterValue = 'Test'

        wrapper.vm.filterChanged()

        expect(wrapper.emitted()).toHaveProperty('changed')
        expect(wrapper.emitted()['changed'][0][0]).toBe('Test')
    })
})
