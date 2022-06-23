import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import KnHint from '@/components/UI/KnHint.vue'
import Card from 'primevue/card'
import Column from 'primevue/column'
import InputText from 'primevue/inputtext'
import DataTable from 'primevue/datatable'
import Dossier from './Dossier.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'
import Button from 'primevue/button'

const mockedActivities = [
    {
        id: 1,
        documentId: 3254,
        activity: 'test2',
        parameters: null,
        partial: 0,
        total: 0,
        status: 'DOWNLOAD',
        progressId: 1,
        hasBinContent: false,
        hasDocBinContent: false,
        pptExists: false,
        creationDate: 1631622971000,
        configContent: null
    },
    {
        id: 22,
        documentId: 3254,
        activity: 'Darkox32',
        parameters: null,
        partial: 0,
        total: 0,
        status: 'DOWNLOAD',
        progressId: 22,
        hasBinContent: false,
        hasDocBinContent: false,
        pptExists: false,
        creationDate: 1631716151000,
        configContent: null
    },
    {
        id: 23,
        documentId: 3254,
        activity: 'Ovo je buopa',
        parameters: null,
        partial: 0,
        total: 0,
        status: 'DOWNLOAD',
        progressId: 23,
        hasBinContent: false,
        hasDocBinContent: false,
        pptExists: false,
        creationDate: 1631716158000,
        configContent: null
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: mockedActivities
        })
    ),
    post: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: []
        })
    ),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(Dossier, {
        attachToDocument: true,
        global: {
            plugins: [createTestingPinia()],
            stubs: { Button, InputText, ProgressBar, DataTable, Column, Toolbar, Card, KnHint },
            mocks: {
                $t: (msg) => msg,

                $confirm,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Dossier loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('the activities list shows an hint when loaded empty', async () => {
        const wrapper = factory()

        wrapper.vm.loading = false

        expect(wrapper.vm.showHint).toBe(true)
        expect(wrapper.find('[data-test="hint"]').exists()).toBe(true)

        wrapper.vm.dossierActivities = mockedActivities
        await nextTick()

        expect(wrapper.vm.showHint).toBe(false)
        expect(wrapper.find('[data-test="hint"]').exists()).toBe(false)
    })
})

describe('Dossier usage', () => {
    it('Launch activity button is disabled if the name is missing or invalid', () => {
        const wrapper = factory()

        expect(wrapper.vm.activity).toStrictEqual({ activityName: '' })
        expect(wrapper.vm.buttonDisabled).toBe(true)

        wrapper.vm.activity.activityName = 'test'
        expect(wrapper.vm.buttonDisabled).toBe(false)
    })
    it('shows a prompt when user click on an activity delete button to delete it', async () => {
        const wrapper = factory()

        wrapper.vm.loading = false
        wrapper.vm.dossierActivities = mockedActivities
        await nextTick()

        const deleteButton = wrapper.find('[data-test="delete-button"]')
        await deleteButton.trigger('click')
        expect($confirm.require).toHaveBeenCalledTimes(1)
    })
    it('adds an activity item in the list when Launch activity button is clicked', async () => {
        const wrapper = factory()

        wrapper.vm.loading = false
        wrapper.vm.dossierActivities = mockedActivities
        wrapper.vm.activity.activityName = 'test'
        await nextTick()

        const datatable = wrapper.find('[data-test="activities-table"]')
        const inputButton = wrapper.find('[data-test="input-button"]')

        await inputButton.trigger('click')
        await nextTick()
        expect(datatable.html()).toContain('test')
    })
})
