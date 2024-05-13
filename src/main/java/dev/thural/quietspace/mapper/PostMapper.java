package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.Poll;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.response.PollResponse;
import dev.thural.quietspace.model.response.PostResponse;
import dev.thural.quietspace.service.PostService;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PostMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "poll", ignore = true)
    Post postRequestToEntity(PostRequest postRequest);

    @Mapping(target = "username", source ="user.username")
    @Mapping(target = "userId", source ="user.id")
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "commentCount", expression = "java(post.getComments().size())")
    PostResponse postEntityToResponse(Post post);

    default PollResponse pollEntityToResponse (Poll poll){
        return Mappers.getMapper(PollMapper.class).pollEntityToResponse(poll);
    }

    @AfterMapping
    default void likeCount(Post post, @MappingTarget PostResponse postResponse, @Context PostService postService) {
        postResponse.setLikeCount(postService.getPostLikesByPostId(post.getId()).size());
    }

}
