<template>
    <div>
        <label v-if="settings.label" class="kn-material-input-label">{{ $t(settings.label) }}</label>
        <div :class="class" :options="options">
            <WidgetEditorFormListItem v-for="(item, index) in items" :key="index" :widgetModel="widgetModel" :settings="settings.itemsSettings" :propItem="item" :itemIndex="index" @change="onChange($event, index)"></WidgetEditorFormListItem>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget } from '@/modules/documentExecution/Dashboard/Dashboard'
import { getModelProperty } from '../WidgetEditorGenericHelper'
import Listbox from 'primevue/listbox'
import WidgetEditorButtons from './WidgetEditorButtons.vue'
import WidgetEditorFormListItem from './WidgetEditorFormListItem.vue'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'widget-editor-form-list',
    components: { Listbox, WidgetEditorButtons, WidgetEditorFormListItem },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, class: { type: String }, options: { type: Array }, settings: { type: Object, required: true } },
    emits: [],
    data() {
        return {
            items: [] as any[]
        }
    },
    created() {
        this.loadItems()
        this.setWatchers()
    },
    methods: {
        loadItems() {
            if (!this.settings.items) return
            let tempItems = []
            const tempFunction = getModelProperty(this.widgetModel, this.settings.items, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') tempItems = tempFunction(this.widgetModel)
            console.log('>>>>>>> LOADED TEMP ITEMS: ', tempItems)
            this.items = tempItems ? deepcopy(tempItems) : []
        },
        setWatchers() {
            console.log('THIS SETTINGS WATCHERS: ', this.settings.watchers)
            if (this.settings.watchers) {
                for (let i = 0; i < this.settings.watchers.length; i++) {
                    this.$watch('widgetModel.' + this.settings.watchers[i], () => this.loadItems(), { deep: true })
                }
            }
        },
        onChange(event: any, index: number) {
            if (!event) return
            if (event.component?.settings?.onUpdate) {
                const tempFunction = getModelProperty(this.widgetModel, event.component.settings.onUpdate, 'getValue', null)
                if (tempFunction && typeof tempFunction === 'function') tempFunction(this.widgetModel, event.item, index)
            }
        }
    }
})
</script>
