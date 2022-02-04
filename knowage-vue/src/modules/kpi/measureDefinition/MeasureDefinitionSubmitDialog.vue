<template>
    <Dialog :contentStyle="metadataDefinitionTabViewDescriptor.dialog.style" :header="$t('kpi.measureDefinition.saveInProgress')" :visible="true" :modal="true" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" :closable="false">
        <div class="p-field p-m-2">
            <span class="p-float-label">
                <InputText class="kn-material-input" type="text" v-model.trim="name" />
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
            <Chip v-for="alias in newAlias" class="p-m-2" :key="alias.id" :label="alias"></Chip>
        </div>

        <div v-if="reusedAlias.length > 0">
            <h4>{{ $t('common.reused') }}</h4>
            <Chip v-for="alias in reusedAlias" class="p-m-2" :key="alias.id" :label="alias"></Chip>
        </div>

        <Toolbar v-if="newPlaceholder.length > 0 || reusedPlaceholder.length > 0" class="kn-toolbar kn-toolbar--primary">
            <template #start>
                {{ $t('kpi.measureDefinition.placeholder') }}
            </template>
        </Toolbar>

        <div v-if="newPlaceholder.length > 0">
            <h4>{{ $t('common.new') }}</h4>
            <Chip v-for="placeholder in newPlaceholder" class="p-m-2" :key="placeholder.id" :label="placeholder"></Chip>
        </div>

        <div v-if="reusedPlaceholder.length > 0">
            <h4>{{ $t('common.reused') }}</h4>
            <Chip v-for="placeholder in reusedPlaceholder" class="p-m-2" :key="placeholder.id" :label="placeholder"></Chip>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" :label="$t('common.close')" @click="$emit('close')"></Button>
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="$emit('save', name)" :disabled="saveRuleButtonDisabled"></Button>
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
            name: null as String | null
        }
    },
    computed: {
        saveRuleButtonDisabled(): Boolean {
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
            this.name = this.ruleName as String
        }
    }
})
</script>
