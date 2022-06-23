import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import PrimeVue from 'primevue/config'
import ProgressBar from 'primevue/progressbar'
import SchedulerTimingOutputDetailDialog from './SchedulerTimingOutputDetailDialog.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Toolbar from 'primevue/toolbar'

const mockedTrigger = {
    jobName: 'Output Tab Tests',
    jobGroup: 'BIObjectExecutions',
    chrono: {
        type: 'single',
        parameter: {}
    },
    documents: [
        {
            id: 3211,
            label: 'DM_PromotionMap_file',
            name: 'USA MAP ',
            description: 'USA MAP (file dataset)',
            engine: 'knowagegisengine',
            parameters: []
        }
    ],
    startDateTiming: '2021-10-11T22:00:24.379Z',
    startTimeTiming: '2021-10-12T13:54:24.379Z',
    endDateTiming: null,
    endTimeTiming: null
}

vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation(() =>
        Promise.resolve({
            data: []
        })
    )
}

const factory = () => {
    return mount(SchedulerTimingOutputDetailDialog, {
        props: {
            propTrigger: mockedTrigger,
            visible: true
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Button,
                Dialog,
                ProgressBar,
                SchedulerTimingOutputTimingTab: true,
                SchedulerTimingOutputOutputTab: true,
                SchedulerDocumentAccordion: true,
                SchedulerTimingOutputWarningDialog: true,
                TabView,
                TabPanel,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}

describe('Scheduler Output Detail', () => {
    it('should disable the apply button if a warning is present', async () => {
        const wrapper = factory()

        expect(wrapper.vm.trigger).toStrictEqual(mockedTrigger)
        expect(wrapper.vm.saveDisabled).toBe(true)
    })
})
