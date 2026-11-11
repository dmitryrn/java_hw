package org.example.java_8;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Java8ApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void postAndGetMessagesWithPaginationAndAuthorFilter() throws Exception {
        String payload = "{\"content\":\"New message via POST\",\"author\":\"Tester\"}";

        mockMvc.perform(post("/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.author", is("Tester")))
                .andExpect(jsonPath("$.content", is("New message via POST")));

        mockMvc.perform(get("/messages").param("page", "0").param("size", "1")
                        .param("author", "Tester"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].author", is("Tester")))
                .andExpect(jsonPath("$.totalElements", equalTo(1)));
    }

    @Test
    void batchCreatesMessagesAndMissingIdReturnsNotFound() throws Exception {
        mockMvc.perform(post("/messages/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"content\":\"One\",\"author\":\"A\"}," +
                                "{\"content\":\"Two\",\"author\":\"B\"}]"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.created", is(2)));

        mockMvc.perform(get("/messages/99999"))
                .andExpect(status().isNotFound());
    }
}
