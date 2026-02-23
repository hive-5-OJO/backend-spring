package org.backend.analysis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class LtvApiTest {
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    @WithMockUser
//    @DisplayName("특정 고객 LTV 조회 test")
//    void getLtvTest() throws Exception{
//        Long testMemberId = 1L;
//
//        mockMvc.perform(get("/api/analysis/ltv/" + testMemberId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.memberId").exists())
//                .andExpect(jsonPath("$.ltv").isNumber())
//                .andExpect(jsonPath("$.lifecycleStage").isString());
//    }
//
//    @Test
//    @WithMockUser
//    @DisplayName("고객 통합 분석 요약 test")
//    void getAnalysisSummaryTest() throws Exception{
//        Long testMemberId = 1L;
//
//        mockMvc.perform(get("/api/analysis/summary/" + testMemberId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.rfmType").isString())
//                .andExpect(jsonPath("$.ltv").exists());
//    }

    @Test
    void helloTest() {
        System.out.println("Hello OJO Project!");
        Assertions.assertEquals(2, 1 + 1);
    }
}
