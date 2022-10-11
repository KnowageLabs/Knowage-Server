<template>
    <div v-if="widgetModel" class="p-m-2">
        <div class="p-grid p-mx-2">
            <TypeCard v-for="(type, index) of selectorTypes" :widgetModel="widgetModel" :key="index" :selectorType="type" />
        </div>

        <div v-if="showAlignment" class="p-d-flex p-flex-row p-m-2">
            <div v-for="(layout, index) of layouts" :key="index" class="p-m-2">
                <RadioButton :inputId="layout.key" name="layout" :value="layout.name" v-model="widgetModel.settings.configuration.selectorType.alignment" />
                <i :class="layout.icon" class="p-mx-2" />
                <label :for="layout.key">{{ layout.name }}</label>
            </div>
        </div>
        <span v-if="widgetModel.settings.configuration.selectorType.alignment === 'Grid'" class="p-float-label">
            <InputText id="colSize" class="kn-material-input kn-width-full" v-model="widgetModel.settings.configuration.selectorType.columnSize" />
            <label for="colSize" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.uploadTemplate') }} </label>
        </span>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import TypeCard from './SelectorWidgetTypeCard.vue'
import RadioButton from 'primevue/radiobutton'

export default defineComponent({
    name: 'table-widget-rows',
    components: { TypeCard, RadioButton },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    computed: {
        showAlignment(): boolean {
            return this.widgetModel.settings.configuration.selectorType.modality === 'singleValue' || this.widgetModel.settings.configuration.selectorType.modality === 'multiValue'
        }
    },
    data() {
        return {
            selectorTypes: [
                { label: 'singleValue', value: 'singleValue', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/radio.svg' },
                { label: 'multiValue', value: 'multiValue', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/check.svg' },
                { label: 'dropdown', value: 'dropdown', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/dropdown.svg' },
                { label: 'multiDropdown', value: 'multiDropdown', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/multiDropdown.svg' },
                { label: 'date', value: 'date', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/singleDate.svg' },
                { label: 'dateRange', value: 'dateRange', imageUrl: 'http://localhost:8080/knowage/themes/commons/img/cockpit/selectorWidget/multiDate.svg' }
            ],
            layouts: [
                { value: 'vertical', name: 'Vertical', icon: 'fa fa-ellipsis-v' },
                { value: 'horizontal', name: 'Horizontal', icon: 'fa fa-ellipsis-h' },
                { value: 'grid', name: 'Grid', icon: 'fa fa-th' }
            ]
        }
    },
    created() {},
    unmounted() {},
    methods: {}
})
</script>

<style lang="scss" scoped>
#index-column-switch {
    border-bottom: 1px solid #c2c2c2;
}
</style>
