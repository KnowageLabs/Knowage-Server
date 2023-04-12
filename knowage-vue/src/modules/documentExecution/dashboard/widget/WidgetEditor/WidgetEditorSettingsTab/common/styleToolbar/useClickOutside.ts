import { onMounted, onBeforeMount } from "vue"

export function useClickOutside(el_target_ref, callback_fn) {
    if (!el_target_ref) return

    const listener = (e) => {
        if (e.target == el_target_ref.value || e.composedPath().filter((el) => {
            return el && el.className && typeof el.className.includes === 'function' && el.className.includes('click-outside')
        }).length > 0) {
            return
        }

        if (typeof callback_fn == 'function') {
            callback_fn()
        }
    }

    onMounted(() => {
        window.addEventListener('click', listener)
    })
    onBeforeMount(() => {
        window.removeEventListener('click', listener)
    })
    return { listener }
}