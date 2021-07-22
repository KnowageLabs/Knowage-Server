<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0">
        <template #right>
            <Button icon="pi pi-save" class="kn-button p-button-text p-button-rounded" />
            <Button icon="pi pi-times" class="kn-button p-button-text p-button-rounded" @click="closeTemplate" />
        </template>
    </Toolbar>
    <div class="p-grid p-m-0 p-fluid p-jc-center">
        <div class="p-col-9">
            <Card>
                <template #content>
                    <form class="p-fluid p-m-5">
                        <div class="p-d-flex p-jc-between">
                            <div class="p-field">
                                <span class="p-float-label">
                                    <InputText id="name" class="kn-material-input" type="text" v-model="selectedAlert.name" />
                                    <label for="name" class="kn-material-input-label">{{ $t('kpi.alert.name') }} * </label>
                                </span>
                            </div>
                            <div class="p-field">
                                <span class="p-float-label">
                                    <Dropdown id="listener" class="kn-material-input" v-model="selectedAlert.alertListener" :options="listeners" optionLabel="name" />
                                    <label for="category" class="kn-material-input-label"> {{ $t('kpi.alert.kpiListener') }} * </label>
                                </span>
                            </div>
                        </div>
                    </form>
                </template>
            </Card>
        </div>
    </div>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iAlert, iListener } from './Alert'
import Dropdown from 'primevue/dropdown'
import axios from 'axios'
export default defineComponent({
    name: 'alert-details',
    components: { Dropdown },
    props: {
        id: {
            type: String,
            required: false
        }
    },
    watch: {
        async id() {
            await this.checkId()
        }
    },
    created() {
        if (this.id) {
            this.loadAlert()
        }
        this.loadListener()
    },
    data() {
        return {
            selectedAlert: {} as iAlert,
            listeners: [] as iListener[],
            selectedListener: {} as iListener
        }
    },
    methods: {
        async loadAlert() {
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/alert/' + this.id + '/load')
                .then((response) => {
                    this.selectedAlert = response.data
                })
                .finally(() => console.log('selected', this.selectedAlert))
        },
        async loadListener() {
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/alert/listListener')
                .then((response) => {
                    this.listeners = response.data
                })
                .finally(() => console.log('selected', this.selectedAlert))
        },
        async checkId() {
            if (this.id) {
                await this.loadAlert()
            } else {
                this.selectedAlert = { id: null }
            }
        },
        closeTemplate() {
            this.$emit('close')
        }
    }
})
</script>
