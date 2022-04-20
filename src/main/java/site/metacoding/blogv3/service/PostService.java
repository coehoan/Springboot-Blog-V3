package site.metacoding.blogv3.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.category.CategoryRepository;
import site.metacoding.blogv3.domain.post.Post;
import site.metacoding.blogv3.domain.post.PostRepository;
import site.metacoding.blogv3.domain.user.User;
import site.metacoding.blogv3.handler.ex.CustomException;
import site.metacoding.blogv3.util.UtilFileUpload;
import site.metacoding.blogv3.web.dto.post.PostRespDto;
import site.metacoding.blogv3.web.dto.post.PostWriteReqDto;

@RequiredArgsConstructor
@Service
public class PostService {

    @Value("${file.path}")
    private String uploadFolder;

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public List<Category> 게시글쓰기화면(User principal) {
        return categoryRepository.findByUserId(principal.getId());
    }

    // 하나의 서비스는 여러가지 일을 한 번에 처리한다. (여러가지 일이 하나의 트랜잭션이다.)
    @Transactional
    public void 게시글쓰기(PostWriteReqDto postWriteReqDto, User principal) {

        // 1. UUID로 파일 쓰고 경로 리턴받기
        String thumnail = UtilFileUpload.write(uploadFolder, postWriteReqDto.getThumnailFile());

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

    public PostRespDto 게시글목록보기(Integer userId) {
        List<Category> categoriesEntity = categoryRepository.findByUserId(userId);
        List<Post> postsEntity = postRepository.findByUserId(userId);
        PostRespDto postRespDto = new PostRespDto(postsEntity, categoriesEntity);
        return postRespDto;
    }
}
