import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import TimespanIntervalTable from './TimespanIntervalTable.vue'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

const mockedTimespan = {
    category: '',
    staticFilter: false,
    name: 'Time Test 2 edited twice',
    id: 27,
    type: 'time',
    definition: [
        {
            from: '08:50',
            to: '10:51'
        },
        {
            from: '12:54',
            to: '14:55'
        }
    ],
    commonInfo: 'it.eng.spagobi.commons.metadata.SbiCommonInfo@28141a3d'
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
        expect(wrapper.html()).toContain('08:50')
        expect(wrapper.html()).toContain('10:51')
        expect(wrapper.html()).toContain('12:54')
        expect(wrapper.html()).toContain('14:55')
    })
})
