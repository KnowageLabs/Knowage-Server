<template>
    <Dialog class="metaweb-dialog remove-padding p-fluid kn-dialog--toolbar--primary" :content-style="mainDescriptor.style.flex" :visible="visible" :modal="false" :closable="false" position="right" :base-z-index="1" :auto-z-index="true">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start> {{ $t('metaweb.title') }} </template>
                <template #end>
                    <Button v-tooltip.bottom="$t('common.save')" icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="metadataSave" />
                    <Button v-tooltip.bottom="$t('common.close')" icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeMetawebConfirm" />
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
                    <BusinessModelTab :business-model-id="businessModel.dataSourceId" :prop-meta="meta" :observer="observer" :meta-updated="metaUpdated" @metaUpdated="onMetaUpdated" />
                </TabPanel>
                <TabPanel>
                    <template #header>
                        <span>{{ $t('metaweb.physicalModel.title') }}</span>
                    </template>

                    <MetawebPhysicalModel :prop-meta="meta" :observer="observer" @loading="setLoading"></MetawebPhysicalModel>
                </TabPanel>
            </TabView>
        </div>

        <MetawebInvalidRelationshipsDialog :visible="invalidRelationshipsDialogVisible" :prop-incorrect-relationships="incorrectRelationships" @close="invalidRelationshipsDialogVisible = false" @save="generateModel"></MetawebInvalidRelationshipsDialog>
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
import MetawebInvalidRelationshipsDialog from './invalidRelationshipsDialog/MetawebInvalidRelationshipsDialog.vue'
import { mapActions } from 'pinia'
import mainStore from '@/App.store'
import { observe, generate, applyPatch } from 'fast-json-patch'

export default defineComponent({
    name: 'metaweb',
    components: { BusinessModelTab, MetawebPhysicalModel, TabView, TabPanel, Dialog, MetawebInvalidRelationshipsDialog },
    props: { visible: { type: Boolean }, propMeta: { type: Object }, businessModel: { type: Object } },
    emits: ['closeMetaweb', 'modelGenerated'],
    data() {
        return {
            v$: useValidate() as any,
            mainDescriptor,
            meta: null as any,
            observer: null as any,
            metaUpdated: false,
            invalidRelationshipsDialogVisible: false,
            incorrectRelationships: [] as any[],
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
        ...mapActions(mainStore, ['setInfo']),
        loadMeta() {
            this.meta = this.propMeta

            if (this.meta) {
                this.observer = observe(this.meta)
            }
        },
        setLoading(loading: boolean) {
            this.loading = loading
        },
        async metadataSave() {
            await this.checkRelationships(true)
        },
        onMetaUpdated() {
            this.checkRelationships(false)
        },
        async checkRelationships(generateModel: boolean) {
            this.loading = true
            const postData = { data: { name: this.businessModel?.name, id: this.businessModel?.id }, diff: generate(this.observer) }
            await this.$http
                .post(import.meta.env.VITE_META_API_URL + `/1.0/metaWeb/checkRelationships`, postData)
                .then(async (response: AxiosResponse<any>) => {
                    this.observer = applyPatch(this.observer, response.data).newDocument
                    this.observer = observe(this.meta)
                    this.metaUpdated = !this.metaUpdated
                    if (generateModel)
                        if (response.data.incorrectRelationships.length === 0) {
                            await this.generateModel()
                        } else {
                            this.invalidRelationshipsDialogVisible = true
                            this.incorrectRelationships = response.data.incorrectRelationships
                        }
                })
                .catch(() => {})
            this.loading = false
        },
        async generateModel() {
            const postData = { data: { name: this.businessModel?.name, id: this.businessModel?.id }, diff: generate(this.observer) }
            await this.$http
                .post(import.meta.env.VITE_META_API_URL + `/1.0/metaWeb/generateModel`, postData)
                .then(() => {
                    this.setInfo({
                        title: this.$t('common.toast.createTitle'),
                        msg: this.$t('common.toast.success')
                    })
                    this.$emit('modelGenerated')
                })
                .catch(() => {})
                .finally(() => (this.invalidRelationshipsDialogVisible = false))
        },
        closeMetawebConfirm() {
            this.$confirm.require({
                header: this.$t('metaweb.exitFromMeta'),
                accept: () => {
                    this.$emit('closeMetaweb')
                    this.meta = null
                    this.observer = null
                    this.incorrectRelationships = []
                }
            })
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
    width: calc(100vw - var(--kn-mainmenu-width));
    margin: 0;
}
.remove-padding.p-dialog .p-dialog-header,
.remove-padding.p-dialog .p-dialog-content {
    padding: 0;
    margin: 0;
    overflow-x: hidden;
}
.metaweb-tab-container .p-tabview .p-tabview-panel,
.metaweb-tab-container .p-tabview .p-tabview-panels {
    display: flex;
    flex-direction: column;
    flex: 1;
    position: relative;
}
</style>
