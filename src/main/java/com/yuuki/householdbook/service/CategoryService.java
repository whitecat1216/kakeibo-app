package com.yuuki.householdbook.service;

import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.entity.Category;
import com.yuuki.householdbook.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> list(AppUser user) {
        return categoryRepository.findByUserOrderBySortOrderAscIdAsc(user);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public void delete(Category category) {
        categoryRepository.delete(category);
    }

    public void move(AppUser user, Long id, String direction) {
        List<Category> categories = list(user);
        int index = -1;
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId().equals(id)) {
                index = i;
                break;
            }
        }
        if (index < 0) return;

        int targetIndex = "up".equals(direction) ? index - 1 : index + 1;
        if (targetIndex < 0 || targetIndex >= categories.size()) return;

        Category current = categories.get(index);
        Category target = categories.get(targetIndex);

        Integer tmp = current.getSortOrder();
        current.setSortOrder(target.getSortOrder());
        target.setSortOrder(tmp);

        categoryRepository.save(current);
        categoryRepository.save(target);
    }

    public void createDefaultCategories(AppUser user) {
        if (!categoryRepository.findByUserOrderBySortOrderAscIdAsc(user).isEmpty()) {
            return;
        }

        List<Category> defaults = new ArrayList<>();
        defaults.add(build(user, "食費", "#FF6B6B", 1, "expense"));
        defaults.add(build(user, "日用品", "#FFD93D", 2, "expense"));
        defaults.add(build(user, "交通", "#6BCB77", 3, "expense"));
        defaults.add(build(user, "通信", "#4D96FF", 4, "expense"));
        defaults.add(build(user, "住居", "#845EC2", 5, "expense"));
        defaults.add(build(user, "医療", "#00C9A7", 6, "expense"));
        defaults.add(build(user, "趣味", "#FF9671", 7, "expense"));

        defaults.add(build(user, "給与", "#2C73D2", 101, "income"));
        defaults.add(build(user, "副収入", "#0081CF", 102, "income"));
        defaults.add(build(user, "ボーナス", "#0089BA", 103, "income"));

        categoryRepository.saveAll(defaults);
    }

    public Category findByName(AppUser user, String name) {
        if (name == null || name.isBlank()) return null;
        return categoryRepository.findByUserAndNameIgnoreCase(user, name.trim());
    }

    public Category findOrCreateByName(AppUser user, String name, String type) {
        if (name == null || name.isBlank()) return null;
        Category existing = categoryRepository.findByUserAndNameIgnoreCase(user, name.trim());
        if (existing != null) return existing;

        int nextSort = list(user).stream()
                .map(Category::getSortOrder)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        Category category = new Category();
        category.setUser(user);
        category.setName(name.trim());
        category.setColor("income".equals(type) ? "#2C73D2" : "#FF6B6B");
        category.setSortOrder(nextSort);
        category.setType(type);
        return categoryRepository.save(category);
    }

    private Category build(AppUser user, String name, String color, int sortOrder, String type) {
        Category c = new Category();
        c.setUser(user);
        c.setName(name);
        c.setColor(color);
        c.setSortOrder(sortOrder);
        c.setType(type);
        return c;
    }
}
