<template>
    <WidgetEditorSettingsList :widgetModel="propWidget" :options="tableDescriptor.settingsListOptions" @itemClicked="onItemClicked"></WidgetEditorSettingsList>
    <div class="p-d-flex kn-flex kn-overflow">
        <TableWidgetSettingsContainer v-if="propWidget" id="model-div" class="kn-flex kn-overflow p-mx-2 p-my-3" :widgetModel="propWidget" :selectedSetting="selectedSetting" :drivers="drivers" :variables="variables"></TableWidgetSettingsContainer>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '../../../Dashboard'
import tableDescriptor from './TableWidget/TableWidgetSettingsDescriptor.json'
import TableWidgetSettingsContainer from './TableWidget/TableWidgetSettingsContainer.vue'
import WidgetEditorSettingsList from './WidgetEditorSettingsList.vue'

export default defineComponent({
    name: 'widget-editor-settings-tab',
    components: { TableWidgetSettingsContainer, WidgetEditorSettingsList },
    props: { propWidget: { type: Object as PropType<IWidget>, required: true }, drivers: { type: Array }, variables: { type: Array } },
    emits: [],
    data() {
        return {
            tableDescriptor,
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
