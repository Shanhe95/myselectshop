package com.sparta.myselectshop.service;

import com.sparta.myselectshop.entity.Folder;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.FolderRepository;
import com.sparta.myselectshop.repository.ProductRepository;
import com.sparta.myselectshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final ProductRepository productRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;


    // 로그인한 회원에 폴더들 등록
    @Transactional
    public List<Folder> addFolders(List<String> folderNames, String username) {

        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
        );

        // 입력으로 들어온 폴더 이름을 기준으로, 회원이 이미 생성한 폴더들을 조회합니다.
        List<Folder> existFolderList = folderRepository.findAllByUserAndNameIn(user, folderNames);

        List<Folder> folderList = new ArrayList<>();

        for (String folderName : folderNames) {
            // 이미 생성한 폴더가 아닌 경우만 폴더 생성
            if (isExistFolderName(folderName, existFolderList).equals("false")) {
                Folder folder = new Folder(folderName, user);
                folderList.add(folder);
            } else {
                throw new IllegalArgumentException("중복된 폴더명 ('" + isExistFolderName(folderName, existFolderList) + "')을 삭제하고 재시도해 주세요");
            }
        }

        return folderRepository.saveAll(folderList);
    }

    // 로그인한 회원이 등록된 모든 폴더 조회
    @Transactional(readOnly = true)
    public List<Folder> getFolders(User user) {
        return folderRepository.findAllByUser(user);
    }

    @Transactional(readOnly = true)
    public Page<Product> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, User user) {

        // 페이징 처리
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);


        return productRepository.findAllByUserIdAndFolderList_Id(user.getId(), folderId, pageable);
    }

    private String isExistFolderName(String folderName, List<Folder> existFolderList) {
        // 기존 폴더 리스트에서 folder name 이 있는지?
        for (Folder existFolder : existFolderList) {
            if (existFolder.getName().equals(folderName)) {
                return folderName;
            }
        }

        return "false";
    }

}





//package com.sparta.myselectshop.service;
//
//import com.sparta.myselectshop.entity.Folder;
//import com.sparta.myselectshop.entity.Product;
//import com.sparta.myselectshop.entity.User;
//import com.sparta.myselectshop.jwt.JwtUtil;
//import com.sparta.myselectshop.repository.FolderRepository;
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
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class FolderService {
//
//    private final ProductRepository productRepository;
//    private final FolderRepository folderRepository;
//    private final UserRepository userRepository;
//    private final JwtUtil jwtUtil;
//
//
//    // 로그인한 회원에 폴더들 등록
//    @Transactional
//    public List<Folder> addFolders(List<String> folderNames, HttpServletRequest request) {
//
//        // Request에서 Token 가져오기
//        String token = jwtUtil.resolveToken(request);
//        Claims claims;
//
//        // 토큰이 있는 경우에만 관심상품 조회 가능
//        if (token != null) {
//
//            // Token 검증
//            if (jwtUtil.validateToken(token)) {
//                // 토큰에서 사용자 정보 가져오기
//                claims = jwtUtil.getUserInfoFromToken(token);
//            } else {
//                throw new IllegalArgumentException("Token Error");
//            }
//
//            // 토큰에서 가져온 사용자 정보를 사용하여 DB 조회
//            User user = userRepository.findByUsername(claims.getSubject()).orElseThrow(     //유저가 있는지 없는지 확인
//                    () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
//            );
//
//            // 입력으로 들어온 폴더 이름을 기준으로, 회원이 이미 생성한 폴더들을 조회합니다.
//            List<Folder> existFolderList = folderRepository.findAllByUserAndNameIn(user, folderNames);
//
//            List<Folder> folderList = new ArrayList<>();      //Folder가 여러가지 생기기때문에 리스트 형식으로 만들어준다
//
//            for (String folderName : folderNames) {           //for문 돌리는 이유 : 우리가 가지고 온 folderNames이 리스트 형식이라서
//                //이미 생성한 폴더가 아닌 경우만 폴더 생성
//                if (!isExistFolderName(folderName, existFolderList)) {
//                    Folder folder = new Folder(folderName, user);
//                    folderList.add(folder);
//                }
//            }
//
//            return folderRepository.saveAll(folderList);
//        } else {
//            return null;
//        }
//    }
//
//    // 로그인한 회원이 등록된 모든 폴더 조회
//    @Transactional(readOnly = true)
//    public List<Folder> getFolders(HttpServletRequest request) {
//
//        // 사용자의 정보를 가져온다
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
//            return folderRepository.findAllByUser(user);      //왜 findAllByUser가 두번??
//
//        } else {
//            return null;
//        }
//    }
//
//
//    @Transactional(readOnly = true)                                                    //폴더별로 관심상품 조회
//    public Page<Product> getProductsInFolder(Long folderId, int page, int size, String sortBy, boolean isAsc, HttpServletRequest request) {
//
//        // 페이징 처리
//        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
//        Sort sort = Sort.by(direction, sortBy);
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
//            return productRepository.findAllByUserIdAndFolderList_Id(user.getId(), folderId, pageable);      //로그인을 한 userid, 우리가 선택한 folderID,
//
//        } else {
//            return null;
//        }
//    }
//
//    //중복 폴더 생성 이슈 해결하기
//    private boolean isExistFolderName(String folderName, List<Folder> existFolderList) {
//                                                // 기존 폴더 리스트에서 folder name 이 있는지?
//        for (Folder existFolder : existFolderList) {
//            if (existFolder.getName().equals(folderName)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//}