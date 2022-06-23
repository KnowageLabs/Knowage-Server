import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import InputText from 'primevue/inputtext'
import ProgressBar from 'primevue/progressbar'
import DocParameters from './CrossNavigationManagementDocParameters.vue'
import Toolbar from 'primevue/toolbar'

const mockedNavigation = {
    simpleNavigation: {
        id: 704,
        name: 'asd123',
        description: null,
        breadcrumb: null,
        type: 0,
        fromDoc: 'BestProductSingPar',
        fromDocId: 2329,
        toDoc: 'DEMO_Report',
        toDocId: 2341,
        fixedValue: null,
        popupOptions: null
    },
    fromPars: [
        {
            id: 7294,
            name: 'Store Type',
            type: 1,
            fixedValue: null,
            parType: 'STRING',
            links: []
        },
        {
            id: 7295,
            name: 'Store state',
            type: 1,
            fixedValue: null,
            parType: 'STRING',
            links: []
        }
    ],
    toPars: [
        {
            id: 7304,
            name: 'Product Category',
            type: 1,
            fixedValue: null,
            parType: 'STRING',
            links: [
                {
                    id: 7295,
                    name: 'Store state',
                    type: 1,
                    fixedValue: null,
                    parType: 'STRING',
                    links: []
                }
            ]
        },
        {
            id: 7303,
            name: 'Brand Name',
            type: 1,
            fixedValue: null,
            parType: 'STRING',
            links: []
        },
        {
            id: 7305,
            name: 'Age Range',
            type: 1,
            fixedValue: null,
            parType: 'STRING',
            links: []
        },
        {
            id: 7306,
            name: 'Product hierarchy',
            type: 1,
            fixedValue: null,
            parType: 'STRING',
            links: []
        }
    ],
    newRecord: false
}

const factory = () => {
    return mount(DocParameters, {
        props: {
            selectedNavigation: mockedNavigation
        },
        global: {
            plugins: [createTestingPinia()],
            stubs: {
                Button,
                Card,
                InputText,
                Listbox,
                ProgressBar,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Cross-navigation Management Detail', () => {
    it('removes association if the remove association button is clicked (in the input parameters box)', async () => {
        const wrapper = factory()
        await wrapper.find('[data-test="remove"]').trigger('click')
        expect(wrapper.vm.removeLink(7304)).toBeCalled
    })
})
