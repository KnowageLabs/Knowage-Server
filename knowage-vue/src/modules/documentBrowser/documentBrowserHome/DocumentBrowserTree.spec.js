import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import DocumentBrowserTree from './DocumentBrowserTree.vue'
import Tree from 'primevue/tree'

const mockedFolders = [
    {
        id: 538,
        parentId: null,
        name: 'Functionalities',
        codType: 'LOW_FUNCT',
        code: 'Functionalities',
        biObjects: []
    },
    {
        id: 601,
        parentId: 538,
        name: 'Analytical Documents',
        codType: 'LOW_FUNCT',
        code: 'Analytical Documents',
        biObjects: []
    },

    {
        id: 724,
        parentId: 538,
        name: 'Demo Analytical Documents',
        codType: 'LOW_FUNCT',
        code: 'Demo Analytical Documents',
        biObjects: []
    },
    {
        id: 614,
        parentId: null,
        name: 'demo_admin',
        codType: 'USER_FUNCT',
        code: 'ufr_demo_admin',
        biObjects: []
    },
    {
        id: 640,
        parentId: null,
        name: 'demo_user',
        codType: 'USER_FUNCT',
        code: 'ufr_demo_user',
        biObjects: []
    }
]

const $store = {
    state: {
        user: {}
    }
}

const factory = (folders) => {
    return mount(DocumentBrowserTree, {
        props: {
            propFolders: folders
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
            stubs: {
                Tree
            },
            mocks: {
                $t: (msg) => msg,
                $store
            }
        }
    })
}

describe('Document Browser Tree', () => {
    it('should show all folders if no searchtext is provided', async () => {
        const wrapper = factory(mockedFolders)

        expect(wrapper.find('.p-inputtext').wrapperElement.value).toBeFalsy()

        expect(wrapper.vm.nodes.length).toBe(2)
        expect(wrapper.vm.nodes[0].children.length).toBe(2)
        expect(wrapper.vm.nodes[1].children.length).toBe(2)
        expect(wrapper.html()).toContain('Functionalities')
        expect(wrapper.html()).toContain('Personal_Folders')
    })
    it('selects folder and emits proper data on click', async () => {
        const wrapper = factory(mockedFolders)

        expect(wrapper.vm.selectedFolder).toBe(null)

        await wrapper.find('.p-treenode-label').trigger('click')

        expect(wrapper.vm.selectedFolder).toStrictEqual({ codType: 'LOW_FUNCT', code: 'Personal_Folders', createRoles: [], description: 'Personal Folders', id: -1, name: 'Personal_Folders', parentId: null, path: '/Personal-Folders', subfolders: [] })
        expect(wrapper.emitted()).toHaveProperty('folderSelected')
        expect(wrapper.emitted()['folderSelected'][0][0]).toStrictEqual({ codType: 'LOW_FUNCT', code: 'Personal_Folders', createRoles: [], description: 'Personal Folders', id: -1, name: 'Personal_Folders', parentId: null, path: '/Personal-Folders', subfolders: [] })
    })
    it('should show the personal folder folder if the user is an administrator', async () => {
        const wrapper = factory(mockedFolders)

        expect(wrapper.vm.nodes[0].label).toBe('Personal_Folders')
        expect(wrapper.vm.nodes[0].children[0].label).toBe('demo_admin')

        await wrapper.find('.p-tree-toggler').trigger('click')

        expect(wrapper.html()).toContain('Personal_Folders')
        expect(wrapper.html()).toContain('demo_admin')
    })
})
