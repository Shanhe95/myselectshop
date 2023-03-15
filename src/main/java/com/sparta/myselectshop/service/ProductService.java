package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Folder;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final FolderRepository folderRepository;
    private final ProductRepository productRepository;

    public static final int MIN_MY_PRICE = 100;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {

        // 요청받은 DTO 로 DB에 저장할 객체 만들기
        Product product = productRepository.saveAndFlush(new Product(requestDto, user.getId()));

        return new ProductResponseDto(product);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProducts(User user,
                                     int page, int size, String sortBy, boolean isAsc) {
        // 페이징 처리
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        // 사용자 권한 가져와서 ADMIN 이면 전체 조회, USER 면 본인이 추가한 부분 조회
        UserRoleEnum userRoleEnum = user.getRole();

        Page<Product> products;

        if (userRoleEnum == UserRoleEnum.USER) {
            // 사용자 권한이 USER일 경우
            products = productRepository.findAllByUserId(user.getId(), pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products;
    }

    @Transactional
    public Long updateProduct(Long id, ProductMypriceRequestDto requestDto, User user) {

        int myprice = requestDto.getMyprice();
        if (myprice < MIN_MY_PRICE) {
            throw new IllegalArgumentException("유효하지 않은 관심 가격입니다. 최소 " + MIN_MY_PRICE + " 원 이상으로 설정해 주세요.");
        }

        Product product = productRepository.findByIdAndUserId(id, user.getId()).orElseThrow(
                () -> new NullPointerException("해당 상품은 존재하지 않습니다.")
        );

        product.update(requestDto);

        return product.getId();

    }

    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 상품은 존재하지 않습니다.")
        );
        product.updateByItemDto(itemDto);
    }

    @Transactional
    public Product addFolder(Long productId, Long folderId, User user) {

        // 1) 상품을 조회합니다.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NullPointerException("해당 상품 아이디가 존재하지 않습니다."));

        // 2) 관심상품을 조회합니다.
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new NullPointerException("해당 폴더 아이디가 존재하지 않습니다."));

        // 3) 조회한 폴더와 관심상품이 모두 로그인한 회원의 소유인지 확인합니다.
        Long loginUserId = user.getId();
        if (!product.getUserId().equals(loginUserId) || !folder.getUser().getId().equals(loginUserId)) {
            throw new IllegalArgumentException("회원님의 관심상품이 아니거나, 회원님의 폴더가 아닙니다~^^");
        }

        // 중복확인
        Optional<Product> overlapFolder = productRepository.findByIdAndFolderList_Id(product.getId(), folder.getId());

        if (overlapFolder.isPresent()) {
            throw new IllegalArgumentException("중복된 폴더입니다.");
        }

        // 4) 상품에 폴더를 추가합니다.
        product.addFolder(folder);

        return product;
    }

}




//package com.sparta.myselectshop.service;
//
//import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
//import com.sparta.myselectshop.dto.ProductRequestDto;
//import com.sparta.myselectshop.dto.ProductResponseDto;
//import com.sparta.myselectshop.entity.Product;
//import com.sparta.myselectshop.entity.User;
//import com.sparta.myselectshop.entity.UserRoleEnum;
//import com.sparta.myselectshop.jwt.JwtUtil;
//import com.sparta.myselectshop.naver.dto.ItemDto;
//import com.sparta.myselectshop.repository.ProductRepository;
//import com.sparta.myselectshop.repository.UserRepository;
//import io.jsonwebtoken.Claims;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import com.sparta.myselectshop.repository.FolderRepository;
//import com.sparta.myselectshop.entity.Folder;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class ProductService {
//    private final FolderRepository folderRepository;
//    private final ProductRepository productRepository;
//    private final UserRepository userRepository;
//    private JwtUtil jwtUtil;
//
////    @Transactional
////    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
////        // 요청받은 DTO 로 DB에 저장할 객체 만들기
////        Product product = productRepository.saveAndFlush(new Product(requestDto));
////
////        return new ProductResponseDto(product);
////    }                                             //여기서는 바로 가지고 온 requestDto를 사용해서 product객체를 만든 다음에
//    //save를 하고나서 반환을 했었다. 아래 코드는 검증과정을 거치는 코드
//
//    @Transactional
//    public ProductResponseDto createProduct(ProductRequestDto requestDto, HttpServletRequest request) {
//        // Request에서 Token 가져오기
//        String token = jwtUtil.resolveToken(request);
//        Claims claims;
//
//        // 토큰이 있는 경우에만 관심상품 추가 가능
//        if (token != null) {
//            if (jwtUtil.validateToken(token)) {
//                // 토큰에서 사용자 정보 가져오기
//                claims = jwtUtil.getUserInfoFromToken(token);
//            } else {
//                throw new IllegalArgumentException("Token Error");
//            }
//
//            // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
//            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
//                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
//            );
//
//            // 요청받은 DTO 로 DB에 저장할 객체 만들기
//            Product product = productRepository.saveAndFlush(new Product(requestDto, user.getId()));       //검증이 완료가 되면 new Product 에 requestDto, user.getId())가 들어간다
//            //원래는 Product에 userid가 들어가지 않았기 떄문에 saveAndFlush를 했었는데(위에 주석 코드)
//            //여기서는 Product와 user가 연관관계가 있다는 가정을 해서 Product쪽에 userid를 넣어줬기 때문에
//            return new ProductResponseDto(product);
//        } else {
//            return null;
//        }
//    }
//
//
//    //    @Transactional(readOnly = true)
////    public List<ProductResponseDto> getProducts() {
////
////        List<ProductResponseDto> list = new ArrayList<>();
////
////        List<Product> productList = productRepository.findAll();
////        for (Product product : productList) {
////            list.add(new ProductResponseDto(product));
////        }
////
////        return list;
////    }                                             이떄는 누구나 다 조회할 수 있게 되어 있었다면
////
////    @Transactional(readOnly = true)                  //여기는 token을 통해 검증한 다음 검증된 사람만 조회를 할 수 있는 코드
////    public List<ProductResponseDto> getProducts(HttpServletRequest request) {
////        // Request에서 Token 가져오기
////        String token = jwtUtil.resolveToken(request);        //토큰 가져온다
////        Claims claims;                                       //jwt안에 들어있는 정보들을 담을 수 있는 객체
////
////        // 토큰이 있는 경우에만 관심상품 조회 가능
////        if(token != null) {
////            // Token 검증
////            if (jwtUtil.validateToken(token)) {        //토큰이 위변조가 일어나지 않았는지, 만료가 되지않았는지를 검증
////                // 토큰에서 사용자 정보 가져오기
////                claims = jwtUtil.getUserInfoFromToken(token);
////            } else {
////                throw new IllegalArgumentException("Token Error");
////            }
////
////            // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
////            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(       //claims.getSubject() 안에 user의 이름을 넣어놨음
////                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
////            );
////
////            // 사용자 권한 가져와서 ADMIN 이면 전체 조회, USER 면 본인이 추가한 부분 조회
////            UserRoleEnum userRoleEnum = user.getRole();                 //가져온 user의 권한을 Enum에 담는다.
////            System.out.println("role = " + userRoleEnum);
////
////            List<ProductResponseDto> list = new ArrayList<>();         //반환할 list만들어주고
////            List<Product> productList;
////
////            if (userRoleEnum == UserRoleEnum.USER) {                      //user의 권한이 만약에 user다
////                // 사용자 권한이 USER일 경우
////                productList = productRepository.findAllByUserId(user.getId());          //userid가 동일한 product들만 가지고 와서 담아주고
////            } else {
////                productList = productRepository.findAll();                             //admin이면 모든 product들을 가져옴
////            }
////
////            for (Product product : productList) {
////                list.add(new ProductResponseDto(product));
////            }
////
////            return list;
////
////        }else {
////            return null;
////        }
////    }
//    @Transactional(readOnly = true)
//    //기존에는  token만 확인하는 코드였는데, 바뀐코드는
//    public Page<Product> getProducts(HttpServletRequest request,                          //페이징 처리가 추가됐다.    페이징 처리를 해서 product를 가져오는 로직
//                                     int page, int size, String sortBy, boolean isAsc) {   // <--얘네 넷 받아오고 페이징 처리
//        // 페이징 처리
//        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;    //true면 오름차순 , false면 내림차순
//        Sort sort = Sort.by(direction, sortBy);                                       //sort기준에 오름차순인지 내림차순인지를 direction에 넣어주고, sortBy에는 어떤걸 기준으로 오름,내림차순할건지(우린 id, 상품명, 최저가 기준임)
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        // Request에서 Token 가져오기
//        String token = jwtUtil.resolveToken(request);
//        Claims claims;
//
//        // 토큰이 있는 경우에만 관심상품 조회 가능
//        if (token != null) {
//            // Token 검증
//            if (jwtUtil.validateToken(token)) {
//                // 토큰에서 사용자 정보 가져오기
//                claims = jwtUtil.getUserInfoFromToken(token);
//            } else {
//                throw new IllegalArgumentException("Token Error");
//            }
//
//            // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
//            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
//                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
//            );
//
//            // 사용자 권한 가져와서 ADMIN 이면 전체 조회, USER 면 본인이 추가한 부분 조회
//            UserRoleEnum userRoleEnum = user.getRole();
//            System.out.println("role = " + userRoleEnum);
//
//            Page<Product> products;
//
//            if (userRoleEnum == UserRoleEnum.USER) {                                    //기존에는 userid만 넣어서 product를 찾았는데, 여긴 pageable까지 추가해준 걸 볼 수 있음
//                // 사용자 권한이 USER일 경우
//                products = productRepository.findAllByUserId(user.getId(), pageable);
//            } else {                                                                   //else문은 admin, 기존에는 전체를 조회했었는데 admin도 정렬 등 페이징 처리가 필요하기 떄문에
//                products = productRepository.findAll(pageable);                        //위에서 만들었던 pageable객체를 넣어줬음.
//            }
//            return products;
//
//        } else {
//            return null;
//        }
//    }
//
//
////    @Transactional
////    public Long updateProduct(Long id, ProductMypriceRequestDto requestDto) {
////
////        Product product = productRepository.findById(id).orElseThrow(
////                () -> new NullPointerException("해당 상품은 존재하지 않습니다.")
////        );
////
////        product.update(requestDto);
////
////        return product.getId();
////    }
//
//
//    @Transactional
//    public Long updateProduct(Long id, ProductMypriceRequestDto requestDto, HttpServletRequest request) {
//        // Request에서 Token 가져오기
//        String token = jwtUtil.resolveToken(request);
//        Claims claims;
//
//        // 토큰이 있는 경우에만 관심상품 최저가 업데이트 가능
//        if (token != null) {
//            // Token 검증
//            if (jwtUtil.validateToken(token)) {
//                // 토큰에서 사용자 정보 가져오기
//                claims = jwtUtil.getUserInfoFromToken(token);
//            } else {
//                throw new IllegalArgumentException("Token Error");
//            }
//
//            // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
//            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
//                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
//            );
//
//            Product product = productRepository.findByIdAndUserId(id, user.getId()).orElseThrow(//productid와 userid가 다 들어간 모습. 내가 가지고 온 productid면서 그 product가 동일한userid까지 가지고있는지 확인
//                    () -> new NullPointerException("해당 상품은 존재하지 않습니다.")
//            );                                                                                  //현재 로그인을 한 user가 선택한 product가 맞는지 확인한다
//
//            product.update(requestDto);
//
//            return product.getId();
//
//        } else {
//            return null;
//        }
//    }
//
//
//
//    @Transactional
//    public void updateBySearch(Long id, ItemDto itemDto) {
//        Product product = productRepository.findById(id).orElseThrow(
//                () -> new NullPointerException("해당 상품은 존재하지 않습니다.")
//        );
//        product.updateByItemDto(itemDto);
//    }
//
//
//    @Transactional
//    public Product addFolder(Long productId, Long folderId, HttpServletRequest request) {
//        // Request에서 Token 가져오기
//        String token = jwtUtil.resolveToken(request);
//        Claims claims;
//
//        // 토큰이 있는 경우에만 관심상품 최저가 업데이트 가능
//        if (token != null) {
//            // Token 검증
//            if (jwtUtil.validateToken(token)) {
//                // 토큰에서 사용자 정보 가져오기
//                claims = jwtUtil.getUserInfoFromToken(token);
//            } else {
//                throw new IllegalArgumentException("Token Error");
//            }
//
//            // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
//            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(
//                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
//            );
//
//            // 1) 관심상품을 조회합니다.
//            Product product = productRepository.findById(productId)
//                    .orElseThrow(() -> new NullPointerException("해당 상품 아이디가 존재하지 않습니다."));
//
//            // 2) 폴더를 조회합니다.
//            Folder folder = folderRepository.findById(folderId)
//                    .orElseThrow(() -> new NullPointerException("해당 폴더 아이디가 존재하지 않습니다."));
//
//            // 3) 조회한 폴더와 관심상품이 모두 로그인한 회원의 소유인지 확인합니다.
//            Long loginUserId = user.getId();
//            if (!product.getUserId().equals(loginUserId) || !folder.getUser().getId().equals(loginUserId)) {
//                throw new IllegalArgumentException("회원님의 관심상품이 아니거나, 회원님의 폴더가 아닙니다~^^");
//            }    //if문 뜻 : Product의 User ID와 277줄에 우리가 가지고 온 User의 ID가 같은지,
//            //if문 뜻 : Folder가 가지고 있는 User의 ID와  277줄에 토큰에서 가지고 온 User의 ID가 같은지 확인하겠다~
//            // 중복확인
//            Optional<Product> product1 = productRepository.findByIdAndFolderList_Id(productId, folderId);
//            if (product1.isPresent()){
////                throw new IllegalArgumentException("중복된 폴더입니다.");
//                return product;
//            }
//            // 4) 상품에 폴더를 추가합니다.
//            product.addFolder(folder);
//            return product;
//        } else {
//            return null;
//        }
//    }
//}
//
////
////    // 중복확인
////    Optional<Product> overlapFolder = productRepository.findByIdAndFolderList_Id(product.getId(), folder.getId());
////            if(overlapFolder.isPresent()) {
////        throw new IllegalArgumentException("중복된 폴더입니다.");
////    }
////    // 4) 상품에 폴더를 추가합니다.
////            product.addFolder(folder);
////            return product;
////} else {
////        return null;
////        }
////        }
////
////        }