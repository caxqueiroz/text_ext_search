package one.cax.doc_search;

import io.github.jbellis.jvector.vector.VectorUtil;
import io.github.jbellis.jvector.vector.VectorizationProvider;
import io.github.jbellis.jvector.vector.types.VectorFloat;
import io.github.jbellis.jvector.vector.types.VectorTypeSupport;
import one.cax.doc_search.exception.EmbedderException;
import one.cax.doc_search.exception.VectorSearchException;
import one.cax.doc_search.model.Session;
import one.cax.doc_search.model.XDoc;
import one.cax.doc_search.model.XPage;
import one.cax.doc_search.service.OpenAIEmbedderService;
import one.cax.doc_search.service.SessionService;
import one.cax.doc_search.service.VectorSearch;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestPropertySource("classpath:application-test.properties")
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class VectorSearchTest {

    @Value("${doc_ext_search.similarity.function}")
    private String similarityFunctionName;
    @Mock
    private OpenAIEmbedderService embedderService;
    @Mock
    private SessionService sessionService;
    @InjectMocks
    private VectorSearch vectorSearch;
    private UUID sessionId;
    private XDoc document;
    private List<XDoc> documents;

    public static VectorFloat<?> randomVector(int dim) {
        VectorTypeSupport vts = VectorizationProvider.getInstance().getVectorTypeSupport();
        Random R = ThreadLocalRandom.current();
        VectorFloat<?> vec = vts.createFloatVector(dim);
        for (int i = 0; i < dim; i++) {
            vec.set(i, R.nextFloat());
            if (R.nextBoolean()) {
                vec.set(i, -vec.get(i));
            }
        }
        VectorUtil.l2normalize(vec);
        return vec;
    }

    @Test
    void testLoadProperty() {
        assertNotNull(similarityFunctionName);
        assertEquals("EUCLIDEAN", similarityFunctionName);
    }

    @BeforeEach
    public void setUp() {
        sessionId = UUID.randomUUID();
        document = new XDoc();
        document.setDocTitle("Title 1");


        XPage xPage = new XPage();
        xPage.setPageNumber(1);
        xPage.setText("similarityFunctionName");
        xPage.setVector(new float[]{1.0f, 2.0f, 3.0f});
        List<XPage> listOfPages = new ArrayList<>();
        listOfPages.add(xPage);  // Add the page to the list
        document.setPages(listOfPages);
        documents = new ArrayList<>();
        documents.add(document);

        ReflectionTestUtils.setField(vectorSearch, "similarityFunctionName", "EUCLIDEAN");
    }

    @Test
    void testAddDocument_Success() throws VectorSearchException {
        when(sessionService.sessionExists(sessionId)).thenReturn(true);
        Session session = mock(Session.class);
        when(sessionService.getSession(sessionId)).thenReturn(session);


        vectorSearch.addDocument(sessionId, document);

        verify(sessionService, times(1)).getSession(sessionId);
        assertEquals(1, documents.size());
    }

    @Test
    void testAddDocument_SessionDoesNotExist() {
        when(sessionService.sessionExists(sessionId)).thenReturn(false);

        assertThrows(VectorSearchException.class, () -> vectorSearch.addDocument(sessionId, document));
    }

    @Test
    void testSearch_Success() throws VectorSearchException, IOException, JSONException, EmbedderException {

        when(sessionService.sessionExists(sessionId)).thenReturn(true);
        Session session = mock(Session.class);
        when(sessionService.getSession(sessionId)).thenReturn(session);
        when(session.getDocuments()).thenReturn(documents);


        var v = new float[3];
        when(embedderService.embed("query")).thenReturn(v);
        JSONObject result = vectorSearch.search(sessionId, "query");

        assertNotNull(result);
        assertTrue(result.has("results"));
    }

    @Test
    void testSearch_SessionDoesNotExist() {
        when(sessionService.sessionExists(sessionId)).thenReturn(false);

        assertThrows(VectorSearchException.class, () -> vectorSearch.search(sessionId, "query"));
    }
}