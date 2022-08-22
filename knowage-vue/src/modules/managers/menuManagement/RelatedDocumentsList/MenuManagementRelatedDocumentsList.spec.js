import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import RelatedDocumentList from './MenuManagementRelatedDocumentList.vue'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import Card from 'primevue/card'
import Toolbar from 'primevue/toolbar'

const mockedRelatedDocumentsList = [
    {
        DOCUMENT_ID: 3,
        DOCUMENT_LABEL: 'python_widget',
        DOCUMENT_NAME: 'Python main development cockpit',
        DOCUMENT_DESCR: '',
        DOCUMENT_AUTH: 'biadmin'
    },
    {
        DOCUMENT_ID: 5,
        DOCUMENT_LABEL: 'matplotlib_samples',
        DOCUMENT_NAME: 'Python widget examples with matplotlib',
        DOCUMENT_DESCR: '',
        DOCUMENT_AUTH: 'biadmin'
    },
    {
        DOCUMENT_ID: 7,
        DOCUMENT_LABEL: 'python_demo_cockpit',
        DOCUMENT_NAME: 'Car performance demo',
        DOCUMENT_DESCR: '',
        DOCUMENT_AUTH: 'biadmin'
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: mockedRelatedDocumentsList
        })
    ),
    post: vi.fn().mockImplementation(() => Promise.resolve()),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(RelatedDocumentList, {
        props: {
            documents: mockedRelatedDocumentsList
        },
        attachToDocument: true,
        global: {
            plugins: [createTestingPinia()],
            stubs: { Button, InputText, ProgressBar, Toolbar, Card },
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

describe('related documents list component loading', () => {
    it('show progress bar when loading', async () => {
        const wrapper = factory()

        expect(wrapper.vm.load).toBe(true)
        expect(wrapper.find('[data-test="related-docs-progress-bar"]').exists()).toBe(true)
    })

    it('shows "no data" label when loaded empty', async () => {
        $http.get.mockReturnValueOnce(
            Promise.resolve({
                data: []
            })
        )
        const wrapper = factory()
        await flushPromises()
        expect(wrapper.find('[data-test="related-documents-list"]').html()).toContain('common.info.noDataFound')
    })
})

describe('related documents prop', () => {
    it('set prop values', async () => {
        const wrapper = factory()

        await wrapper.setProps({
            documents: [
                {
                    DOCUMENT_ID: 3,
                    DOCUMENT_LABEL: 'python_widget',
                    DOCUMENT_NAME: 'Python main development cockpit',
                    DOCUMENT_DESCR: '',
                    DOCUMENT_AUTH: 'biadmin'
                },
                {
                    DOCUMENT_ID: 5,
                    DOCUMENT_LABEL: 'matplotlib_samples',
                    DOCUMENT_NAME: 'Python widget examples with matplotlib',
                    DOCUMENT_DESCR: '',
                    DOCUMENT_AUTH: 'biadmin'
                }
            ]
        })
    })
})
