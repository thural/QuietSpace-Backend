package dev.thural.quietspacebackend.mapper;

import dev.thural.quietspacebackend.entity.Post;
import dev.thural.quietspacebackend.model.request.PostRequest;
import dev.thural.quietspacebackend.model.response.PostResponse;
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
