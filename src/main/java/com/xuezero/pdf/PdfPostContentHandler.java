package com.xuezero.pdf;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.theme.ReactivePostContentHandler;

/**
 * PDF 查看器后内容处理器，用于在页面内容中注入 PDF 查看器的 HTML 元素、CSS 样式和初始化脚本。
 * 适配 pjax / swup 无刷新页面切换方案。
 *
 * @author AR-26710
 * @since 1.0.0
 */
@Component
public class PdfPostContentHandler implements ReactivePostContentHandler {

    private static final String VIEWER_BASE_PATH = "/plugins/plugin-pdf/assets/pdfjs/viewer.html";

    private static final java.util.Set<String> ALLOWED_SCHEMES =
        java.util.Set.of("http", "https");

    boolean isValidPdfSrc(String src) {
        String trimmed = src.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            return false;
        }
        if (trimmed.startsWith("/")) {
            return true;
        }
        int colonIndex = trimmed.indexOf(':');
        if (colonIndex > 0) {
            String scheme = trimmed.substring(0, colonIndex);
            return ALLOWED_SCHEMES.contains(scheme);
        }
        return false;
    }

    private static final String PDF_VIEWER_CSS =
        ".hp2df-pdf-wrapper{"
        + "border:1px solid var(--hp2df--pdf-border-color,#333);"
        + "border-radius:var(--hp2df--pdf-radius,8px);overflow:hidden;"
        + "margin:var(--hp2df--pdf-margin,16px 0);"
        + "background:var(--hp2df--pdf-bg,#1e1e1e)}"
        + ".hp2df-pdf-wrapper--empty{background:var(--hp2df--pdf-empty-bg,#2a2a2a)}"
        + ".hp2df-pdf-iframe{width:100%;height:var(--hp2df--pdf-iframe-height,600px);"
        + "border:none;display:block;background:var(--hp2df--pdf-iframe-bg,#525659)}"
        + ".hp2df-pdf-placeholder{padding:var(--hp2df--pdf-placeholder-padding,40px 20px);"
        + "text-align:center;font-size:var(--hp2df--pdf-placeholder-size,14px);"
        + "color:var(--hp2df--pdf-placeholder-color,#888)}"
        + "@media(prefers-color-scheme:light){"
        + ".hp2df-pdf-wrapper{border-color:var(--hp2df--pdf-border-color,#d1d5db);"
        + "background:var(--hp2df--pdf-bg,#f9fafb)}"
        + ".hp2df-pdf-wrapper--empty{background:var(--hp2df--pdf-empty-bg,#f3f4f6)}"
        + ".hp2df-pdf-iframe{background:var(--hp2df--pdf-iframe-bg,#e5e7eb)}"
        + ".hp2df-pdf-placeholder{color:var(--hp2df--pdf-placeholder-color,#9ca3af)}}"
        + "@media(max-width:768px){"
        + ".hp2df-pdf-iframe{height:var(--hp2df--pdf-iframe-height-mobile,400px)}}";

    private static final String GLOBAL_INIT_SCRIPT =
        "(function(){"
        + "if(window.__hp2df_ready__)return;"
        + "window.__hp2df_ready__=!0;"
        + "function t(){"
        + "var e=document.documentElement;"
        + "if(e.getAttribute('data-theme')==='dark'||e.getAttribute('data-theme')==='dark-mode')return'dark';"
        + "if(e.getAttribute('data-theme')==='light'||e.getAttribute('data-theme')==='light-mode')return'light';"
        + "if(e.classList.contains('dark')||e.classList.contains('dark-mode'))return'dark';"
        + "if(e.classList.contains('light')||e.classList.contains('light-mode'))return'light';"
        + "if(window.matchMedia&&window.matchMedia('(prefers-color-scheme:dark)').matches)return'dark';"
        + "var n=getComputedStyle(e).backgroundColor;"
        + "if(n&&n!=='transparent'&&n!=='rgba(0,0,0,0)'){"
        + "var o=n.match(/\\d+/g);"
        + "if(o&&o.length>=3&&o[0]*.299+o[1]*.587+o[2]*.114<128)return'dark'"
        + "}return'light'"
        + "}"
        + "function i(e){"
        + "if(e.hasAttribute('data-hp2df-init'))return;"
        + "e.setAttribute('data-hp2df-init','');"
        + "var n=e.querySelector('.hp2df-pdf-iframe');"
        + "if(!n)return;"
        + "function o(){try{n.contentWindow.postMessage({type:'pdf-theme',theme:t()},'*')}catch(e){}}"
        + "n.addEventListener('load',o);"
        + "if(n.contentDocument&&n.contentWindow)o()"
        + "}"
        + "function n(){"
        + "var e=document.querySelectorAll('.hp2df-pdf-wrapper');"
        + "for(var n=0;n<e.length;n++)i(e[n])"
        + "}"
        + "n();"
        + "var o=null;"
        + "new MutationObserver(function(){"
        + "o&&clearTimeout(o);"
        + "o=setTimeout(n,120)"
        + "}).observe(document.body,{childList:!0,subtree:!0});"
        + "window.matchMedia&&window.matchMedia('(prefers-color-scheme:dark)').addEventListener('change',function(){n()});"
        + "document.addEventListener('pjax:complete',n);"
        + "document.addEventListener('pjax:success',n);"
        + "document.addEventListener('pjax:end',n);"
        + "document.addEventListener('swup:contentReplaced',n);"
        + "document.addEventListener('swup:willReplaceContent',n);"
        + "document.addEventListener('barba:transitionCompleted',n);"
        + "document.addEventListener('barba:after',n);"
        + "document.addEventListener('turbo:load',n);"
        + "document.addEventListener('turbolinks:load',n);"
        + "document.addEventListener('fui:ready',n);"
        + "window.__hp2df_initAll__=n"
        + "})();";

    @Override
    public Mono<PostContentContext> handle(PostContentContext postContent) {
        String content = postContent.getContent();
        if (content == null || !content.contains("data-type=\"pdf\"")) {
            return Mono.just(postContent);
        }

        Document doc = Jsoup.parseBodyFragment(content);
        Elements pdfDivs = doc.select("div[data-type=pdf]");

        if (pdfDivs.isEmpty()) {
            return Mono.just(postContent);
        }

        for (Element div : pdfDivs) {
            String src = div.attr("data-src");

            Element wrapper = new Element("div").addClass("hp2df-pdf-wrapper");

            if (src.isEmpty()) {
                wrapper.addClass("hp2df-pdf-wrapper--empty");
                Element placeholder = new Element("div")
                    .addClass("hp2df-pdf-placeholder")
                    .text("📄 PDF 文件未配置");
                wrapper.appendChild(placeholder);
            } else if (!isValidPdfSrc(src)) {
                wrapper.addClass("hp2df-pdf-wrapper--empty");
                Element placeholder = new Element("div")
                    .addClass("hp2df-pdf-placeholder")
                    .text("⚠️ PDF 地址不合法");
                wrapper.appendChild(placeholder);
            } else {
                String encodedSrc = URLEncoder.encode(src, StandardCharsets.UTF_8);
                String viewerUrl = VIEWER_BASE_PATH + "?file=" + encodedSrc;
                Element iframe = new Element("iframe")
                    .addClass("hp2df-pdf-iframe")
                    .attr("src", viewerUrl)
                    .attr("frameborder", "0")
                    .attr("allowfullscreen", "");
                wrapper.appendChild(iframe);
            }

            div.replaceWith(wrapper);
        }

        Element inlineStyle = new Element("style")
            .attr("type", "text/css")
            .attr("data-pdf-viewer-inline", "")
            .text(PDF_VIEWER_CSS);

        Element initScript = new Element("script")
            .attr("type", "text/javascript")
            .attr("data-pdf-viewer-init", "")
            .text(GLOBAL_INIT_SCRIPT);

        doc.body().prependChild(inlineStyle);

        doc.body().appendChild(initScript);

        postContent.setContent(doc.body().html());
        return Mono.just(postContent);
    }


}
