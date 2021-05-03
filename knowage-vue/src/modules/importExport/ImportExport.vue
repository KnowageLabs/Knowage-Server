<template>
    <div class="kn-importExport kn-page">
        <ImportDialog v-bind:visibility="displayImportDialog"></ImportDialog>
        <ExportDialog v-bind:visibility="displayExportDialog" @export="startExport"></ExportDialog>
        <Toolbar class="kn-toolbar kn-toolbar--primary">
            <template #left>
                {{ $t('importExport.title') }}
            </template>
            <template #right>
                <Button class="kn-button p-button-text" @click="openImportDialog">{{ $t('common.import') }}</Button>
                <Button class="kn-button p-button-text" @click="exportSelection" :disabled="isExportDisabled()">{{ $t('common.export') }}</Button>
            </template>
        </Toolbar>
        <div class="kn-page-content p-grid p-m-0">
            <div class="functionalities-container p-col-3 p-sm-3 p-md-2">
                <KnTabCard :element="functionality" v-for="(functionality, index) in importExportDescriptor.functionalities" v-bind:key="index" @click="selectType(functionality)" :badge="selectedItems['gallery'].length"></KnTabCard>
            </div>
            <div class="p-col-9 p-sm-9 p-md-10 p-p-0 p-m-0">
                <router-view @onItemSelected="getSelectedItems($event)" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
/* 	import axios from 'axios' */
import importExportDescriptor from './ImportExportDescriptor.json'
import ExportDialog from './ExportDialog.vue'
import ImportDialog from './ImportDialog.vue'
import KnTabCard from '@/components/UI/KnTabCard.vue'

export default defineComponent({
    name: 'import-export',
    components: { ExportDialog, KnTabCard, ImportDialog },
    data() {
        return {
            importExportDescriptor: importExportDescriptor,
            displayImportDialog: false,
            displayExportDialog: false,
            fileName: '',
            selectedItems: {
                gallery: []
            }
        }
    },
    emits: ['onItemSelected'],
    methods: {
        getSelectedItems(e) {
            this.selectedItems[e.functionality] = e.items
        },
        isExportDisabled() {
            for (var index in this.selectedItems) {
                if (this.selectedItems[index].length > 0) return false
            }
            return true
        },
        selectType(type): void {
            this.$router.push(type.route)
        },
        openImportDialog(): void {
            this.displayImportDialog = !this.displayImportDialog
        },
        exportSelection(): void {
            this.displayExportDialog = !this.displayExportDialog
        },

        startExport(): void {
            /* 				axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/widgetgallery/', this.selectedItems).then(
					() => {},
					(error) => console.error(error)
				) */
            console.log(this.selectedItems)
        }
    }
})
</script>

<style lang="scss" scoped></style>
