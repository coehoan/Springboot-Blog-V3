package site.metacoding.blogv3.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.category.CategoryRepository;
import site.metacoding.blogv3.domain.post.Post;
import site.metacoding.blogv3.domain.post.PostRepository;
import site.metacoding.blogv3.domain.user.User;
import site.metacoding.blogv3.domain.user.UserRepository;
import site.metacoding.blogv3.domain.visit.Visit;
import site.metacoding.blogv3.domain.visit.VisitRepository;
import site.metacoding.blogv3.handler.ex.CustomException;
import site.metacoding.blogv3.util.UtilFileUpload;
import site.metacoding.blogv3.web.dto.post.PostDetailRespDto;
import site.metacoding.blogv3.web.dto.post.PostRespDto;
import site.metacoding.blogv3.web.dto.post.PostWriteReqDto;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    // Slf4j 어노테이션을 적어주면 아래 코드가 실행됨
    // private static final Logger LOGGER = LogManager.getLogger(PostService.class);

    @Value("${file.path}")
    private String uploadFolder;

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final VisitRepository visitRepository;
    private final UserRepository userRepository;

    @Transactional
    public void 게시글삭제(Integer id, User principal) {

        Optional<Post> postOp = postRepository.findById(id);

        if (postOp.isPresent()) {
            Post postEntity = postOp.get();

            // 권한 체크
            if (principal.getId() == postEntity.getUser().getId()) {

                postRepository.deleteById(id);
            } else {
                throw new CustomException("삭제 권한이 없습니다");
            }
        } else {
            throw new CustomException("해당 게시글이 존재하지 않습니다");
        }

    }

    public List<Category> 게시글쓰기화면(User principal) {
        return categoryRepository.findByUserId(principal.getId());
    }

    @Transactional
    public PostDetailRespDto 게시글상세보기(Integer id) {

        PostDetailRespDto postDetailRespDto = new PostDetailRespDto();

        Optional<Post> postOp = postRepository.findById(id);

        if (postOp.isPresent()) {
            Post postEntity = postOp.get();

            postDetailRespDto.setPost(postEntity);
            postDetailRespDto.setPageOwner(false);

            // 방문자 카운트 증가
            Optional<Visit> visitOp = visitRepository.findById(postEntity.getUser().getId());
            if (visitOp.isPresent()) {
                Visit visitEntity = visitOp.get();
                Long totalCount = visitEntity.getTotalCount();
                visitEntity.setTotalCount(totalCount + 1);
            } else {
                log.error("심각한 오류 발생", "회원가입할때 Visit이 안만들어지는 오류 발생");
                throw new CustomException("일시적 문제가 생겼습니다. 관리자에게 문의해주세요.");
            }
            return postDetailRespDto;
        } else {
            throw new CustomException("게시글을 찾을 수 없습니다.");
        }
    }

    @Transactional
    public PostDetailRespDto 게시글상세보기(Integer id, User principal) {

        PostDetailRespDto postDetailRespDto = new PostDetailRespDto();
        // 해당 게시글 주인 id
        Integer pageOwnerId = null;
        // 로그인한 사용자의 id
        Integer loginUserId = principal.getId();

        Optional<Post> postOp = postRepository.findById(id);

        if (postOp.isPresent()) {
            Post postEntity = postOp.get();

            postDetailRespDto.setPost(postEntity);

            // 두 값을 비교해서 isPageOwner에 true,false
            pageOwnerId = postEntity.getUser().getId();
            if (pageOwnerId == loginUserId) {
                postDetailRespDto.setPageOwner(true);
            } else {
                postDetailRespDto.setPageOwner(false);
            }

            // 방문자 카운트 증가
            Optional<Visit> visitOp = visitRepository.findById(postEntity.getUser().getId());
            if (visitOp.isPresent()) {
                Visit visitEntity = visitOp.get();
                Long totalCount = visitEntity.getTotalCount();
                visitEntity.setTotalCount(totalCount + 1);
            } else {
                log.error("심각한 오류 발생", "회원가입할때 Visit이 안만들어지는 오류 발생");
                throw new CustomException("일시적 문제가 생겼습니다. 관리자에게 문의해주세요.");
            }
            return postDetailRespDto;
        } else {
            throw new CustomException("게시글을 찾을 수 없습니다.");
        }
    }

    // 하나의 서비스는 여러가지 일을 한 번에 처리한다. (여러가지 일이 하나의 트랜잭션이다.)
    @Transactional
    public void 게시글쓰기(PostWriteReqDto postWriteReqDto, User principal) {

        // 1. UUID로 파일 쓰고 경로 리턴받기
        String thumnail = null;
        if (!postWriteReqDto.getThumnailFile().isEmpty()) {
            thumnail = UtilFileUpload.write(uploadFolder, postWriteReqDto.getThumnailFile());
        }

        // 이 방식으로 사용하면 없는 카테고리id로 공격당할 수 있음
        // Category category = new Category();
        // category.setId(postWriteReqDto.getCategoryId());

        // 2. DB접근해서 카테고리 존재하는지 확인
        Optional<Category> categoryOp = categoryRepository.findById(postWriteReqDto.getCategoryId());

        // 3. post DB 저장
        if (categoryOp.isPresent()) {
            Post post = postWriteReqDto.toEntity(thumnail, principal, categoryOp.get());
            postRepository.save(post);
        } else {
            throw new CustomException("해당 카테고리가 존재하지 않습니다.");
        }
        // postRepository.mSave(postWriteReqDto.getCategoryId(), principal.getId(),
        // postWriteReqDto.getTitle(), postWriteReqDto.getContent(), thumnail);
    }

    @Transactional
    public PostRespDto 게시글목록보기(Integer pageOwnerId, Pageable pageable) {

        List<Category> categoriesEntity = categoryRepository.findByUserId(pageOwnerId);
        Page<Post> postsEntity = postRepository.findByUserId(pageOwnerId, pageable);

        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 0; i < postsEntity.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        PostRespDto postRespDto = new PostRespDto(
                postsEntity,
                categoriesEntity,
                pageOwnerId,
                postsEntity.getNumber() - 1,
                postsEntity.getNumber() + 1,
                pageNumbers,
                0L);

        // 방문자 카운트 증가
        Optional<User> pageOwnerOp = userRepository.findById(pageOwnerId);
        if (pageOwnerOp.isPresent()) {
            User pageOwnerEntity = pageOwnerOp.get();
            Optional<Visit> visitOp = visitRepository.findById(pageOwnerEntity.getId());
            if (visitOp.isPresent()) {
                Visit visitEntity = visitOp.get();
                // Dto에 방문자수 담기(request에서 ip주소 받아서 동일하면 증가 안시키는 로직 필요함)
                postRespDto.setTotalCount(visitEntity.getTotalCount());

                Long totalCount = visitEntity.getTotalCount();
                visitEntity.setTotalCount(totalCount + 1);
            } else {
                log.error("심각한 오류 발생", "회원가입할때 Visit이 안만들어지는 오류 발생");
                throw new CustomException("일시적 문제가 생겼습니다. 관리자에게 문의해주세요.");
            }
        } else
            throw new CustomException("없는 블로그입니다.");
        return postRespDto;
    }

    public PostRespDto 카테고리별게시글보기(Integer pageOwnerId, Integer categoryId, Pageable pageable) {
        List<Category> categoriesEntity = categoryRepository.findByUserId(pageOwnerId);
        Page<Post> postsEntity = postRepository.findByUserIdAndCategoryId(pageOwnerId, categoryId, pageable);
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = 0; i < postsEntity.getTotalPages(); i++) {
            pageNumbers.add(i);
        }
        PostRespDto postRespDto = new PostRespDto(
                postsEntity,
                categoriesEntity,
                pageOwnerId,
                postsEntity.getNumber() - 1,
                postsEntity.getNumber() + 1,
                pageNumbers,
                0L);
        // 방문자 카운트 증가
        Optional<User> pageOwnerOp = userRepository.findById(pageOwnerId);
        if (pageOwnerOp.isPresent()) {
            User pageOwnerEntity = pageOwnerOp.get();
            Optional<Visit> visitOp = visitRepository.findById(pageOwnerEntity.getId());
            if (visitOp.isPresent()) {
                Visit visitEntity = visitOp.get();
                // Dto에 방문자수 담기(request에서 ip주소 받아서 동일하면 증가 안시키는 로직 필요함)
                postRespDto.setTotalCount(visitEntity.getTotalCount());

                Long totalCount = visitEntity.getTotalCount();
                visitEntity.setTotalCount(totalCount + 1);
            } else {
                log.error("심각한 오류 발생", "회원가입할때 Visit이 안만들어지는 오류 발생");
                throw new CustomException("일시적 문제가 생겼습니다. 관리자에게 문의해주세요.");
            }
        } else
            throw new CustomException("없는 블로그입니다.");
        return postRespDto;
    }
}
