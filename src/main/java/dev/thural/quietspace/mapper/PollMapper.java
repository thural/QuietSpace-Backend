package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.Poll;
import dev.thural.quietspace.entity.PollOption;
import dev.thural.quietspace.model.request.PollRequest;
import dev.thural.quietspace.model.response.OptionResponse;
import dev.thural.quietspace.model.response.PollResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;


@Mapper
public interface PollMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "options", ignore = true)
    Poll pollRequestToEntity(PollRequest pollRequest);

    @Mapping(target = "postId", source ="post.id")
    @Mapping(target = "voteCount", expression="java(voteCounter(poll))")
    @Mapping(target = "votedOption", expression = "java(getVotedOption(poll))")
    PollResponse pollEntityToResponse(Poll poll);

    default OptionResponse optionEntityToResponse (PollOption option){
        return Mappers.getMapper(PollOptionMapper.class).pollOptionEntityToResponse(option);
    }

    default Integer voteCounterUsingRepo(@MappingTarget Poll poll){
        // TODO: count total votes using repository
        return null;
    }

    default String getVotedOption(Poll poll){
        // TODO: get voted option by user from service layer
        return "default";
    }

    default Integer voteCounter(Poll poll){
        return poll.getOptions().stream()
                .map(option -> option.getVotes().size())
                .reduce(0, Integer::sum);
    }

}
