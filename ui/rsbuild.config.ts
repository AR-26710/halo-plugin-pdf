import { rsbuildConfig } from '@halo-dev/ui-plugin-bundler-kit';
import Icons from "unplugin-icons/rspack";
import { pluginSass } from "@rsbuild/plugin-sass";
import type { RsbuildConfig } from "@rsbuild/core";

export default rsbuildConfig({
  rsbuild: {
    resolve: {
      alias: {
        "@": "./src",
      },
    },
    plugins: [pluginSass()],
    tools: {
      rspack: {
        plugins: [Icons({ compiler: "vue3" })],
      },
    },
    output: {
      copy: [
        { from: './node_modules/pdfjs-dist/build/pdf.min.mjs', to: 'pdfjs/build' },
        { from: './node_modules/pdfjs-dist/build/pdf.worker.min.mjs', to: 'pdfjs/build' },
        { from: './node_modules/pdfjs-dist/web/pdf_viewer.mjs', to: 'pdfjs/web' },
        { from: './node_modules/pdfjs-dist/web/pdf_viewer.css', to: 'pdfjs/web' },
        { from: './node_modules/pdfjs-dist/web/images', to: 'pdfjs/web/images' },
        { from: './node_modules/pdfjs-dist/cmaps', to: 'pdfjs/cmaps' },
      ],
    },
  },
}) as RsbuildConfig
