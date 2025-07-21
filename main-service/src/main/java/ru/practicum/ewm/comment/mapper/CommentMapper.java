package ru.practicum.ewm.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentSaveDto;
import ru.practicum.ewm.comment.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "user", ignore = true)
    Comment mapToComment(CommentSaveDto commentSaveDto);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    CommentDto mapToCommentDto(Comment comment);
}