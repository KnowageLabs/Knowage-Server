import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import LovsManagementPreviewDialog from './LovsManagementPreviewDialog.vue'
import InputText from 'primevue/inputtext'
import PrimeVue from 'primevue/config'

const mockedDataForPreview = {
    metaData: {
        fields: [{ dataIndex: 'education', name: 'education', header: 'education' }],
        root: 'root',
        totalProperty: 'results'
    },
    root: [{ education: 'Partial High School' }, { education: 'Bachelors Degree' }, { education: 'Partial College' }, { education: 'High School Degree' }, { education: 'Graduate Degree' }]
}

const factory = () => {
    return mount(LovsManagementPreviewDialog, {
        props: {
            dataForPreview: mockedDataForPreview,
            pagination: {
                paginationEnd: 20,
                paginationLimit: 20,
                paginationStart: 0}
            },
        global: {
            plugins: [PrimeVue],
            stubs: {
                Button,
                Column,
                DataTable,
                Dialog,
                InputText
            },
            mocks: {
                $t: (msg) => msg,
            }
        }
    })
}

describe('Lovs Management preview dialog', () => {
    it('loads correct data for preview', () => {
        const wrapper = factory()

        expect(wrapper.vm.columns).toStrictEqual([{ field: "education", header: 'education'}])
        expect(wrapper.vm.rows).toStrictEqual([{ education: "Partial High School" }, { education: "Bachelors Degree" }, { education: "Partial College" }, { education: "High School Degree" }, { education: "Graduate Degree" }])
    })
})
