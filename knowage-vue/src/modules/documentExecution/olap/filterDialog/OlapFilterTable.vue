<template>
    <DataTable :value="levels" class="p-datatable-sm kn-table p-m-4" responsive-layout="stack" breakpoint="960px">
        <template #empty>{{ $t('common.info.noDataFound') }}</template>

        <Column class="kn-truncated" :header="$t('documentExecution.olap.filterDialog.level')" :style="olapFilterDialogDescriptor.iconColumnStyle">
            <template #body="slotProps">
                <span> {{ slotProps.index + 1 }} </span>
            </template>
        </Column>
        <Column :key="'name'" class="kn-truncated" :field="'LEVEL'" :header="$t('common.name')"> </Column>
        <Column key="value" field="value" :header="$t('documentExecution.olap.filterDialog.driverProfileAttribute')">
            <template #body="slotProps">
                <Dropdown v-model="slotProps.data[slotProps.column.props.field]" class="olap-filter-table-dropdown" :options="options" option-value="value" option-label="label" option-group-value="value" option-group-label="label" option-group-children="items" @change="onLevelUpdate(slotProps.data)">
                    <template #option="slotProps">
                        <span> {{ slotProps.option.label }} </span>
                    </template>
                </Dropdown>
            </template>
        </Column>

        <Column :style="olapFilterDialogDescriptor.iconColumnStyle">
            <template #body="slotProps">
                <Button icon="pi pi-trash" class="p-button-link" @click="remove(slotProps.data)" />
            </template>
        </Column>
    </DataTable>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iParameter, iProfileAttribute } from '../Olap'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import olapFilterDialogDescriptor from './OlapFilterDialogDescriptor.json'

export default defineComponent({
    name: 'olap-filter-table',
    components: { Column, DataTable, Dropdown },
    props: { propFilter: { type: Object }, propLevels: { type: Object }, parameters: { type: Array as PropType<iParameter[]> }, profileAttributes: { type: Array as PropType<iProfileAttribute[]> } },
    data() {
        return {
            olapFilterDialogDescriptor,
            levels: [] as any[],
            options: [
                { label: 'Drivers', items: [] },
                { label: 'Profile Attributes', items: [] }
            ] as any[]
        }
    },
    watch: {
        propLevels() {
            this.loadLevels()
        },
        parameters() {
            this.loadParameters()
        },
        profileAttributes() {
            this.loadProfileAttributes()
        }
    },
    created() {
        this.loadLevels()
        this.loadParameters()
        this.loadProfileAttributes()
        this.removeEmptyOptions()
    },
    methods: {
        loadLevels() {
            this.levels = this.propLevels as any[]
        },
        loadParameters() {
            this.options[0].items = this.parameters?.map((parameter: iParameter) => {
                return { ...parameter, value: parameter.label, label: parameter.label, url: parameter.url, type: 'driver' }
            })
        },
        loadProfileAttributes() {
            this.options[1].items = this.profileAttributes?.map((profileAttribute: iProfileAttribute) => {
                {
                    return { ...profileAttribute, value: profileAttribute.attributeName, label: profileAttribute.attributeName, type: 'profileAttribute' }
                }
            })
        },
        removeEmptyOptions() {
            if (this.options[1].items.length === 0) this.options.splice(1, 1)
            if (this.options[0].items.length === 0) this.options.splice(0, 1)
        },
        remove(level: any) {
            level.value = ''
            level.DRIVER = null
            level.PROFILE_ATTRIBUTE = null
        },
        onLevelUpdate(level: any) {
            const index = this.parameters?.findIndex((parameter: iParameter) => parameter.label === level.value)
            if (index !== -1 && this.parameters) {
                level.DRIVER = level.value
                level.PROFILE_ATTRIBUTE = null
                level.url = this.parameters[index as any].url
            } else {
                level.DRIVER = null
                level.PROFILE_ATTRIBUTE = level.value
                delete level.url
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.olap-filter-table-dropdown {
    max-width: 500px;
}
</style>
