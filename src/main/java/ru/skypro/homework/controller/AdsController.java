package ru.skypro.homework.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.service.AdsService;

@RestController
@RequestMapping("/ads")
@Slf4j
public class AdsController {

    private final AdsService adsService;

    public AdsController(AdsService adsService) {
        this.adsService = adsService;
    }

    @GetMapping(value = "/{ad_pk}/comments/{id}")
    public ResponseEntity<Comment> getComments(@PathVariable(name = "ad_pk") String adPk,
                                               @PathVariable(name = "id") int id) {
        return ResponseEntity.ok(adsService.getComments(adPk, id));
    }

    @DeleteMapping(value = "/{ad_pk}/comments/{id}")
    public ResponseEntity deleteComments(@PathVariable(name = "ad_pk") String adPk,
                                               @PathVariable(name = "id") int id) {
        adsService.deleteComments(adPk, id);
        return ResponseEntity.ok().build();
    }
}
