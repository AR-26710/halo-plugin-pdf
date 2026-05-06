package com.xuezero.pdf;

import org.junit.jupiter.api.Test;
import run.halo.app.theme.ReactivePostContentHandler.PostContentContext;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class PdfPostContentHandlerTest {

    private final PdfPostContentHandler handler = new PdfPostContentHandler();

    @Test
    void isValidPdfSrc_absoluteHttpsUrl() {
        assertTrue(handler.isValidPdfSrc("https://example.com/doc.pdf"));
    }

    @Test
    void isValidPdfSrc_absoluteHttpUrl() {
        assertTrue(handler.isValidPdfSrc("http://example.com/doc.pdf"));
    }

    @Test
    void isValidPdfSrc_relativePath() {
        assertTrue(handler.isValidPdfSrc("/uploads/2024/doc.pdf"));
    }

    @Test
    void isValidPdfSrc_relativePathSingleSlash() {
        assertTrue(handler.isValidPdfSrc("/doc.pdf"));
    }

    @Test
    void isValidPdfSrc_javascriptScheme() {
        assertFalse(handler.isValidPdfSrc("javascript:alert(1)"));
    }

    @Test
    void isValidPdfSrc_javascriptSchemeMixedCase() {
        assertFalse(handler.isValidPdfSrc("JaVaScRiPt:alert(1)"));
    }

    @Test
    void isValidPdfSrc_dataScheme() {
        assertFalse(handler.isValidPdfSrc("data:text/html,<script>alert(1)</script>"));
    }

    @Test
    void isValidPdfSrc_vbscriptScheme() {
        assertFalse(handler.isValidPdfSrc("vbscript:MsgBox"));
    }

    @Test
    void isValidPdfSrc_fileScheme() {
        assertFalse(handler.isValidPdfSrc("file:///etc/passwd"));
    }

    @Test
    void isValidPdfSrc_ftpScheme() {
        assertFalse(handler.isValidPdfSrc("ftp://example.com/doc.pdf"));
    }

    @Test
    void isValidPdfSrc_emptyString() {
        assertFalse(handler.isValidPdfSrc(""));
    }

    @Test
    void isValidPdfSrc_whitespaceOnly() {
        assertFalse(handler.isValidPdfSrc("   "));
    }

    @Test
    void isValidPdfSrc_bareWordNoScheme() {
        assertFalse(handler.isValidPdfSrc("example.com/doc.pdf"));
    }

    @Test
    void isValidPdfSrc_leadingWhitespaceWithValidUrl() {
        assertTrue(handler.isValidPdfSrc("  https://example.com/doc.pdf  "));
    }

    @Test
    void isValidPdfSrc_upperCaseScheme() {
        assertTrue(handler.isValidPdfSrc("HTTPS://EXAMPLE.COM/DOC.PDF"));
    }

    @Test
    void handle_nullContent_returnsUnmodified() {
        PostContentContext ctx = PostContentContext.builder()
            .content(null)
            .build();
        PostContentContext result = handler.handle(ctx).block();
        assertNotNull(result);
        assertNull(result.getContent());
    }

    @Test
    void handle_noPdfDiv_returnsUnmodified() {
        String html = "<p>Hello World</p>";
        PostContentContext ctx = PostContentContext.builder()
            .content(html)
            .build();
        PostContentContext result = handler.handle(ctx).block();
        assertNotNull(result);
        assertEquals(html, result.getContent());
    }

    @Test
    void handle_validSrc_rendersIframeWithEncodedUrl() {
        String src = "/uploads/2024/my doc.pdf";
        String html = "<div data-type=\"pdf\" data-src=\"" + src + "\"></div>";
        PostContentContext ctx = PostContentContext.builder()
            .content(html)
            .build();

        PostContentContext result = handler.handle(ctx).block();
        assertNotNull(result);
        assertNotNull(result.getContent());
        String expectedEncoded = URLEncoder.encode(src, StandardCharsets.UTF_8);
        assertTrue(result.getContent().contains("file=" + expectedEncoded));
        assertTrue(result.getContent().contains("hp2df-pdf-iframe"));
        assertFalse(result.getContent().contains("PDF 文件未配置"));
        assertFalse(result.getContent().contains("PDF 地址不合法"));
    }

    @Test
    void handle_emptySrc_showsNotConfiguredPlaceholder() {
        String html = "<div data-type=\"pdf\" data-src=\"\"></div>";
        PostContentContext ctx = PostContentContext.builder()
            .content(html)
            .build();

        PostContentContext result = handler.handle(ctx).block();
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getContent().contains("PDF 文件未配置"));
        assertFalse(result.getContent().contains("<iframe"));
    }

    @Test
    void handle_javascriptSrc_showsInvalidPlaceholder() {
        String html = "<div data-type=\"pdf\" data-src=\"javascript:alert(1)\"></div>";
        PostContentContext ctx = PostContentContext.builder()
            .content(html)
            .build();

        PostContentContext result = handler.handle(ctx).block();
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getContent().contains("PDF 地址不合法"));
        assertFalse(result.getContent().contains("<iframe"));
    }

    @Test
    void handle_dataSchemeSrc_showsInvalidPlaceholder() {
        String html = "<div data-type=\"pdf\" data-src=\"data:text/html,<script>alert(1)</script>\"></div>";
        PostContentContext ctx = PostContentContext.builder()
            .content(html)
            .build();

        PostContentContext result = handler.handle(ctx).block();
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getContent().contains("PDF 地址不合法"));
        assertFalse(result.getContent().contains("<iframe"));
        assertFalse(result.getContent().contains("data:text/html"));
    }

    @Test
    void handle_httpsSrc_rendersIframe() {
        String src = "https://example.com/doc.pdf";
        String html = "<div data-type=\"pdf\" data-src=\"" + src + "\"></div>";
        PostContentContext ctx = PostContentContext.builder()
            .content(html)
            .build();

        PostContentContext result = handler.handle(ctx).block();
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getContent().contains("hp2df-pdf-iframe"));
        String expectedEncoded = URLEncoder.encode(src, StandardCharsets.UTF_8);
        assertTrue(result.getContent().contains("file=" + expectedEncoded));
    }

    @Test
    void handle_specialCharactersInSrc_areUrlEncoded() {
        String src = "/files/doc?a=1&b=2#page=3";
        String html = "<div data-type=\"pdf\" data-src=\"" + src + "\"></div>";
        PostContentContext ctx = PostContentContext.builder()
            .content(html)
            .build();

        PostContentContext result = handler.handle(ctx).block();
        assertNotNull(result);
        assertNotNull(result.getContent());
        String expectedEncoded = URLEncoder.encode(src, StandardCharsets.UTF_8);
        assertTrue(result.getContent().contains("file=" + expectedEncoded));
        assertFalse(result.getContent().contains("file=/files/doc?a=1&b=2#page=3"));
    }

    @Test
    void handle_multiplePdfDivs_mixedValidity() {
        String html = "<div data-type=\"pdf\" data-src=\"/valid.pdf\"></div>"
            + "<div data-type=\"pdf\" data-src=\"javascript:alert(1)\"></div>"
            + "<div data-type=\"pdf\" data-src=\"\"></div>";

        PostContentContext ctx = PostContentContext.builder()
            .content(html)
            .build();

        PostContentContext result = handler.handle(ctx).block();
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getContent().contains("hp2df-pdf-iframe"));
        assertTrue(result.getContent().contains("PDF 地址不合法"));
        assertTrue(result.getContent().contains("PDF 文件未配置"));
    }

    @Test
    void handle_injectsStyleAndScript() {
        String html = "<div data-type=\"pdf\" data-src=\"/doc.pdf\"></div>";
        PostContentContext ctx = PostContentContext.builder()
            .content(html)
            .build();

        PostContentContext result = handler.handle(ctx).block();
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertTrue(result.getContent().contains("data-pdf-viewer-inline"));
        assertTrue(result.getContent().contains("data-pdf-viewer-init"));
    }
}
