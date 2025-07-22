package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.CommentSaveDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto addComment(CommentSaveDto commentSaveDto) {
        Event event = eventRepository.findById(commentSaveDto.getEventId())
                .orElseThrow(() -> new NotFoundException("Event not found: " + commentSaveDto.getEventId()));
        User user = userRepository.findById(commentSaveDto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found: " + commentSaveDto.getUserId()));
        Comment comment = commentMapper.mapToComment(commentSaveDto);
        comment.setEvent(event);
        comment.setUser(user);
        Comment saved = commentRepository.save(comment);
        log.info("Comment saved: {}", saved);
        return commentMapper.mapToCommentDto(saved);
    }

    @Override
    public List<CommentDto> getCommentsByEventId(Long eventId) {
        List<Comment> comments = commentRepository.findByEventId(eventId);
        return comments.stream()
                .map(commentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }
}