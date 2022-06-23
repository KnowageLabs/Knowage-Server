import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import SchedulerDocumentAccordion from './SchedulerDocumentAccordion.vue'

const mockedDocument = {
    id: 2341,
    label: 'DEMO_Report',
    name: 'Store Sales Analysis',
    description: 'Store Sales Analysis',
    engine: 'knowagebirtreporteng',
    parameters: ['Brand Name', 'Product Category', 'Age Range', 'Product hierarchy'],
    saveasdocument: true,
    invalid: {},
    documentname: '',
    useFixedFolder: false,
    funct: []
}

const mockedJobInfo = {
    documents: [{ label: 'DEMO_Report', parameters: [] }]
}

const factory = () => {
    return mount(SchedulerDocumentAccordion, {
        props: {
            propDocument: mockedDocument,
            jobInfo: mockedJobInfo
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Accordion,
                AccordionTab,
                Checkbox,
                Dropdown,
                InputText,
                Message,
                SchedulerDocumentAccordionTree: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Scheduler File Accordion', () => {
    it('should show a warning icon in the output tab if one or more fields are missing', async () => {
        const wrapper = factory()

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)
        expect(wrapper.vm.document.documentname).toStrictEqual('')

        wrapper.vm.validateDocument()

        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(true)
    })

    it('should show a warning icon in the output tab if one the document has output type set but empty', async () => {
        const wrapper = factory()

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)

        wrapper.vm.document.documentname = 'valid name'
        wrapper.vm.document.useFixedFolder = true

        wrapper.vm.validateDocument()

        await nextTick()

        expect(wrapper.vm.document.funct.length).toBe(0)
        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(true)

        wrapper.vm.document.useFixedFolder = false
        wrapper.vm.document.useFolderDataset = true

        wrapper.vm.validateDocument()

        await nextTick()

        expect(wrapper.vm.document.datasetFolderLabel).toBeFalsy()
        expect(wrapper.vm.document.datasetFolderParameter).toBeFalsy()

        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(true)

        wrapper.vm.document.useFolderDataset = false

        wrapper.vm.validateDocument()

        await nextTick()

        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(false)
    })
})
