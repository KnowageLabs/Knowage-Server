<template>
    <div class="p-m-4">
        <Message v-if="selectedParameter.type === 'From Cell'" class="p-m-4" severity="info" :closable="false" :style="olapCrossNavigationDefinitionDialogDescriptor.styles.message">
            {{ $t('documentExecution.olap.crossNavigationDefinition.hint') }}
        </Message>

        <div class="p-fluid p-col-12 p-md-12 p-mt-4">
            <span class="p-float-label">
                <InputText id="value" class="kn-material-input" v-model.trim="value" />
                <label for="value" class="kn-material-input-label">{{ $t('common.value') }}</label>
            </span>
        </div>

        <Button id="olap-select-from-table-button" class="kn-button kn-button--primary p-mt-4" @click="selectFromTable"> {{ $t('documentExecution.olap.crossNavigationDefinition.selectFromTable') }}</Button>

        <div class="p-fluid p-col-12 p-md-12 p-mt-4">
            <span class="p-float-label">
                <InputText
                    id="name"
                    class="kn-material-input"
                    v-model.trim="selectedParameter.name"
                    :class="{
                        'p-invalid': !selectedParameter.name && nameTouched
                    }"
                    @blur="nameTouched = true"
                />
                <label for="name" class="kn-material-input-label">{{ $t('documentExecution.olap.crossNavigationDefinition.parameterName') }} *</label>
            </span>
            <div v-if="!selectedParameter.name && nameTouched" class="p-error">
                <small class="p-col-12"> {{ $t('common.validation.required', { fieldName: $t('documentExecution.olap.crossNavigationDefinition.parameterName') }) }} </small>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iOlapCrossNavigationParameter } from '../Olap'
import Message from 'primevue/message'
import olapCrossNavigationDefinitionDialogDescriptor from './OlapCrossNavigationDefinitionDialogDescriptor.json'

export default defineComponent({
    name: 'olap-cross-navigation-step-two',
    components: { Message },
    props: { propSelectedParameter: { type: Object as PropType<iOlapCrossNavigationParameter | null> } },
    data() {
        return {
            olapCrossNavigationDefinitionDialogDescriptor,
            selectedParameter: {} as iOlapCrossNavigationParameter,
            value: '',
            nameTouched: false
        }
    },
    watch: {
        propSelectedParameter() {
            this.loadSelectedParameter()
        }
    },
    created() {
        this.loadSelectedParameter()
    },
    methods: {
        loadSelectedParameter() {
            this.selectedParameter = this.propSelectedParameter as iOlapCrossNavigationParameter
            this.value = this.selectedParameter.name ? `dimension=${this.selectedParameter.dimension} hierarchy=${this.selectedParameter.hierarchy} level=${this.selectedParameter.level}` : ''
            console.log('LOADED SELECTED PARAMETER IN STEP TWO: ', this.selectedParameter)
        },
        selectFromTable() {
            console.log('SELECT FROM TABLE CLICKED!')
        }
    }
})
</script>

<style lang="scss">
#olap-select-from-table-button {
    max-width: 200px;
}
</style>
