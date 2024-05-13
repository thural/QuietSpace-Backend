package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.Poll;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.response.PollResponse;
import dev.thural.quietspace.model.response.PostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PostMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "poll", ignore = true)
    Post postRequestToEntity(PostRequest postRequest);

    @Mapping(target = "username", source ="user.username")
    @Mapping(target = "userId", source ="user.id")
    @Mapping(target = "likeCount", expression = "java(post.getLikes().size())")
    @Mapping(target = "commentCount", expression = "java(post.getComments().size())")
    PostResponse postEntityToResponse(Post post);

    default PollResponse pollEntityToResponse (Poll poll){
        return Mappers.getMapper(PollMapper.class).pollEntityToResponse(poll);
    }

}
