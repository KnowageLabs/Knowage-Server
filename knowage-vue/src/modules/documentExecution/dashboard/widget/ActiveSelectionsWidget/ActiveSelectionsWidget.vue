<template>
    <div class="active-selections-widget">
        <div v-if="widgetType === 'list' && dataToShow.length > 0">table</div>

        <div v-if="widgetType === 'chips' && dataToShow.length > 0" class="p-d-flex p-flex-row p-flex-wrap">
            <ActiveSelectionsChips v-for="(value, index) of dataToShow" :key="index" :activeSelection="value" :showDataset="showDataset" :showColumn="showColumn" :style="getChipsStyle()" />
        </div>

        <Message v-if="dataToShow.length == 0" class="p-mx-2" severity="info" :closable="false">{{ noSelectionsMessage }}</Message>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../Dashboard'
import ActiveSelectionsChips from './ActiveSelectionsWidgetChips.vue'
import Message from 'primevue/message'
import { getWidgetStyleByTypeWithoutValidation } from '../TableWidget/TableWidgetHelper'

export default defineComponent({
    name: 'datasets-catalog-datatable',
    components: { ActiveSelectionsChips, Message },
    props: {
        propWidget: { type: Object as PropType<IWidget>, required: true },
        dataToShow: { type: Array as any, required: true }
    },
    emits: ['close'],
    computed: {
        widgetType(): boolean {
            return this.propWidget.settings.configuration.type || null
        },
        showDataset(): boolean {
            return this.propWidget.settings.configuration.valuesManagement.showDataset || false
        },
        showColumn(): boolean {
            return this.propWidget.settings.configuration.valuesManagement.showColumn || false
        },
        noSelectionsMessage(): string {
            let noSelections = this.propWidget.settings.configuration.noSelections
            if (noSelections.enabled) return noSelections.customText
            else return 'TODO: PUT THIS INTO EN_US No Active Selections'
        }
    },
    data() {
        return {}
    },
    setup() {},
    created() {},
    updated() {},
    methods: {
        getChipsStyle() {
            let height = this.propWidget.settings.style.chips.height
            return getWidgetStyleByTypeWithoutValidation(this.propWidget, 'chips') + `height: ${height != 0 ? height : 25}px`
        }
    }
})
</script>
