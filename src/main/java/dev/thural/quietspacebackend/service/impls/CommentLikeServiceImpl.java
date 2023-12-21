package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.CommentEntity;
import dev.thural.quietspacebackend.entity.CommentLikeEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.CommentLikeMapper;
import dev.thural.quietspacebackend.mapper.CommentMapper;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.CommentDTO;
import dev.thural.quietspacebackend.model.CommentLikeDTO;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.CommentLikeRepository;
import dev.thural.quietspacebackend.service.CommentLikeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentLikeServiceImpl implements CommentLikeService {
    CommentLikeMapper commentLikeMapper;
    UserMapper userMapper;
    CommentMapper commentMapper;
    CommentLikeRepository commentLikeRepository;

    @Override
    public List<CommentLikeDTO> getAll() {
        List<CommentLikeEntity> commentLikeEntities = commentLikeRepository.findAll();
        List<CommentLikeDTO> commentLikeDTOs = commentLikeEntities.stream()
                .map(commentLikeMapper::commentLikeEntityToDto)
                .toList();

        return commentLikeDTOs;
    }

    @Override
    public Optional<CommentLikeDTO> getById(UUID id) {
        return Optional.empty();
    }

    @Override
    public List<CommentLikeDTO> getAllByUser(UserDTO user) {
        List<CommentLikeEntity> commentLikeEntities = commentLikeRepository.getAllByUser(userMapper.userDtoToEntity(user));
        List<CommentLikeDTO> commentLikeDTOs = commentLikeEntities.stream()
                .map(commentLikeMapper::commentLikeEntityToDto)
                .toList();

        return commentLikeDTOs;
    }

    @Override
    public List<CommentLikeDTO> getAllByComment(CommentDTO comment) {
        List<CommentLikeEntity> commentLikeEntities = commentLikeRepository.getAllByComment(commentMapper.commentDtoToEntity(comment));
        List<CommentLikeDTO> commentLikeDTOs = commentLikeEntities.stream()
                .map(commentLikeMapper::commentLikeEntityToDto)
                .toList();
        return commentLikeDTOs;
    }

    @Override
    public void toggleCommentLike(CommentLikeDTO commentLike) {
        UserEntity user = commentLike.getUser();
        CommentEntity comment = commentLike.getComment();
        UUID id = commentLike.getId();
        if (commentLikeRepository.existsByUserAndComment(user, comment))
            commentLikeRepository.deleteById(id);
        else commentLikeRepository.save(commentLikeMapper.commentLikeDtoToEntity(commentLike));
    }
}
