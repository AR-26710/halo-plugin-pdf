# Plugin PDF

Halo 2.x 插件，支持在编辑器中插入 PDF 文件并提供在线预览功能。

## 功能特性

- **编辑器集成**：在 Halo 富文本编辑器中以 Tiptap 扩展形式嵌入，通过工具栏按钮、斜杠命令或工具箱面板即可插入 PDF 区块
- **多种来源**：支持从 Halo 附件库选择 PDF 文件，或直接使用外部链接
- **在线预览**：基于 PDF.js 渲染，支持缩放、分页导航、下载功能
- **明暗主题**：自动适配主题的浅色/深色模式
- **无刷新兼容**：自动适配 pjax / swup ，页面切换后 PDF 预览仍然正常工作

## 环境要求

- Halo `>= 2.23.0`
- Java `21+`
- Node.js `18+`
- pnpm

## 安装

1. 从 [Releases](https://github.com/AR-26710/halo-plugin-pdf/releases) 页面下载最新版本的 JAR 文件
2. 在后台「插件管理」中安装插件

## 使用方式

1. 在 Halo 后台进入文章或页面编辑器
2. 点击工具栏中的「插入 PDF」按钮，或通过工具箱面板、斜杠命令（输入 `/pdf`）插入
3. 在弹出的 PDF 设置面板中，你可以：
   - 点击「从附件库选择」选取已上传的 PDF 文件
   - 点击「外链地址」输入外部 PDF 链接
4. 配置完成后，PDF 预览区块将显示在编辑器中，发布后访问者即可在文章/页面中在线预览 PDF

## 本地开发

```bash
# 克隆仓库
git clone https://github.com/AR-26710/halo-plugin-pdf.git
cd plugin-pdf

# 启动 Halo 开发环境（启用插件热加载）
./gradlew haloServer

# 前端开发（另一个终端）
cd ui
pnpm install
pnpm dev
```

## 构建

```bash
./gradlew build
```

构建产物位于 `build/libs` 目录下。

## 许可证

[GPL-3.0](./LICENSE) © AR-26710
