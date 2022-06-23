import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import AutoComplete from 'primevue/autocomplete'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import MeasureDefinitionMetadataCard from './MeasureDefinitionMetadataCard.vue'

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

const factory = () => {
    return mount(MeasureDefinitionMetadataCard, {
        props: {
            currentRule: mockedRule,
            tipologiesType: [],
            categories: []
        },

        global: {
            directives: {
                tooltip() {}
            },
            stubs: {
                AutoComplete,
                Column,
                DataTable,
                Dropdown
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Metadata Card', () => {
    it('shows a table with correct aliases', async () => {
        const wrapper = factory()

        await wrapper.setProps({ currentRule: mockedRule })

        const dataTable = wrapper.find('[data-test="metadata-table"]')

        expect(wrapper.vm.rule).toStrictEqual(mockedRule)
        expect(dataTable.html()).toContain('Demo alias')
        expect(dataTable.html()).toContain('Category one')
        expect(dataTable.html()).toContain('Category two')
    })
    it('shows a warning on a metadata row if the row has a new alias', async () => {
        const wrapper = factory()

        await wrapper.setProps({ currentRule: mockedRule })

        const dataTable = wrapper.find('[data-test="metadata-table"]')

        expect(wrapper.vm.rule).toStrictEqual(mockedRule)
        expect(dataTable.html()).toContain('Demo alias 2')
        expect(dataTable.html()).toContain('icon-used')
    })
})
