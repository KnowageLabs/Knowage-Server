<template>
    <grid-layout :layout.sync="layout"
                 :col-num="12"
                 :row-height="30"
                 :is-draggable="draggable"
                 :is-resizable="resizable"
                 :vertical-compact="true"
                 :use-css-transforms="true"
    >
        <grid-item v-for="item in layout"
                   :static="item.static"
                   :x="item.x"
                   :y="item.y"
                   :w="item.w"
                   :h="item.h"
                   :i="item.i"
        >
            <span class="text">{{itemTitle(item)}}</span>
        </grid-item>
    </grid-layout>
</template>

                    <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :sortable="true" :style="[col.style, [col.field == 'valueCheck' ? 'max-width: 200px' : '']]" class="kn-truncated">
                        <template #filter="{ filterModel }">
                            <InputText type="text" v-model="filterModel.value" class="p-column-filter"></InputText>
                        </template>
                        <template #body="slotProps">
                            <span :title="slotProps.data[col.field]">{{ slotProps.data[col.field] }}</span>
                        </template>
                    </Column>
                    <Column :style="configurationManagementDescriptor.table.iconColumn.style" @rowClick="false">
                        <template #body="slotProps">
                            <Button icon="pi pi-trash" class="p-button-link" @click="showDeleteDialog(slotProps.data.id)" :data-test="'delete-button'" />
                        </template>
                    </Column>
                </DataTable>
            </div>
            <div v-if="formVisible">
                <ConfigurationManagementDialog :model="selectedConfiguration" @created="reload" @close="closeForm" data-test="configuration-form"></ConfigurationManagementDialog>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iConfiguration } from './ConfigurationManagement'
import { FilterOperator } from 'primevue/api'
import { filterDefault } from '@/helpers/commons/filterHelper'
import configurationManagementDescriptor from './ConfigurationManagementDescriptor.json'
import { AxiosResponse } from 'axios'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import ConfigurationManagementDialog from './ConfigurationManagementDialog.vue'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'configuration-management',
    components: {
        GridLayout,
        GridItem
    },
    data() {
        return {
            configurationManagementDescriptor: configurationManagementDescriptor,
            configurations: [] as iConfiguration[],
            selectedConfiguration: null as iConfiguration | null,
            columns: configurationManagementDescriptor.columns,
            formVisible: false,
            loading: false,

            filters: {
                global: [filterDefault],
                label: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                name: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                category: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                valueCheck: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                },
                active: {
                    operator: FilterOperator.AND,
                    constraints: [filterDefault]
                }
            } as Object
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.loadConfigurations()
    },
    methods: {
        async loadConfigurations() {
            this.loading = true
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/configs')
                .then((response: AxiosResponse<any>) => {
                    this.configurations = response.data
                })
                .finally(() => (this.loading = false))
        },
        showDeleteDialog(configurationId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteConfiguration(configurationId)
            })
        },
        async deleteConfiguration(configurationId: number) {
            await this.$http.delete(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/configs/' + configurationId).then(() => {
                this.store.setInfo({
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.loadConfigurations()
            })
        },

        showForm(event) {
            if (event) {
                this.selectedConfiguration = event.data
            }
            this.formVisible = true
        },
        closeForm() {
            this.selectedConfiguration = null
            this.formVisible = false
        },
        reload() {
            this.formVisible = false
            this.loadConfigurations()
        }
    }
})
</script>

<style scoped>
.vue-grid-layout {
    background: #eee;
}
.vue-grid-item:not(.vue-grid-placeholder) {
    background: #ccc;
    border: 1px solid black;
}
.vue-grid-item .resizing {
    opacity: 0.9;
}
.vue-grid-item .static {
    background: #cce;
}
.vue-grid-item .text {
    font-size: 24px;
    text-align: center;
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    right: 0;
    margin: auto;
    height: 100%;
    width: 100%;
}
.vue-grid-item .no-drag {
    height: 100%;
    width: 100%;
}
.vue-grid-item .minMax {
    font-size: 12px;
}
.vue-grid-item .add {
    cursor: pointer;
}
.vue-draggable-handle {
    position: absolute;
    width: 20px;
    height: 20px;
    top: 0;
    left: 0;
    background: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='10' height='10'><circle cx='5' cy='5' r='5' fill='#999999'/></svg>") no-repeat;
    background-position: bottom right;
    padding: 0 8px 8px 0;
    background-repeat: no-repeat;
    background-origin: content-box;
    box-sizing: border-box;
    cursor: pointer;
}
</style>