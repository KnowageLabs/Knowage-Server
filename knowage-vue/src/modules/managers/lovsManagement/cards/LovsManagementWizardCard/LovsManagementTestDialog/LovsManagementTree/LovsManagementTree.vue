<template>
    <div class="p-d-flex p-flex-row">
        <div class="p-col-3">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.lovsManagement.fields') }}
                </template>
            </Toolbar>
            <Listbox
                class="kn-list"
                :options="data"
                :listStyle="lovsManagementTreeDescriptor.listBox.style"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                optionLabel="name"
                filterMatchMode="contains"
                :filterFields="lovsManagementTreeDescriptor.filterFields"
                :emptyFilterMessage="$t('common.info.noDataFound')"
                @change="setSelectedValue($event.value)"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item">
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.name }}</span>
                        </div>
                    </div>
                </template></Listbox
            >
        </div>
        <div class="p-col-9">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    {{ $t('managers.lovsManagement.definition') }}
                </template>
            </Toolbar>
            <DataTable :value="selectedValues" class="p-datatable-sm kn-table" editMode="cell" responsiveLayout="stack" breakpoint="960px" @cell-edit-complete="onCellEditComplete">
                <Column class="kn-truncated" field="level" :header="$t('managers.lovsManagement.level')"></Column>
                <Column class="kn-truncated p-mr-2" field="value" :header="$t('managers.lovsManagement.value')">
                    <template #editor="slotProps">
                        <Dropdown class="p-mr-2" v-model.trim="slotProps.data[slotProps.column.props.field]" optionLabel="name" optionValue="name" :options="options" />
                        <i class="pi pi-pencil edit-icon" />
                    </template>
                    <template #body="slotProps">
                        <span class="p-mr-2">{{ slotProps.data.value }}</span>
                        <i class="pi pi-pencil edit-icon" />
                    </template>
                </Column>
                <Column class="kn-truncated p-mr-2" field="description" :header="$t('managers.lovsManagement.description')">
                    <template #editor="slotProps">
                        <Dropdown class="p-mr-2" v-model.trim="slotProps.data[slotProps.column.props.field]" optionLabel="name" optionValue="name" :options="options" />
                        <i class="pi pi-pencil edit-icon" />
                    </template>
                    <template #body="slotProps">
                        <span class="p-mr-2">{{ slotProps.data.description }}</span>
                        <i class="pi pi-pencil edit-icon" />
                    </template>
                </Column>
                <Column :style="lovsManagementTreeDescriptor.table.iconColumn.style">
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" class="p-button-link" @click="removeValueConfirm(slotProps.index)" />
                    </template>
                </Column>
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
            </DataTable>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import Listbox from 'primevue/listbox'
import lovsManagementTreeDescriptor from './LovsManagementTreeDescriptor.json'

export default defineComponent({
    name: 'lovs-management-tree',
    components: { Column, DataTable, Dropdown, Listbox },
    props: {
        listData: {
            type: Object
        },
        treeModel: {
            type: Array
        }
    },
    emits: ['modelChanged'],
    data() {
        return {
            lovsManagementTreeDescriptor,
            data: {} as any,
            selectedValues: [] as any[],
            options: [] as any[]
        }
    },
    watch: {
        listData() {
            this.loadData()
            this.loadModel()
        },
        treeModel() {
            this.loadData()
            this.loadModel()
        }
    },
    created() {
        this.loadData()
        this.loadModel()
    },
    methods: {
        loadData() {
            this.data = this.listData
            this.options = []
            this.data.forEach((el: any) => this.options.push({ name: el.name, label: el.name }))
        },
        loadModel() {
            this.selectedValues = this.treeModel as any[]

            this.removeUnusedSelectedValues()
        },
        removeUnusedSelectedValues() {
            for (let i = 0; i < this.selectedValues.length; i++) {
                const index = this.data.findIndex((el: any) => el.name === this.selectedValues[i].value)
                if (index === -1) this.selectedValues.splice(i, 1)
            }
        },
        setSelectedValue(value: any) {
            const index = this.selectedValues.findIndex((el: any) => el.level === value.name)
            if (index === -1) {
                this.selectedValues.push({ level: value.name, value: value.name, description: value.name })
            }
            this.$emit('modelChanged', this.selectedValues)
        },
        removeValueConfirm(index: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.removeValue(index)
                }
            })
        },
        removeValue(index: number) {
            this.selectedValues.splice(index, 1)
            this.$emit('modelChanged', this.selectedValues)
        },
        onCellEditComplete(event: any) {
            this.selectedValues[event.index] = event.newData
        }
    }
})
</script>
