import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import mocks from '../../DatasetEditorTestMocks.json'
import PrimeVue from 'primevue/config'
import DatasetEditorDataDialog from './DatasetEditorDataDialog.vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import Chip from 'primevue/chip'

const factory = () => {
    return mount(DatasetEditorDataDialog, {
        props: {
            visible: true,
            availableDatasetsProp: mocks.availableDatasetsMock,
            selectedDatasetsProp: []
        },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
            stubs: {
                Column,
                DataTable,
                Dialog,
                Chip
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('DatasetEditorDataDialog', () => {
    it('should show an info if no dataset are available in the add dataset dialog', async () => {
        //TODO: Confirm that i cant test because of dialog teleport?
    })
})
