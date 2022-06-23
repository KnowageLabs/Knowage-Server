import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import DetailSidebar from './DetailSidebar.vue'
import InputText from 'primevue/inputtext'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Menu from 'primevue/contextmenu'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

const mockedDocument = {
    actions: [
        {
            name: 'detaildataset',
            description: 'Dataset detail'
        },
        {
            name: 'delete',
            description: 'Delete dataset'
        },
        {
            name: 'qbe',
            description: 'Show Qbe'
        },
        {
            name: 'loaddata',
            description: 'Load data'
        }
    ],
    usedByNDocs: 0,
    drivers: [],
    meta: {
        dataset: [],
        columns: []
    },
    description: 'Dataset created from execution of document function_catalog by user demo_user',
    author: 'demo_user',
    tags: [],
    dateIn: '2017-02-08T13:36:04Z',
    catTypeId: null,
    dsTypeCd: 'File',
    name: 'demo_user_function_catalog_salesOutput',
    id: 71,
    owner: 'demo_user',
    label: 'demo_user_function_catalog_salesOutput',
    pars: []
}

const $store = {
    state: {
        user: {}
    }
}

const $router = {
    push: jest.fn()
}

const factory = (viewType) => {
    return mount(DetailSidebar, {
        props: {
            document: mockedDocument,
            visible: true,
            viewType: viewType,
            datasetCategories: []
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
                $store,
                $router
            }
        }
    })
}

describe('Detail Sidebar', () => {
    it('loads proper fields and icons if document type is recent', () => {
        const wrapper = factory('recent')

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)
        expect(wrapper.vm.documentFields[0].value).toBe('documentName')
        expect(wrapper.vm.documentFields[1].value).toBe('documentLabel')
        expect(wrapper.vm.documentFields[2].value).toBe('documentDescription')
        expect(wrapper.vm.documentFields[3].value).toBe('documentType')
        expect(wrapper.vm.documentFields[4].value).toBe('requestTime')

        expect(wrapper.vm.documentButtons[0].icon).toBe('fas fa-play-circle')
    })

    it('loads proper fields and icons if document type is repository', () => {
        const wrapper = factory('repository')

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)
        expect(wrapper.vm.documentFields[0].value).toBe('documentName')
        expect(wrapper.vm.documentFields[1].value).toBe('documentLabel')
        expect(wrapper.vm.documentFields[2].value).toBe('documentDescription')
        expect(wrapper.vm.documentFields[3].value).toBe('documentType')
        expect(wrapper.vm.documentFields[4].value).toBe('requestTime')

        expect(wrapper.vm.documentButtons[0].icon).toBe('fas fa-play-circle')
        expect(wrapper.vm.documentButtons[1].icon).toBe('fas fa-share')
        expect(wrapper.vm.documentButtons[2].icon).toBe('fas fa-trash')
    })

    it('loads proper fields and icons if document type is dataset', () => {
        const wrapper = factory('dataset')

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)
        expect(wrapper.vm.documentFields[0].value).toBe('name')
        expect(wrapper.vm.documentFields[1].value).toBe('label')
        expect(wrapper.vm.documentFields[2].value).toBe('catTypeId')
        expect(wrapper.vm.documentFields[3].value).toBe('description')
        expect(wrapper.vm.documentFields[4].value).toBe('dsTypeCd')
        expect(wrapper.vm.documentFields[5].value).toBe('owner')
        expect(wrapper.vm.documentFields[6].value).toBe('dateIn')

        expect(wrapper.vm.documentButtons[0].icon).toBe('fas fa-eye')
        expect(wrapper.vm.documentButtons[1].icon).toBe('fas fa-question-circle')
        expect(wrapper.vm.documentButtons[2].icon).toBe('fas fa-ellipsis-v')
    })

    it('loads proper fields and icons if document type is analysis', () => {
        const wrapper = factory('analysis')

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)
        expect(wrapper.vm.documentFields[0].value).toBe('name')
        expect(wrapper.vm.documentFields[1].value).toBe('label')
        expect(wrapper.vm.documentFields[2].value).toBe('description')
        expect(wrapper.vm.documentFields[3].value).toBe('typeCode')
        expect(wrapper.vm.documentFields[4].value).toBe('creationUser')
        expect(wrapper.vm.documentFields[5].value).toBe('creationDate')

        expect(wrapper.vm.documentButtons[0].icon).toBe('fas fa-play-circle')
        expect(wrapper.vm.documentButtons[1].icon).toBe('fas fa-edit')
        expect(wrapper.vm.documentButtons[2].icon).toBe('fas fa-ellipsis-v')
    })

    it('loads proper fields and icons if document type is businessModel', () => {
        const wrapper = factory('businessModel')

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)
        expect(wrapper.vm.documentFields[0].value).toBe('name')
        expect(wrapper.vm.documentFields[1].value).toBe('description')

        expect(wrapper.vm.documentButtons[0].icon).toBe('fa fa-search')
    })

    it('loads proper fields and icons if document type is federationDataset', () => {
        const wrapper = factory('federationDataset')

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)
        expect(wrapper.vm.documentFields[0].value).toBe('label')
        expect(wrapper.vm.documentFields[1].value).toBe('name')

        expect(wrapper.vm.documentButtons[0].icon).toBe('fa fa-search')
        expect(wrapper.vm.documentButtons[1].icon).toBe('pi pi-pencil')
        expect(wrapper.vm.documentButtons[2].icon).toBe('fas fa-trash-alt')
    })
})
