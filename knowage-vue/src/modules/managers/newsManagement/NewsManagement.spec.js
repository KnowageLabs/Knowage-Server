import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import axios from 'axios'
import Button from 'primevue/button'
import flushPromises from 'flush-promises'
import Listbox from 'primevue/listbox'
import NewsManagement from './NewsManagement.vue'
import ProgressBar from 'primevue/progressbar'
import Toolbar from 'primevue/toolbar'

const mockedNews = [
    {
        id: 1,
        title: 'Test',
        description: 'Test',
        type: 1
    },
    {
        id: 2,
        title: 'Dummy item',
        description: 'Dummy description',
        type: 2
    },
    {
        id: 3,
        title: 'Another item',
        description: 'Another description',
        type: 3
    }
]

vi.mock('axios')

const $http = {
    get: vi.fn().mockImplementation(() => Promise.resolve({ data: mockedNews })),
    delete: vi.fn().mockImplementation(() => Promise.resolve())
}

const $confirm = {
    require: vi.fn()
}

const $store = {
    commit: jest.fn()
}

const $router = {
    push: jest.fn()
}

const factory = () => {
    return mount(NewsManagement, {
        global: {
            plugins: [],
            stubs: {
                Button,
                Listbox,
                ProgressBar,
                Toolbar,
                routerView: true
            },
            mocks: {
                $t: (msg) => msg,
                $store,
                $confirm,
                $router,
                $http
            }
        }
    })
}

afterEach(() => {
    vi.clearAllMocks()
})

describe('News Management loading', () => {
    it('show progress bar when loading', () => {
        const wrapper = factory()

        expect(wrapper.vm.loading).toBe(true)
        expect(wrapper.find('[data-test="progress-bar"]').exists()).toBe(true)
    })
    it('the list shows "no data" label when loaded empty', async () => {
        $http.get.mockReturnValueOnce(Promise.resolve({ data: [] }))
        const wrapper = factory()

        await flushPromises()

        expect(wrapper.vm.newsList.length).toBe(0)
        expect(wrapper.find('[data-test="news-list"]').html()).toContain('common.info.noDataFound')
    })
})

describe('News Management', () => {
    it('deletes news clicking on delete icon', async () => {
        const wrapper = factory()
        await flushPromises()

        expect(wrapper.vm.newsList.length).toBe(3)

        const deleteButton = wrapper.find('[data-test="delete-button"]')
        await deleteButton.trigger('click')

        expect($confirm.require).toHaveBeenCalledTimes(1)

        await flushPromises()

        await wrapper.vm.deleteNews(mockedNews[0])
        expect($http.delete).toHaveBeenCalledTimes(1)
        expect($http.delete).toHaveBeenCalledWith(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/news/' + 1)
    })
    it('changes url when the "+" button is clicked', async () => {
        const wrapper = factory()
        const openButton = wrapper.find('[data-test="open-form-button"]')

        await openButton.trigger('click')

        expect($router.push).toHaveBeenCalledWith('/news-management/new-news')
    })
    it('changes url with clicked row id when a row is clicked', async () => {
        const wrapper = factory()
        await flushPromises()
        await wrapper.find('[data-test="list-item"]').trigger('click')

        expect($router.push).toHaveBeenCalledWith('/news-management/' + 1)
    })
})

describe('News Management Search', () => {
    it('filters the list if a Title (or description) is provided', async () => {
        const wrapper = factory()
        await flushPromises()
        const newsList = wrapper.find('[data-test="news-list"]')
        const searchInput = newsList.find('input')

        expect(newsList.html()).toContain('Test')
        expect(newsList.html()).toContain('Dummy item')
        expect(newsList.html()).toContain('Another item')

        // Title
        await searchInput.setValue('item')
        await newsList.trigger('filter')
        expect(newsList.html()).not.toContain('Test')
        expect(newsList.html()).toContain('Dummy item')
        expect(newsList.html()).toContain('Another item')

        // Descrpiton
        await searchInput.setValue('Test')
        await newsList.trigger('filter')
        expect(newsList.html()).toContain('Test')
        expect(newsList.html()).not.toContain('Dummy item')
        expect(newsList.html()).not.toContain('Another item')

        // News type
        await searchInput.setValue('Warning')
        await newsList.trigger('filter')
        expect(newsList.html()).not.toContain('Test')
        expect(newsList.html()).not.toContain('Dummy item')
        expect(newsList.html()).toContain('Another item')
    })
    it('returns no data if the Title is not present', async () => {
        const wrapper = factory()
        await flushPromises()
        const newsList = wrapper.find('[data-test="news-list"]')
        const searchInput = newsList.find('input')

        expect(newsList.html()).toContain('Test')
        expect(newsList.html()).toContain('Dummy item')
        expect(newsList.html()).toContain('Another item')

        await searchInput.setValue('not present value')
        await newsList.trigger('filter')

        expect(newsList.html()).not.toContain('Test')
        expect(newsList.html()).not.toContain('Dummy item')
        expect(newsList.html()).not.toContain('Another item')
    })
})
