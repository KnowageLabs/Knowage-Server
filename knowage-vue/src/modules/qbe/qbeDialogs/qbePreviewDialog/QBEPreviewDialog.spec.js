import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import QBEPreviewDialog from './QBEPreviewDialog.vue'
import PrimeVue from 'primevue/config'
import Tooltip from 'primevue/tooltip'
import Toolbar from 'primevue/toolbar'

const mockedData = {
    metaData: {
        totalProperty: 'results',
        root: 'rows',
        id: 'id',
        fields: [
            'recNo',
            {
                name: 'column_1',
                header: 'Product category',
                dataIndex: 'column_1',
                type: 'string',
                multiValue: false
            }
        ]
    },
    results: 47,
    rows: [
        {
            id: 1,
            column_1: 'Specialty'
        },
        {
            id: 2,
            column_1: 'Seafood'
        },
        {
            id: 3,
            column_1: 'Fruit'
        }
    ]
}

const factory = (data) => {
    return mount(QBEPreviewDialog, {
        props: {
            id: '1',
            queryPreviewData: data,
            pagination: { start: 0, limit: 25 }
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue, createTestingPinia()],
            stubs: {
                Button,
                Column,
                DataTable,
                Tooltip,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('QBE Preview Table', () => {
    it('create a preview if one or more fields are added', () => {
        const wrapper = factory(mockedData)

        expect(wrapper.vm.columns).toStrictEqual([{ name: 'column_1', header: 'Product category', dataIndex: 'column_1', type: 'string', multiValue: false }])
        expect(wrapper.vm.rows).toStrictEqual(mockedData.rows)
        expect(wrapper.html()).toContain('Specialty')
        expect(wrapper.html()).toContain('Seafood')
        expect(wrapper.html()).toContain('Fruit')
    })
})
