import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import WorkspaceCard from './WorkspaceCard.vue'
import InputText from 'primevue/inputtext'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Menu from 'primevue/contextmenu'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

const mockedDocument = {
    previewFile: '',
    documentLabel: 'demo_user_function_catalog_salesOutput label',
    documentName: 'demo_user_function_catalog_salesOutput',
    documentType: 'Enterprise'
}

const mockedDataset = {
    previewFile: '',
    label: 'Dataset Label',
    name: 'Dataset Name',
    dsTypeCd: 'Dataset Type'
}

const mockedAnalysis = {
    previewFile: '',
    label: 'Analysis Label',
    name: 'Analysis Name',
    typeCode: 'Analysis Type'
}

const mockedBusinessModel = {
    previewFile: '',
    name: 'Business Model Label',
    description: 'Business Model Name',
    dataSourceLabel: 'Business Model Type'
}

const mockedFederationDataset = {
    previewFile: '',
    label: 'Federation Dataset Label',
    name: 'Federation Dataset Name',
    owner: 'Federation Dataset Type'
}

const $store = {
    state: {
        user: {}
    }
}

const $router = {
    push: vi.fn()
}

const factory = (viewType, document) => {
    return mount(WorkspaceCard, {
        props: {
            document: document,
            visible: true,
            viewType: viewType
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Button,
                InputText,
                KnFabButton,
                Menu,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,

                $router
            }
        }
    })
}

describe('Workspace Card', () => {
    it('loads proper fields and icons if document type is repository', () => {
        const wrapper = factory('repository', mockedDocument)

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)

        expect(wrapper.html()).toContain('demo_user_function_catalog_salesOutput')
        expect(wrapper.html()).toContain('demo_user_function_catalog_salesOutput label')
        expect(wrapper.html()).toContain('Enterprise')
    })

    it('loads proper fields and icons if document type is dataset', () => {
        const wrapper = factory('dataset', mockedDataset)

        expect(wrapper.vm.document).toStrictEqual(mockedDataset)

        expect(wrapper.html()).toContain('Dataset Label')
        expect(wrapper.html()).toContain('Dataset Name')
        expect(wrapper.html()).toContain('Dataset Type')
    })

    it('loads proper fields and icons if document type is analysis', () => {
        const wrapper = factory('analysis', mockedAnalysis)

        expect(wrapper.vm.document).toStrictEqual(mockedAnalysis)

        expect(wrapper.html()).toContain('Analysis Label')
        expect(wrapper.html()).toContain('Analysis Name')
        expect(wrapper.html()).toContain('Analysis Type')
    })

    it('loads proper fields and icons if document type is businessModel', () => {
        const wrapper = factory('businessModel', mockedBusinessModel)

        expect(wrapper.vm.document).toStrictEqual(mockedBusinessModel)

        expect(wrapper.html()).toContain('Business Model Label')
        expect(wrapper.html()).toContain('Business Model Name')
        expect(wrapper.html()).toContain('Business Model Type')
    })

    it('loads proper fields and icons if document type is federationDataset', () => {
        const wrapper = factory('federationDataset', mockedFederationDataset)

        expect(wrapper.vm.document).toStrictEqual(mockedFederationDataset)

        expect(wrapper.html()).toContain('Federation Dataset Label')
        expect(wrapper.html()).toContain('Federation Dataset Name')
        expect(wrapper.html()).toContain('Federation Dataset Type')
    })
})
