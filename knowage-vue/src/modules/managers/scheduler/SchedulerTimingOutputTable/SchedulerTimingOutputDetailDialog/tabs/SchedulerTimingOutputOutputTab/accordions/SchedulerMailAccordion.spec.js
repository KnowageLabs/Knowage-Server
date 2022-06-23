import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import SchedulerMailAccordion from './SchedulerMailAccordion.vue'

const mockedDocument = {
    id: 2341,
    label: 'DEMO_Report',
    name: 'Store Sales Analysis',
    description: 'Store Sales Analysis',
    engine: 'knowagebirtreporteng',
    parameters: ['Brand Name', 'Product Category', 'Age Range', 'Product hierarchy'],
    sendmail: true,
    useFixedRecipients: true,
    invalid: {},
    mailtos: '',
    useDataset: false,
    datasetLabel: '',
    datasetParameter: '',
    mailsubj: '',
    mailtxt: ''
}

const mockedJobInfo = {
    documents: [{ label: 'DEMO_Report', parameters: [] }]
}

const factory = () => {
    return mount(SchedulerMailAccordion, {
        props: {
            propDocument: mockedDocument,
            jobInfo: mockedJobInfo
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [createTestingPinia()],
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

describe('Scheduler Mail Accordion', () => {
    it('should show a warning icon in the output tab if one or more fields are missing', async () => {
        const wrapper = factory()

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)
        expect(wrapper.vm.document.mailsubj).toStrictEqual('')
        expect(wrapper.vm.document.mailtxt).toStrictEqual('')
        expect(wrapper.vm.document.useFixedRecipients).toBe(true)
        expect(wrapper.vm.document.mailtos.length).toBe(0)

        wrapper.vm.validateDocument()
        await nextTick()

        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(true)

        wrapper.vm.document.mailsubj = 'Test subject'
        wrapper.vm.document.mailtxt = 'Test mail text'
        wrapper.vm.document.useFixedRecipients = true
        wrapper.vm.document.mailtos = 'Mail tos'

        wrapper.vm.validateDocument()
        await nextTick()

        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(false)

        wrapper.vm.document.useFixedRecipients = false
        wrapper.vm.document.useExpression = true

        wrapper.vm.validateDocument()
        await nextTick()

        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(true)

        wrapper.vm.document.expression = 'Test expression'

        wrapper.vm.validateDocument()
        await nextTick()

        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(false)

        wrapper.vm.document.useExpression = false
        wrapper.vm.document.useDataset = true

        wrapper.vm.validateDocument()
        await nextTick()

        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(true)

        wrapper.vm.document.datasetLabel = 'Test label'
        wrapper.vm.document.datasetParameter = 'Test parameter'

        wrapper.vm.validateDocument()
        await nextTick()

        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(false)
    })
})
