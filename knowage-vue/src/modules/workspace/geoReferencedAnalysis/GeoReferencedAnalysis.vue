<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('workspace.geoRef.title') }}
            </template>
            <template #end>
                <Button class="p-button-text p-button-rounded p-button-plain" :label="$t('workspace.geoRef.editMap')" />
                <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" />
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" />
            </template>
        </Toolbar>
        <div class="p-d-flex p-flex-column">
            <Card class="georef-steps-card p-m-3">
                <template #content>
                    <Steps class="georef-steps" :model="stepItems" :readonly="false" />
                </template>
            </Card>
            <router-view v-slot="{ Component }" :formData="formObject" :allLayers="allLayers" @prevPage="prevPage($event)" @nextPage="nextPage($event)" @complete="complete">
                <keep-alive>
                    <component class="p-m-3" :is="Component" />
                </keep-alive>
            </router-view>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { iLayer } from '@/modules/managers/layersManagement/LayersManagement'
import Steps from 'primevue/steps'

export default defineComponent({
    name: 'geo-referenced-analysis',
    components: { Steps },
    emits: [],
    props: {},
    computed: {},
    data() {
        return {
            loading: false,
            stepItems: [
                {
                    label: 'Dataset & Layer',
                    to: '/geo-ref/dnl'
                },
                {
                    label: 'Dataset Join',
                    to: '/geo-ref/dsj'
                },
                {
                    label: 'Indicator',
                    to: '/geo-ref/ind'
                },
                {
                    label: 'Filters & Menu',
                    to: '/geo-ref/fnm'
                }
            ],
            formObject: {} as any,
            allLayers: [] as iLayer[]
        }
    },
    created() {
        this.loadPage()
    },
    methods: {
        async loadPage() {
            this.loading = true
            await Promise.all([await this.getAllLayers()])
            this.loading = false
        },
        async getAllLayers() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `layers`).then((response: AxiosResponse<any>) => (this.allLayers = response.data.root))
        },
        nextPage(event) {
            // for (let field in event.formData) {
            //     this.formObject[field] = event.formData[field]
            // }

            this.$router.push(this.stepItems[event.pageIndex + 1].to)
        },
        prevPage(event) {
            this.$router.push(this.stepItems[event.pageIndex - 1].to)
        },
        complete() {
            this.$toast.add({ severity: 'success', summary: 'Order submitted', detail: 'Dear, ' + this.formObject.firstname + ' ' + this.formObject.lastname + ' your order completed.' })
        }
    }
})
</script>
<style lang="scss">
.georef-steps-card .p-card-content,
.georef-steps {
    padding: 0;
    .p-menuitem-link {
        flex-direction: column;
    }
    .p-steps-number {
        margin-bottom: 5px;
    }
}
</style>
