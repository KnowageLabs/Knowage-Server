import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import Button from 'primevue/button'
import Card from 'primevue/card'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import KnListBox from '@/components/UI/KnListBox/KnListBox.vue'
import flushPromises from 'flush-promises'
import CrossNavigationManagementHint from './CrossNavigationManagementHint.vue'
import Toolbar from 'primevue/toolbar'
import ProgressBar from 'primevue/progressbar'
import crossNavigationManagement from './CrossNavigationManagement.vue'
import PrimeVue from 'primevue/config'


const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: CrossNavigationManagementHint
        },
        {
            path: '/cross-navigation-management',
            component: CrossNavigationManagementHint
        },
        {
            path: '/cross-navigation-management/new-navigation',
            component: null
        },
        {
            path: '/cross-navigation-management/:id',
            props: true,
            component: null
        }
    ]
})

const $confirm = {
    require: jest.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn()
}

const factory = () => {
    return mount(crossNavigationManagement, {
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [router,PrimeVue],
            stubs: {
                Button,
                Card,
                KnFabButton,
                KnListBox,
                Listbox,
                ProgressBar,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router
            }
        }
    })
}


describe("Cross-navigation Management", () => {
    it(
        "shows a prompt when user click on a list item delete button to delete it",
        () => {}
    );
    it("shows and empty detail when clicking on the add button", async () => {
        const wrapper = factory()

        await wrapper.find('[data-test="new-button"]').trigger('click')

        await flushPromises()

        expect($router.push).toHaveBeenCalledWith('/cross-navigation-management/new-navigation')
    });
    it("shows the detail when clicking on a item", () => {});
});