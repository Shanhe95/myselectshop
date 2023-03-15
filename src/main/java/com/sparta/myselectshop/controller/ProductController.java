package com.sparta.myselectshop.controller;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.security.UserDetailsImpl;
import com.sparta.myselectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 관심 상품 등록하기
    //@Secured(UserRoleEnum.Authority.ADMIN)
    @PostMapping("/products")    //밑에 createProduct 주석은 aop 미적용ㅁ
    public ProductResponseDto createProduct(@RequestBody ProductRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 응답 보내기
        return productService.createProduct(requestDto, userDetails.getUser());
    }

//    // 관심 상품 등록하기
//    //@Secured(UserRoleEnum.Authority.ADMIN)
//    @PostMapping("/products")
//    public ProductResponseDto createProduct(@RequestBody ProductRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
//        // 측정 시작 시간
//        long startTime = System.currentTimeMillis();
//        try {
//            return productService.createProduct(requestDto, userDetails.getUser());
//        } finally {
//            // 측정 종료 시간
//            long endTime = System.currentTimeMillis();
//            // 수행시간 = 종료 시간 - 시작 시간
//            long runTime = endTime - startTime;
//
//            // 로그인 회원 정보
//            User loginUser = userDetails.getUser();
//
//            // API 사용시간 및 DB 에 기록
//            ApiUseTime apiUseTime = apiUseTimeRepository.findByUser(loginUser)
//                    .orElse(null);
//            if (apiUseTime == null) {
//                // 로그인 회원의 기록이 없으면
//                apiUseTime = new ApiUseTime(loginUser, runTime);
//            } else {
//                // 로그인 회원의 기록이 이미 있으면
//                apiUseTime.addUseTime(runTime);
//            }
//
//            log.info("[API Use Time] Username: " + loginUser.getUsername() + ", Total Time: " + apiUseTime.getTotalTime() + " ms");
//            apiUseTimeRepository.save(apiUseTime);
//        }
//    }

    // 관심 상품 조회하기
    @GetMapping("/products")
    public Page<Product> getProducts(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("isAsc") boolean isAsc,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        // 응답 보내기
        return productService.getProducts(userDetails.getUser(), page-1, size, sortBy, isAsc);
    }

    // 관심 상품 최저가 등록하기
    @PutMapping("/products/{id}")
    public Long updateProduct(@PathVariable Long id, @RequestBody ProductMypriceRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 응답 보내기 (업데이트된 상품 id)
        return productService.updateProduct(id, requestDto, userDetails.getUser());
    }

    // 상품에 폴더 추가
    @PostMapping("/products/{productId}/folder")
    public Long addFolder(
            @PathVariable Long productId,
            @RequestParam Long folderId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Product product = productService.addFolder(productId, folderId, userDetails.getUser());
        return product.getId();
    }

}





//package com.sparta.myselectshop.controller;
//
//import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
//import com.sparta.myselectshop.dto.ProductRequestDto;
//import com.sparta.myselectshop.dto.ProductResponseDto;
//import com.sparta.myselectshop.entity.Product;
//import com.sparta.myselectshop.service.ProductService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.web.bind.annotation.*;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api")
//@RequiredArgsConstructor
//public class ProductController {
//
//    private final ProductService productService;
//
////    // 관심 상품 등록하기
////    @PostMapping("/products")
////    public ProductResponseDto createProduct(@RequestBody ProductRequestDto requestDto) {
////        // 응답 보내기
////        return productService.createProduct(requestDto);
////    }                                                                          //HttpServletRequest가 추가돼야 함
//
//    // 관심 상품 등록하기
//    @PostMapping("/products")
//    public ProductResponseDto createProduct(@RequestBody ProductRequestDto requestDto, HttpServletRequest request) {
//        // 응답 보내기
//        return productService.createProduct(requestDto, request);
//    }
//
//
//
////    // 관심 상품 조회하기
////    @GetMapping("/products")
////    public List<ProductResponseDto> getProducts() {
////        // 응답 보내기
////        return productService.getProducts();
////    }                                             아래 코드랑 비교해보자
//
//
////    // 관심 상품 조회하기
////    @GetMapping("/products")
////    public List<ProductResponseDto> getProducts(HttpServletRequest request) {
////        // 응답 보내기
////        return productService.getProducts(request);        //http헤더에 있는 토큰을 가져와야 하기 때문에 코드가 이렇게 바뀜 (근데 뭔 소린지 모름)
////    }                                                      //로그인을 확인하기 위해서 request객체만 받아왔었는데,
//
//    // 관심 상품 조회하기
//    @GetMapping("/products")                            //원래 위에 코드는 로그인을 확인하기 위해서 request객체만 받아왔었는데,
//    public Page<Product> getProducts(                     //여기에 추가로 @RequestParam으로 쿼리 방식으로 날아왔으니까
//      @RequestParam("page") int page,               // 얘네 4개를 추가로 받아오고 return해주는걸 볼 수 있다.
//      @RequestParam("size") int size,
//      @RequestParam("sortBy") String sortBy,
//      @RequestParam("isAsc") boolean isAsc,
//      HttpServletRequest request
//    ) {
//        // 응답 보내기
//        return productService.getProducts(request, page-1, size, sortBy, isAsc);          //  page-1 해주는 이유 : index가 0번째부터니까
//    }
//
//
////    // 관심 상품 최저가 등록하기
////    @PutMapping("/products/{id}")
////    public Long updateProduct(@PathVariable Long id, @RequestBody ProductMypriceRequestDto requestDto) {
////        // 응답 보내기 (업데이트된 상품 id)
////        return productService.updateProduct(id, requestDto);
////    }
//
//    // 관심 상품 최저가 등록하기
//    @PutMapping("/products/{id}")
//    public Long updateProduct(@PathVariable Long id, @RequestBody ProductMypriceRequestDto requestDto, HttpServletRequest request){    //토큰을 가져와야 하기 때문에 HttpServletRequest
//        // 응답 보내기 (업데이트된 상품 id)
//        return productService.updateProduct(id, requestDto, request);
//    }
//
//    // 상품(product)에 폴더 추가
//    @PostMapping("/products/{productId}/folder")     //PathValiable 형식으로 {productId} 넘어옴
//    public Long addFolder(
//            @PathVariable Long productId,             //어노테이션 물어보자
//            @RequestParam Long folderId,               //어노테이션 물어보자
//            HttpServletRequest request
//    ) {
//        Product product = productService.addFolder(productId, folderId, request);
//        return product.getId();
//    }
//
//}