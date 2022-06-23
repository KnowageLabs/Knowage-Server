import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import MeasureDefinitionPreviewDialog from './MeasureDefinitionPreviewDialog.vue'
import Toolbar from 'primevue/toolbar'

const mockedRule = {
    ruleOutputs: [
        {
            alias: 'Demo alias',
            aliasId: 491,
            author: 'demo_admin',
            category: {
                valueCd: 'Category one'
            },
            hierarchy: null,
            rule: 'Bojan Test 123154',
            type: {
                domainCode: 'KPI_RULEOUTPUT_TYPE',
                domainName: 'KPI Rule Output types',
                translatedValueDescription: 'Measure',
                translatedValueName: 'Measure',
                valueCd: 'MEASURE',
                valueDescription: 'sbidomains.ds.measure',
                valueId: 234,
                valueName: 'sbidomains.nm.measure'
            }
        },
        {
            alias: 'Demo alias 2',
            aliasIcon: 'fa fa-exclamation-triangle icon-used',
            category: {
                valueCd: 'Category two'
            },
            hierarchy: null,
            rule: 'Bojan Test 123154',
            type: {
                domainCode: 'KPI_RULEOUTPUT_TYPE',
                domainName: 'KPI Rule Output types',
                translatedValueDescription: 'Measure',
                translatedValueName: 'Measure',
                valueCd: 'MEASURE',
                valueDescription: 'sbidomains.ds.measure',
                valueId: 234,
                valueName: 'sbidomains.nm.measure'
            }
        }
    ]
}

const mockedColumns = [
    { name: 'column_1', label: 'account_id', type: 'int' },
    { name: 'column_2', label: 'account_parent', type: 'int' },
    { name: 'column_3', label: 'account_description', type: 'string' }
]

const mockedRows = [{ id: 1, column_1: 1000, column_2: '', column_3: 'Assets' }]

const factory = () => {
    return mount(MeasureDefinitionPreviewDialog, {
        props: {
            currentRule: mockedRule,
            placeholders: [],
            columns: mockedColumns,
            propRows: mockedRows
        },
        global: {
            stubs: {
                Button,
                Column,
                DataTable,
                Dialog: true,
                InputText,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('Preview Dialog', () => {
    it('loads with proper data for preview table', () => {
        const wrapper = factory()

        expect(wrapper.vm.rule).toStrictEqual(mockedRule)
        expect(wrapper.vm.placeholders).toStrictEqual([])
        expect(wrapper.vm.columns).toStrictEqual(mockedColumns)
        expect(wrapper.vm.rows).toStrictEqual(mockedRows)
    })
})
