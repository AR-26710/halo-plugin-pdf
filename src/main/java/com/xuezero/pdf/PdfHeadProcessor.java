package com.xuezero.pdf;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.dialect.TemplateHeadProcessor;
import run.halo.app.theme.router.ModelConst;

/**
 * PDF 查看器头部处理器，仅在文章和自定义页面中注入 PDF 查看器所需的 CSS 样式。
 * 
 * @author AR-26710
 * @since 1.0.0
 */
@Component
public class PdfHeadProcessor implements TemplateHeadProcessor {

    private static final String PDF_VIEWER_CSS =
        "/* === Halo PDF Viewer Theme Styles === */\n"
        + ".hp2df-pdf-wrapper {\n"
        + "  border: 1px solid var(--hp2df--pdf-border-color, #333);\n"
        + "  border-radius: var(--hp2df--pdf-radius, 8px);\n"
        + "  overflow: hidden;\n"
        + "  margin: var(--hp2df--pdf-margin, 16px 0);\n"
        + "  background: var(--hp2df--pdf-bg, #1e1e1e);\n"
        + "}\n"
        + ".hp2df-pdf-wrapper--empty {\n"
        + "  background: var(--hp2df--pdf-empty-bg, #2a2a2a);\n"
        + "}\n"
        + ".hp2df-pdf-iframe {\n"
        + "  width: 100%;\n"
        + "  height: var(--hp2df--pdf-iframe-height, 600px);\n"
        + "  border: none;\n"
        + "  display: block;\n"
        + "  background: var(--hp2df--pdf-iframe-bg, #525659);\n"
        + "}\n"
        + ".hp2df-pdf-placeholder {\n"
        + "  padding: var(--hp2df--pdf-placeholder-padding, 40px 20px);\n"
        + "  text-align: center;\n"
        + "  font-size: var(--hp2df--pdf-placeholder-size, 14px);\n"
        + "  color: var(--hp2df--pdf-placeholder-color, #888);\n"
        + "}\n"
        + "@media (prefers-color-scheme: light) {\n"
        + "  .hp2df-pdf-wrapper {\n"
        + "    border-color: var(--hp2df--pdf-border-color, #d1d5db);\n"
        + "    background: var(--hp2df--pdf-bg, #f9fafb);\n"
        + "  }\n"
        + "  .hp2df-pdf-wrapper--empty {\n"
        + "    background: var(--hp2df--pdf-empty-bg, #f3f4f6);\n"
        + "  }\n"
        + "  .hp2df-pdf-iframe {\n"
        + "    background: var(--hp2df--pdf-iframe-bg, #e5e7eb);\n"
        + "  }\n"
        + "  .hp2df-pdf-placeholder {\n"
        + "    color: var(--hp2df--pdf-placeholder-color, #9ca3af);\n"
        + "  }\n"
        + "}\n"
        + "@media (max-width: 768px) {\n"
        + "  .hp2df-pdf-iframe {\n"
        + "    height: var(--hp2df--pdf-iframe-height-mobile, 400px);\n"
        + "  }\n"
        + "}\n";

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        if (!isContentTemplate(context)) {
            return Mono.empty();
        }
        var factory = context.getModelFactory();
        model.add(factory.createOpenElementTag("style", "data-pdf-viewer", ""));
        model.add(factory.createText("\n" + PDF_VIEWER_CSS + "\n"));
        model.add(factory.createCloseElementTag("style"));
        return Mono.empty();
    }

    private boolean isContentTemplate(ITemplateContext context) {
        String templateId = (String) context.getVariable(ModelConst.TEMPLATE_ID);
        return "post".equals(templateId) || "page".equals(templateId);
    }
}
