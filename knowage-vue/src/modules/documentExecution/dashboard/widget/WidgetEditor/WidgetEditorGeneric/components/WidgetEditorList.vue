<template>
    <div>
        <Listbox :class="class" :options="options" :listStyle="settings.listStyle" :filter="settings.filter" :filterPlaceholder="$t('common.search')" filterMatchMode="contains" :filterFields="settings.filterFields" :emptyFilterMessage="$t('common.info.noDataFound')">
            <template #option="slotProps">
                <div class="kn-list-item" @click="itemClicked(slotProps.option)">
                    <div class="kn-list-item-text">
                        <i v-if="slotProps.option.icon" :class="slotProps.option.icon" class="p-mr-2"></i>
                        <span v-if="settings.titleField !== false">{{ slotProps.option[settings.titleField || 'label'] }}</span>
                        <span class="kn-list-item-text-secondary kn-truncated" v-if="settings.textField !== false">{{ slotProps.option[settings.textField || 'name'] }}</span>
                    </div>
                    <WidgetEditorButtons :buttons="settings.buttons" @click="buttonClicked"></WidgetEditorButtons>
                </div>
            </template>
        </Listbox>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Listbox from 'primevue/listbox'
import WidgetEditorButtons from './WidgetEditorButtons.vue'

export default defineComponent({
    name: 'widget-editor-list',
    components: { Listbox, WidgetEditorButtons },
    props: { class: { type: String }, options: { type: Array }, settings: { type: Object, required: true } },
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
        }
    }
})
</script>
