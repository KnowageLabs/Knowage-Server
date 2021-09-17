<template>
    <Card style="width:100%">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    <span>{{ $t('managers.glossary.common.attributes') }}</span>
                </template>
                <template #right>
                    <Button :label="$t('managers.glossary.common.add')" class="p-button-text p-button-rounded p-button-plain" @click="showDialog" />
                </template>
            </Toolbar>
        </template>
        <template #content>
            <DataTable class="p-datatable-sm kn-table" :value="mockedAtributes" responsiveLayout="stack">
                <template #empty>
                    {{ $t('common.info.noDataFound') }}
                </template>
                <template #loading>
                    {{ $t('common.info.dataLoading') }}
                </template>
                <Column field="name" header="Attribute"></Column>
                <Column field="value" :header="$t('common.value')"></Column>
                <Column headerStyle="glossaryDefinitionDialogDescriptor.table.iconColumn.style" :style="glossaryDefinitionDialogDescriptor.table.iconColumn.style">
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteAttribute(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import glossaryDefinitionDialogDescriptor from '../GlossaryDefinitionDialogDescriptor.json'
export default defineComponent({
    name: 'attributes-table',
    components: { DataTable, Column },
    props: {},
    data() {
        return {
            mockedAtributes: [
                { name: 'mock1', value: 0 },
                { name: 'mock2', value: 0 }
            ],
            glossaryDefinitionDialogDescriptor
        }
    },
    computed: {},
    watch: {
        propWord() {}
    },
    mounted() {},
    methods: {
        showDialog() {
            console.log('DIALOG')
        },
        deleteAttribute(attribute: any) {
            console.log(attribute)
        }
    }
})
</script>
