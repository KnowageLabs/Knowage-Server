<template>
    <div class="p-grid p-m-0 kn-flex">
        <div class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start> {{ $t('metaweb.physicalModel.tables') }}</template>
                <template #end>
                    <Button class="p-button-text p-button-rounded p-button-plain p-button-sm" @click="openUpdateDialog">{{ $t('metaweb.physicalModel.updatePhysicalModel') }}</Button>
                </template>
            </Toolbar>

            <div class="kn-flex kn-relative">
                <div class="metaweb-right-border" :style="physDescriptor.style.mainListContainer">
                    <MetawebPhysicalModelList :style="physDescriptor.style.mainList" :propMeta="meta" @selected="onSelectedItem"></MetawebPhysicalModelList>
                </div>
            </div>
        </div>

        <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 p-d-flex p-flex-column">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <span data-test="physical-model-name">{{ selectedPhysicalModel?.name }}</span>
                </template>
            </Toolbar>
            <div v-if="selectedPhysicalModel" class="metaweb-tab-container p-d-flex p-flex-column kn-flex">
                <TabView class="metaweb-tabview p-d-flex p-flex-column kn-flex" scrollable data-test="tab-view">
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.physicalModel.propertyList') }}</span>
                        </template>

                        <MetawebPropertyListTab class="p-m-2" :selectedPhysicalModel="selectedPhysicalModel"></MetawebPropertyListTab>
                    </TabPanel>
                    <TabPanel v-if="selectedPhysicalModel?.type === 'TABLE'">
                        <template #header>
                            <span>{{ $t('metaweb.physicalModel.foreignKey') }}</span>
                        </template>

                        <MetawebForeignKeyTab class="p-m-2" :propForeignKeys="selectedPhysicalModel.foreignKeys"></MetawebForeignKeyTab>
                    </TabPanel>
                </TabView>
            </div>
        </div>

        <MetawebPhysicalModelUpdateDialog :visible="updateDialogVisible" :changedItem="changedItem" @close="updateDialogVisible = false" @updated="onPhysicalModelUpdate"></MetawebPhysicalModelUpdateDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { iChangedData, iColumn, iPhysicalModel } from '../Metaweb'
import MetawebForeignKeyTab from './tabs/MetawebForeignKeyTab.vue'
import MetawebPhysicalModelList from './metawebPhysicalModelList/MetawebPhysicalModelList.vue'
import MetawebPropertyListTab from './tabs/MetawebPropertyListTab.vue'
import MetawebPhysicalModelUpdateDialog from './metawebPhysicalModelUpdateDialog/MetawebPhysicalModelUpdateDialog.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import physDescriptor from './PhysicalModelDescriptor.json'

const { applyPatch, generate } = require('fast-json-patch')

export default defineComponent({
    name: 'metaweb-physical-model',
    components: { MetawebForeignKeyTab, MetawebPhysicalModelList, MetawebPropertyListTab, MetawebPhysicalModelUpdateDialog, TabView, TabPanel },
    props: { propMeta: { type: Object }, observer: { type: Object } },
    emits: ['loading'],
    data() {
        return {
            physDescriptor,
            meta: null as any,
            selectedPhysicalModel: null as iColumn | iPhysicalModel | null,
            updateDialogVisible: false,
            changedItem: null as iChangedData | null
        }
    },
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
            this.meta = this.propMeta
        },
        onSelectedItem(selectedPhysicalModel: iColumn | iPhysicalModel) {
            this.selectedPhysicalModel = selectedPhysicalModel
        },
        async openUpdateDialog() {
            this.$emit('loading', true)
            await this.$http.get(process.env.VUE_APP_META_API_URL + '1.0/metaWeb/updatePhysicalModel').then((response: AxiosResponse<any>) => (this.changedItem = response.data))
            this.updateDialogVisible = true
            this.$emit('loading', false)
        },
        onPhysicalModelUpdate(changes: any) {
            this.meta = applyPatch(this.meta, changes).newDocument
            generate(this.observer)
            this.updateDialogVisible = false
        }
    }
})
</script>
