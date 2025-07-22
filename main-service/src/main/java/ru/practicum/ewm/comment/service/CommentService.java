package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentSaveDto;

import java.util.List;

public interface CommentService {
    CommentDto addComment(CommentSaveDto commentSaveDto);

    List<CommentDto> getCommentsByEventId(Long eventId);
}