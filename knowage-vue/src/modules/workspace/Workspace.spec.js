import { mount } from '@vue/test-utils'
import Workspace from './Workspace.vue'

const factory = () => {
    return mount(Workspace, {
        global: {
            stubs: {
                routerView: true
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Workspace loading', () => {
    it.todo('show progress bar when loading', () => {
        const wrapper = factory()
        expect(wrapper.vm.loading).toBe(true)
    })
})
