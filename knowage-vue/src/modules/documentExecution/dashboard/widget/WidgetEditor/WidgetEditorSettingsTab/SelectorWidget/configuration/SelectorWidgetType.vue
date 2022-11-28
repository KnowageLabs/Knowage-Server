<template>
    <div v-if="widgetModel && widgetModel.type == 'selector'" class="p-m-2">
        <div class="p-grid p-mx-2">
            <TypeCard v-for="(type, index) of selectorTypes" :widgetModel="widgetModel" :key="index" :selectorType="type" />
        </div>

        <div v-if="showAlignment" class="p-d-flex p-flex-row p-m-2">
            <div v-for="(layout, index) of descriptor.layouts" :key="index" class="p-m-2">
                <RadioButton :inputId="layout.name" :name="layout.name" :value="layout.value" v-model="widgetModel.settings.configuration.selectorType.alignment" />
                <i :class="layout.icon" class="p-mx-2" />
                <label :for="layout.name">{{ layout.name }}</label>
            </div>
        </div>
        <span v-if="widgetModel.settings.configuration.selectorType.alignment === 'grid'" class="p-float-label">
            <InputText id="colSize" class="kn-material-input kn-width-full" v-model="widgetModel.settings.configuration.selectorType.columnSize" />
            <label for="colSize" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.info.uploadTemplate') }} </label>
        </span>
    </div>

    <div v-if="widgetModel && widgetModel.type == 'selection'" class="p-m-2">
        <div class="p-grid p-mx-2">
            <TypeCard v-for="(type, index) of selectionTypes" :widgetModel="widgetModel" :key="index" :selectorType="type" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import TypeCard from './SelectorWidgetTypeCard.vue'
import RadioButton from 'primevue/radiobutton'
import descriptor from './SelectorWidgetDescriptor.json'

export default defineComponent({
    name: 'selector-widget-type',
    components: { TypeCard, RadioButton },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true } },
    computed: {
        showAlignment(): boolean {
            let modality = this.widgetModel.settings.configuration.selectorType.modality
            return modality === 'singleValue' || modality === 'multiValue' || modality === 'dateRange'
        }
    },
    data() {
        return {
            descriptor,
            selectorTypes: [] as { imageUrl: string; label: string; value: string }[],
            selectionTypes: [] as { imageUrl: string; label: string; value: string }[]
        }
    },
    created() {
        this.loadSelectorTypes()
        this.loadSelectionTypes()
    },
    unmounted() {},
    methods: {
        loadSelectorTypes() {
            this.selectorTypes = this.descriptor.selectorTypes.map((type: { imageUrl: string; label: string; value: string }) => {
                return { imageUrl: import.meta.env.VITE_HOST_URL + type.imageUrl, label: type.label, value: type.value }
            })
        },
        loadSelectionTypes() {
            this.selectionTypes = this.descriptor.selectionTypes.map((type: { imageUrl: string; label: string; value: string }) => {
                return { imageUrl: import.meta.env.VITE_HOST_URL + type.imageUrl, label: type.label, value: type.value }
            })
        }
    }
})
</script>

<style lang="scss" scoped>
#index-column-switch {
    border-bottom: 1px solid #c2c2c2;
}
</style>
