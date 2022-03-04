import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import TimespanIntervalTable from './TimespanIntervalTable.vue'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

const mockedTimespan = {
    name: 'Temporal Test',
    id: 82,
    type: 'temporal',
    definition: [
        {
            from: '04/03/2022',
            to: '05/03/2022',
            fromLocalized: '3/4/22',
            toLocalized: '3/5/22'
        },
        {
            from: '17/03/2022',
            to: '31/03/2022',
            fromLocalized: '17/3/22',
            toLocalized: '31/3/22'
        },
        {
            from: '01/04/2022',
            to: '14/04/2022',
            fromLocalized: '1/4/22',
            toLocalized: '14/04/22'
        }
    ],
    category: '',
    staticFilter: false,
    commonInfo: 'it.eng.spagobi.commons.metadata.SbiCommonInfo@3485798'
}

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn()
}

const factory = () => {
    return mount(TimespanIntervalTable, {
        props: {
            propTimespan: mockedTimespan
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
                TimespanIntervalForm: true,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router
            }
        }
    })
}

describe('Timespan Interval Table', () => {
    it('shows loaded intervals', async () => {
        const wrapper = factory()

        expect(wrapper.vm.timespan).toStrictEqual(mockedTimespan)
        expect(wrapper.html()).toContain('3/4/22')
        expect(wrapper.html()).toContain('3/5/22')
        expect(wrapper.html()).toContain('17/3/22')
        expect(wrapper.html()).toContain('31/3/22')
    })
})
