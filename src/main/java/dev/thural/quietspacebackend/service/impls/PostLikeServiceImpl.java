package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.PostEntity;
import dev.thural.quietspacebackend.entity.PostLikeEntity;
import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.mapper.PostLikeMapper;
import dev.thural.quietspacebackend.mapper.PostMapper;
import dev.thural.quietspacebackend.mapper.UserMapper;
import dev.thural.quietspacebackend.model.PostDTO;
import dev.thural.quietspacebackend.model.PostLikeDTO;
import dev.thural.quietspacebackend.model.UserDTO;
import dev.thural.quietspacebackend.repository.PostLikeRepository;
import dev.thural.quietspacebackend.service.PostLikeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class PostLikeServiceImpl implements PostLikeService {
    PostLikeMapper postLikeMapper;
    UserMapper userMapper;

    PostMapper postMapper;
    PostLikeRepository postLikeRepository;

    @Override
    public List<PostLikeDTO> getAll() {
        List<PostLikeEntity> postLikeEntities = postLikeRepository.findAll();
        List<PostLikeDTO> postLikeDTOs = postLikeEntities.stream()
                .map(postLikeMapper::postLikeEntityToDto)
                .toList();

        return postLikeDTOs;
    }

    @Override
    public Optional<PostLikeDTO> getById(UUID id) {
        return Optional.empty();
    }

    @Override
    public List<PostLikeDTO> getAllByUser(UserDTO user) {
        List<PostLikeEntity> postLikeEntities = postLikeRepository.getAllByUser(userMapper.userDtoToEntity(user));
        List<PostLikeDTO> postLikeDTOs = postLikeEntities.stream()
                .map(postLikeMapper::postLikeEntityToDto)
                .toList();

        return postLikeDTOs;
    }

    @Override
    public List<PostLikeDTO> getAllByPost(PostDTO post) {
        List<PostLikeEntity> postLikeEntities = postLikeRepository.getAllByPost(postMapper.postDtoToEntity(post));
        List<PostLikeDTO> postLikeDTOs = postLikeEntities.stream()
                .map(postLikeMapper::postLikeEntityToDto)
                .toList();
        return postLikeDTOs;
    }

    @Override
    public void togglePostLike(PostLikeDTO postLikeDTO) {
        UserEntity user = postLikeDTO.getUser();
        PostEntity post = postLikeDTO.getPost();
        UUID id = postLikeDTO.getId();
        if (postLikeRepository.existsByUserAndPost(user, post))
            postLikeRepository.deleteById(id);
        else postLikeRepository.save(postLikeMapper.postLikeDtoToEntity(postLikeDTO));
    }
}
