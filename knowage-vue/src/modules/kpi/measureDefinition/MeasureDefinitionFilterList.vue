<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary">
        <template #left>{{ header }}</template>
        <template #right>
            <i :class="iconClass" @click="sortArray" data-test="sort-icon"></i>
        </template>
    </Toolbar>
    <Listbox
        class="kn-list kn-flex"
        :options="filters"
        :listStyle="metadataDefinitionTabViewDescriptor.listBox.style"
        :filter="true"
        :filterPlaceholder="$t('common.search')"
        optionLabel="name"
        filterMatchMode="contains"
        :filterFields="metadataDefinitionTabViewDescriptor.aliasFilterFields"
        :emptyFilterMessage="$t('common.info.noDataFound')"
        @change="$emit('selected', { value: $event.value.name, type: listType })"
    >
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <template #option="slotProps">
            <div class="kn-list-item" :data-test="'list-item-' + slotProps.option.id">
                <div class="kn-list-item-text">
                    <span>{{ slotProps.option.name }}</span>
                </div>
            </div>
        </template></Listbox
    >
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Listbox from 'primevue/listbox'
import metadataDefinitionTabViewDescriptor from './MetadataDefinitionTabViewDescriptor.json'

export default defineComponent({
    name: 'measure-definition-filter-list',
    components: { Listbox },
    props: { header: { type: String }, list: { type: Array }, listType: { type: String } },
    emits: ['selected'],
    data() {
        return {
            metadataDefinitionTabViewDescriptor,
            filters: [],
            sorted: 'DESC'
        }
    },
    computed: {
        iconClass(): String {
            return this.sorted === 'DESC' ? 'pi pi-arrow-down' : 'pi pi-arrow-up'
        }
    },
    watch: {
        list() {
            this.loadList()
        }
    },
    created() {
        this.loadList()
    },
    methods: {
        loadList() {
            this.filters = this.list as any
        },
        sortArray() {
            if (this.sorted === 'DESC') {
                this.filters = this.filters.sort((a: any, b: any) => (a.name > b.name ? 1 : -1))
                this.sorted = 'ASC'
            } else {
                this.filters = this.filters.sort((a: any, b: any) => (a.name < b.name ? 1 : -1))
                this.sorted = 'DESC'
            }
        }
    }
})
</script>
<style lang="scss" scoped>
.kn-list {
    border-left: 1px solid $color-borders !important;
}
</style>
