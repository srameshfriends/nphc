package org.nphc.payroll.embeded;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void employeeUploadTest() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        try(InputStream inputStream = getClass().getResourceAsStream("employee.csv")) {
            MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "employee.csv",
                    "text/csv", inputStream);
            MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/users/upload")
                    .file(mockMultipartFile);
            mockMvc.perform(builder).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
        }
    }

    @Test
    public void employeeListTest() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/users");
        mockMvc.perform(builder).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void employeeInsertTest() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode emp = objectMapper.createObjectNode();
        emp.put("id", "R001");
        emp.put("login", "abcdef");
        emp.put("name", "Ramesh Selvaraj");
        emp.put("salary", "18500");
        emp.put("startDate", "2022-05-20");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/users/");
        builder.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(emp));
        mockMvc.perform(builder).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void employeeDeleteTest() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete("/users/R001");
        mockMvc.perform(builder).andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
