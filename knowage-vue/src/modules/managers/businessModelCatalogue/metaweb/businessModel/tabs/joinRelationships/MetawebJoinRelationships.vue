<template>
    <DataTable :value="businessModel.joinRelationships" class="p-datatable-sm kn-table p-ml-2" responsiveLayout="scroll" breakpoint="400px">
        <Column class="p-text-center">
            <template #header>
                <Button :label="$t('metaweb.businessModel.editJoin')" class="p-button-link p-text-right" @click="showBusinessViewDialog = true" />
            </template>
            <template #body="slotProps">
                <span v-for="(rel, index) in slotProps.data.sourceColumns" v-bind:key="index" class="associator-block-hover p-d-flex p-flex-row p-ai-center kn-width-full">
                    <!-- <span class="p-mr-6">{{ slotProps.data.sourceTable.name }}.{{ rel.name }}</span>
                    <span class="p-mx-6"><i class="fa fa-link" aria-hidden="true"></i></span>
                    <span class="p-ml-6">{{ slotProps.data.destinationTable.name }}.{{ slotProps.data.destinationColumns[index].name }}</span> -->
                    <span class="kn-truncated kn-flex-05 p-ml-2 p-text-right">{{ slotProps.data.sourceTable.name }}.{{ rel.name }}</span>
                    <i class="fas fa-link kn-flex-05" aria-hidden="true" />
                    <span class="kn-truncated kn-flex-05 p-ml-2 p-text-left">{{ slotProps.data.destinationTable.name }}.{{ slotProps.data.destinationColumns[index].name }}</span>
                    <br />
                </span>
            </template>
        </Column>
    </DataTable>

    <MetawebBusinessViewDialog
        v-if="showBusinessViewDialog"
        :selectedBusinessModel="selectedBusinessModel"
        :physicalModels="propMeta.physicalModels"
        :meta="propMeta"
        :observer="observer"
        :editMode="true"
        :showBusinessViewDialog="showBusinessViewDialog"
        @closeDialog="showBusinessViewDialog = false"
    />
</template>

<script lang="ts">
import useValidate from '@vuelidate/core'
import { defineComponent, PropType } from 'vue'
import { iBusinessModel } from '@/modules/managers/businessModelCatalogue/metaweb/Metaweb'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import MetawebBusinessViewDialog from '@/modules/managers/businessModelCatalogue/metaweb/businessModel/dialogs/MetawebBusinessViewDialog.vue'

export default defineComponent({
    name: 'metaweb-attributes-tab',
    components: { DataTable, Column, MetawebBusinessViewDialog },
    props: {
        selectedBusinessModel: { type: Object as PropType<iBusinessModel | null>, required: true },
        propMeta: { type: Object },
        observer: { type: Object }
    },
    emits: ['loading'],
    computed: {},
    data() {
        return {
            v$: useValidate() as any,
            businessModel: null as iBusinessModel | null,
            showBusinessViewDialog: false
        }
    },
    watch: {
        selectedBusinessModel() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
    },
    validations() {},
    methods: {
        loadData() {
            this.businessModel = this.selectedBusinessModel as iBusinessModel
        }
    }
})
</script>
