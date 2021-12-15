<template>
    <div v-if="meta" class="p-grid p-m-0 kn-flex">
        <div id="CONTAINER ELEMENT LIST" class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column kn-flex">
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            <div class="p-d-flex p-flex-column kn-flex">
                <Toolbar class="kn-toolbar kn-toolbar--default">
                    <template #left>
                        <span>{{ $t('metaweb.businessModel.businessClass') }}</span>
                    </template>
                    <template #right>
                        <KnFabButton icon="fas fa-plus" @click="showMenu" />
                    </template>
                </Toolbar>
                <div class="kn-relative kn-flex">
                    <Listbox v-if="!loading" class="kn-list--column kn-absolute kn-height-full kn-width-full" :options="meta.businessModels" optionLabel="name" @change="selectBusinessModel">
                        <template #empty>{{ $t('common.info.noDataFound') }}</template>
                        <template #option="slotProps">
                            <div class="kn-list-item" data-test="list-item">
                                <div class="kn-list-item-text">
                                    <span>{{ slotProps.option.name }}</span>
                                    <span class="kn-list-item-text-secondary">{{ slotProps.option.columns.length }} Attributes</span>
                                </div>
                                <Button icon="pi pi-trash" class="p-button-text p-button-rounded p-button-plain" />
                            </div>
                        </template>
                    </Listbox>
                </div>
            </div>
            <div class="p-d-flex p-flex-column kn-flex">
                <Toolbar class="kn-toolbar kn-toolbar--default">
                    <template #left>
                        <span>{{ $t('metaweb.businessModel.businessView') }}</span>
                    </template>
                </Toolbar>
                <div class="kn-relative kn-flex">
                    <Listbox v-if="!loading" class="kn-list--column kn-absolute kn-height-full kn-width-full" :options="meta.businessViews" optionLabel="name" @change="selectBusinessModel">
                        <template #empty>{{ $t('common.info.noDataFound') }}</template>
                        <template #option="slotProps">
                            <div class="kn-list-item" data-test="list-item">
                                <div class="kn-list-item-text">
                                    <span>{{ slotProps.option.name }}</span>
                                    <span class="kn-list-item-text-secondary">{{ slotProps.option.columns.length }} Attributes</span>
                                </div>
                                <Button icon="pi pi-trash" class="p-button-text p-button-rounded p-button-plain" />
                            </div>
                        </template>
                    </Listbox>
                </div>
            </div>
        </div>
        <div id="CONTAINER ELEMENT DETAILS" class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 p-d-flex p-flex-column" :style="mainDescriptor.style.flex3">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
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
                            <MetawebBusinessPropertyListTab :selectedBusinessModel="selectedBusinessModel" @metaUpdated="$emit('metaUpdated')"></MetawebBusinessPropertyListTab>
                        </div>
                    </TabPanel>

                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.attributes') }}</span>
                        </template>

                        <div :style="mainDescriptor.style.absoluteScroll">
                            <MetawebAttributesTab :selectedBusinessModel="selectedBusinessModel" :propMeta="meta" :observer="observer" @metaUpdated="$emit('metaUpdated')"></MetawebAttributesTab>
                        </div>
                    </TabPanel>

                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.calcField') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll">
                            {{ mainDescriptor.test }}
                        </div>
                    </TabPanel>
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.inbound') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll">
                            <InboundRelationships :selectedBusinessModel="selectedBusinessModel" :businessModels="meta.businessModels" :propMeta="meta" :observer="observer" :businessViews="meta.businessViews" />
                        </div>
                        <!-- <div class="kn-relative kn-flex">
                            <div class="kn-height-full kn-width-full kn-absolute"> -->
                        <!-- </div>
                        </div> -->
                    </TabPanel>
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.outbound') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll">
                            <OutboundRelationships :selectedBusinessModel="selectedBusinessModel" :businessModels="meta.businessModels" :propMeta="meta" :observer="observer" :businessViews="meta.businessViews" />
                        </div>
                    </TabPanel>
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.filter') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll">
                            {{ mainDescriptor.test }}
                        </div>
                    </TabPanel>

                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.filter') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll">
                            {{ mainDescriptor.test }}
                        </div>
                    </TabPanel>

                    <TabPanel>
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
    <BusinessViewDialog v-if="showBusinessViewDialog" :meta="meta" :observer="observer" :physicalModels="meta.physicalModels" :showBusinessViewDialog="showBusinessViewDialog" @closeDialog="showBusinessViewDialog = false" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iBusinessModel } from '../Metaweb'
import mainDescriptor from '../MetawebDescriptor.json'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Listbox from 'primevue/listbox'
import bmDescriptor from './MetawebBusinessModelDescriptor.json'
import Menu from 'primevue/contextmenu'
import MetawebBusinessPropertyListTab from './tabs/propertyListTab/MetawebBusinessPropertyListTab.vue'
import BusinessClassDialog from './dialogs/MetawebBusinessClassDialog.vue'
import BusinessViewDialog from './dialogs/MetawebBusinessViewDialog.vue'
import MetawebAttributesTab from './tabs/metawebAttributesTab/MetawebAttributesTab.vue'
import InboundRelationships from './tabs/inboundRelationships/MetawebInboundRelationships.vue'
import OutboundRelationships from './tabs/outboundRelationships/MetawebOutboundRelationships.vue'
import MetawebPhysicalTableTab from './tabs/physicalTable/MetawebPhysicalTableTab.vue'

export default defineComponent({
    name: 'metaweb-business-model',
    components: { OutboundRelationships, BusinessClassDialog, BusinessViewDialog, KnFabButton, TabView, TabPanel, Listbox, Menu, MetawebBusinessPropertyListTab, MetawebAttributesTab, InboundRelationships, MetawebPhysicalTableTab },
    props: { propMeta: { type: Object }, observer: { type: Object }, metaUpdated: { type: Boolean } },
    emits: ['loading', 'metaUpdated'],
    computed: {},
    data() {
        return {
            bmDescriptor,
            mainDescriptor,
            meta: null as any,
            menuButtons: [] as any,
            showBusinessClassDialog: false,
            showBusinessViewDialog: false,
            selectedBusinessModel: {} as iBusinessModel
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
            // console.log('LOADED META BUSIENSS MODEL: ', this.meta)
        },
        selectBusinessModel(event) {
            console.log(event.value)
            this.selectedBusinessModel = event.value as iBusinessModel
        },
        showBusinessClass() {
            this.showBusinessClassDialog = true
        },
        showBusinessView() {
            this.showBusinessViewDialog = true
        }
    }
})
</script>
