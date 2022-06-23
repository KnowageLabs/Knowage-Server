import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import KpiSchedulerKpiCard from './KpiSchedulerKpiCard.vue'
import Toolbar from 'primevue/toolbar'

const mockedKpis = [
    {
        id: 1,
        name: 'MARKUP',
        author: 'demo_admin',
        dateCreation: 1477312334000
    },
    {
        id: 2,
        name: 'ROTATION',
        author: 'demo_admin',
        dateCreation: 1594037177000
    },
    {
        id: 3,
        name: 'KPI Inventory Turns',
        author: 'demo_admin',
        dateCreation: 1481125085000
    }
]

const factory = () => {
    return mount(KpiSchedulerKpiCard, {
        props: {
            expired: true,
            kpis: mockedKpis
        },
        global: {
            stubs: {
                Button,
                Card,
                Column,
                DataTable,
                Dialog,
                InputText,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('KPI Scheduler Detail', () => {
    it('shows a warning if the schedulation is expired', () => {
        const wrapper = factory()

        expect(wrapper.vm.expired).toBe(true)
        expect(wrapper.vm.showExpired).toBe(true)
        expect(wrapper.html()).toContain('kpi.kpiScheduler.expiredInterval')
        expect(wrapper.find('[data-test="expired-warning"]').exists()).toBe(true)
    })
    it('shows list of kpi for selected scheduler', () => {
        const wrapper = factory()

        expect(wrapper.vm.kpis.length).toBe(3)
        expect(wrapper.find('[data-test="kpi-table"]').html()).toContain('MARKUP')
        expect(wrapper.find('[data-test="kpi-table"]').html()).toContain('ROTATION')
        expect(wrapper.find('[data-test="kpi-table"]').html()).toContain('KPI Inventory Turns')
    })
})
