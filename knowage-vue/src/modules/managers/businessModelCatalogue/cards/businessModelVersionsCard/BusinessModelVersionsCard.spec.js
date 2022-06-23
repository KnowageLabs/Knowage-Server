import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import BusinessModelVersionsCard from './BusinessModelVersionsCard.vue'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import Menu from 'primevue/menu'
import RadioButton from 'primevue/radiobutton'
import Toolbar from 'primevue/toolbar'

const mockedVersions = [
    {
        id: 1,
        fileName: 'MODEL_WITH_3_DRIVERS',
        creationUser: 'biadmin',
        active: false
    },
    {
        id: 2,
        fileName: 'MODEL_WITH_3_DRIVERS',
        creationUser: 'kte ADMIN',
        active: true
    }
]

const $confirm = {
    require: vi.fn()
}

const $router = {
    push: jest.fn(),
    replace: jest.fn()
}

const factory = () => {
    return mount(BusinessModelVersionsCard, {
        props: {
            id: 1,
            versions: []
        },
        global: {
            stubs: {
                Button,
                Card,
                Listbox,
                Menu,
                RadioButton,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,

                $confirm,
                $router
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('Business Model Management Saved versions', () => {
    it("shows 'no saved versions' label if no previous versions are present", () => {
        const wrapper = factory()

        expect(wrapper.vm.businessModelVersions.length).toBe(0)
        expect(wrapper.find('[data-test="versions-list"]').html()).toContain('managers.businessModelManager.noSavedVersions')
    })
    it('shows a selected radio button if a version is selected', async () => {
        const wrapper = factory()

        await wrapper.setProps({ versions: mockedVersions })

        expect(wrapper.vm.businessModelVersions.length).toBe(2)
        expect(wrapper.find('[data-test="version-2"]').html()).toContain('kte ADMIN')
        expect(wrapper.find('[data-test="version-2"]').html()).toContain('aria-checked="true"')
    })
})
