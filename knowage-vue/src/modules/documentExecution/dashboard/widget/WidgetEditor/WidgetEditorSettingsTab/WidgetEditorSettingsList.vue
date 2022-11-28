<template>
    <div class="dashboard-editor-list-card-container p-m-3">
        <Listbox class="kn-list kn-list-no-border-right dashboard-editor-list" :options="options" optionDisabled="disabled">
            <template #option="slotProps">
                <div class="kn-list-item" :style="descriptor.listStyle.listItem" @click="itemClicked(slotProps.option)" data-test="widget-editor-list-item">
                    <i v-if="slotProps.option.icon" :class="slotProps.option.icon" class="p-mr-2" :style="descriptor.listStyle.listIcon"></i>
                    <div class="kn-list-item-text">
                        <span>{{ $t(slotProps.option.label) }}</span>
                    </div>
                </div>
            </template>
        </Listbox>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import Listbox from 'primevue/listbox'
import descriptor from './WidgetEditorSettingsTabDescriptor.json'

export default defineComponent({
    name: 'widget-editor-list',
    components: { Listbox },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, options: { type: Array } },
    emits: ['itemClicked'],
    data() {
        return {
            descriptor
        }
    },
    async created() {},
    methods: {
        itemClicked(item: any) {
            this.$emit('itemClicked', item)
        }
    }
})
</script>
