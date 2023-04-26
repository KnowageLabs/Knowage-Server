<template>
    <Card class="domainCard" style="height: calc(100vh - 75px)">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ title }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable
                v-model:selection="selectedCategories"
                :value="categoryList"
                class="p-datatable-sm kn-table"
                data-key="categoryId"
                :paginator="true"
                :rows="20"
                responsive-layout="stack"
                breakpoint="960px"
                :scrollable="true"
                scroll-height="flex"
                data-test="data-table"
                @rowSelect="setDirty"
                @rowUnselect="setDirty"
                @rowSelectAll="onSelectAll"
                @rowUnselectAll="onUnselectAll"
            >
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <Column class="kn-column-checkbox" selection-mode="multiple" data-key="categoryId"></Column>
                <Column field="categoryName" :header="$t('common.name')" :style="domainCategoryTabDescriptor.column.header.style"></Column>
            </DataTable>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iCategory } from './../../RolesManagement'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import domainCategoryTabDescriptor from './DomainCategoryTabDescriptor.json'

export default defineComponent({
    name: 'domain-category-tab',
    components: {
        Card,
        Column,
        DataTable
    },
    props: {
        title: String,
        categoryList: Array,
        selected: Array
    },
    emits: ['changed'],
    data() {
        return {
            domainCategoryTabDescriptor,
            selectedCategories: [] as iCategory[]
        }
    },
    watch: {
        selected() {
            this.selectedCategories = this.selected as iCategory[]
        }
    },
    created() {
        this.selectedCategories = this.selected as iCategory[]
    },
    methods: {
        setDirty() {
            this.$emit('changed', this.selectedCategories)
        },
        onSelectAll(event: any) {
            this.selectedCategories = event.data
            this.$emit('changed', this.selectedCategories)
        },
        onUnselectAll() {
            this.selectedCategories = []
            this.$emit('changed', this.selectedCategories)
        }
    }
})
</script>
<style lang="scss" scoped>
.domainCard {
    &:deep(.p-card-body) {
        height: calc(100% - 35px);
        .p-card-content {
            height: 100%;
            padding-bottom: 0;
            .p-paginator-bottom {
                border: none;
            }
        }
    }
}
</style>
