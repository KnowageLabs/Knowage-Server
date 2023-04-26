<template>
    <Dialog :content-style="metadataDefinitionTabViewDescriptor.dialog.style" :header="$t('kpi.measureDefinition.saveInProgress')" :visible="true" :modal="true" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-field p-m-2">
            <span class="p-float-label">
                <InputText v-model.trim="name" class="kn-material-input" type="text" />
                <label class="kn-material-input-label"> {{ $t('common.name') }} </label>
            </span>
        </div>
        <Toolbar v-if="newAlias.length > 0 || reusedAlias.length > 0" class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('kpi.measureDefinition.alias') }}
            </template>
        </Toolbar>
        <div v-if="newAlias.length > 0">
            <h4>{{ $t('common.new') }}</h4>
            <Chip v-for="alias in newAlias" :key="alias.id" class="p-m-2" :label="alias"></Chip>
        </div>

        <div v-if="reusedAlias.length > 0">
            <h4>{{ $t('common.reused') }}</h4>
            <Chip v-for="alias in reusedAlias" :key="alias.id" class="p-m-2" :label="alias"></Chip>
        </div>

        <Toolbar v-if="newPlaceholder.length > 0 || reusedPlaceholder.length > 0" class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('kpi.measureDefinition.placeholder') }}
            </template>
        </Toolbar>

        <div v-if="newPlaceholder.length > 0">
            <h4>{{ $t('common.new') }}</h4>
            <Chip v-for="placeholder in newPlaceholder" :key="placeholder.id" class="p-m-2" :label="placeholder"></Chip>
        </div>

        <div v-if="reusedPlaceholder.length > 0">
            <h4>{{ $t('common.reused') }}</h4>
            <Chip v-for="placeholder in reusedPlaceholder" :key="placeholder.id" class="p-m-2" :label="placeholder"></Chip>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="$emit('close')"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" :disabled="saveRuleButtonDisabled" @click="$emit('save', name)"></Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Chip from 'primevue/chip'
import Dialog from 'primevue/dialog'
import metadataDefinitionTabViewDescriptor from './MetadataDefinitionTabViewDescriptor.json'

export default defineComponent({
    name: 'measure-definition-submit-dialog',
    components: { Chip, Dialog },
    props: { ruleName: { type: String }, newAlias: { type: Array }, reusedAlias: { type: Array }, newPlaceholder: { type: Array }, reusedPlaceholder: { type: Array } },
    emits: ['close'],
    data() {
        return {
            metadataDefinitionTabViewDescriptor,
            name: null as string | null
        }
    },
    computed: {
        saveRuleButtonDisabled(): boolean {
            return !this.name
        }
    },
    watch: {
        currentRule() {
            this.loadRuleName()
        }
    },
    async created() {
        this.loadRuleName()
    },
    methods: {
        loadRuleName() {
            this.name = this.ruleName as string
        }
    }
})
</script>
