package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.model.request.PostRequest;
import dev.thural.quietspace.model.response.PostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PostMapper {
    @Mapping(target = "id", ignore = true)
    Post postRequestToEntity(PostRequest postRequest);

    @Mapping(target = "username", source ="user.username")
    @Mapping(target = "userId", source ="user.id")
    PostResponse postEntityToResponse(Post post);
}
