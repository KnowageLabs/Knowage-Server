<template>
    <div>
        <Listbox :class="class" :options="options" :listStyle="settings.listStyle" :filter="settings.filter" :filterPlaceholder="$t('common.search')" filterMatchMode="contains" :filterFields="settings.filterFields" :emptyFilterMessage="$t('common.info.noDataFound')">
            <template #option="slotProps">
                <div class="kn-list-item" @click="itemClicked(slotProps.option)">
                    <div class="kn-list-item-text">
                        <span v-if="settings.titleField !== false">{{ slotProps.option[settings.titleField || 'label'] }}</span>
                        <span class="kn-list-item-text-secondary kn-truncated" v-if="settings.textField !== false">{{ slotProps.option[settings.textField || 'name'] }}</span>
                    </div>
                    <div></div>
                </div>
            </template>
        </Listbox>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'widget-editor-list',
    components: { Listbox },
    props: { class: { type: String }, options: { type: Array }, settings: { type: Object, required: true } },
    emits: ['itemClicked'],
    data() {
        return {}
    },
    async created() {},
    methods: {
        itemClicked(item: any) {
            this.$emit('itemClicked', item)
        }
    }
})
</script>
