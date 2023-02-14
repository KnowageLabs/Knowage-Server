import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Calendar from 'primevue/calendar'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import HierarchyManagementHierarchiesCard from './HierarchyManagementHierarchiesCard.vue'
import ProgressSpinner from 'primevue/progressspinner'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

const mockedNodes = [
    {
        id: 'root',
        key: '849a16a997f7fea0f1f88a3942cb21a0',
        label: 'M_Teeest',
        leaf: false,
        parent: null,
        data: {},
        children: [
            { id: 'Teeest', key: '"aa13a2e4953f4b8aba2ccbcb6e629304"', label: 'Teeest', leaf: false, parent: { key: '849a16a997f7fea0f1f88a3942cb21a0' }, data: {}, children: [{ id: 'Child', key: 'frewfewfwefwfxdscsdfcwdewdwsxddw', label: 'Child', leaf: true, data: {} }] },
            { id: 'Empty', key: '849a16a997f7fea0f1f88a3942cb21a0', label: 'Empty', leaf: false, parent: { key: '849a16a997f7fea0f1f88a3942cb21a0' }, data: {}, children: [] }
        ]
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() => Promise.resolve({ data: [] }))
}

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(HierarchyManagementHierarchiesCard, {
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue, createTestingPinia()],
            stubs: { Button, Card, Calendar, Checkbox, Dropdown, InputText, ProgressSpinner, Toolbar },
            mocks: {
                $t: (msg) => msg,
                $http,
                $confirm
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Hierarchy Management Hierarchies ard', () => {
    it('Should show a warning popup when saving an empty hierarchy (parent without children)', async () => {
        const wrapper = factory()

        wrapper.vm.updateTreeModel(mockedNodes)
        expect(wrapper.vm.checkIfNodesWithoutChildren(wrapper.vm.treeModel)).toBe(true)
        wrapper.vm.handleSaveHiararchy()
        expect($confirm.require).toHaveBeenCalledTimes(1)
        expect($confirm.require).toHaveBeenCalledWith(expect.objectContaining({ message: 'managers.hierarchyManagement.parentWithoutChildrenConfirm', header: 'managers.hierarchyManagement.saveChanges' }))
    })
})
