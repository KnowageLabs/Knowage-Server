<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left>{{ $t('managers.glossaryDefinition.title') }}</template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    </Toolbar>
    <Card class="p-m-3">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-m-3">
                <template #left>
                    {{ $t('managers.glossaryDefinition.glossary') }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text">{{ $t('common.delete') }}</Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div>
                <div class="p-field p-d-flex p-ai-center p-m-3">
                    <div class="p-d-flex p-flex-column p-mr-2" id="glossary-select-container">
                        <label for="glossary" class="kn-material-input-label">{{ $t('managers.glossaryDefinition.title') }}</label>
                        <Dropdown id="glossary" class="kn-material-input" v-model="selectedGlossary" :options="glossaries" optionLabel="GLOSSARY_NM" optionValue="GLOSSARY_ID" :placeholder="$t('managers.glossaryDefinition.glossary')" @change="test($event.value)" />
                    </div>
                    <div v-if="selectedGlossary" class="p-m-3" id="code-container">
                        <span class="p-float-label p-mt-3">
                            <InputText id="code" class="kn-material-input full-width" v-model.trim="selectedGlossary.code" disabled />
                            <label for="code" class="kn-material-input-label"> {{ $t('managers.glossaryDefinition.code') }}</label>
                        </span>
                    </div>
                </div>
                <div v-if="selectedGlossary" class="p-field p-d-flex p-m-3 kn-flex">
                    <div class="p-float-label kn-flex p-m-3">
                        <InputText id="description" class="kn-material-input full-width" v-model.trim="selectedGlossary.description" disabled />
                        <label for="description" class="kn-material-input-label"> {{ $t('common.description') }}</label>
                    </div>
                </div>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iGlossary } from './GlossaryDefinition'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'glossary-definition-detail',
    components: { Card, Dropdown },
    props: { glossaryList: { type: Array } },
    data() {
        return {
            glossaries: [] as iGlossary[],
            selectedGlossary: null as iGlossary | null,
            loading: false
        }
    },
    created() {
        this.loadGlossaries()
    },
    methods: {
        loadGlossaries() {
            this.glossaries = [...(this.glossaryList as iGlossary[])]
        },
        test(glossary: iGlossary) {
            console.log('SELECTED GLOSSARY: ', glossary)
        }
    }
})
</script>

<style lang="scss" scoped>
#glossary-select-container {
    width: 60%;
}

#code-container {
    width: 40%;
}

.full-width {
    width: 100%;
}
</style>
