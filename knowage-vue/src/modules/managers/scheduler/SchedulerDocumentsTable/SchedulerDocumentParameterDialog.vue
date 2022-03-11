<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="schedulerDocumentParameterDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.scheduler.documentParameter') }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" />
        <Message class="p-m-2" v-if="deletedParams.length > 0" severity="warn" :closable="false" :style="schedulerDocumentParameterDialogDescriptor.styles.message">
            {{ deletedParamsMessage }}
        </Message>
        <SchedulerDocumentParameterForm v-for="(parameter, index) in parameters" :key="index" class="p-m-3" :propParameter="parameter" :roles="roles" :formulas="formulas" :documentLabel="documentLabel" @loading="setLoading($event)"></SchedulerDocumentParameterForm>
        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="setParameters">{{ $t('common.set') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { AxiosResponse } from 'axios'
    import Dialog from 'primevue/dialog'
    import Message from 'primevue/message'
    import schedulerDocumentParameterDialogDescriptor from './SchedulerDocumentParameterDialogDescriptor.json'
    import SchedulerDocumentParameterForm from './SchedulerDocumentParameterForm.vue'

    export default defineComponent({
        name: 'scheduler-document-parameter-dialog',
        components: { Dialog, Message, SchedulerDocumentParameterForm },
        props: { propParameters: { type: Array }, roles: { type: Array }, deletedParams: { type: Array }, documentLabel: { type: String } },
        emits: ['documentSelected', 'close', 'setParameters'],
        data() {
            return {
                schedulerDocumentParameterDialogDescriptor,
                parameters: [] as any[],
                formulas: [] as any[],
                loading: false
            }
        },
        computed: {
            deletedParamsMessage() {
                let message = ''
                this.deletedParams?.forEach((el: any) => (message += el.name + ' '))
                return message
            }
        },
        watch: {
            propParameters: {
                handler() {
                    this.loadParameters()
                },
                deep: true
            }
        },
        async created() {
            this.loadParameters()
            await this.loadFormulas()
        },
        methods: {
            loadParameters() {
                this.parameters = []
                this.propParameters?.forEach((el: any) => this.parameters.push({ ...el }))
                this.parameters.sort((a: any, b: any) => (a.name > b.name ? 1 : -1))
            },
            closeDialog() {
                this.parameters = []
                this.$emit('close')
            },
            async loadFormulas() {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/formulas/`).then((response: AxiosResponse<any>) => (this.formulas = response.data))
            },
            setParameters() {
                this.$emit('setParameters', this.parameters)
            },
            setLoading(loadingValue: boolean) {
                this.loading = loadingValue
            }
        }
    })
</script>
