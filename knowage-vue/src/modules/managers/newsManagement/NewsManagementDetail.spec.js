import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import NewsManagementDetail from './NewsManagementDetail.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedNews = {
    id: 1,
    title: 'First news',
    description: 'Description',
    type: 1,
    html: '<p>Test</p>',
    roles: mockedRoles,
    expirationDate: '2019-10-02 00:00:00.0',
    active: true
}
const mockedRoles = [
    {
        id: 1,
        name: '/kte/admin'
    },
    {
        id: 2,
        name: 'user'
    },
    {
        id: 3,
        name: 'dev'
    }
]
vi.mock('axios')

const $http = {
    get: axios.get.mockImplementation((url) => {
        switch (url) {
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/news/1?isTechnical=true':
                return Promise.resolve({ data: mockedNews })
            case import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/roles':
                return Promise.resolve({ data: mockedRoles })
        }
    }),
    post: vi.fn().mockImplementation(() => Promise.resolve())
}

const $store = {
    commit: jest.fn()
}

const $router = {
    replace: jest.fn(),
    push: jest.fn()
}

const factory = () => {
    return mount(NewsManagementDetail, {
        global: {
            stubs: {
                Button,
                ProgressBar,
                NewsDetailCard: true,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $router,
                $http
            }
        }
    })
}

afterEach(() => {
    jest.clearAllMocks()
})

describe('News Management Detail', () => {
    it('save button is disabled if a mandatory input is empty', () => {
        const wrapper = factory()

        expect(wrapper.vm.selectedNews).toStrictEqual({
            type: 1,
            roles: []
        })
        expect(wrapper.vm.invalid).toBe(true)
    })

    it('loads correct news and shows succes info if it is saved', async () => {
        const wrapper = factory()
        wrapper.setProps({ id: '1' })

        await flushPromises()

        expect(wrapper.vm.selectedNews).toStrictEqual({ ...mockedNews, expirationDate: new Date(mockedNews.expirationDate) })

        wrapper.vm.handleSubmit()

        expect($http.post).toHaveBeenCalledTimes(1)
        expect($http.post).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/news', { ...mockedNews, expirationDate: new Date(mockedNews.expirationDate).valueOf() })
    })

    it('shows success info if new data is saved', async () => {
        const wrapper = factory()

        wrapper.vm.selectedNews = mockedNews
        delete wrapper.vm.selectedNews.id

        wrapper.vm.handleSubmit()

        expect($http.post).toHaveBeenCalledTimes(1)
        expect($http.post).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/news', { ...mockedNews, expirationDate: new Date(mockedNews.expirationDate).valueOf() })
    })
    it('close button (X) closes the detail without saving data', async () => {
        const wrapper = factory()
        wrapper.setProps({ id: '1' })

        await wrapper.find('[data-test="close-button"]').trigger('click')

        expect($http.post).toHaveBeenCalledTimes(0)
        expect($router.push).toHaveBeenCalledWith('/news-management')
        expect(wrapper.emitted()).toHaveProperty('closed')
    })
})
