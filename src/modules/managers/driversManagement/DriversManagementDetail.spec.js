import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import Button from 'primevue/button'
import Card from 'primevue/card'
import DriversManagementDetail from './DriversManagementDetail.vue'


vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() =>
        Promise.resolve({
            data: []
        })
    )
}


const factory = () => {
    return mount(DriversManagementDetail, {
        global: {
            plugins: [createTestingPinia({
                initialState: {
                    store: {
                        user: {
                            functionalities: ['MapDriverManagement']
                        }
                    }
                }
            })
            
            ],
            stubs: {
                Button,
                Card
            },
            mocks: {
                $t: (msg) => msg,
                $http
            }
        }
    })
}


describe('Drivers Management Detail', () => {
    it('save button is disabled if one of the mandatory input is invalid', () => {
        const wrapper = factory()
        expect(wrapper.vm.driver).toStrictEqual({})
        expect(wrapper.vm.buttonDisabled).toBe(true)
    })
})
