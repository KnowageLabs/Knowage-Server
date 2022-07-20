<template>
    <div>
        <Button v-for="(button, index) in filteredButtons" :key="index" :icon="button.icon" class="p-button-text p-button-rounded p-button-plain" @click="clickedButton(button)" v-tooltip.bottom="$t(button.label)" />
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'kn-list-button-renderer',
    components: {},
    props: {
        buttons: { type: Array }
    },
    data() {
        return {
            filteredButtons: [] as any[]
        }
    },
    emits: ['click'],
    mounted() {
        this.filteredButtons = this.buttons!.filter((button: any) => !button.condition)
    },
    methods: {
        clickedButton(button: any) {
            this.$emit('click', button)
        }
    }
})
</script>
