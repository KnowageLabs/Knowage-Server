<template>
    <Card :class="['kn-tab-card--icon', 'selectable', selected ? 'selected' : '']">
        <template #content>
            <span class="heading">{{ $t(element.label) }} <Badge v-if="badge && badge > 0" :value="badge"></Badge></span>
            <span v-if="element.description" class="subheading">{{ $t(element.description) }}</span>
            <i :class="['icon', element.icon]"></i>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Badge from 'primevue/badge'

export default defineComponent({
    name: 'kn-tab-card',
    components: { Badge },
    props: {
        badge: null,
        element: {},
        selected: Boolean
    }
})
</script>

<style lang="scss"></style>
