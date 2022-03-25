<template>
    <Card class="domainCard" :style="domainCategoryTabDescriptor.card.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ title }}
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable
                :value="categoryList"
                v-model:selection="selectedCategories"
                class="p-datatable-sm kn-table"
                dataKey="categoryId"
                :paginator="true"
                :rows="20"
                responsiveLayout="stack"
                breakpoint="960px"
                @rowSelect="setDirty"
                @rowUnselect="setDirty"
                @rowSelectAll="setDirty"
                @rowUnselectAll="setDirty"
                data-test="data-table"
            >
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <Column selectionMode="multiple" :style="domainCategoryTabDescriptor.column.style" dataKey="categoryId"></Column>
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
