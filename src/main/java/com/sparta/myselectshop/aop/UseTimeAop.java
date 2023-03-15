package com.sparta.myselectshop.aop;

import com.sparta.myselectshop.entity.ApiUseTime;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.ApiUseTimeRepository;
import com.sparta.myselectshop.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class UseTimeAop {

    private final ApiUseTimeRepository apiUseTimeRepository;

    @Around("execution(public * com.sparta.myselectshop.controller..*(..))")
    public synchronized Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        // ���� ���� �ð�
        long startTime = System.currentTimeMillis();

        try {
            // �ٽɱ�� ����
            Object output = joinPoint.proceed();
            return output;
        } finally {
            // ���� ���� �ð�
            long endTime = System.currentTimeMillis();
            // ����ð� = ���� �ð� - ���� �ð�
            long runTime = endTime - startTime;

            // �α��� ȸ���� ���� ���, ����ð� ������� ����
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.getPrincipal().getClass() == UserDetailsImpl.class) {
                // �α��� ȸ�� ����
                UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
                User loginUser = userDetails.getUser();

                // API ���ð� �� DB �� ���
                ApiUseTime apiUseTime = apiUseTimeRepository.findByUser(loginUser)
                        .orElse(null);
                if (apiUseTime == null) {
                    // �α��� ȸ���� ����� ������
                    apiUseTime = new ApiUseTime(loginUser, runTime);
                } else {
                    // �α��� ȸ���� ����� �̹� ������
                    apiUseTime.addUseTime(runTime);
                }

                log.info("[API Use Time] Username: " + loginUser.getUsername() + ", Total Time: " + apiUseTime.getTotalTime() + " ms");
                apiUseTimeRepository.save(apiUseTime);

            }
        }
    }
}