<template>
    <div>
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('managers.scheduler.documents') }}
            </template>
            <template #right>
                <Button class="kn-button p-button-text p-button-rounded">{{ $t('common.add') }}</Button>
            </template>
        </Toolbar>

        <DataTable
            id="documents-datatable"
            :value="documents"
            :paginator="true"
            :rows="schedulerDocumentsTableDescriptor.rows"
            class="p-datatable-sm kn-table"
            dataKey="name"
            :globalFilterFields="schedulerDocumentsTableDescriptor.globalFilterFields"
            :responsiveLayout="schedulerDocumentsTableDescriptor.responsiveLayout"
            :breakpoint="schedulerDocumentsTableDescriptor.breakpoint"
        >
            <Column class="kn-truncated" :header="$t('common.name')" :style="schedulerDocumentsTableDescriptor.nameColumnStyle">
                <template #body="slotProps">
                    {{ slotProps.data.name }}
                </template></Column
            >
            <Column class="kn-truncated" :header="$t('managers.scheduler.parameters')">
                <template #body="slotProps">
                    {{ getParametersString(slotProps.data.parameters) }}
                </template></Column
            >
            <Column :style="schedulerDocumentsTableDescriptor.iconColumnStyle">
                <template #body="slotProps">
                    <Button icon="pi pi-trash" class="p-button-link" @click="removeDocument(slotProps.index)" />
                </template>
            </Column>
        </DataTable>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iDocument, iParameter } from '../Scheduler'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import schedulerDocumentsTableDescriptor from './SchedulerDocumentsTableDescriptor.json'

export default defineComponent({
    name: 'scheduler-documents-table',
    components: { Column, DataTable },
    props: { jobDocuments: { type: Array } },
    data() {
        return {
            schedulerDocumentsTableDescriptor,
            documents: [] as iDocument[]
        }
    },
    watch: {
        jobDocuments() {
            this.loadDocuments()
        }
    },
    created() {
        this.loadDocuments()
    },
    methods: {
        loadDocuments() {
            this.documents = this.jobDocuments as iDocument[]
        },
        getParametersString(parameters: iParameter[]) {
            let parameterString = ''
            for (let i = 0; i < parameters.length; i++) {
                parameterString += parameters[i].name + ' = ' + parameters[i].value
                parameterString += i === parameters.length - 1 ? '' : ' | '
            }
            return parameterString
        },
        removeDocument(documentIndex: number) {
            console.log('DOCUMENT TO REMOVE: ', documentIndex)
        }
    }
})
</script>

<style lang="scss">
#documents-datatable .p-datatable-wrapper {
    height: auto;
}
</style>
