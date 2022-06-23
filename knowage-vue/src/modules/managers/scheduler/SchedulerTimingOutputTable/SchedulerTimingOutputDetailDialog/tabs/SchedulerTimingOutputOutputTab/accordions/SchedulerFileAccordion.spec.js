import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import Checkbox from 'primevue/checkbox'
import InputText from 'primevue/inputtext'
import SchedulerFileAccordion from './SchedulerFileAccordion.vue'

const mockedDocument = {
    id: 3211,
    label: 'DM_PromotionMap_file',
    name: 'USA MAP ',
    description: 'USA MAP (file dataset)',
    engine: 'knowagegisengine',
    parameters: [],
    saveasfile: true,
    invalid: {},
    fileName: '',
    zipFileDocument: false,
    zipFileName: ''
}

const factory = () => {
    return mount(SchedulerFileAccordion, {
        props: {
            propDocument: mockedDocument
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
                InputText
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
        expect(wrapper.vm.document.fileName).toStrictEqual('')
        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(true)
    })
})
