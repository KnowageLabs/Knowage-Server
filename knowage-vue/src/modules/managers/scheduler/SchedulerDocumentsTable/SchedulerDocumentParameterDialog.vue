<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="schedulerDocumentParameterDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('managers.scheduler.documentParameter') }}
                </template>
            </Toolbar>
        </template>
        <SchedulerDocumentParameterForm v-for="(parameter, index) in parameters" :key="index" class="p-m-3" :propParameter="parameter" :roles="roles" :formulas="formulas"></SchedulerDocumentParameterForm>
        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="setParameters">{{ $t('managers.scheduler.set') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import Dialog from 'primevue/dialog'
import schedulerDocumentParameterDialogDescriptor from './SchedulerDocumentParameterDialogDescriptor.json'
import SchedulerDocumentParameterForm from './SchedulerDocumentParameterForm.vue'

export default defineComponent({
    name: 'scheduler-document-parameter-dialog',
    components: { Dialog, SchedulerDocumentParameterForm },
    props: { propParameters: { type: Array }, roles: { type: Array } },
    emits: ['documentSelected', 'close', 'setParameters'],
    data() {
        return {
            schedulerDocumentParameterDialogDescriptor,
            parameters: [] as any[],
            formulas: [] as any[]
        }
    },
    watch: {
        propParameters() {
            this.loadParameters()
        }
    },
    async created() {
        this.loadParameters()
        await this.loadFormulas()
    },
    methods: {
        loadParameters() {
            this.parameters = this.propParameters ? [...(this.propParameters as any[])] : []
            this.parameters.sort((a: any, b: any) => (a.name > b.name ? 1 : -1))
            // console.log('LOADED PARAMETERS: ', this.parameters)
        },
        closeDialog() {
            this.$emit('close')
        },
        async loadFormulas() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/formulas/`).then((response) => (this.formulas = response.data))
            // console.log('LOADED FORMULAS: ', this.formulas)
        },
        setParameters() {
            // console.log('SET PARAMETERS: ', this.parameters)

            this.$emit('setParameters', this.parameters)
        }
    }
})
</script>
