package ru.skypro.homework.dto;


import javax.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * DTO для комментариев
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDTO {

  /**Id автора комментария */
//  Integer id;
  Integer author;

  String createdAt;

  @Min(1)
  Integer pk;
  /**Текст комментария */
  String text;

}
