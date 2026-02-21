package org.backend.domain.advice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.backend.domain.advice.document.AdviceDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdviceSearchRepositoryTest {

    @Autowired
    private AdviceSearchRepository adviceSearchRepository;


    @AfterEach
    void tearDown(){
        adviceSearchRepository.deleteAll();
    }

    @Test
    void 상담_내용에_특정_키워드가_포함된_문서를_검색한다()throws InterruptedException{
        //given
        AdviceDocument doc1 = AdviceDocument.builder()
                .id(1L)
                .adviceContent("앱에서 결제가 자꾸 실패해서 정말 불만입니다. 실이랑 패랑 불이랑 만이랑 실패만, 불만해")
                .memberId(100L)
                .build();


        AdviceDocument doc2 = AdviceDocument.builder()
                .id(2L)
                .adviceContent("앱에서 결제가 자꾸 실패해서 정말 불만입니다. 실이랑 패랑 불이랑 만이랑 실패만, 불만해, 결제가 제대로 안 되잖아요")
                .memberId(101L)
                .build();


        adviceSearchRepository.save(doc1);
        adviceSearchRepository.save(doc2);

        Thread.sleep(1500);

        //when

        List<AdviceDocument> results = adviceSearchRepository.findByAdviceContentContaining("불만");

        //then
        assertThat(results).hasSize(2);

    }



}