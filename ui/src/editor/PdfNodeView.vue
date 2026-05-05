<script setup lang="ts">
import { computed, ref } from 'vue'
import { NodeViewWrapper, type NodeViewProps } from '@tiptap/vue-3'
import { utils } from '@halo-dev/ui-shared'
import type { AttachmentLike } from '@halo-dev/ui-shared'
import { Icon } from '@iconify/vue'

const VIEWER_BASE_PATH = '/plugins/plugin-pdf/assets/pdfjs/viewer.html'

const props = defineProps<NodeViewProps>()

const externalUrl = ref('')
const showExternalInput = ref(false)
const attachmentSelectorVisible = ref(false)
const isEditing = ref(false)

const src = computed(() => props.node.attrs.src || '')
const fileName = computed(() => props.node.attrs.fileName || '')
const isConfigured = computed(() => !!src.value)

const displayName = computed(() => {
  if (fileName.value) return fileName.value
  if (src.value) {
    const parts = src.value.split('/')
    return parts[parts.length - 1] || 'PDF文档'
  }
  return 'PDF文档'
})

const viewerUrl = computed(() => {
  if (!src.value) return ''
  return `${VIEWER_BASE_PATH}?file=${encodeURIComponent(src.value)}`
})

function handleUrlConfirm() {
  const url = externalUrl.value.trim()
  if (!url) return
  props.updateAttributes({ src: url, fileName: '' })
  externalUrl.value = ''
  showExternalInput.value = false
  isEditing.value = false
}

function handleAttachmentSelect(attachments: AttachmentLike[]) {
  attachmentSelectorVisible.value = false
  if (!attachments.length) return

  const attachment = attachments[0]
  const simple = utils.attachment.convertToSimple(attachment)
  if (simple) {
    props.updateAttributes({
      src: simple.url,
      fileName: simple.alt || displayName.value,
    })
  }
  isEditing.value = false
}

function handleStartEdit() {
  isEditing.value = true
}

function handleRemove() {
  props.deleteNode()
}

const iframeHeight = ref(600)
</script>

<template>
  <NodeViewWrapper as="div" class="pdf-node-view pdf-node-view--block">
    <AttachmentSelectorModal
      v-if="attachmentSelectorVisible"
      :accepts="['application/pdf']"
      :min="1"
      :max="1"
      @select="handleAttachmentSelect"
      @close="attachmentSelectorVisible = false"
    />

    <template v-if="!isConfigured || isEditing">
      <div class="pdf-node-view__config">
        <div class="pdf-node-view__dashed-area">
          <div class="pdf-node-view__dashed-inner">
            <div class="pdf-node-view__icon-circle">
              <Icon icon="ri:file-pdf-line" class="pdf-node-view__icon-svg" />
            </div>
            <div class="pdf-node-view__button-group">
              <button
                v-if="utils.permission.has(['uc:attachments:manage'])"
                class="pdf-node-view__btn pdf-node-view__btn--primary"
                @click="attachmentSelectorVisible = true"
              >
                从附件库选择
              </button>
              <div class="pdf-node-view__external-wrapper">
                <button
                  class="pdf-node-view__btn pdf-node-view__btn--default"
                  @click="showExternalInput = !showExternalInput"
                >
                  外链地址
                </button>
                <div v-if="showExternalInput" class="pdf-node-view__external-dropdown">
                  <input
                    v-model="externalUrl"
                    type="text"
                    class="pdf-node-view__external-input"
                    placeholder="https://example.com/document.pdf"
                    @keyup.enter="handleUrlConfirm"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <div
        class="pdf-node-view__preview"
        :class="{ 'pdf-node-view__preview--selected': selected }"
      >
        <div
          class="pdf-node-view__hover-overlay"
          @click.stop
        >
          <button class="pdf-node-view__overlay-btn" title="更换PDF" @click="handleStartEdit">
            <Icon icon="ri:edit-line" />
          </button>
          <button class="pdf-node-view__overlay-btn pdf-node-view__overlay-btn--danger" title="删除" @click="handleRemove">
            <Icon icon="ri:delete-bin-line" />
          </button>
        </div>

        <div class="pdf-node-view__toolbar">
          <span class="pdf-node-view__label">{{ displayName }}</span>
          <div class="pdf-node-view__actions">
            <button class="pdf-node-view__action-btn" title="更换PDF" @click="handleStartEdit">
              <Icon icon="ri:edit-line" />
            </button>
            <button class="pdf-node-view__action-btn pdf-node-view__action-btn--danger" title="删除" @click="handleRemove">
              <Icon icon="ri:delete-bin-line" />
            </button>
          </div>
        </div>
        <div class="pdf-node-view__iframe-wrapper" :style="{ height: iframeHeight + 'px' }">
          <iframe
            :src="viewerUrl"
            class="pdf-node-view__iframe"
            frameborder="0"
            allowfullscreen
          />
        </div>
      </div>
    </template>
  </NodeViewWrapper>
</template>

<style scoped>
.pdf-node-view--block {
  margin: 16px 0;
}

.pdf-node-view__config {
  width: 100%;
}

.pdf-node-view__dashed-area {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 200px;
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  background: #f9fafb;
  cursor: default;
}

.pdf-node-view__dashed-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 28px;
  padding: 24px 20px;
}

.pdf-node-view__icon-circle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.15);
}

.pdf-node-view__icon-svg {
  width: 28px;
  height: 28px;
  color: #ef4444;
}

.pdf-node-view__button-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pdf-node-view__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.pdf-node-view__btn--primary {
  background: #3b82f6;
  color: #fff;
}

.pdf-node-view__btn--primary:hover {
  background: #2563eb;
}

.pdf-node-view__btn--default {
  background: #fff;
  color: #374151;
  border: 1px solid #d1d5db;
}

.pdf-node-view__btn--default:hover {
  background: #f3f4f6;
}

.pdf-node-view__external-wrapper {
  position: relative;
}

.pdf-node-view__external-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  z-index: 10;
  width: 320px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.pdf-node-view__external-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.pdf-node-view__external-input:focus {
  border-color: #3b82f6;
}

.pdf-node-view__preview {
  position: relative;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: #fff;
}

.pdf-node-view__preview--selected {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.pdf-node-view__hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 5;
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 6px 6px 0 0;
  background: linear-gradient(to bottom, rgba(209, 213, 219, 0.85), transparent);
  opacity: 0;
  transition: opacity 0.2s;
}

.pdf-node-view__preview:hover .pdf-node-view__hover-overlay {
  opacity: 1;
}

.pdf-node-view__overlay-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  color: #6b7280;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.pdf-node-view__overlay-btn svg {
  width: 14px;
  height: 14px;
}

.pdf-node-view__overlay-btn:hover {
  background: #fff;
  color: #374151;
}

.pdf-node-view__overlay-btn--danger:hover {
  background: #fee2e2;
  color: #ef4444;
}

.pdf-node-view__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: #f3f4f6;
  border-bottom: 1px solid #e5e7eb;
}

.pdf-node-view__label {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 70%;
}

.pdf-node-view__actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.pdf-node-view__action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: none;
  background: transparent;
  color: #6b7280;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.pdf-node-view__action-btn svg {
  width: 16px;
  height: 16px;
}

.pdf-node-view__action-btn:hover {
  background: #e5e7eb;
  color: #374151;
}

.pdf-node-view__action-btn--danger:hover {
  background: #fee2e2;
  color: #ef4444;
}

.pdf-node-view__iframe-wrapper {
  width: 100%;
  min-height: 400px;
  background: #525659;
}

.pdf-node-view__iframe {
  width: 100%;
  height: 100%;
  border: none;
}
</style>
