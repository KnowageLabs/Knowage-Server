<template>
    <Dialog id="function-catalog-detail-dialog" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" :contentStyle="functionsCatalogDetailDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ selectedFunction.name }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text p-m-2" :label="$t('common.close')" @click="$emit('close')"></Button>
                    <Button class="kn-button p-button-text" :label="$t('common.save')" @click="onSave"></Button>
                </template>
            </Toolbar>
        </template>

        <h4>{{ selectedFunction }}</h4>

        <TabView>
            <TabPanel>
                <template #header>
                    <span>{{ $t('common.kpi') }}</span>
                </template>

                <FunctionCatalogGeneralTab :propFunction="selectedFunction" :readonly="readonly" :functionTypes="filteredFunctionTypes" :propKeywords="keywords"></FunctionCatalogGeneralTab>
            </TabPanel>
            <TabPanel>
                <template #header>
                    <span>TODO INPUT</span>
                </template>
            </TabPanel>
            <TabPanel>
                <template #header>
                    <span>TODO SCRIPT</span>
                </template>
            </TabPanel>
            <TabPanel>
                <template #header>
                    <span>TODO OUTPUT</span>
                </template>
            </TabPanel>
        </TabView>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunction, iFunctionType } from './FunctionsCatalog'
import Dialog from 'primevue/dialog'
import functionsCatalogDetailDescriptor from './FunctionsCatalogDetailDescriptor.json'
import FunctionCatalogGeneralTab from './tabs/FunctionCatalogGeneralTab/FunctionCatalogGeneralTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'functions-catalog-detail',
    components: { Dialog, FunctionCatalogGeneralTab, TabView, TabPanel },
    props: {
        visible: { type: Boolean },
        propFunction: { type: Object },
        readonly: { type: Boolean },
        functionTypes: { type: Array },
        keywords: { type: Array }
    },
    data() {
        return {
            functionsCatalogDetailDescriptor,
            selectedFunction: {} as iFunction,
            filteredFunctionTypes: [] as iFunctionType[]
        }
    },
    watch: {
        propFunction() {
            this.loadFunction()
        },
        functionTypes() {
            this.loadFunctionTypes()
        }
    },
    created() {
        this.loadFunction()
        this.loadFunctionTypes()
    },
    methods: {
        loadFunction() {
            this.selectedFunction = this.propFunction
                ? ({ ...this.propFunction } as iFunction)
                : ({
                      description: '',
                      owner: (this.$store.state as any).user.userId
                  } as iFunction)
        },
        loadFunctionTypes() {
            this.filteredFunctionTypes = this.functionTypes?.filter((el: any) => el.valueCd !== 'All') as iFunctionType[]
            console.log('FILTERED FUNCTION TYPES: ', this.filteredFunctionTypes)
        },
        onSave() {
            console.log('onSave() selectedFunction: ', this.selectedFunction)
        }
    }
})
</script>

<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
}

.full-screen-dialog.p-dialog .p-dialog-content {
    padding: 0;
}

#function-catalog-detail-dialog .p-toolbar-group-right {
    height: 100%;
}
</style>
