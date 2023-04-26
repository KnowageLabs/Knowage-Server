<template>
    <Card v-if="dataset">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ dataset.name }}
                </template>
            </Toolbar>
        </template>

        <template #content>
            <Listbox v-model="selectedMetafield" class="metafield-list" :options="dataset.metadata.fieldsMeta" @change="metafieldsSelected($event.value)">
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item p-d-flex p-flex-row" :class="{ ' selected-metafield': slotProps.option.selected }">
                        <div class="kn-list-item-text">
                            <span class="p-text-uppercase">{{ slotProps.option.name }}</span>
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
    import Listbox from 'primevue/listbox'

    export default defineComponent({
        name: 'workspace-federation-definition-metafields-list',
        components: { Card, Listbox },
        props: { propDataset: { type: Object }, selectedMetafields: { type: Array }, resetSelectedMetafield: { type: Boolean } },
        data() {
            return {
                dataset: null as any,
                selectedMetafield: null as any,
                selected: [] as any[]
            }
        },
        watch: {
            propDataset() {
                this.loadDataset()
            },
            selectedMetafields() {
                this.loadSelectedMetafields()
            },
            resetSelectedMetafield() {
                this.selectedMetafield = null
            }
        },
        created() {
            this.loadDataset()
            this.loadSelectedMetafields()
        },
        methods: {
            loadDataset() {
                this.dataset = this.propDataset as any[]
            },
            loadSelectedMetafields() {
                this.selected = this.selectedMetafields as any[]
            },
            metafieldsSelected(metafield: any) {
                if (!this.selectedMetafield) {
                    this.removeMetafieldFromSelected()
                } else if (this.selected.length < 2 && this.dataset.id !== this.selected[0]?.dataset.id && this.dataset.id !== this.selected[1]?.dataset.id) {
                    this.selected.push({ dataset: this.dataset, metafield: metafield })
                } else if (this.dataset.id === this.selected[0]?.dataset.id || this.dataset.id === this.selected[1]?.dataset.id) {
                    this.removeMetafieldFromSelected()
                    this.selected.push({ dataset: this.dataset, metafield: metafield })
                } else if (this.selected.length === 2) {
                    this.removeMetafieldFromSelected()
                    this.selectedMetafield = null
                }
            },
            removeMetafieldFromSelected() {
                const index = this.selected.findIndex((el: any) => el.dataset.id === this.dataset.id)
                if (index !== -1) this.selected.splice(index, 1)
            }
        }
    })
</script>

<style lang="scss" scoped>
    .metafield-list {
        border: none;
        height: 25vh;
        overflow-y: auto;
    }

    .selected-metafield {
        color: #3f51b5;
        background: rgba(63, 81, 181, 0.12);
    }
</style>
