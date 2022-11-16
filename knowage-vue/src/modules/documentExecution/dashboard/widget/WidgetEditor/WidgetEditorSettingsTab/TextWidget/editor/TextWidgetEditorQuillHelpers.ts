import { Delta, Quill } from '@vueup/vue-quill'
import deepcopy from 'deepcopy'

const Inline = Quill.import('blots/inline')

export class CrossNavBlot extends Inline {
    static blotName = 'crossNav'
    static className = 'crossNavigation'
    static tagName = 'span'

    static create(value) {
        let node = super.create(value)
        node.setAttribute('kn-cross', '')
        return node
    }

    static formats(domNode: HTMLElement): any {
        if (typeof this.tagName === 'string') {
            return true
        } else if (Array.isArray(this.tagName)) {
            return domNode.tagName.toLowerCase()
        }
        return undefined
    }
}

export class PreviewBlot extends Inline {
    static blotName = 'preview'
    static className = 'preview'
    static tagName = 'span'

    static create(value) {
        let node = super.create(value)
        console.log('>>>>>>>>>> CREATE - VALUE: ', value)
        console.log('>>>>>>>>>> NODE: ', node)
        console.log('>>>>>>>>>> node domNode: ', deepcopy(node.textContent))
        // console.log('>>>>>>>>>> node kn-preview: ', node.domNode['kn-preview'])
        node.setAttribute('kn-preview', '')
        return node
    }


    static formats(domNode: HTMLElement): any {
        console.log(">>>> FORMATS: ", domNode)
        console.log(">>>> FORMATS       domNode.innerHTML: ", domNode.innerHTML)
        return domNode.getAttribute('kn-preview') || true;
    }

    format(format: any, value: any) {
        console.log("---------- format: ", format)
        console.log("---------- value: ", value)
    }
}