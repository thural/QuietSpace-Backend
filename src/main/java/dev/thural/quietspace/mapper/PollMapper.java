package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.Poll;
import dev.thural.quietspace.entity.PollOption;
import dev.thural.quietspace.model.request.PollRequest;
import dev.thural.quietspace.model.response.OptionResponse;
import dev.thural.quietspace.model.response.PollResponse;
import dev.thural.quietspace.service.PostService;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface PollMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "options", ignore = true)
    Poll pollRequestToEntity(PollRequest pollRequest);

    @Mapping(target = "voteCount", expression="java(voteCounter(poll))")
    @Mapping(target = "votedOption", ignore = true)
    PollResponse pollEntityToResponse(Poll poll);

    default OptionResponse optionEntityToResponse (PollOption option){
        return Mappers.getMapper(PollOptionMapper.class).pollOptionEntityToResponse(option);
    }

    @Named("voteCounter")
    default Integer voteCounter(Poll poll){
        return poll.getOptions().stream()
                .map(option -> option.getVotes().size())
                .reduce(0, Integer::sum);
    }

    @AfterMapping
    default void votedOption(Poll poll, @MappingTarget PollResponse pollResponse, @Context PostService postService) {
        pollResponse.setVotedOption(postService.getVotedPollOptionLabel(poll));
    }

}
