import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import KnListButtonRenderer from './KnListButtonRenderer.vue'
import Menu from 'primevue/menu'

import PrimeVue from 'primevue/config'

const mockedButtons = [
    { emits: 'clone', icon: 'far fa-copy', label: 'common.clone' },
    {
        emits: 'delete',
        icon: 'fas fa-trash-alt',
        label: 'common.delete'
    }
]

const $confirm = {
    require: vi.fn()
}

const factory = () => {
    return mount(KnListButtonRenderer, {
        props: {
            buttons: mockedButtons,
            selectedItem: {}
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [PrimeVue],
            stubs: {
                Button,
                Menu
            },
            mocks: {
                $t: (msg) => msg,

                $confirm
            }
        }
    })
}

describe('KnListButtonRenderer', () => {
    it('loads buttons properly', async () => {
        const wrapper = factory()

        expect(wrapper.vm.filteredButtons).toStrictEqual(mockedButtons)
    })
})
