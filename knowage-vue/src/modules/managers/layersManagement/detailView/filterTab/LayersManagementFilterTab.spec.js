import { mount } from '@vue/test-utils'
import LayersManagementFilterTab from './LayersManagementFilterTab.vue'
import Listbox from 'primevue/listbox'
import PrimeVue from 'primevue/config'
import Toolbar from 'primevue/toolbar'

const mockedLayer = {
    layerId: 95,
    name: 'Mocked Layout',
    descr: '',
    type: 'File',
    label: 'Mocked Layout',
    baseLayer: false,
    layerDef:
        'eyJwcm9wZXJ0aWVzIjpbIk5hbWUiXSwibGF5ZXJJZCI6IkJvamFuIiwibGF5ZXJMYWJlbCI6IkJvamFuIiwibGF5ZXJOYW1lIjoiQm9qYW4iLCJsYXllcl9maWxlIjoiTGF5ZXJcXEJvamFuIiwibGF5ZXJfdXJsIjoibnVsbCIsImxheWVyX3pvb20iOiJudWxsIiwibGF5ZXJfY2V0cmFsX3BvaW50IjoibnVsbCIsImxheWVyX3BhcmFtcyI6Im51bGwiLCJsYXllcl9vcHRpb25zIjoibnVsbCIsImxheWVyX29yZGVyIjoxfQ==',
    pathFile: 'C:\\Users\\Mocked Layout.sovtic\\Desktop\\Setup\\apache-tomcat-8.5.37/resources\\DEMO\\Layer\\Mocked Layout',
    layerLabel: 'Mocked Layout',
    layerName: 'Mocked Layout',
    layerIdentify: 'Mocked Layout',
    layerURL: null,
    layerOptions: null,
    layerParams: null,
    layerOrder: 1,
    category_id: 435,
    category: {
        valueId: 435,
        domainCd: 'GEO_CATEGORY',
        domainNm: 'Layer Category',
        valueCd: 'Default Layer Category',
        valueNm: 'Default Layer Category',
        valueDs: 'Default Layer Category'
    },
    roles: null,
    properties: [{ property: 'Name' }],
    filebody: null
}

const mockedFilters = [
    {
        property: 'Name'
    },
    {
        property: 'Description'
    },
    {
        property: 'Phone'
    }
]

const factory = () => {
    return mount(LayersManagementFilterTab, {
        props: {
            selectedLayer: mockedLayer,
            propFilters: mockedFilters
        },
        provide: [PrimeVue],
        global: {
            directives: {
                tooltip() {}
            },
            plugins: [],
            stubs: {
                Listbox,
                Toolbar
            },
            mocks: {
                $t: (msg) => msg
            }
        }
    })
}

describe('Layout Management Filter Tab', () => {
    it('show properties correctly', () => {
        const wrapper = factory()

        expect(wrapper.vm.layer).toStrictEqual(mockedLayer)
        expect(wrapper.find('[data-test="available-properties-list"]').html()).toContain('Description')
        expect(wrapper.find('[data-test="selected-properties-list"]').html()).toContain('Name')
    })

    it('moves property when clicked', async () => {
        const wrapper = factory()

        expect(wrapper.vm.layer).toStrictEqual(mockedLayer)

        await wrapper.find('[data-test="available-property-Description"]').trigger('click')

        expect(wrapper.find('[data-test="available-properties-list"]').html()).not.toContain('Description')
        expect(wrapper.find('[data-test="selected-properties-list"]').html()).toContain('Description')
    })
})
