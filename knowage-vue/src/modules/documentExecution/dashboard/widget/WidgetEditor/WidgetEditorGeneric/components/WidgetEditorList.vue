<template>
    <div class="dashboard-editor-list-card-container p-m-3">
        <Listbox class="kn-list kn-list-no-border-right dashboard-editor-list" :options="options">
            <template #option="slotProps">
                <div class="kn-list-item" :style="dataListDescriptor.style.list.listItem" @click="itemClicked(slotProps.option)" data-test="widget-editor-list-item">
                    <i v-if="slotProps.option.icon" :class="slotProps.option.icon" class="p-mr-2" :style="dataListDescriptor.style.list.listIcon"></i>
                    <div class="kn-list-item-text">
                        <span>{{ getTitle(slotProps.option) }}</span>
                        <span class="kn-list-item-text-secondary kn-truncated" v-if="settings.textField != false || settings.translatedTextField != false">{{ getTextField(slotProps.option) }}</span>
                    </div>
                    <WidgetEditorButtons :buttons="settings.buttons" @click="buttonClicked"></WidgetEditorButtons>
                </div>
            </template>
        </Listbox>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/dashboard/Dashboard'
import Listbox from 'primevue/listbox'
import WidgetEditorButtons from './WidgetEditorButtons.vue'
import dataListDescriptor from '../../../../dataset/DatasetEditorDataTab/DatasetEditorDataList/DatasetEditorDataListDescriptor.json'

export default defineComponent({
    name: 'widget-editor-list',
    components: { Listbox, WidgetEditorButtons },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, class: { type: String }, options: { type: Array }, settings: { type: Object, required: true } },
    emits: ['itemClicked', 'buttonClicked'],
    data() {
        return {
            dataListDescriptor
        }
    },
    async created() {},
    methods: {
        itemClicked(item: any) {
            this.$emit('itemClicked', item)
        },
        buttonClicked(item: any) {
            this.$emit('buttonClicked', item)
        },
        getTitle(item: any) {
            if (this.settings.titleField) {
                return item[this.settings.titleField]
            } else if (this.settings.translatedTitleField) {
                return this.$t(item[this.settings.translatedTitleField])
            } else {
                return ''
            }
        },
        getTextField(item: any) {
            if (this.settings.textField) {
                return item[this.settings.textField]
            } else if (this.settings.translatedTextField) {
                return this.$t(item[this.settings.translatedTextField])
            } else {
                return ''
            }
        }
    }
})
</script>
