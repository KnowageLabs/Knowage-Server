<template>
    <div class="kn-page">
        <Card class="p-m-3">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #left>
                        {{ $t('managers.glossary.common.details') }}
                    </template>
                </Toolbar>
            </template>
            <template #content>
                <form class="p-fluid p-formgrid p-grid">
                    <div class="p-field p-col-12 p-md-10">
                        <span class="p-float-label">
                            <InputText id="name" class="kn-material-input" type="text" v-model.trim="name" maxLength="100" />
                            <label for="name" class="kn-material-input-label"> {{ $t('documentExecution.dossier.activityName') }} * </label>
                        </span>
                    </div>
                    <div class="p-field p-md-2">
                        <Button class="p-button-link" :label="$t('documentExecution.dossier.launchActivity')" @click="getDossier" />
                    </div>
                </form>
            </template>
        </Card>

        <Card class="p-m-3">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #left>
                        {{ $t('documentExecution.dossier.launchedActivities') }}
                    </template>
                </Toolbar>
            </template>
            <template #content>
                <DataTable :value="activities" :loading="loading" :rows="20" class="p-datatable-sm kn-table" dataKey="id" responsiveLayout="stack" breakpoint="960px" data-test="activities-table">
                    <Column field="activity" :header="$t('documentExecution.dossier.headers.activity')" :sortable="true" />
                    <Column field="creationDate" :header="$t('managers.mondrianSchemasManagement.headers.creationDate')" :sortable="true" dataType="date">
                        <template #body="{data}">
                            {{ formatDate(data.creationDate) }}
                        </template>
                    </Column>
                    <Column v-for="col of columns" :field="col.field" :header="$t(col.header)" :key="col.field" :style="col.style" class="kn-truncated" :sortable="true" />
                    <Column header style="text-align:right">
                        <template #body="slotProps">
                            <Button icon="pi pi-download" class="p-button-link" />
                            <Button icon="pi pi-trash" class="p-button-link" @click="deleteDossierConfirm(slotProps.data.id)" />
                        </template>
                    </Column>
                </DataTable>
            </template>
        </Card>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import dossierDescriptor from './DossierDescriptor.json'
import axios from 'axios'
import Card from 'primevue/card'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
// import KnFabButton from '@/components/UI/KnFabButton.vue'

export default defineComponent({
    name: 'configuration-management',
    components: {
        Card,
        Column,
        DataTable
        // KnFabButton,
    },
    created() {},
    data() {
        return {
            name: 'test',
            activities: dossierDescriptor.activities,
            columns: dossierDescriptor.columns,
            test: [] as any
        }
    },
    methods: {
        formatDate(date) {
            let fDate = new Date(date)
            return fDate.toLocaleString()
        },
        getDossier() {
            // dossier/activities/697
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `dossier/activities/3251`).then((response) => {
                this.test = response.data
            })
        },
        deleteDossierConfirm(selectedDossierId) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteDossier(selectedDossierId)
            })
        },
        deleteDossier(selectedDossierId) {
            console.log(selectedDossierId)
        }
    }
})
</script>

<style lang="scss" scoped></style>
