<template>
    <div class="p-mb-3">
        <Button :label="$t('managers.driversManagement.useModes.backToList')" icon="pi pi-arrow-left" class="p-button-text" style="width:120px;" @click="$emit('close')" />
    </div>
    <form class="p-fluid p-formgrid p-grid">
        <div class="p-field p-col-4">
            <span class="p-float-label">
                <InputText id="label" class="kn-material-input" type="text" v-model="selectedLov.label" disabled />
                <label for="label" class="kn-material-input-label">{{ $t('common.label') }} </label>
            </span>
        </div>
        <div class="p-field p-col-4">
            <span class="p-float-label">
                <InputText id="name" class="kn-material-input" type="text" v-model="selectedLov.name" disabled />
                <label for="name" class="kn-material-input-label">{{ $t('common.name') }} </label>
            </span>
        </div>
        <div class="p-field p-col-4">
            <span class="p-float-label">
                <InputText id="type" class="kn-material-input" type="text" v-model="selectedLov.itypeCd" disabled />
                <label for="type" class="kn-material-input-label">{{ $t('common.type') }} </label>
            </span>
        </div>
        <div class="p-field p-col-12">
            <span class="p-float-label">
                <InputText id="desc" class="kn-material-input" type="text" v-model="selectedLov.description" disabled />
                <label for="desc" class="kn-material-input-label">{{ $t('common.description') }} </label>
            </span>
        </div>
        <VCodeMirror v-if="codeMirrorVisiable" ref="codeMirror" class="p-mt-2" :options="options" v-model:value="code" :autoHeight="true" />
        <DataTable v-if="this.selectedLov.itypeCd === 'FIX_LOV'" :value="rows" class="p-datatable-sm kn-table" responsiveLayout="stack">
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>

            <Column field="VALUE" :header="$t('common.value')" class="kn-truncated"></Column>
            <Column field="DESCRIPTION" :header="$t('common.description')" class="kn-truncated"></Column>
        </DataTable>
        <div class="p-field p-col-6" v-if="selectedLov.itypeCd === 'DATASET' || selectedLov.itypeCd === 'JAVACLASS'">
            <span class="p-float-label">
                <InputText id="label" class="kn-material-input" type="text" v-model="label" disabled />
                <label for="label" class="kn-material-input-label">{{ $t('common.label') }} </label>
            </span>
        </div>
        <div class="p-field p-col-6" v-if="selectedLov.itypeCd === 'DATASET' || selectedLov.itypeCd === 'JAVACLASS'">
            <span class="p-float-label">
                <InputText id="description" class="kn-material-input" type="text" v-model="name" disabled />
                <label for="description" class="kn-material-input-label">{{ $t('common.name') }} </label>
            </span>
        </div>
    </form>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import useModeDescriptor from './UseModesDescriptor.json'
import { VCodeMirror } from 'vue3-code-mirror'
import { decode } from 'js-base64'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
export default defineComponent({
    name: 'lovs-detail',
    components: { VCodeMirror, Column, DataTable },
    props: {
        lov: {
            type: Object,
            required: false
        }
    },
    emits: ['close', 'apply'],
    data() {
        return {
            selectedLov: {} as any,
            useModeDescriptor,
            code: '',
            rows: [],
            codeMirror: {} as any,
            codeMirrorVisiable: false,
            label: null,
            name: null,
            options: {
                mode: 'text/x-mysql',
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                autofocus: true,
                theme: 'eclipse',
                lineNumbers: true,
                readOnly: true
            }
        }
    },
    mounted() {
        this.selectedLov = { ...this.lov }
        this.decode()
        this.setupCodeMirror()
    },
    watch: {
        lov() {
            this.selectedLov = { ...this.lov }
            this.decode()
            this.setupCodeMirror()
        }
    },
    methods: {
        setupCodeMirror() {
            if (this.$refs.codeMirror) {
                this.codeMirror = (this.$refs.codeMirror as any).editor as any
            }
        },
        escapeXml(value: string) {
            return value
                .replace(/'/g, "'")
                .replace(/"/g, '"')
                .replace(/>/g, '>')
                .replace(/</g, '<')
                .replace(/&/g, '&')
                .replace(/&apos;/g, "'")
        },
        decode() {
            if (this.selectedLov.itypeCd === 'QUERY') {
                this.codeMirrorVisiable = true
                this.options.mode = 'text/x-mysql'
                let x = JSON.parse(this.lov?.lovProviderJSON)
                this.code = this.escapeXml(decode(x.QUERY.STMT))
            } else if (this.selectedLov.itypeCd === 'SCRIPT') {
                this.codeMirrorVisiable = true
                this.options.mode = 'text/javascript'
                let x = JSON.parse(this.lov?.lovProviderJSON)
                this.code = this.escapeXml(decode(x.SCRIPTLOV.SCRIPT))
            } else if (this.selectedLov.itypeCd === 'FIX_LOV') {
                this.codeMirrorVisiable = false
                let x = JSON.parse(this.lov?.lovProviderJSON)
                Array.isArray(x.FIXLISTLOV.ROWS.ROW) ? (this.rows = x.FIXLISTLOV.ROWS.ROW) : (this.rows = Object.values(x.FIXLISTLOV.ROWS))
            } else if (this.selectedLov.itypeCd === 'DATASET') {
                this.codeMirrorVisiable = false
                let x = JSON.parse(this.lov?.lovProviderJSON)
                this.label = x.DATASET.LABEL
            } else if (this.selectedLov.itypeCd === 'JAVACLASS') {
                this.codeMirrorVisiable = false
                let x = JSON.parse(this.lov?.lovProviderJSON)
                this.label = x.JAVACLASS.label
                this.name = x.JAVACLASS.name
            }
        }
    }
})
</script>
