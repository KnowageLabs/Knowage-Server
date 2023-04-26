<template>
    <div class="knRotate">
        <div class="phone"></div>
        <div class="message">
            {{ $t('common.rotateDevice') }}
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'kn-rotate'
})
</script>
