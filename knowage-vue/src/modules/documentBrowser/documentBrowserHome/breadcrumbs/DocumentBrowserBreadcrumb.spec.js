import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Breadcrumb from 'primevue/breadcrumb'
import DocumentBrowserBreadcrumb from './DocumentBrowserBreadcrumb.vue'

const mockedBreadcrumbs = [
    {
        label: 'Functionalities',
        node: {
            children: [],
            id: 538,
            data: {
                id: 538,
                parentId: null,
                name: 'Functionalities'
            },
            key: 'Functionalities',
            label: 'Functionalities',
            parentId: null
        }
    },
    {
        label: 'Analytical Engines',
        node: {
            children: [],
            id: 724,
            data: {
                id: 724,
                parentId: 538,
                name: 'Analytical Engines'
            },
            key: 'Analytical Engines',
            label: 'Analytical Engines',
            parentId: 538
        }
    },
    {
        label: 'Registry',
        node: {
            children: [],
            id: 725,
            data: {
                id: 725,
                parentId: 724,
                name: 'Registry'
            },
            key: 'Registry',
            label: 'Registry',
            parentId: 724
        }
    }
]

const $store = {
    state: {
        user: {}
    }
}

const factory = () => {
    return mount(DocumentBrowserBreadcrumb, {
        props: {
            breadcrumbs: mockedBreadcrumbs
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Breadcrumb,
                'router-link': true
            },
            mocks: {
                $t: (msg) => msg,
                $store
            }
        }
    })
}

describe('Document Browser Breadcrumbs', () => {
    it('should show the tree path in the breadcrumbs', () => {
        const wrapper = factory()

        expect(wrapper.html()).toContain('Functionalities')
        expect(wrapper.html()).toContain('Analytical Engines')
        expect(wrapper.html()).toContain('Registry')
    })
    it('emits proper data on breadcrumb select', async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="breadcrumb-Registry"]').trigger('click')

        expect(wrapper.emitted()).toHaveProperty('breadcrumbClicked')
        expect(wrapper.emitted()['breadcrumbClicked'][0][0]).toStrictEqual({ label: 'Registry', node: { children: [], data: { id: 725, name: 'Registry', parentId: 724 }, id: 725, key: 'Registry', label: 'Registry', parentId: 724 } })
    })
})
