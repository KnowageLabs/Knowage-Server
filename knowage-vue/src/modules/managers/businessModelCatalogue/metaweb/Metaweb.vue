<template>
    <Dialog class="metaweb-dialog remove-padding p-fluid kn-dialog--toolbar--primary" :contentStyle="mainDescriptor.style.flex" :visible="visible" :modal="false" :closable="false" position="right" :baseZIndex="1" :autoZIndex="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left> {{ $t('metaweb.title') }} : NAME HERE </template>
                <template #right>
                    <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.save')" @click="metadataSave" />
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="$emit('closeMetaweb')" />
                </template>
            </Toolbar>
        </template>
        <div class="metaweb-tab-container p-d-flex p-flex-column kn-flex">
            <ProgressBar v-if="loading" class="kn-progress-bar" mode="indeterminate" data-test="progress-bar" />
            <TabView class="metaweb-tabview p-d-flex p-flex-column kn-flex">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('metaweb.businessModel.title') }}</span>
                    </template>
                    <BusinessModelTab :propMeta="meta" :observer="observer" />
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('metaweb.physicalModel.title') }}</span>
                    </template>

                    <MetawebPhysicalModel :propMeta="meta" :observer="observer" @loading="setLoading"></MetawebPhysicalModel>
                </TabPanel>
            </TabView>
        </div>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import useValidate from '@vuelidate/core'
import mainDescriptor from './MetawebDescriptor.json'
import Dialog from 'primevue/dialog'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import BusinessModelTab from './businessModel/MetawebBusinessModel.vue'
import MetawebPhysicalModel from './physicalModel/MetawebPhysicalModel.vue'
import metaMock from './MetawebMock.json'

const { observe, generate } = require('fast-json-patch')

export default defineComponent({
    name: 'metaweb',
    components: { BusinessModelTab, MetawebPhysicalModel, TabView, TabPanel, Dialog },
    props: { visible: { type: Boolean }, propMeta: { type: Object }, businessModel: { type: Object } },
    emits: ['closeMetaweb'],
    data() {
        return {
            v$: useValidate() as any,
            metaMock,
            mainDescriptor,
            meta: null as any,
            observer: null as any,
            loading: false
        }
    },
    computed: {},
    watch: {
        propMeta() {
            this.loadMeta()
        }
    },
    created() {
        this.loadMeta()
    },
    methods: {
        loadMeta() {
            // this.meta = this.metaMock.metaSales

            this.meta = this.propMeta

            if (this.meta) {
                this.observer = observe(this.meta)
            }
            console.log('LOADED META: ', this.meta)
        },
        setLoading(loading: boolean) {
            this.loading = loading
        },
        async metadataSave() {
            let patch = generate(this.observer)
            console.log('PATCH from MAIN SAVE ', patch)
            await this.checkRelationships()
        },
        async checkRelationships() {
            this.loading = true
            console.log('BUSINESS MODEL: ', this.businessModel)
            const postData = { data: { name: this.businessModel?.name, id: this.businessModel?.id }, diff: generate(this.observer) }
            await this.$http
                .post(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/checkRelationships`, postData)
                .then(async (response: AxiosResponse<any>) => {
                    console.log('response, ', response)
                    if (response.data.incorrectRelationships.length === 0) {
                        await this.generateModel()
                    }
                })
                .catch(() => {})
            this.loading = false
        },
        async generateModel() {
            const postData = { data: { name: this.businessModel?.name, id: this.businessModel?.id }, diff: generate(this.observer) }
            await this.$http
                .post(process.env.VUE_APP_META_API_URL + `/1.0/metaWeb/generateModel`, postData)
                .then(async (response: AxiosResponse<any>) => {
                    console.log('response, ', response)
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                })
                .catch(() => {})
        }
    }
})
</script>

<style lang="scss">
.metaweb-right-border {
    border-right: 1px solid #ccc;
}
.metaweb-tabview .p-tabview-panels {
    padding: 0 !important;
}
.metaweb-dialog.p-dialog {
    max-height: 100%;
    height: 100vh;
    width: calc(100vw - #{$mainmenu-width});
    margin: 0;
}
.remove-padding.p-dialog .p-dialog-header,
.remove-padding.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
}
.metaweb-tab-container .p-tabview .p-tabview-panel,
.metaweb-tab-container .p-tabview .p-tabview-panels {
    display: flex;
    flex-direction: column;
    flex: 1;
    position: relative;
}
</style>
