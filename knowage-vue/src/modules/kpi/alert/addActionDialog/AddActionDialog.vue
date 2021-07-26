<template>
    <Dialog :header="$t('kpi.alert.addAction')" :breakpoints="addActionDialogDescriptor.dialog.breakpoints" :style="addActionDialogDescriptor.dialog.style" :visible="dialogVisible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('kpi.alert.addAction') }}
                </template>
                <template #right>
                    <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" @click="handleSave" />
                    <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="$emit('close')" />
                </template>
            </Toolbar>
        </template>
        <div class="p-field p-col-6">
            <span class="p-float-label">
                <Dropdown id="type" class="kn-material-input" v-model="type" optionLabel="name" :options="addActionDialogDescriptor.actionType" @change="setType" />
                <label for="type" class="kn-material-input-label"> {{ $t('kpi.alert.type') }} * </label>
            </span>
            <span class="p-float-label">
                <Dropdown id="threshold" class="kn-material-input" />
                <label for="threshold" class="kn-material-input-label"> {{ $t('kpi.alert.threshold') }} * </label>
            </span>
        </div>
        <exectute-etl-card v-if="type && type.id == 63" :loading="loading" :files="data.item"></exectute-etl-card>
        <context-broker-card v-if="type && type.id == 86"></context-broker-card>
        <SendMailCard v-else-if="type && type.id == 62" :action="action" :users="data"></SendMailCard>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import addActionDialogDescriptor from './AddActionDialogDescriptor.json'
import ExectuteEtlCard from './ExectuteEtlCard.vue'
import ContextBrokerCard from './ContextBrokerCard.vue'
import SendMailCard from './SendMailCard.vue'

export default defineComponent({
    name: 'add-action-dialog',
    components: {
        Dialog,
        Dropdown,
        ExectuteEtlCard,
        ContextBrokerCard,
        SendMailCard
    },
    props: {
        dialogVisible: {
            type: Boolean,
            default: false
        },
        action: {
            type: Object
        }
    },
    data() {
        return {
            addActionDialogDescriptor,
            loading: false,
            type: { id: null },
            data: {}
        }
    },
    methods: {
        async setType() {
            console.log(this.type)
            if (this.type.id == 63) {
                await this.loadData('2.0/documents/listDocument?includeType=ETL')
            } else if (this.type.id === 62) {
                await this.loadData('2.0/users')
            }
            console.log('DATA ', this.data)
        },
        async loadData(path: string) {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + path)
                .then((response) => {
                    this.data = response.data
                })
                .finally(() => (this.loading = false))
        },
        handeleSave() {
            console.log('save')
        }
    }
})
</script>
