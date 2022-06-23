import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Accordion from 'primevue/accordion'
import AccordionTab from 'primevue/accordiontab'
import InputText from 'primevue/inputtext'
import SchedulerJavaClassAccordion from './SchedulerJavaClassAccordion.vue'

const mockedDocument = { id: 3211, label: 'DM_PromotionMap_file', name: 'USA MAP ', description: 'USA MAP (file dataset)', engine: 'knowagegisengine', parameters: [], sendtojavaclass: true, javaclasspath: '', invalid: {} }

const factory = () => {
    return mount(SchedulerJavaClassAccordion, {
        props: {
            propDocument: mockedDocument
        },
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Accordion,
                AccordionTab,
                InputText
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Scheduler Java Class Accordion', () => {
    it('should show a warning icon in the output tab if one or more fields are missing', async () => {
        const wrapper = factory()

        expect(wrapper.vm.document).toStrictEqual(mockedDocument)
        expect(wrapper.vm.document.javaclasspath).toStrictEqual('')
        expect(wrapper.find('[data-test="warning-icon"]').exists()).toBe(true)
    })
})
