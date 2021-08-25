<template>
    <Button :label="$t('managers.driversManagement.useModes.backToList')" icon="pi pi-arrow-left" class="p-button-text" style="pading:5px" @click="$emit('close')" />
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
        <VCodeMirror ref="codeMirror" class="p-mt-2" :options="options" v-model:value="code" :autoHeight="true" />
    </form>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import useModeDescriptor from './UseModesDescriptor.json'
import { VCodeMirror } from 'vue3-code-mirror'
export default defineComponent({
    name: 'lovs-detail',
    components: { VCodeMirror },
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
            codeMirror: {} as any,
            options: {
                mode: 'text/x-mysql',
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                autofocus: true,
                theme: 'eclipse',
                lineNumbers: true
            }
        }
    },
    mounted() {
        this.selectedLov = { ...this.lov }
        this.setupCodeMirror()
    },
    watch: {
        lov() {
            this.selectedLov = { ...this.lov }
            this.setupCodeMirror()
        }
    },
    methods: {
        setupCodeMirror() {
            if (this.$refs.codeMirror) {
                this.codeMirror = (this.$refs.codeMirror as any).editor as any
            }
        }
    }
})
</script>
