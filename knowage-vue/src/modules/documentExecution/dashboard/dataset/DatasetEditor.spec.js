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
            plugins: [
                createTestingPinia({
                    initialState: {
                        dashboardStore: {
                            dashboards: [
                                {},
                                {
                                    configuration: { datasets: [] }
                                }
                            ]
                        }
                    }
                })
            ],
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
        //WARN: Cannot make the tests because of teleporting dialog...problem since beginning of the project.
    })
})
