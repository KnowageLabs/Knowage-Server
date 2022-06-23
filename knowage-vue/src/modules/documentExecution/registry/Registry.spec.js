import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Registry from './Registry.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedRegistry = {
    metaData: {
        totalProperty: 'results',
        root: 'rows',
        id: 'id',
        fields: [
            'recNo',
            { name: 'column_1', header: 'store_id', dataIndex: 'column_1', type: 'int', precision: 0, scale: 0, format: '#,###', multiValue: false, defaultValue: null },
            { name: 'column_2', header: 'store_name', dataIndex: 'column_2', type: 'string', multiValue: false, defaultValue: null },
            { name: 'column_3', header: 'store_type', dataIndex: 'column_3', type: 'string', multiValue: false, defaultValue: null },
            { name: 'column_4', header: 'store_city', dataIndex: 'column_4', type: 'string', multiValue: false, defaultValue: null },
            { name: 'column_5', header: 'store_state', dataIndex: 'column_5', type: 'string', multiValue: false, defaultValue: null },
            { name: 'column_6', header: 'florist', dataIndex: 'column_6', type: 'boolean', multiValue: false, defaultValue: null },
            { name: 'column_7', header: 'coffee_bar', dataIndex: 'column_7', type: 'boolean', multiValue: false, defaultValue: null },
            { name: 'column_8', header: 'video_store', dataIndex: 'column_8', type: 'boolean', multiValue: false, defaultValue: null },
            { name: 'column_9', header: 'first_opened_date', dataIndex: 'column_9', type: 'timestamp', dateFormat: 'd/m/Y H:i:s.uuu', dateFormatJava: 'dd/MM/yyyy HH:mm:ss.SSS', multiValue: false, defaultValue: null },
            { name: 'column_10', header: 'store_sqft', dataIndex: 'column_10', type: 'int', precision: 0, scale: 0, format: '#,###', multiValue: false, defaultValue: null },
            { name: 'column_11', header: 'sales_city', dataIndex: 'column_11', type: 'string', multiValue: false, defaultValue: null }
        ],
        mandatory: [],
        maxSize: null,
        columnsInfos: [],
        summaryCellsCoordinates: [],
        summaryColorCellsCoordinates: []
    },
    results: 24,
    rows: [
        { id: 1, column_1: 91, column_2: 'Bojan', column_3: 'Supermarket', column_4: '', column_5: '', column_6: true, column_7: false, column_8: false, column_9: '', column_10: 35345, column_11: 'Colma' },
        { id: 2, column_1: 57, column_2: 'Hmm', column_3: 'Supermarket', column_4: '', column_5: '', column_6: true, column_7: true, column_8: false, column_9: '', column_10: 241241, column_11: 'Altadena' },
        { id: 3, column_1: 11, column_2: 'Store 11', column_3: 'Supermarket', column_4: 'Portland', column_5: 'OR', column_6: false, column_7: true, column_8: false, column_9: '', column_10: 20319, column_11: 'Guadalajara' },
        { id: 4, column_1: 1, column_2: 'Store 11', column_3: 'Supermarket', column_4: 'Salem', column_5: 'OR', column_6: true, column_7: false, column_8: false, column_9: '2021-08-09 15:57:40.0', column_10: 30251, column_11: 'Acapulco' },
        { id: 5, column_1: 12, column_2: 'Store 124', column_3: 'Deluxe Supermarket', column_4: 'Hidalgo', column_5: 'Zacatecas', column_6: true, column_7: true, column_8: true, column_9: '', column_10: 30584, column_11: 'Hidalgo' },
        { id: 6, column_1: 13, column_2: 'Store 13', column_3: 'Deluxe Supermarket', column_4: 'Salem', column_5: 'OR', column_6: true, column_7: true, column_8: true, column_9: '', column_10: 27694, column_11: 'Salem' },
        { id: 7, column_1: 15, column_2: 'Store 15', column_3: 'Supermarket', column_4: 'Seattle', column_5: 'WA', column_6: false, column_7: true, column_8: false, column_9: '', column_10: 21215, column_11: 'Seattle' },
        { id: 8, column_1: 17, column_2: 'Store 17', column_3: 'Deluxe Supermarket', column_4: 'Tacoma', column_5: 'WA', column_6: true, column_7: true, column_8: false, column_9: '', column_10: 33858, column_11: 'Tacoma' },
        { id: 9, column_1: 18, column_2: 'Store 18', column_3: 'Mid-Size Grocery', column_4: 'Hidalgo', column_5: 'Zacatecas', column_6: false, column_7: false, column_8: false, column_9: '', column_10: 38382, column_11: 'Hidalgo' },
        { id: 10, column_1: 2, column_2: 'Store 2', column_3: 'Small Grocery', column_4: 'Bellingham', column_5: 'WA', column_6: false, column_7: true, column_8: false, column_9: '2021-08-09 15:57:40.0', column_10: 28206, column_11: 'Bellingham' },
        { id: 11, column_1: 20, column_2: 'Store 20', column_3: 'Mid-Size Grocery', column_4: 'Victoria', column_5: 'BC', column_6: true, column_7: true, column_8: false, column_9: '', column_10: 34452, column_11: 'Victoria' },
        { id: 12, column_1: 21, column_2: 'Store 21', column_3: 'Deluxe Supermarket', column_4: 'San Andres', column_5: 'DF', column_6: false, column_7: true, column_8: false, column_9: '', column_10: '', column_11: 'San Andres' },
        { id: 13, column_1: 22, column_2: 'Store 22', column_3: 'Small Grocery', column_4: 'Walla Walla', column_5: 'WA', column_6: false, column_7: false, column_8: false, column_9: '', column_10: '', column_11: 'Walla Walla' },
        { id: 14, column_1: 4, column_2: 'Store 4', column_3: 'Deluxe Supermarket', column_4: 'Camacho', column_5: 'Zacatecas', column_6: true, column_7: true, column_8: false, column_9: '2021-08-09 15:57:40.0', column_10: 23759, column_11: 'Camacho' },
        { id: 15, column_1: 5, column_2: 'Store 5', column_3: 'Small Grocery', column_4: 'Guadalajara', column_5: 'Jalisco', column_6: false, column_7: true, column_8: false, column_9: '', column_10: 24597, column_11: 'Guadalajara' },
        { id: 16, column_1: 6, column_2: 'Store 6', column_3: 'Gourmet Supermarket', column_4: 'Beverly Hills', column_5: 'CA', column_6: true, column_7: true, column_8: true, column_9: '', column_10: 23688, column_11: 'Beverly Hills' },
        { id: 17, column_1: 8, column_2: 'Store 8', column_3: 'Deluxe Supermarket', column_4: 'Merida', column_5: 'Yucatan', column_6: true, column_7: true, column_8: true, column_9: '', column_10: 30797, column_11: 'Merida' },
        { id: 18, column_1: 9, column_2: 'Store 9', column_3: 'Mid-Size Grocery', column_4: 'Mexico City', column_5: 'DF', column_6: false, column_7: false, column_8: false, column_9: '', column_10: 36509, column_11: 'Mexico City' },
        { id: 19, column_1: 10, column_2: 'store diesci', column_3: 'Mid-Size Grocery', column_4: 'Orizaba', column_5: 'Veracruz', column_6: true, column_7: false, column_8: false, column_9: '', column_10: 34795, column_11: 'Port Hammond' },
        { id: 20, column_1: 51, column_2: 'Test', column_3: 'Deluxe Supermarket', column_4: '', column_5: '', column_6: true, column_7: true, column_8: false, column_9: '', column_10: 34234, column_11: 'Albany' },
        { id: 21, column_1: 37, column_2: 'TestStoreName.', column_3: 'Mid-Size Grocery', column_4: '', column_5: '', column_6: true, column_7: true, column_8: false, column_9: '', column_10: 45, column_11: 'Anacortes' },
        { id: 22, column_1: 34, column_2: 'tretre', column_3: 'Deluxe Supermarket', column_4: '', column_5: '', column_6: true, column_7: true, column_8: false, column_9: '', column_10: 40, column_11: 'Acapulco' },
        { id: 23, column_1: 33, column_2: 'tweet', column_3: 'Gourmet Supermarket', column_4: '', column_5: '', column_6: false, column_7: true, column_8: false, column_9: '', column_10: 30, column_11: 'Albany' },
        { id: 24, column_1: 58, column_2: 'vsd', column_3: 'Deluxe Supermarket', column_4: '', column_5: '', column_6: true, column_7: false, column_8: false, column_9: '', column_10: '', column_11: 'Arcadia' }
    ],
    registryConfig: {
        pagination: true,
        summaryColor: '#00AAAA',
        filters: [
            { title: 'Store type', presentationType: 'MANUAL', field: 'store_type', isStatic: false, isVisible: false },
            { title: 'Sales city', presentationType: 'COMBO', field: 'sales_city', isStatic: false, isVisible: false }
        ],
        columns: [
            { field: 'store_id', editorType: 'TEXT', isEditable: false, infoColumn: false, isVisible: false, unsigned: false },
            { field: 'store_name', editorType: 'TEXT', isEditable: true, title: 'store name', infoColumn: false, isVisible: true, sorter: 'ASC', unsigned: false },
            { field: 'store_type', editorType: 'COMBO', isEditable: true, title: 'store type', infoColumn: false, isVisible: true, unsigned: false },
            { field: 'store_city', editorType: 'TEXT', isEditable: false, title: 'store city', infoColumn: false, isVisible: true, unsigned: false },
            { field: 'store_state', editorType: 'TEXT', isEditable: false, title: 'store state', infoColumn: false, isVisible: true, unsigned: false },
            { field: 'florist', editorType: 'TEXT', isEditable: true, title: 'has florist', infoColumn: false, isVisible: true, unsigned: false },
            { field: 'coffee_bar', editorType: 'TEXT', isEditable: true, title: 'has coffee bar', infoColumn: false, isVisible: true, unsigned: false },
            { field: 'video_store', editorType: 'TEXT', isEditable: false, title: 'has video store', infoColumn: false, isVisible: true, unsigned: false },
            { field: 'first_opened_date', editorType: 'TEXT', isEditable: true, title: 'first opened date', infoColumn: false, isVisible: true, unsigned: false },
            { field: 'store_sqft', editorType: 'TEXT', isEditable: true, title: 'store square foot', infoColumn: false, isVisible: true, unsigned: false },
            { field: 'sales_city', subEntity: 'rel_region_id_in_region', foreignKey: 'rel_region_id_in_region', editorType: 'COMBO', isEditable: true, title: 'Foreign key - city', infoColumn: false, isVisible: true, unsigned: false }
        ],
        configurations: [{ name: 'enableButtons', value: 'true' }],
        entity: 'it.eng.knowage.meta.stores_for_registry.Store'
    }
}

vi.mock('axios')

const $http = {
    post: vi.fn().mockImplementation((url) => {
        switch (url) {
            case `/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=LOAD_REGISTRY_ACTION&SBI_EXECUTION_ID=1`:
                return Promise.resolve({ data: mockedRegistry })
            default:
                return Promise.resolve({ data: [] })
        }
    })
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(Registry, {
        props: {
            id: '1'
        },
        global: {
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                RegistryDatatable: true,
                RegistryFiltersCard: true,
                ProgressBar,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $confirm,

                $http
            }
        }
    })
}

describe('Registry loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
})
