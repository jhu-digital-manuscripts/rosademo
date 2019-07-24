package rosa.iiif.presentation.core.html;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Before;
import org.junit.Test;

import rosa.archive.core.serialize.AORAnnotatedPageSerializer;
import rosa.archive.model.Book;
import rosa.archive.model.BookCollection;
import rosa.archive.model.BookImage;
import rosa.archive.model.BookReferenceSheet;
import rosa.archive.model.BookReferenceSheet.Link;
import rosa.archive.model.aor.AnnotatedPage;
import rosa.archive.model.aor.AorLocation;
import rosa.archive.model.aor.InternalReference;
import rosa.archive.model.aor.Marginalia;
import rosa.archive.model.aor.MarginaliaLanguage;
import rosa.archive.model.aor.Position;
import rosa.archive.model.aor.ReferenceTarget;
import rosa.iiif.presentation.core.IIIFPresentationRequestFormatter;
import rosa.iiif.presentation.core.PresentationUris;
import rosa.iiif.presentation.core.StaticResourceRequestFormatter;
import rosa.iiif.presentation.core.extras.BookReferenceResourceDb;

public class MarginaliaHtmlAdapterTest {
    private MarginaliaHtmlAdapter adapter;
    private BookCollection fakeCollection;

    @Before
    public void setup() {
        IIIFPresentationRequestFormatter requestFormatter = new IIIFPresentationRequestFormatter(
                "https",
                "example.com",
                "",
                -1
        );
        StaticResourceRequestFormatter staticFormatter = new StaticResourceRequestFormatter(
                "https",
                "example.com",
                "",
                -1
        );

        adapter = new MarginaliaHtmlAdapter(new PresentationUris(requestFormatter, null, staticFormatter));

        fakeCollection = new BookCollection();

        Map<String, AorLocation> map = new HashMap<>();
        fakeCollection.setAnnotationMap(map);
        map.put("id", new AorLocation("col", "book", "page", null));
        /*
        t1.add(new ReferenceTarget("PrincetonPA6452:00000023", "[a1r]"));
        t1.add(new ReferenceTarget("PrincetonPA6452:00000028", "[6]"));
        t1.add(new ReferenceTarget("PrincetonPA6452:00000031", "[9]"));
        t1.add(new ReferenceTarget("PrincetonPA6452:00000051", "[20]"));
        t1.add(new ReferenceTarget("PrincetonPA6452:00000055", "[33]"));
        t1.add(new ReferenceTarget("PrincetonPA6452:00000057", "[35]"));
         */
        map.put("PrincetonPA6452:00000027", new AorLocation("aor", "PrincetonPA6452", "22r", null));
        map.put("PrincetonPA6452:00000023", new AorLocation("aor", "PrincetonPA6452", "20r", null));
        map.put("PrincetonPA6452:00000028", new AorLocation("aor", "PrincetonPA6452", "11v", null));
        map.put("PrincetonPA6452:00000031", new AorLocation("aor", "PrincetonPA6452", "13r", null));
        map.put("PrincetonPA6452:00000051", new AorLocation("aor", "PrincetonPA6452", "23r", null));
        map.put("PrincetonPA6452:00000055", new AorLocation("aor", "PrincetonPA6452", "25r", null));
        map.put("PrincetonPA6452:00000057", new AorLocation("aor", "PrincetonPA6452", "26r", null));
        map.put("BLC120b4:c_120_b_4_(2)_f054v", new AorLocation("aor", "BLC120b4", "32v", null));
    }

    @Test
    public void internalRefListTest() throws Exception {
        List<ReferenceTarget> t1 = new ArrayList<>();
        t1.add(new ReferenceTarget("PrincetonPA6452:00000023", "[a1r]"));
        t1.add(new ReferenceTarget("PrincetonPA6452:00000028", "[6]"));
        t1.add(new ReferenceTarget("PrincetonPA6452:00000031", "[9]"));
        t1.add(new ReferenceTarget("PrincetonPA6452:00000051", "[20]"));
        t1.add(new ReferenceTarget("PrincetonPA6452:00000055", "[33]"));
        t1.add(new ReferenceTarget("PrincetonPA6452:00000057", "[35]"));
        InternalReference r1 = new InternalReference("s[upr]a", t1);

        List<InternalReference> refs = new ArrayList<>(Collections.singletonList(r1));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);

        Marginalia marg = new Marginalia();
        marg.setLanguages(Collections.singletonList(new MarginaliaLanguage()));
        marg.getLanguages().get(0).setLang("en");
        marg.getLanguages().get(0).setPositions(Collections.singletonList(new Position()));
        marg.getLanguages().get(0).getPositions().get(0).setInternalRefs(refs);

        adapter.addInternalRefs(fakeCollection, marg, refs, writer);

        String result = out.toString();
//        System.out.println(result);
        assertNotNull(result);
        assertTrue(result.contains("<span class=\"emphasize\">Internal References:</span>"));
        assertTrue("There must be ', ' between text and targets", result.contains("s[upr]a, <a"));
        assertFalse("There should be ', ' between targets", result.contains("</a><a"));
        assertTrue("There should be ', ' between targets", result.contains("</a>, <a "));
    }

    @Test
    public void fromVoarchadumiaTest() {
        final String transcription = "vide synonia pag[ina] 54:";
        final String expected = "vide synonia <a class=\"internal-ref\" href=\"javascript:;\" " +
                "data-targetid=\"https://example.com/aor/BLC120b4/32v/canvas\" " +
                "data-manifestid=\"https://example.com/aor/BLC120b4/manifest\">pag[ina] 54</a>:";

        List<ReferenceTarget> targets = new ArrayList<>();
        targets.add(new ReferenceTarget("BLC120b4:c_120_b_4_(2)_f054v", "pag[ina] 54"));

        InternalReference ref = new InternalReference(null, targets);

        String result = adapter.addInternalRefs(fakeCollection, transcription, Collections.singletonList(ref));

        assertEquals(expected, result);
    }

    @Test
    public void referenceAtEndTest() {
        final String expected = "This is a <a class=\"internal-ref\" href=\"javascript:;\" " +
                "data-targetid=\"https://example.com/col/book/page/canvas\" " +
                "data-manifestid=\"https://example.com/col/book/manifest\">test</a> moo";
        final String trans = "This is a test moo";

        String result = adapter.addInternalRefs(fakeCollection, trans, Collections.singletonList(newRef()));

        assertEquals(expected, result);
    }

    @Test
    public void referenceAtStartTest() {
        final String transcription = "is a test moo sound";
        final String expected = "is a <a class=\"internal-ref\" href=\"javascript:;\" " +
                "data-targetid=\"https://example.com/col/book/page/canvas\" " +
                "data-manifestid=\"https://example.com/col/book/manifest\">test</a> moo sound";

        String result = adapter.addInternalRefs(fakeCollection, transcription, Collections.singletonList(newRef()));

        assertEquals(expected, result);
    }

    @Test
    public void referenceInMiddleTest() {
        final String transcription = "This is a test moo sound";
        final String expected = "This is a <a class=\"internal-ref\" href=\"javascript:;\" " +
                "data-targetid=\"https://example.com/col/book/page/canvas\" " +
                "data-manifestid=\"https://example.com/col/book/manifest\">test</a> moo sound";

        String result = adapter.addInternalRefs(fakeCollection, transcription, Collections.singletonList(newRef()));

        assertEquals(expected, result);
    }

    @Test
    public void referenceIsTextTest() {
        final String transcription = "is a test moo";
        final String expected = "is a <a class=\"internal-ref\" href=\"javascript:;\" " +
                "data-targetid=\"https://example.com/col/book/page/canvas\" " +
                "data-manifestid=\"https://example.com/col/book/manifest\">test</a> moo";

        String result = adapter.addInternalRefs(fakeCollection, transcription, Collections.singletonList(newRef()));

        assertEquals(expected, result);
    }

    /**
     * Tests the case where the source text is actually in the internal_ref#text, instead of
     * internal_ref/target#text. This is done on a subset of internal references. In many cases,
     * the target also has text which we will ignore for now.
     *
     *
     */
    @Test
    public void sourceTextInReferenceTest() {
        final String transcription = "This is a test moo";
        final String expected = "This is a test <a class=\"internal-ref\" href=\"javascript:;\" " +
                "data-targetid=\"https://example.com/col/book/page/canvas\" " +
                "data-label=\"test\" " +
                "data-manifestid=\"https://example.com/col/book/manifest\">moo</a>";

        String result = adapter.addInternalRefs(fakeCollection, transcription, Collections.singletonList(weirdRef()));
        assertEquals(expected, result);
    }

    private InternalReference newRef() {
        ReferenceTarget tar = new ReferenceTarget("id", "text", "pre", "post");
        InternalReference r = new InternalReference(null, new ArrayList<>(Collections.singletonList(tar)));

        tar.setText("test");
        tar.setTextPrefix("is a ");
        tar.setTextSuffix(" moo");

        return r;
    }

    private InternalReference weirdRef() {
        InternalReference r = newRef();
        r.setText("moo");

        return r;
    }
  
    @Test
    public void moo() throws Exception {
        String data = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<!DOCTYPE transcription SYSTEM \"http://www.livesandletters.ac.uk/schema/aor_20141023.dtd\">\n" +
                "<transcription xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://www.livesandletters.ac.uk/schema/aor2_18112016.xsd\">\n" +
                "    <page filename=\"BLC120b4.016r.tif\" pagination=\"16\" reader=\"John Dee\"/>\n" +
                "    <annotation>\n" +
                "        <marginalia hand=\"Italian\" method=\"pen\">\n" +
                "            <language ident=\"EN\">\n" +
                "                <position place=\"tail\" book_orientation=\"0\">\n" +
                "                    <marginalia_text>&Sun; - 1/2</marginalia_text>\n" +        // Entity here :: &Sun;  -->   ☉
                "                    <symbol_in_text name=\"Sun\"/>\n" +                        //   - Defined only in DTD
                "                </position>\n" +
                "            </language>\n" +
                "        </marginalia>\n" +
                "        <marginalia hand=\"Italian\" anchor_text=\"Adum&aacute;\" method=\"pen\">\n" +         // á
                "            <language ident=\"LA\">\n" +
                "                <position place=\"right_margin\" book_orientation=\"0\">\n" +
                "                    <marginalia_text>\n" +
                "                        Nota quod dicat Adum&aacute;, tantu[m], no[n] adiecta particula illa Voarch[adumia]:\n" +
                "                    </marginalia_text>\n" +
                "                    <person name=\"Adum&aacute;\"/>\n" +
                "                    <book title=\"Voarchadumia\"/>\n" +
                "                    <book title=\"Tragoedia\"/>\n" +
                "                </position>\n" +
                "            </language>\n" +
                "            <translation>Note what Aduma says, only that particular was not aimed at Voarchadumia:</translation>\n" +
                "        </marginalia>\n" +
                "    </annotation>\n" +
                "</transcription>";

        BookCollection col = new BookCollection();
        col.setId("Moo");
        BookReferenceSheet sheet = new BookReferenceSheet();
        sheet.setLines(Collections.singletonList("Tragoedia,,,,,,Lucian,,,,,http://www.perseus.tufts.edu/hopper/text?doc=Perseus:text:2008.01.0437,,,,,,,,,,,,"));
        col.setBooksRef(sheet);

        Book book = new Book();
        book.setId("The Greatest Moo");

        BookImage img = new BookImage("FirstMoo", 3, 3, false);

        AORAnnotatedPageSerializer serializer = new AORAnnotatedPageSerializer();
        List<String> errors = new ArrayList<>();
        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes("UTF-8"));

        AnnotatedPage loadedPage = serializer.read(in, errors);

        Marginalia m = loadedPage.getMarginalia().get(1);
        String result = adapter.adapt(col, book, img, m, new BookReferenceResourceDb(Link.PERSEUS, col.getBooksRef()));

        assertTrue(result.contains("data-Perseus=\"http://www.perseus.tufts.edu/hopper/text?doc=Perseus:text:2008.01.0437\""));
    }
}
