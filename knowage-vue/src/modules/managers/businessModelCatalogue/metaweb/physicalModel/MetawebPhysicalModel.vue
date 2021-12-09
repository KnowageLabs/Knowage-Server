<template>
    <div class="p-grid p-m-0 kn-flex">
        <div class="p-col-4 p-sm-4 p-md-3 p-p-0 p-d-flex p-flex-column kn-flex">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left> {{ $t('metaweb.physicalModel.tables') }}</template>
                <template #right>
                    <Button class="p-button-text p-button-rounded p-button-plain p-button-sm">{{ $t('metaweb.physicalModel.updatePhysicalModel') }}</Button>
                </template>
            </Toolbar>

            <MetawebPhysicalModelList :propMeta="meta" @selected="onSelectedItem"></MetawebPhysicalModelList>
        </div>

        <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 p-d-flex p-flex-column">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ selectedPhysicalModel?.name }}
                </template>
            </Toolbar>
            <div class="metaweb-tab-container p-d-flex p-flex-column kn-flex">
                <TabView class="metaweb-tabview p-d-flex p-flex-column kn-flex" scrollable>
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.physicalModel.propertyList') }}</span>
                        </template>

                        <MetawebPropertyListTab :selectedPhysicalModel="selectedPhysicalModel"></MetawebPropertyListTab>
                    </TabPanel>
                    <TabPanel>
                        <template #header>
                            <span>{{ $t('metaweb.physicalModel.foreignKey') }}</span>
                        </template>
                    </TabPanel>
                </TabView>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iColumn, iPhysicalModel } from '../Metaweb'
import mock from './mock.json'
import MetawebPhysicalModelList from './metawebPhysicalModelList/MetawebPhysicalModelList.vue'
import MetawebPropertyListTab from './tabs/MetawebPropertyListTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'metaweb-physical-model',
    components: { MetawebPhysicalModelList, MetawebPropertyListTab, TabView, TabPanel },
    data() {
        return {
            meta: null as any,
            selectedPhysicalModel: null as iColumn | iPhysicalModel | null
        }
    },
    created() {
        this.loadMeta()
    },
    methods: {
        loadMeta() {
            this.meta = mock
            console.log('LOADED META: ', this.meta)
        },
        onSelectedItem(selectedPhysicalModel: iColumn | iPhysicalModel) {
            this.selectedPhysicalModel = selectedPhysicalModel
            console.log('SELECTED ITEM: ', this.selectedPhysicalModel)
        }
    }
})
</script>
