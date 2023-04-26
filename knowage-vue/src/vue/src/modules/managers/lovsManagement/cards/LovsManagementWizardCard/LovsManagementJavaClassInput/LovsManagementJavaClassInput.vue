<template>
    <div class="p-fluid p-m-4">
        <span class="p-float-label">
            <InputText
                id="javaClassName"
                v-model.trim="javaClass.name"
                class="kn-material-input"
                type="text"
                :class="{
                    'p-invalid': !javaClass.name && dirty
                }"
                max-length="160"
                @blur="dirty = true"
                @input="$emit('touched')"
            />
            <label for="javaClassName" class="kn-material-input-label"> {{ $t('managers.lovsManagement.javaClassName') }} * </label>
        </span>
        <div v-if="!javaClass.name && dirty" class="p-error p-grid">
            <small class="p-col-12">
                {{ $t('common.validation.required', { fieldName: $t('managers.lovsManagement.javaClassName') }) }}
            </small>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'lovs-management-java-class-input',
    components: {},
    props: {
        selectedJavaClass: { type: Object }
    },
    data() {
        return {
            javaClass: {} as { name: string },
            dirty: false
        }
    },
    watch: {
        selectedJavaClass() {
            this.loadJavaClass()
        }
    },
    created() {
        this.loadJavaClass()
    },
    methods: {
        loadJavaClass() {
            this.javaClass = this.selectedJavaClass as { name: string }
        }
    }
})
</script>
