<template>
    <Card>
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText id="url" class="kn-material-input" type="text" v-model="broker.url" />
                        <label for="url" class="kn-material-input-label">{{ $t('kpi.alert.brokerUrl') }} * </label>
                    </span>
                    <span class="p-float-label">
                        <InputText id="type" class="kn-material-input" type="text" v-model="broker.type" />
                        <label for="type" class="kn-material-input-label">{{ $t('kpi.alert.brokerType') }} * </label>
                    </span>
                </div>
            </form>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
export default defineComponent({
    name: 'contex-broker-card',
    data() {
        return {
            broker: { url: null, type: null }
        }
    }
})
</script>
