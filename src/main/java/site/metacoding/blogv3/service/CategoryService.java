package site.metacoding.blogv3.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import site.metacoding.blogv3.domain.category.Category;
import site.metacoding.blogv3.domain.category.CategoryRepository;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    // rollbackFor - 특정 익셉션 발생 시 롤백처리 가능
    @Transactional
    public void 카테고리등록(Category category) {
        categoryRepository.save(category);
    }
}
