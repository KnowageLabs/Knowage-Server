import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import Button from 'primevue/button'
import Card from 'primevue/card'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import SchedulerDetail from './SchedulerDetail.vue'
import Toolbar from 'primevue/toolbar'

const mockedJob = {
    jobName: 'A - With Params',
    jobGroup: 'BIObjectExecutions',
    jobDescription: '',
    jobClass: 'it.eng.spagobi.tools.scheduler.jobs.ExecuteBIDocumentJob',
    jobDurability: true,
    jobRequestRecovery: false,
    jobMergeAllSnapshots: false,
    jobCollateSnapshots: false,
    useVolatility: false,
    jobParameters: [
        {
            name: 'documentLabels',
            value: 'DEMO_Report__1'
        },
        {
            name: 'DEMO_Report__1',
            value: 'age_range=30-40; 40-50; 60-70; 50-60 %26par_brand_name=%26prod_category=%26product_hierarchy=%26'
        },
        {
            name: 'empty',
            value: 'empty'
        }
    ],
    documents: [
        {
            name: 'DEMO_Report',
            nameTitle: 'Store Sales Analysis',
            condensedParameters: ' age_range = 30-40; 40-50; 60-70; 50-60  |  par_brand_name =  |  prod_category =  |  product_hierarchy =  | ',
            parameters: [
                {
                    name: 'age_range',
                    value: '30-40; 40-50; 60-70; 50-60 ',
                    type: 'fixed',
                    iterative: false
                },
                {
                    name: 'par_brand_name',
                    value: '',
                    type: 'fixed',
                    iterative: false
                },
                {
                    name: 'prod_category',
                    value: '',
                    type: 'fixed',
                    iterative: false
                },
                {
                    name: 'product_hierarchy',
                    value: '',
                    type: 'fixed',
                    iterative: false
                }
            ]
        }
    ],
    triggers: [],
    numberOfDocuments: 1,
    edit: true
}

const factory = () => {
    return mount(SchedulerDetail, {
        props: {
            selectedJob: mockedJob,
            id: mockedJob.jobName,
            clone: 'false'
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                Card,
                InputText,
                ProgressBar,
                SchedulerDocumentsTable: true,
                SchedulerTimingOutputTable: true,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Scheduler detail', () => {
    it('should show progress bar when loading', async () => {
        const wrapper = factory()

        wrapper.vm.loading = true

        await nextTick()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('should show disabled name input if opened from list', () => {
        const wrapper = factory()

        expect(wrapper.vm.job).toStrictEqual(mockedJob)
        expect(wrapper.vm.job.edit).toBe(true)

        expect(wrapper.find('[data-test="name-input"]').element.disabled).toBe(true)
    })
    it('should disable the save button if no document or timing has been set', async () => {
        const wrapper = factory()

        wrapper.vm.job.documents = []
        wrapper.vm.job.triggers = []

        await nextTick()

        expect(wrapper.vm.saveDisabled).toBe(true)
        expect(wrapper.find('[data-test="save-button"]').element.disabled).toBe(true)
    })
})
