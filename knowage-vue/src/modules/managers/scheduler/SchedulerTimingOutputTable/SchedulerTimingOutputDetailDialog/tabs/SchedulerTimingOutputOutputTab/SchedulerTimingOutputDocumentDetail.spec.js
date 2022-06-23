import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Checkbox from 'primevue/checkbox'
import SchedulerTimingOutputDocumentDetail from './SchedulerTimingOutputDocumentDetail.vue'
import Toolbar from 'primevue/toolbar'

const mockedDocument = {
    labelId: '1__1',
    id: '1',
    label: 'Mocked Document'
}

const factory = () => {
    return mount(SchedulerTimingOutputDocumentDetail, {
        props: {
            propDocument: mockedDocument
        },
        provide: [],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Checkbox,
                SchedulerSnapshotAccordion: true,
                SchedulerFileAccordion: true,
                SchedulerDocumentAccordion: true,
                SchedulerJavaClassAccordion: true,
                SchedulerMailAccordion: true,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Scheduler Output Tab Document detail', () => {
    it('should show in the output tab many expander as the selected checkboxes', async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="snapshot-checkbox"]').trigger('click')
        expect(wrapper.find('[data-test="snapshot-accordion"]').exists()).toBe(true)

        await wrapper.find('[data-test="file-checkbox"]').trigger('click')
        expect(wrapper.find('[data-test="file-accordion"]').exists()).toBe(true)

        await wrapper.find('[data-test="document-checkbox"]').trigger('click')
        expect(wrapper.find('[data-test="document-accordion"]').exists()).toBe(true)

        await wrapper.find('[data-test="java-checkbox"]').trigger('click')
        expect(wrapper.find('[data-test="java-accordion"]').exists()).toBe(true)

        await wrapper.find('[data-test="mail-checkbox"]').trigger('click')
        expect(wrapper.find('[data-test="mail-accordion"]').exists()).toBe(true)
    })
})
