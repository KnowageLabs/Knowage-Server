<template>
    <WidgetEditorList :widgetModel="propWidget" :settings="descriptor[propWidget.type].listSettings" :options="descriptor[propWidget.type].listOptions" @itemClicked="onItemClicked" data-test="widget-editor-settings-list"></WidgetEditorList>
    <div class="p-d-flex kn-flex kn-overflow">
        <!-- <WidgetEditorGeneric v-if="propWidget" id="model-div" class="kn-flex kn-overflow p-mx-2 p-my-3" :widgetModel="propWidget" :propDescriptor="selectedDescriptor" data-test="widget-editor-generic"></WidgetEditorGeneric> -->
        <TableWidgetSettingsContainer v-if="propWidget" id="model-div" class="kn-flex kn-overflow p-mx-2 p-my-3" :widgetModel="propWidget" :selectedSetting="selectedSetting"></TableWidgetSettingsContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../Dashboard'
import descriptor from './WidgetEditorSettingsTabDescriptor.json'
import TableWidgetSettingsContainer from './TableWidget/TableWidgetSettingsContainer.vue'
import WidgetEditorList from '../WidgetEditorGeneric/components/WidgetEditorList.vue'
import WidgetEditorGeneric from '../WidgetEditorGeneric/WidgetEditorGeneric.vue'

export default defineComponent({
    name: 'widget-editor-settings-tab',
    components: { TableWidgetSettingsContainer, WidgetEditorList, WidgetEditorGeneric },
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
            this.selectedDescriptor = { table: item.descriptor }
        }
    }
})
</script>
