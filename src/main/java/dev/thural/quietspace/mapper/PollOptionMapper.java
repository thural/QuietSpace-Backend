package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.PollOption;
import dev.thural.quietspace.model.response.OptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PollOptionMapper {

    @Mapping(target = "pollId", source ="poll.id")
    @Mapping(target = "voteShare", expression = "java(getVoteShare(option))")
    OptionResponse pollOptionEntityToResponse(PollOption option);

    default String getVoteShare(PollOption option){
        // TODO: get vote counts using repository
        return "0%";
    }

}
