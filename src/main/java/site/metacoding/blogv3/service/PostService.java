package site.metacoding.blogv3.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.category.CategoryRepository;
import site.metacoding.blogv3.domain.post.Post;
import site.metacoding.blogv3.domain.post.PostRepository;
import site.metacoding.blogv3.domain.user.User;
import site.metacoding.blogv3.web.dto.post.PostRespDto;
import site.metacoding.blogv3.web.dto.post.PostWriteReqDto;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public List<Category> 게시글쓰기화면(User principal) {
        return categoryRepository.findByUserId(principal.getId());
    }

    @Transactional
    public void 게시글쓰기(PostWriteReqDto postWriteReqDto) {
        // 1. 파일 저장 (이미지파일) - UUID 사용

        // 2. 이미지 파일명을 Post 오브젝트에 thumnail로 옮긴다.

        // 3. title, content도 Post 오브젝트로 옮긴다.

        // 4. userId도 Post 오브젝트에 옮긴다.

        // 5. categoryId도 Post 오브젝트에 옮긴다.

        // 6. save
    }

    public PostRespDto 게시글목록보기(Integer userId) {
        List<Category> categoriesEntity = categoryRepository.findByUserId(userId);
        List<Post> postsEntity = postRepository.findByUserId(userId);
        PostRespDto postRespDto = new PostRespDto(postsEntity, categoriesEntity);
        return postRespDto;
    }
}
