import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Tree from 'primevue/tree'
import WorkspaceDocumentTree from './WorkspaceDocumentTree.vue'

const mockedFolders = [
    {
        functId: 41,
        parentFunct: null,
        code: 'demo_user',
        name: 'Home'
    },
    {
        functId: 44,
        parentFunct: 41,
        code: 'To be checked',
        name: 'To be checked'
    },
    {
        functId: 50,
        parentFunct: 41,
        code: 'Test Three',
        name: 'Test Three'
    },
    {
        functId: 58,
        parentFunct: 50,
        code: 'Test Four',
        name: 'Test Four'
    },
    {
        functId: 61,
        parentFunct: 41,
        code: 'Test Two',
        name: 'Test Two'
    },
    {
        functId: 63,
        parentFunct: 41,
        code: 'Test One',
        name: 'Test One'
    },
    {
        functId: 65,
        parentFunct: 44,
        code: 'Mocked Folder',
        name: 'Mocked Folder'
    },
    {
        functId: 68,
        parentFunct: 65,
        code: 'Test',
        name: 'Test'
    },
    {
        functId: 70,
        parentFunct: 68,
        code: 'Mocked',
        name: 'Mocked'
    },
    {
        functId: 71,
        parentFunct: 68,
        code: 'vsvds',
        name: 'xccvx',
        descr: null,
        path: '/demo_user/To%20be%20checked/Bojasfsaf/dvsdvs/vsvds',
        prog: 1,
        timeIn: 1635779989000,
        userIn: 'demo_user'
    }
]

const $store = {
    state: {
        user: {}
    }
}

const $router = {
    push: vi.fn()
}

const factory = () => {
    return mount(WorkspaceDocumentTree, {
        props: {
            propFolders: mockedFolders,
            mode: 'select'
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Button,
                InputText,
                Tree
            },
            mocks: {
                $t: (msg) => msg,

                $router
            }
        }
    })
}

describe('Workspace Document Tree', () => {
    it('should show a folder tree', async () => {
        const wrapper = factory()

        expect(wrapper.vm.folders).toStrictEqual(mockedFolders)
        expect(wrapper.html()).toContain('Home')

        await wrapper.find('.p-tree-toggler-icon').trigger('click')

        expect(wrapper.html()).toContain('To be checked')
        expect(wrapper.html()).toContain('Test One')
    })
})
