package ru.skypro.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skypro.homework.dto.CommentDTO;
import ru.skypro.homework.entity.AdEntity;

/**
 * репозиторий для объявления
 */

@Repository
public interface AdsRepository extends JpaRepository<AdEntity, Integer> {
    AdEntity findByAuthorAndId(int author,int id);


}
