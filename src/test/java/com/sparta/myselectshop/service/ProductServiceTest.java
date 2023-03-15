package com.sparta.myselectshop.service;


import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;


import static com.sparta.myselectshop.service.ProductService.MIN_MY_PRICE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock //  (1)
    ProductRepository productRepository;

    @InjectMocks //  (2)
    ProductService productService;

    @Mock
    User user;


    @Test
    @DisplayName("���� ��ǰ ����� - ������ �̻����� ����")
    void updateProduct_Success() {
        // given
        Long productId = 100L;
        int myprice = MIN_MY_PRICE + 100;
        Long userId = 777L;

        ProductMypriceRequestDto requestMyPriceDto = new ProductMypriceRequestDto(
                myprice
        );


        ProductRequestDto requestProductDto = new ProductRequestDto(
                "������ ����Ĩ �����򷯽��� 160g",
                "https://shopping-phinf.pstatic.net/main_2416122/24161228524.20200915151118.jpg",
                "https://search.shopping.naver.com/gate.nhn?id=24161228524",
                2350
        );

        Product product = new Product(requestProductDto, userId);

        //  (3)
        when(user.getId())
                .thenReturn(userId);
        when(productRepository.findByIdAndUserId(productId, userId))
                .thenReturn(Optional.of(product));


        // when, then
        assertDoesNotThrow( () -> {
            productService.updateProduct(productId, requestMyPriceDto, user);
        });
    }

    @Test
    @DisplayName("���� ��ǰ ����� - ������ �̸����� ����")
    void updateProduct_Failed() {
        // given
        Long productId = 100L;
        int myprice = MIN_MY_PRICE - 50;

        ProductMypriceRequestDto requestMyPriceDto = new ProductMypriceRequestDto(
                myprice
        );

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Long result = productService.updateProduct(productId, requestMyPriceDto, user);
        });

        // then
        assertEquals(
                "��ȿ���� ���� ���� �����Դϴ�. �ּ� " + MIN_MY_PRICE + " �� �̻����� ������ �ּ���.",
                exception.getMessage()
        );
    }
}