import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { createRouter, createWebHistory } from 'vue-router'
import AlertHint from './AlertDefinitionHint.vue'
import Button from 'primevue/button'
import Card from 'primevue/card'
import flushPromises from 'flush-promises'
import InputText from 'primevue/inputtext'
import Listbox from 'primevue/listbox'
import ProgressBar from 'primevue/progressbar'
import AlertDefinition from './AlertDefinition.vue'
import Toolbar from 'primevue/toolbar'
import KnHint from '@/components/UI/KnHint.vue'
import mainStore from '../../../App.store'

const mockedTarget = [
    {
        id: 1,
        name: 'Test Alert 1',
        jsonOptions: '{"actions":[{"jsonActionParameters":"{\\"listDocIdSelected\\":[{\\"DOCUMENT_ID\\":3247}]}","idAction":"63","thresholdValues":["154","155"]}],"kpiId":218,"kpiVersion":4}',
        singleExecution: true,
        eventBeforeTriggerAction: null,
        alertListener: {
            id: 33,
            name: 'KPI Listener',
            className: 'it.eng.spagobi.tools.alert.listener.KpiListener',
            template: 'angular_1.4/tools/alert/listeners/kpiListener/templates/kpiListener.html'
        },
        jobStatus: 'ACTIVE',
        frequency: {
            cron: null,
            startDate: null,
            endDate: null,
            startTime: null,
            endTime: null
        }
    },
    {
        id: 2,
        name: 'Test Alert 2',
        jsonOptions: '{"actions":[{"jsonActionParameters":"{\\"listDocIdSelected\\":[{\\"DOCUMENT_ID\\":3247}]}","idAction":"63","thresholdValues":["154","155"]}],"kpiId":218,"kpiVersion":4}',
        singleExecution: true,
        eventBeforeTriggerAction: null,
        alertListener: {
            id: 33,
            name: 'KPI Listener',
            className: 'it.eng.spagobi.tools.alert.listener.KpiListener',
            template: 'angular_1.4/tools/alert/listeners/kpiListener/templates/kpiListener.html'
        },
        jobStatus: 'ACTIVE',
        frequency: {
            cron: null,
            startDate: null,
            endDate: null,
            startTime: null,
            endTime: null
        }
    },
    {
        id: 3,
        name: 'Test Alert 3',
        jsonOptions: '{"actions":[{"jsonActionParameters":"{\\"listDocIdSelected\\":[{\\"DOCUMENT_ID\\":3247}]}","idAction":"63","thresholdValues":["154","155"]}],"kpiId":218,"kpiVersion":4}',
        singleExecution: true,
        eventBeforeTriggerAction: null,
        alertListener: {
            id: 33,
            name: 'KPI Listener',
            className: 'it.eng.spagobi.tools.alert.listener.KpiListener',
            template: 'angular_1.4/tools/alert/listeners/kpiListener/templates/kpiListener.html'
        },
        jobStatus: 'ACTIVE',
        frequency: {
            cron: null,
            startDate: null,
            endDate: null,
            startTime: null,
            endTime: null
        }
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: mockedTarget
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}

const $route = { path: '/alert' }

const $router = {
    push: vi.fn(),
    replace: vi.fn()
}
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/alert',
            component: AlertHint
        },
        {
            path: '/',
            component: AlertHint
        },
        {
            path: '/new-alert',
            component: null
        },
        {
            path: '/:id',
            props: true,
            component: null
        }
    ]
})

const factory = () => {
    return mount(AlertDefinition, {
        global: {
            plugins: [router, createTestingPinia()],
            stubs: {
                Button,
                Card,
                InputText,
                Listbox,
                ProgressBar,
                Toolbar,
                KnHint
            },
            mocks: {
                $t: (msg) => msg,
                $confirm,
                $route,
                $router,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Alert Definition loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('the list shows "no data" label when loaded empty', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.alertList.length).toBe(0)
        expect(wrapper.find('[data-test="target-list"]').html()).toContain('common.info.noDataFound')
    })
    describe('Alert Definition List', () => {
        it('shows an hint when no item is selected', async () => {
            router.push('/alert')

            await router.isReady()

            await flushPromises()

            const wrapper = factory()

            expect(wrapper.html()).toContain('kpi.alert.hint')
        })
        it('deletes alert when clicking on delete icon', async () => {
            const wrapper = factory()
            const store = mainStore()

            await flushPromises()

            expect(wrapper.vm.alertList.length).toBe(3)

            await wrapper.find('[data-test="delete-button"]').trigger('click')

            expect($confirm.require).toHaveBeenCalledTimes(1)

            await wrapper.vm.deleteAlert(1)
            expect($http.delete).toHaveBeenCalledTimes(1)
            expect($http.delete).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/alert/' + 1 + '/delete')
            expect(store.setInfo).toHaveBeenCalledTimes(1)
        })
        it("opens empty detail form when the '+' button is clicked", async () => {
            const wrapper = factory()
            await wrapper.find('[data-test="open-form-button"]').trigger('click')
            expect($router.push).toHaveBeenCalledWith('/alert/new-alert')
        })
        it('open filled detail when an item is clicked', async () => {
            const wrapper = factory()
            await flushPromises()
            await wrapper.find('[data-test="list-item"]').trigger('click')
            expect($router.push).toHaveBeenCalledWith(`/alert/1`)
        })
    })
})
