<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ title }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <Listbox class="kn-list" :options="items" :filter="true" :filterPlaceholder="$t('common.search')" filterMatchMode="contains" :filterFields="glossaryUsageNavigationCardDescriptor.filterFields" :emptyFilterMessage="$t('managers.glossaryUsage.noWordsPresent', { type: title })">
                <template #empty>{{ $t('managers.glossaryUsage.noWordsPresent', { type: title }) }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item">
                        <div class="kn-list-item-text">
                            <div>
                                <span class="label">{{ slotProps.option.label }}</span>
                                <Button class="p-button-link p-button-sm" icon="pi pi-info-circle" @click="$emit('infoClicked', slotProps.option)" />
                            </div>
                        </div>
                    </div>
                </template>
            </Listbox>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Card from 'primevue/card'
import glossaryUsageNavigationCardDescriptor from './GlossaryUsageNavigationCardDescriptor.json'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'glossary-usage-navigation-card',
    components: { Card, Listbox },
    props: {
        title: { type: String },
        items: { type: Object }
    },
    emits: ['infoClicked'],
    data() {
        return {
            glossaryUsageNavigationCardDescriptor
        }
    },
    async created() {},
    methods: {}
})
</script>

<style lang="scss" scoped>
.label {
    text-transform: uppercase;
}
</style>
