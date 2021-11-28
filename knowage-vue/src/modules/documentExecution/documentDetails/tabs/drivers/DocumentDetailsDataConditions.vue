<template>
    <div class="data-condition-container p-col">
        {{}}
        <Toolbar class="kn-toolbar kn-toolbar--default">
            <template #left>
                {{ $t('documentExecution.documentDetails.drivers.conditionsTitle') }}
            </template>
            <template #right>
                <Button :label="$t('managers.businessModelManager.addCondition')" class="p-button-text p-button-rounded p-button-plain" :style="mainDescriptor.style.white" />
            </template>
        </Toolbar>
        <Listbox class="kn-list data-condition-list" :options="dataDependencyObjects">
            <template #empty>{{ $t('documentExecution.documentDetails.drivers.noDataCond') }} </template>
            <template #option="slotProps">
                <div class="kn-list-item">
                    <div class="kn-list-item-text">
                        <span class="kn-truncated" v-tooltip.top="slotProps.option.filterOperation + $t('documentExecution.documentDetails.drivers.conditionsTitle') + slotProps.option.parFatherUrlName">
                            <b>{{ slotProps.option.filterOperation }}{{ $t('documentExecution.documentDetails.drivers.conditionsTitle') }}</b> {{ slotProps.option.parFatherUrlName }}
                        </span>
                    </div>
                    <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" />
                </div>
            </template>
        </Listbox>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
import driversDescriptor from './DocumentDetailsDriversDescriptor.json'
import Listbox from 'primevue/listbox'
import { iDriver } from '@/modules/managers/driversManagement/DriversManagement'

export default defineComponent({
    name: 'document-drivers',
    components: { Listbox },
    props: { availableDrivers: { type: Array as PropType<iDriver[]>, required: true }, dataDependencyObjects: { type: Array as any, required: true } },
    emits: ['driversChanged'],
    data() {
        return {
            mainDescriptor,
            driversDescriptor,
            drivers: [] as iDriver
        }
    },
    watch: {
        availableDrivers() {
            this.drivers = this.availableDrivers
        }
    },
    created() {
        this.drivers = this.availableDrivers
    },
    methods: {}
})
</script>
<style lang="scss" scoped>
.data-condition-container {
    :deep(.p-card-body) {
        padding: 0;
        .p-card-content {
            padding: 0;
        }
    }
    .data-condition-list {
        border: 1px solid $color-borders;
        border-top: none;
    }
}
</style>
