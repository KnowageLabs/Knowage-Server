import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Calendar from 'primevue/calendar'
import TimespanIntervalForm from './TimespanIntervalForm.vue'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

const mockedTimespan = {
    name: 'Temporal Test',
    id: 82,
    type: 'temporal',
    definition: [
        {
            from: '04/03/2021',
            to: '05/03/2021',
            fromLocalized: '3/4/21',
            toLocalized: '3/5/21'
        },
        {
            from: '17/03/2021',
            to: '31/03/2021',
            fromLocalized: '17/3/21',
            toLocalized: '31/3/21'
        },
        {
            from: '01/04/2021',
            to: '14/04/2021',
            fromLocalized: '1/4/21',
            toLocalized: '14/04/21'
        }
    ],
    category: '',
    staticFilter: false,
    commonInfo: 'it.eng.spagobi.commons.metadata.SbiCommonInfo@3485798'
}

const $confirm = {
    require: vi.fn()
}

const $router = {
    push: vi.fn()
}

const factory = () => {
    return mount(TimespanIntervalForm, {
        props: {
            propTimespan: mockedTimespan
        },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue, createTestingPinia()],
            stubs: {
                Button,
                Calendar,
                TimespanIntervalForm: true,
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

describe('Timespan Interval Form', () => {
    it('creates new interval', async () => {
        const wrapper = factory()

        wrapper.vm.interval = {
            to: new Date(),
            from: new Date()
        }
        expect(wrapper.vm.timespan.definition.length).toBe(3)
        wrapper.find('[data-test="add-button"]').trigger('click')
        expect(wrapper.vm.timespan.definition.length).toBe(4)
    })
})
