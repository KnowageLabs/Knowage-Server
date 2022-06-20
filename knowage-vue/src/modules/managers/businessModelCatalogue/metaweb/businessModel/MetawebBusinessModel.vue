<template>
    <div v-if="meta" class="p-grid p-m-0 kn-flex">
        <div id="CONTAINER ELEMENT LIST" class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column kn-flex">
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            <div class="p-d-flex p-flex-column kn-flex">
                <Toolbar class="kn-toolbar kn-toolbar--default">
                    <template #start>
                        <span>{{ $t('metaweb.businessModel.businessClass') }}</span>
                    </template>
                    <template #end>
                        <KnFabButton icon="fas fa-plus" @click="showMenu" />
                    </template>
                </Toolbar>
                <div class="kn-relative kn-flex">
                    <div class="kn-height-full kn-width-full kn-absolute">
                        <DataTable class="p-datatable-sm metaweb-table kn-table metaweb-right-border" :loading="loading" :scrollable="true" scrollHeight="100%" :value="meta.businessModels" @row-click="selectBusinessModel" @rowReorder="onRowReorder" data-test="bm-table">
                            <Column :rowReorder="true" :reorderableColumn="false" />
                            <Column :style="mainDescriptor.style.columnMain">
                                <template #body="slotProps">
                                    <span>{{ slotProps.data.name }}</span>
                                    <Chip :label="slotProps.data.columns.length + ' ' + $t('metaweb.businessModel.tabView.attributes')" class="p-ml-2" :style="mainDescriptor.style.chip" />
                                </template>
                            </Column>

                            <Column :style="mainDescriptor.style.columnTrash">
                                <template #body="slotProps">
                                    <Button icon="pi pi-trash" class="p-button-link" @click="deleteFromList(slotProps.data)" />
                                </template>
                            </Column>
                        </DataTable>
                    </div>
                </div>
            </div>
            <div v-if="meta.businessViews.length > 0" class="p-d-flex p-flex-column kn-flex">
                <Toolbar class="kn-toolbar kn-toolbar--default">
                    <template #start>
                        <span>{{ $t('metaweb.businessModel.businessView') }}</span>
                    </template>
                </Toolbar>
                <div class="kn-relative kn-flex">
                    <div class="kn-height-full kn-width-full kn-absolute">
                        <DataTable class="p-datatable-sm metaweb-table kn-table metaweb-right-border" :loading="loading" :scrollable="true" scrollHeight="100%" :value="meta.businessViews" @row-click="selectBusinessModel" @rowReorder="onRowReorder">
                            <Column :rowReorder="true" :reorderableColumn="false" />
                            <Column :style="mainDescriptor.style.columnMain">
                                <template #body="slotProps">
                                    <span>{{ slotProps.data.name }}</span>
                                    <Chip :label="slotProps.data.columns.length + ' ' + $t('metaweb.businessModel.tabView.attributes')" class="p-ml-2" :style="mainDescriptor.style.chip" />
                                </template>
                            </Column>

                            <Column :style="mainDescriptor.style.columnTrash">
                                <template #body="slotProps">
                                    <Button icon="pi pi-trash" class="p-button-link" @click="deleteFromList(slotProps.data)" />
                                </template>
                            </Column>
                        </DataTable>
                    </div>
                </div>
            </div>
        </div>
        <div id="CONTAINER ELEMENT DETAILS" class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 p-d-flex p-flex-column" :style="mainDescriptor.style.flex3">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <span v-if="selectedBusinessModel">{{ selectedBusinessModel.name }}</span>
                </template>
            </Toolbar>
            <div v-if="Object.keys(selectedBusinessModel).length !== 0" class="metaweb-tab-container p-d-flex p-flex-column kn-flex">
                <TabView class="metaweb-tabview p-d-flex p-flex-column kn-flex" scrollable>
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.propertyTitle') }}</span>
                        </template>

                        <div :style="mainDescriptor.style.absoluteScroll">
                            <MetawebBusinessPropertyListTab :selectedBusinessModel="selectedBusinessModel" :roles="roles" :propMeta="meta" @metaUpdated="$emit('metaUpdated')"></MetawebBusinessPropertyListTab>
                        </div>
                    </TabPanel>

                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.attributes') }}</span>
                        </template>

                        <div :style="mainDescriptor.style.absoluteScroll">
                            <MetawebAttributesTab :selectedBusinessModel="selectedBusinessModel" :propMeta="meta" :observer="observer" :roles="roles" @metaUpdated="$emit('metaUpdated')"></MetawebAttributesTab>
                        </div>
                    </TabPanel>

                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.calcField') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll">
                            <CalculatedField :selectedBusinessModel="selectedBusinessModel" :propMeta="meta" :propCustomFunctions="customFunctions" @metaUpdated="$emit('metaUpdated')" :observer="observer" />
                        </div>
                    </TabPanel>
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.inbound') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll">
                            <InboundRelationships :selectedBusinessModel="selectedBusinessModel" :propMeta="meta" :observer="observer" />
                        </div>
                    </TabPanel>
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.outbound') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll">
                            <OutboundRelationships :selectedBusinessModel="selectedBusinessModel" :propMeta="meta" :observer="observer" />
                        </div>
                    </TabPanel>
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.filter') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll"><MetawebFilterTab :selectedBusinessModel="selectedBusinessModel" :propMeta="meta" @metaUpdated="$emit('metaUpdated')"></MetawebFilterTab></div>
                    </TabPanel>

                    <TabPanel v-if="selectedBusinessModel.joinRelationships != undefined">
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.joinRelationships') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll">
                            <MetawebJoinRelationships :selectedBusinessModel="selectedBusinessModel" :propMeta="meta" :observer="observer" />
                        </div>
                    </TabPanel>

                    <TabPanel v-if="selectedBusinessModel.joinRelationships != undefined">
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.physicalTable') }}</span>
                        </template>

                        <div :style="mainDescriptor.style.absoluteScroll">
                            <MetawebPhysicalTableTab :selectedBusinessModel="selectedBusinessModel" :propMeta="meta" :observer="observer"></MetawebPhysicalTableTab>
                        </div>
                    </TabPanel>
                </TabView>
            </div>
        </div>
    </div>
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
    <BusinessClassDialog v-if="showBusinessClassDialog" :meta="meta" :observer="observer" :physicalModels="meta.physicalModels" :showBusinessClassDialog="showBusinessClassDialog" @closeDialog="showBusinessClassDialog = false" />
    <BusinessViewDialog v-if="showBusinessViewDialog" :meta="meta" :observer="observer" :showBusinessViewDialog="showBusinessViewDialog" @closeDialog="showBusinessViewDialog = false" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iBusinessModel } from '../Metaweb'
import { AxiosResponse } from 'axios'
import mainDescriptor from '../MetawebDescriptor.json'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import bmDescriptor from './MetawebBusinessModelDescriptor.json'
import Menu from 'primevue/contextmenu'
import MetawebBusinessPropertyListTab from './tabs/propertyListTab/MetawebBusinessPropertyListTab.vue'
import BusinessClassDialog from './dialogs/MetawebBusinessClassDialog.vue'
import BusinessViewDialog from './dialogs/MetawebBusinessViewDialog.vue'
import MetawebAttributesTab from './tabs/metawebAttributesTab/MetawebAttributesTab.vue'
import InboundRelationships from './tabs/inboundRelationships/MetawebInboundRelationships.vue'
import OutboundRelationships from './tabs/outboundRelationships/MetawebOutboundRelationships.vue'
import MetawebPhysicalTableTab from './tabs/physicalTable/MetawebPhysicalTableTab.vue'
import MetawebJoinRelationships from './tabs/joinRelationships/MetawebJoinRelationships.vue'
import MetawebFilterTab from './tabs/filterTab/MetawebFilterTab.vue'
import CalculatedField from './tabs/calculatedField/MetawebCalculatedField.vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Chip from 'primevue/chip'

const { generate, applyPatch } = require('fast-json-patch')

export default defineComponent({
    name: 'metaweb-business-model',
    components: {
        Chip,
        Column,
        DataTable,
        CalculatedField,
        MetawebJoinRelationships,
        OutboundRelationships,
        BusinessClassDialog,
        BusinessViewDialog,
        KnFabButton,
        TabView,
        TabPanel,
        Menu,
        MetawebBusinessPropertyListTab,
        MetawebAttributesTab,
        InboundRelationships,
        MetawebPhysicalTableTab,
        MetawebFilterTab
    },
    props: { propMeta: { type: Object }, observer: { type: Object }, metaUpdated: { type: Boolean }, businessModelId: Number },
    emits: ['loading', 'metaUpdated'],
    computed: {},
    data() {
        return {
            bmDescriptor,
            mainDescriptor,
            meta: null as any,
            menuButtons: [] as any,
            customFunctions: [] as any,
            showBusinessClassDialog: false,
            showBusinessViewDialog: false,
            selectedBusinessModel: {} as iBusinessModel,
            roles: [] as any[],
            loading: false
        }
    },
    watch: {
        propMeta() {
            this.loadMeta()
        },
        metaUpdated() {
            this.loadMeta()
        }
    },
    created() {
        this.loadMeta()
        this.createMenuItems()
        this.loadRoles()
        this.loadCustomFunctions()
    },
    methods: {
        showMenu(event) {
            // eslint-disable-next-line
            // @ts-ignore
            this.$refs.optionsMenu.toggle(event)
        },
        createMenuItems() {
            this.menuButtons = []
            this.menuButtons.push({ key: '0', label: this.$t('metaweb.businessModel.newBusiness'), command: () => this.showBusinessClass() }, { key: '1', label: this.$t('metaweb.businessModel.newView'), command: () => this.showBusinessView() })
        },
        loadMeta() {
            this.meta = this.propMeta
        },
        selectBusinessModel(event) {
            this.selectedBusinessModel = event.data as iBusinessModel
        },
        showBusinessClass() {
            this.showBusinessClassDialog = true
        },
        showBusinessView() {
            this.showBusinessViewDialog = true
        },
        async deleteFromList(itemForDeletion) {
            const postData = { data: { name: itemForDeletion.uniqueName }, diff: generate(this.observer) }
            let url = ''
            itemForDeletion.joinRelationships ? (url = import.meta.env.VUE_APP_META_API_URL + '/1.0/metaWeb/deleteBusinessView') : (url = import.meta.env.VUE_APP_META_API_URL + '/1.0/metaWeb/deleteBusinessClass')
            await this.$http
                .post(url, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data).newDocument

                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    generate(this.observer)
                })
                .catch(() => {})
        },
        async loadRoles() {
            this.loading = true
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles').then((response: AxiosResponse<any>) => (this.roles = response.data))
            this.loading = false
        },
        async loadCustomFunctions() {
            this.loading = true
            await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/configs/KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS/${this.businessModelId}`).then((response: AxiosResponse<any>) => {
                if (response.data.data && response.data.data.length > 0) {
                    this.customFunctions = response.data.data.map((funct) => ({ category: 'CUSTOM', formula: funct.value, label: funct.label, name: funct.name, help: 'dataPreparation.custom' }))
                } else this.customFunctions = null
            })
            this.loading = false
        },
        async onRowReorder(event: any) {
            this.loading = true
            const postData = { data: { index: event.dragIndex, direction: event.dropIndex - event.dragIndex }, diff: generate(this.observer) }
            await this.$http
                .post(import.meta.env.VUE_APP_META_API_URL + `/1.0/metaWeb/moveBusinessClass`, postData)
                .then((response: AxiosResponse<any>) => {
                    this.meta = applyPatch(this.meta, response.data).newDocument
                })
                .catch(() => {})
                .finally(() => generate(this.observer))
            this.loading = false
        }
    }
})
</script>
<style lang="scss">
.metaweb-table .p-datatable-thead {
    display: none !important;
}
</style>
