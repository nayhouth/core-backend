package com.corebackend.approval.service.impl;

import com.corebackend.approval.entity.ApprovalRequest;
import com.corebackend.approval.repository.ApprovalRequestRepository;
import com.corebackend.approval.service.ApprovalFlowService;
import com.corebackend.approval.service.ApprovalFlowStepService;
import com.corebackend.approval.service.ApprovalRequestStepService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import com.corebackend.approval.service.ApprovalRequestService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ApprovalRequestTransactionTest {

    @Test
    void approvalRequestService_isTransactionalProxy() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {

            ApprovalRequestService service =
                    context.getBean(ApprovalRequestService.class);

            assertTrue(AopUtils.isAopProxy(service));
        }
    }

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {

        @Bean
        ApprovalRequestRepository approvalRequestRepository() {
            return mock(ApprovalRequestRepository.class);
        }

        @Bean
        ApprovalFlowService approvalFlowService() {
            return mock(ApprovalFlowService.class);
        }

        @Bean
        ApprovalFlowStepService approvalFlowStepService() {
            return mock(ApprovalFlowStepService.class);
        }

        @Bean
        ApprovalRequestStepService approvalRequestStepService() {
            return mock(ApprovalRequestStepService.class);
        }

        @Bean
        ApprovalRequestServiceImpl approvalRequestService(
                ApprovalRequestRepository repository,
                ApprovalFlowService flowService,
                ApprovalFlowStepService flowStepService,
                ApprovalRequestStepService requestStepService
        ) {
            return new ApprovalRequestServiceImpl(
                    repository,
                    flowService,
                    flowStepService,
                    requestStepService
            );
        }

        @Bean
        TestTransactionManager transactionManager() {
            return new TestTransactionManager();
        }

    }

    static class TestTransactionManager
            extends AbstractPlatformTransactionManager {

        private int rollbackCount;
        private int commitCount;

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(
                Object transaction,
                TransactionDefinition definition
        ) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            commitCount++;
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            rollbackCount++;
        }

        int getRollbackCount() {
            return rollbackCount;
        }

        int getCommitCount() {
            return commitCount;
        }
    }

    @Test
    void submit_whenExceptionOccurs_rollsBackTransaction() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {

            ApprovalRequestService service =
                    context.getBean(ApprovalRequestService.class);

            TestTransactionManager transactionManager =
                    context.getBean(TestTransactionManager.class);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> service.submit(null)
            );

            assertEquals(1, transactionManager.getRollbackCount());
            assertEquals(0, transactionManager.getCommitCount());
        }
    }

}