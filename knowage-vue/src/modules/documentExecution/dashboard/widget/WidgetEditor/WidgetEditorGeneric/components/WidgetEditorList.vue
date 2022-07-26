<template>
    <div>
        <label v-if="settings.label" class="kn-material-input-label">{{ $t(settings.label) }}</label>
        <Listbox :class="class" :options="options" :listStyle="settings.listStyle" :filter="settings.filter" :filterPlaceholder="$t('common.search')" filterMatchMode="contains" :filterFields="settings.filterFields" :emptyFilterMessage="$t('common.info.noDataFound')">
            <template #option="slotProps">
                <div class="kn-list-item" @click="itemClicked(slotProps.option)">
                    <i v-if="slotProps.option.icon" :class="slotProps.option.icon" class="p-mr-2"></i>
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
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import Listbox from 'primevue/listbox'
import WidgetEditorButtons from './WidgetEditorButtons.vue'

export default defineComponent({
    name: 'widget-editor-list',
    components: { Listbox, WidgetEditorButtons },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, class: { type: String }, options: { type: Array }, settings: { type: Object, required: true } },
    emits: ['itemClicked', 'buttonClicked'],
    data() {
        return {}
    },
    async created() {},
    methods: {
        itemClicked(item: any) {
            console.log('itemClicked() - item: ', item)
            this.$emit('itemClicked', item)
        },
        buttonClicked(item: any) {
            console.log('buttonClicked() - item: ', item)
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
