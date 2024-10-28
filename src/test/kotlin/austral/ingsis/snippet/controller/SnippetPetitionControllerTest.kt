package austral.ingsis.snippet.controller

import austral.ingsis.snippet.model.Snippet
import austral.ingsis.snippet.service.SnippetService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClient.RequestBodyUriSpec
import kotlin.math.absoluteValue

class SnippetPetitionControllerTest {
    private lateinit var mockMvc: MockMvc

    @Mock
    lateinit var snippetService: SnippetService

    @Mock
    lateinit var permissionClient: RestClient

    @Mock
    lateinit var clientBuilder: RestClient.Builder

    @Mock
    private lateinit var responseSpec: RestClient.ResponseSpec

    @Mock
    private lateinit var requestHeadersUriSpec: RestClient.RequestHeadersUriSpec<*>

    @Mock
    private lateinit var requestBodyUriSpec: RequestBodyUriSpec

    @Mock
    private lateinit var requestBodySpec: RestClient.RequestBodySpec

    @Mock
    private lateinit var requestHeadersSpec: RestClient.RequestHeadersSpec<*>

    private lateinit var controller: SnippetPetitionController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(clientBuilder.baseUrl(anyString())).thenReturn(clientBuilder)
        `when`(clientBuilder.build()).thenReturn(permissionClient)
        controller = SnippetPetitionController(snippetService, clientBuilder)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Test
    fun `should return snippet by id`() {
        // Arrange
        val snippet =
            Snippet(
                1L,
                "First Snippet",
                "First Description",
                "First Code",
                "First Language",
                1L,
            )
        `when`(snippetService.getSnippetById(1)).thenReturn(snippet)

        // Act & Assert
        mockMvc.perform(get("/snippets/1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value("First Snippet"))

        verify(snippetService, times(1)).getSnippetById(1)
    }

    @Test
    fun `should delete snippet by id`() {
        // Arrange
        `when`(snippetService.getSnippetById(1)).thenReturn(Snippet(1, "", "", "", "", 1))
        `when`(permissionClient.delete()).thenReturn(requestHeadersUriSpec)
        `when`(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec)
        `when`(requestHeadersSpec.retrieve()).thenReturn(responseSpec)
        doNothing().`when`(snippetService).deleteSnippet(1L)

        // Act & Assert
        mockMvc.perform(delete("/snippets/1"))
            .andExpect(status().isOk)

        verify(snippetService, times(1)).deleteSnippet(1)
    }

    @Test
    fun `should create snippet successfully`() {
        // Arrange
        val snippet =
            Snippet(
                0L,
                "First Snippet",
                "First Description",
                "First Code",
                "First Language",
                1L,
            )
        val id = (snippet.name + snippet.code + snippet.description).hashCode().toLong().absoluteValue

        // Mock service and client behavior
        doNothing().`when`(snippetService).updateSnippet(
            id,
            snippet.name,
            snippet.description,
            snippet.code,
            snippet.language,
            snippet.ownerId,
        )

        // Mock chain of calls for RestClient
        `when`(permissionClient.put()).thenReturn(requestBodyUriSpec)
        `when`(requestBodyUriSpec.uri("/users/snippets/{id}/{snippetId}", snippet.ownerId, id))
            .thenReturn(requestBodySpec)
        `when`(requestBodySpec.retrieve()).thenReturn(responseSpec)

        // Act & Assert
        mockMvc.perform(
            post("/snippets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "First Snippet",
                        "description": "First Description",
                        "code": "First Code",
                        "language": "First Language",
                        "ownerId": 1
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(content().string(id.toString()))

        // Verify all interactions
        verify(snippetService, times(1)).updateSnippet(
            id,
            snippet.name,
            snippet.description,
            snippet.code,
            snippet.language,
            snippet.ownerId,
        )
        verify(permissionClient, times(1)).put()
        verify(requestBodyUriSpec, times(1)).uri("/users/snippets/{id}/{snippetId}", snippet.ownerId, id)
        verify(requestBodySpec, times(1)).retrieve()
    }

    @Test
    fun `should update snippet`() {
        // Arrange
        val snippet =
            Snippet(
                1L,
                "First Snippet",
                "First Description",
                "First Code",
                "First Language",
                1L,
            )
        doNothing().`when`(snippetService).updateSnippet(
            1,
            snippet.name,
            snippet.description,
            snippet.code,
            snippet.language,
            snippet.ownerId,
        )

        // Act & Assert
        mockMvc.perform(
            put("/snippets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "First Snippet",
                        "description": "First Description",
                        "code": "First Code",
                        "language": "First Language",
                        "ownerId": 1
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)

        verify(snippetService, times(1)).updateSnippet(
            1,
            snippet.name,
            snippet.description,
            snippet.code,
            snippet.language,
            snippet.ownerId,
        )
    }
}