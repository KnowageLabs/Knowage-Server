import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import KpiDocumentDesignerKpiListCard from './KpiDocumentDesignerKpiListCard.vue'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

import deepcopy from 'deepcopy'

const mockedPropData = {
    kpi: [
        { isSuffix: 'true', name: 'MARKUP', prefixSuffixValue: 'SE', rangeMaxValue: '14', rangeMinValue: '2', vieweas: 'Speedometer', category: 'PROFIT' },
        { isSuffix: 'false', name: 'ROTATION', prefixSuffixValue: 'SA', rangeMaxValue: '23', rangeMinValue: '3', vieweas: 'Speedometer', category: 'PROFIT' }
    ]
}
const mockedKpiList = [
    {
        id: 216,
        version: 0,
        name: 'PROFIT MARGIN',
        author: 'demo_admin',
        dateCreation: 1477312200000,
        active: false,
        enableVersioning: false,
        definition: null,
        cardinality: null,
        placeholder: null,
        category: {
            valueId: 404,
            valueCd: 'PROFIT',
            valueName: 'PROFIT',
            valueDescription: 'PROFIT',
            domainCode: 'KPI_KPI_CATEGORY',
            domainName: 'KPI_KPI_CATEGORY',
            translatedValueDescription: 'PROFIT',
            translatedValueName: 'PROFIT'
        },
        threshold: null
    },
    {
        id: 217,
        version: 0,
        name: 'MARKUP',
        author: 'demo_admin',
        dateCreation: 1477312334000,
        active: false,
        enableVersioning: false,
        definition: null,
        cardinality: null,
        placeholder: null,
        category: {
            valueId: 404,
            valueCd: 'PROFIT',
            valueName: 'PROFIT',
            valueDescription: 'PROFIT',
            domainCode: 'KPI_KPI_CATEGORY',
            domainName: 'KPI_KPI_CATEGORY',
            translatedValueDescription: 'PROFIT',
            translatedValueName: 'PROFIT'
        },
        threshold: null
    },
    {
        id: 218,
        version: 4,
        name: 'ROTATION',
        author: 'demo_admin',
        dateCreation: 1594037177000,
        active: false,
        enableVersioning: false,
        definition: null,
        cardinality: null,
        placeholder: null,
        category: {
            valueId: 404,
            valueCd: 'PROFIT',
            valueName: 'PROFIT',
            valueDescription: 'PROFIT',
            domainCode: 'KPI_KPI_CATEGORY',
            domainName: 'KPI_KPI_CATEGORY',
            translatedValueDescription: 'PROFIT',
            translatedValueName: 'PROFIT'
        },
        threshold: null
    }
]

const $confirm = {
    require: jest.fn()
}

const $router = {
    push: jest.fn()
}

const factory = () => {
    return mount(KpiDocumentDesignerKpiListCard, {
        props: {
            propData: deepcopy(mockedPropData),
            kpiList: mockedKpiList,
            documentType: 'kpi'
        },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                Card,
                Column,
                DataTable,
                Dropdown,
                InputText,
                KpiDocumentDesignerKpiSelectDialog: true,
                ProgressBar,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $confirm,
                $router
            }
        }
    })
}

describe('Kpi Edit Kpi List Card', () => {
    it('loads kpi list and shows datatable', () => {
        const wrapper = factory()

        expect(wrapper.html()).toContain('MARKUP')
        expect(wrapper.html()).toContain('PROFIT')
        expect(wrapper.html()).toContain('ROTATION')
    })

    it('adds kpi on kpi selected', () => {
        const wrapper = factory()

        expect(wrapper.vm.data.kpi).toStrictEqual(mockedPropData.kpi)

        wrapper.vm.onKpiSelected(mockedKpiList)

        expect(wrapper.vm.data.kpi[2]).toStrictEqual({
            category: 'PROFIT',
            isSuffix: 'false',
            name: 'ROTATION',
            prefixSuffixValue: '',
            rangeMaxValue: '',
            rangeMinValue: '',
            vieweas: 'Speedometer'
        })
    })

    it('removes kpi from kpi selected', () => {
        const wrapper = factory()

        expect(wrapper.vm.data.kpi.length).toBe(2)
        expect(wrapper.vm.data.kpi[0]).toStrictEqual(mockedPropData.kpi[0])

        wrapper.vm.deleteKpiAssociation(mockedPropData.kpi[0])

        expect(wrapper.vm.data.kpi.length).toBe(1)
        expect(wrapper.vm.data.kpi[0]).not.toStrictEqual(mockedPropData.kpi[0])
    })
})
