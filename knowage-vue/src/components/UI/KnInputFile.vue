<template>
    <div :class="getVisibility">
        <span :v-if="label"> {{ label }} </span>
        <input ref="inputFile" id="inputFile" type="file" @change="changeFunction" :accept="accept" />
        <label for="inputFile">
            <i class="pi pi-upload" />
        </label>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    name: 'kn-file-input',
    props: {
        accept: String,
        changeFunction: { type: Function },
        label: String,
        visibility: Boolean,
        triggerInput: Boolean
    },
    computed: {
        getVisibility(): String {
            return this.visibility ? '' : 'kn-hide'
        }
    },
    watch: {
        triggerInput(newTriggerInput, oldTriggerInput) {
            if (newTriggerInput != oldTriggerInput && newTriggerInput) {
                var elem = this.$refs.inputFile as any
                if (elem && document.createEvent) {
                    var evt = document.createEvent('MouseEvents')
                    evt.initEvent('click', true, false)
                    elem.dispatchEvent(evt)
                }
            }
        }
    }
})
</script>

<style scoped lang="scss"></style>
