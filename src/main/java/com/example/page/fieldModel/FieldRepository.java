package com.example.page.fieldModel;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldRepository extends JpaRepository<Field,Integer> {
    Field findFirstByOrderByIdDesc();
    Field findFirstByOrderByIdAsc();
}
