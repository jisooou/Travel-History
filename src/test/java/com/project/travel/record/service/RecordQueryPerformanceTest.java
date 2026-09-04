package com.project.travel.record.service;

import com.project.travel.seeder.RecordDetailSeederTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "kakao.rest-api-key=test-key")
public class RecordQueryPerformanceTest {
    @Autowired
    private RecordService recordService;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private RecordDetailSeederTest recordDetailSeederTest;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        statistics = entityManagerFactory
                .unwrap(SessionFactory.class)
                .getStatistics();
        statistics.clear();
    }

    @Transactional
    @Test
    void Record_상세조회_쿼리_개수_측정() {
        RecordDetailSeederTest.TestData data = recordDetailSeederTest.seed(100);

        entityManager.flush();
        entityManager.clear();

        statistics.clear();
        recordService.getUserRecordDetail(data.userNo(), data.recordNo());

        long queryCnt = statistics.getPrepareStatementCount();
        System.out.println("============= " + queryCnt + "개의 쿼리 확인 =============");
    }
}
