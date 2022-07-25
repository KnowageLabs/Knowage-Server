<template>
    <div class="p-d-flex">
        <Card class="kn-flex p-m-2">
            <template #content>
                <WidgetEditorList :widgetModel="propWidget" class="kn-list knListBox" :settings="descriptor.tableWidget.listSettings" :options="descriptor.tableWidget.listOptions"></WidgetEditorList>
            </template>
        </Card>
        <div class="kn-flex p-m-2">GENERIC</div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../Dashboard'
import Card from 'primevue/card'
import descriptor from './WidgetEditorSettingsTabDescriptor.json'
import WidgetEditorList from '../WidgetEditorGeneric/components/WidgetEditorList.vue'

export default defineComponent({
    name: 'widget-editor-settings-tab',
    components: { Card, WidgetEditorList },
    props: { propWidget: { type: Object as PropType<IWidget>, required: true } },
    emits: [],
    data() {
        return {
            descriptor
        }
    },
    async created() {},
    methods: {}
})
</script>
