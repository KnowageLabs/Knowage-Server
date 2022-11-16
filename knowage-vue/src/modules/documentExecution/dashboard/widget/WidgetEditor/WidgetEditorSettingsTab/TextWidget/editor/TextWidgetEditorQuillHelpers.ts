import { Quill } from '@vueup/vue-quill'

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
        node.setAttribute('kn-preview', value)
        return node
    }


    static formats(domNode: HTMLElement): any {
        return domNode.innerHTML || true;
    }
    formats() {
        let formats = super.formats();
        return formats
    }
}

export class SelectionBlot extends Inline {
    static blotName = 'selection'
    static className = 'selection'
    static tagName = 'span'

    static create(value: any) {
        let node = super.create(value)
        node.setAttribute('kn-selection-column', value['kn-selection-column'])
        node.setAttribute('kn-selection-value', value['kn-selection-value'])
        return node
    }


    static formats(domNode: HTMLElement): any {
        return {
            'kn-selection-column': domNode.getAttribute('kn-selection-column'),
            'kn-selection-value': domNode.getAttribute('kn-selection-value')
        };
    }



    formats() {
        let formats = super.formats();
        return formats
    }
}