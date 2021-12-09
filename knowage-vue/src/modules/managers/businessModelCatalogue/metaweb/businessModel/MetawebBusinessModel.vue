<template>
    <div class="p-grid p-m-0 kn-flex">
        <div id="CONTAINER ELEMENT LIST" class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column kn-flex">
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
            <div class="kn-flex kn-relative">
                <Toolbar class="kn-toolbar kn-toolbar--default">
                    <template #left>
                        <span>{{ $t('metaweb.businessModel.businessClass') }}</span>
                    </template>
                    <template #right>
                        <KnFabButton icon="fas fa-plus" @click="showMenu" />
                    </template>
                </Toolbar>
                <div :style="bmDescriptor.style.businessClassListContainer">
                    <Listbox v-if="!loading" class="kn-list--column" :style="bmDescriptor.style.mainList" :options="metaMock.metaSales.businessModels" optionLabel="name">
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
            <div class="kn-flex kn-relative">
                <Toolbar class="kn-toolbar kn-toolbar--default">
                    <template #left>
                        <span>{{ $t('metaweb.businessModel.businessView') }}</span>
                    </template>
                </Toolbar>
                <div :style="bmDescriptor.style.businessViewListContainer">
                    <Listbox v-if="!loading" class="kn-list--column" :style="bmDescriptor.style.mainList" :options="metaMock.metaSales.businessViews" optionLabel="name">
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
                    test
                </template>
                <template #right>
                    <Button label="Delete" class="p-button-text p-button-rounded p-button-plain" :style="mainDescriptor.style.white" />
                </template>
            </Toolbar>
            <div class="metaweb-tab-container p-d-flex p-flex-column kn-flex">
                <TabView class="metaweb-tabview p-d-flex p-flex-column kn-flex" scrollable>
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.propertyTitle') }}</span>
                        </template>

                        <MetawebBusinessPropertyListTab></MetawebBusinessPropertyListTab>
                    </TabPanel>
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.businessModel.tabView.attributes') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll">
                            {{ mainDescriptor.test }}
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
                            <span>{{ $t('metaweb.businessModel.tabView.outbound') }}</span>
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
                            <span>{{ $t('metaweb.businessModel.tabView.propertyTitle') }}</span>
                        </template>
                        <div :style="mainDescriptor.style.absoluteScroll">
                            {{ mainDescriptor.test }}
                        </div>
                    </TabPanel>
                </TabView>
            </div>
        </div>
    </div>
    <Menu id="optionsMenu" ref="optionsMenu" :model="menuButtons" />
    <BusinessClassDialog v-if="showBusinessClassDialog" :physicalModels="metaMock.metaSales.physicalModels" :showBusinessClassDialog="showBusinessClassDialog" @closeDialog="showBusinessClassDialog = false" />
    <BusinessViewDialog v-if="showBusinessViewDialog" :physicalModels="metaMock.metaSales.physicalModels" :showBusinessViewDialog="showBusinessViewDialog" @closeDialog="showBusinessViewDialog = false" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import mainDescriptor from '../MetawebDescriptor.json'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Listbox from 'primevue/listbox'
import metaMock from '../MetawebMock.json'
import bmDescriptor from './MetawebBusinessModelDescriptor.json'
import Menu from 'primevue/contextmenu'
import MetawebBusinessPropertyListTab from './tabs/MetawebBusinessPropertyListTab.vue'
import BusinessClassDialog from './dialogs/MetawebBusinessClassDialog.vue'
import BusinessViewDialog from './dialogs/MetawebBusinessViewDialog.vue'

export default defineComponent({
    name: 'metaweb-business-model',
    components: { BusinessClassDialog, BusinessViewDialog, KnFabButton, TabView, TabPanel, Listbox, Menu, MetawebBusinessPropertyListTab },
    props: {},
    computed: {},
    data() {
        return {
            bmDescriptor,
            mainDescriptor,
            metaMock,
            menuButtons: [] as any,
            showBusinessClassDialog: false,
            showBusinessViewDialog: false
        }
    },
    created() {
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
        logEvent(event) {
            console.log(event)
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
