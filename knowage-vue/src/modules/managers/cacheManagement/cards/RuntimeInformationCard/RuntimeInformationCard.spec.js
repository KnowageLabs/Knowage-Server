import { mount } from '@vue/test-utils'
import Button from 'primevue/button'
import Card from 'primevue/card'
import RuntimeInformationCard from './RuntimeInformationCard.vue'
import Toolbar from 'primevue/toolbar'

const mockedCache = { totalMemory: 1073741824, availableMemory: 1073709056, availableMemoryPercentage: 100, cachedObjectsCount: 2, cleaningEnabled: true, cleaningQuota: '90%' }
const mockedChartData = [mockedCache.availableMemoryPercentage, 100 - mockedCache.availableMemoryPercentage]

const factory = (item, chartData) => {
    return mount(RuntimeInformationCard, {
        props: {
            item,
            chartData
        },
        global: {
            stubs: { Button, Card, Chart: true, Toolbar },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Cache Management Runtime Information', () => {
    it('when loaded a bar chart is present with available memory percentage', () => {
        const wrapper = factory(mockedCache, mockedChartData)

        expect(wrapper.find('[data-test="chart"]').exists()).toBe(true)

        wrapper.vm.loadChart()

        expect(wrapper.vm.chartData).toStrictEqual(mockedChartData)
        expect(wrapper.vm.cacheData.datasets[0].data).toStrictEqual(mockedChartData)
    })
})
