import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import BusinessModelDriversCard from './BusinessModelDriversCard.vue'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Listbox from 'primevue/listbox'
import Toolbar from 'primevue/toolbar'

const factory = () => {
    return mount(BusinessModelDriversCard, {
        props: {
            id: 1,
            drivers: [],
            driversOptions: []
        },
        global: {
            plugins: [createTestingPinia()],
            stubs: {
                BuisnessModelDriverDetail: true,
                Button,
                Card,
                Listbox,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Business Model Management drivers', () => {
    it("shows 'no data' label if no previous drivers are present", () => {
        const wrapper = factory()

        expect(wrapper.vm.businessModelDrivers.length).toBe(0)
        expect(wrapper.find('[data-test="driver-list"]').html()).toContain('common.info.noDataFound')
    })
})
