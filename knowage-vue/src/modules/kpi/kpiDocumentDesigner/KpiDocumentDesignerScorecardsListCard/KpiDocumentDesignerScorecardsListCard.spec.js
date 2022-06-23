import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import KpiDocumentDesignerScorecardsListCard from './KpiDocumentDesignerScorecardsListCard.vue'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

import deepcopy from 'deepcopy'

const mockedPropData = {
    scorecard: {
        name: 'Company Scorecard'
    }
}

const mockedScorecardList = [
    {
        id: 64,
        name: 'Company Scorecard',
        creationDate: 1477324102000,
        author: 'demo_admin',
        perspectives: []
    },
    {
        id: 70,
        name: 'Retail Scorecard',
        creationDate: 1486634111000,
        author: 'demo_admin',
        perspectives: []
    }
]

const $confirm = {
    require: vi.fn()
}

const $router = {
    push: vi.fn()
}

const factory = () => {
    return mount(KpiDocumentDesignerScorecardsListCard, {
        props: {
            propData: deepcopy(mockedPropData),
            scorecardList: mockedScorecardList
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
                KpiDocumentDesignerScorecardSelectDialog: true,
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

describe('Kpi Scorecard List Card', () => {
    it('loads scorecard shows datatable', () => {
        const wrapper = factory()

        expect(wrapper.html()).toContain('Company Scorecard')
    })

    it('adds scorecard on scorecard selected', () => {
        const wrapper = factory()

        expect(wrapper.vm.scorecards[0].name).toBe('Company Scorecard')

        wrapper.vm.onScorecardSelected(mockedScorecardList[1])

        expect(wrapper.vm.scorecards[0].name).toBe('Retail Scorecard')
    })
})
