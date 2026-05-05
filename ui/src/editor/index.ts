import { Node, type Range } from '@tiptap/core'
import { VueNodeViewRenderer } from '@tiptap/vue-3'
import { markRaw } from 'vue'
import type { Editor } from '@tiptap/core'
import PdfNodeView from './PdfNodeView.vue'
import { Icon } from '@iconify/vue'

declare module '@tiptap/core' {
  interface Commands<ReturnType> {
    pdf: {
      setPdf: (options: { src: string; fileName?: string }) => ReturnType
    }
  }
}

const PdfIcon = markRaw({
  components: { Icon },
  template: `<Icon icon="ri:file-pdf-line" />`
})

export const PdfExtension = Node.create({
  name: 'pdf',

  group: 'block',

  atom: true,

  draggable: true,

  addAttributes() {
    return {
      src: {
        default: '',
        parseHTML: (element) => element.getAttribute('data-src'),
        renderHTML: (attributes) => ({
          'data-src': attributes.src,
        }),
      },
      fileName: {
        default: '',
        parseHTML: (element) => element.getAttribute('data-filename'),
        renderHTML: (attributes) => ({
          'data-filename': attributes.fileName,
        }),
      },
    }
  },

  parseHTML() {
    return [
      {
        tag: 'div[data-type="pdf"]',
      },
    ]
  },

  renderHTML({ HTMLAttributes }) {
    return ['div', { 'data-type': 'pdf', ...HTMLAttributes }]
  },

  addNodeView() {
    return VueNodeViewRenderer(PdfNodeView)
  },

  addCommands() {
    return {
      setPdf:
        (options: { src: string; fileName?: string }) =>
        ({ commands }) => {
          return commands.insertContent({
            type: this.name,
            attrs: {
              src: options.src,
              fileName: options.fileName || '',
            },
          })
        },
    }
  },

  addOptions() {
    const insertEmptyPdf = (editor: Editor) => {
      editor
        .chain()
        .focus()
        .insertContent({
          type: this.name,
          attrs: { src: '', fileName: '' },
        })
        .run()
    }

    return {
      ...this.parent?.(),

      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 60,
          component: markRaw({
            components: { Icon },
            template: `
              <button class="halo-editor-toolbar__item" @click="action" :title="title">
                <Icon :icon="iconName" class="halo-editor-toolbar__icon" />
              </button>
            `,
            props: {
              title: { type: String },
              action: { type: Function },
              iconName: { type: String },
            },
          }),
          props: {
            title: '插入PDF',
            action: () => insertEmptyPdf(editor),
            iconName: 'ri:file-pdf-line',
          },
        }
      },

      getToolboxItems({ editor }: { editor: Editor }) {
        return {
          priority: 40,
          component: markRaw({
            components: { Icon },
            template: `
              <div
                role="menuitem"
                tabindex="-1"
                class="group my-1.5 flex cursor-pointer flex-row items-center gap-3 rounded px-1.5 py-1 transition-colors first:mt-0 last:mb-0 hover:bg-gray-100"
                @click="action"
              >
                <div
                  class="size-7 flex-none rounded bg-gray-100 p-1.5 group-hover:bg-white"
                >
                  <Icon :icon="iconName" class="size-full" />
                </div>
                <div class="flex min-w-0 flex-1 shrink flex-col gap-0.5">
                  <span
                    class="line-clamp-1 text-sm text-gray-600 group-hover:font-medium group-hover:text-gray-900"
                    :title="title"
                  >
                    {{ title }}
                  </span>
                  <span
                    v-if="description"
                    class="line-clamp-2 text-xs text-gray-500"
                    :title="description"
                  >
                    {{ description }}
                  </span>
                </div>
              </div>
            `,
            props: {
              title: { type: String },
              description: { type: String },
              action: { type: Function },
              iconName: { type: String },
            },
          }),
          props: {
            title: 'PDF文件',
            action: () => insertEmptyPdf(editor),
            iconName: 'ri:file-pdf-line',
          },
        }
      },

      getCommandMenuItems() {
        return {
          priority: 100,
          icon: PdfIcon,
          title: 'PDF文件',
          keywords: ['pdf', '文件预览'],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .insertContent({
                type: 'pdf',
                attrs: { src: '', fileName: '' },
              })
              .run()
          },
        }
      },
    }
  },
})

export default PdfExtension
