<template>
    <div class="p-d-flex">
        <Card class="kn-flex p-m-2">
            <template #content>
                <WidgetEditorList :widgetModel="propWidget" class="kn-list knListBox" :settings="descriptor.tableWidget.listSettings" :options="descriptor.tableWidget.listOptions" @itemClicked="onItemClicked"></WidgetEditorList>
            </template>
        </Card>
        <div class="kn-flex p-m-2">
            <WidgetEditorGeneric class="kn-flex p-m-2" :widgetModel="propWidget" :propDescriptor="selectedDescriptor"></WidgetEditorGeneric>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../Dashboard'
import Card from 'primevue/card'
import descriptor from './WidgetEditorSettingsTabDescriptor.json'
import WidgetEditorList from '../WidgetEditorGeneric/components/WidgetEditorList.vue'
import WidgetEditorGeneric from '../WidgetEditorGeneric/WidgetEditorGeneric.vue'

export default defineComponent({
    name: 'widget-editor-settings-tab',
    components: { Card, WidgetEditorList, WidgetEditorGeneric },
    props: { propWidget: { type: Object as PropType<IWidget>, required: true } },
    emits: [],
    data() {
        return {
            descriptor,
            selectedDescriptor: {},
            selectedSetting: ''
        }
    },
    async created() {},
    methods: {
        onItemClicked(item: any) {
            this.selectedSetting = item.value
            // TODO - see about tableWidget key
            this.selectedDescriptor = { tableWidget: item.descriptor }
        }
    }
})
</script>
