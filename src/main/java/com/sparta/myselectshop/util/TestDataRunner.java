package com.sparta.myselectshop.util;

import com.sparta.myselectshop.controller.ProductController;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.naver.service.NaverApiService;
import com.sparta.myselectshop.repository.ProductRepository;
import com.sparta.myselectshop.repository.UserRepository;
import com.sparta.myselectshop.security.UserDetailsImpl;
import com.sparta.myselectshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TestDataRunner implements ApplicationRunner {

    private final ProductController productController;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NaverApiService naverApiService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // �׽�Ʈ User ����
        User testUser1 = new User("User1", passwordEncoder.encode("123"), "user1@sparta.com", UserRoleEnum.USER);
        User testUser2 = new User("User2", passwordEncoder.encode("123"), "user2@sparta.com", UserRoleEnum.USER);
        User testAdminUser1 = new User("Admin", passwordEncoder.encode("123"), "admin@sparta.com", UserRoleEnum.ADMIN);
        testUser1 = userRepository.save(testUser1);
        testUser2 = userRepository.save(testUser2);
        testAdminUser1 = userRepository.save(testAdminUser1);

        // �׽�Ʈ User �� ���ɻ�ǰ ���
        // �˻��� �� ���ɻ�ǰ 10�� ���
        createTestData(testUser1, "�Ź�");
        createTestData(testUser1, "����");
        createTestData(testUser1, "Ű����");
        createTestData(testUser1, "����");
        createTestData(testUser1, "�޴���");
        createTestData(testUser1, "�ٹ�");
        createTestData(testUser2, "�����");
        createTestData(testUser2, "�̾���");
        createTestData(testUser2, "��Ʈ��");
        createTestData(testUser2, "���� �̾���");
        createTestData(testUser2, "�����");
    }

    private void createTestData(User user, String searchWord) throws IOException {
        // ���̹� ���� API ���� ��ǰ �˻�
        List<ProductRequestDto> productRequestDtoList = naverApiService.searchItems(searchWord).stream().map(ProductRequestDto::new).toList();

        UserDetailsImpl userDetails = new UserDetailsImpl(user, user.getUsername());

        for (ProductRequestDto dto : productRequestDtoList) {
            productController.createProduct(dto, userDetails);
        }
    }
}