import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import PrimeVue from 'primevue/config'
import DatasetEditor from './DatasetEditor.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Button from 'primevue/button'
import Toolbar from 'primevue/toolbar'
import mock from './DatasetEditorTestMocks.json'

const factory = () => {
    return mount(DatasetEditor, {
        props: {
            availableDatasetsProp: mock.availableDatasetsMock,
            filtersDataProp: []
        },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
            stubs: {
                TabView,
                TabPanel,
                Button,
                Toolbar,
                AssociationsTab: true,
                DataTab: true,
                IndexesCard: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('DatasetEditor', () => {
    it('should remove a dataset from the list if the confirm button is clicked in the prompt', async () => {
        const wrapper = factory()
        //TODO: See why isnt store being loaded correctly
    })
})
