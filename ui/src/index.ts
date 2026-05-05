import { definePlugin, type ExtensionPoint } from '@halo-dev/ui-shared'
import PdfExtension from './editor'

type AnyExtension = Awaited<
  ReturnType<NonNullable<ExtensionPoint['default:editor:extension:create']>>
>[number]

export default definePlugin({
  extensionPoints: {
    'default:editor:extension:create': () => {
      return [PdfExtension as unknown as AnyExtension]
    },
  },
})
