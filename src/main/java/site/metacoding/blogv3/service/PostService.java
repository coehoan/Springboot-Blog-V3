package site.metacoding.blogv3.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.category.CategoryRepository;
import site.metacoding.blogv3.domain.post.Post;
import site.metacoding.blogv3.domain.post.PostRepository;
import site.metacoding.blogv3.web.dto.post.PostRespDto;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public PostRespDto 게시글목록보기(Integer userId) {
        List<Category> categoriesEntity = categoryRepository.findByUserId(userId);
        List<Post> postsEntity = postRepository.findByUserId(userId);
        PostRespDto postRespDto = new PostRespDto(postsEntity, categoriesEntity);
        return postRespDto;
    }
}
