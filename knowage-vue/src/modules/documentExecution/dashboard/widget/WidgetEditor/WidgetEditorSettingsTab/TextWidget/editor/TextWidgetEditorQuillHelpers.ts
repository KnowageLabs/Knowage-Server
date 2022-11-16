import { Delta, Quill } from '@vueup/vue-quill'

const Inline = Quill.import('blots/inline')

export class CrossNavBlot extends Inline {
    static blotName = 'crossNav'
    static className = 'crossNavigation'
    static tagName = 'span'

    static create(value) {
        let node = super.create(value)
        console.log('>>>>>>>>>> VALUE: ', value)
        node.setAttribute('kn-cross', '')
        // node.addEventListener('click', () => alert('works'))
        return node
    }

    static formats(domNode: HTMLElement): any {
        console.log('>>>>>>>>>> formats: ', domNode)
        if (typeof this.tagName === 'string') {
            return true
        } else if (Array.isArray(this.tagName)) {
            return domNode.tagName.toLowerCase()
        }
        return undefined
    }
}