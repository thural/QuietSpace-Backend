package dev.thural.quietspace.mapper;

import dev.thural.quietspace.entity.PollOption;
import dev.thural.quietspace.model.response.OptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PollOptionMapper {

    @Mapping(target = "voteShare", expression = "java(getVoteShare(option))")
    OptionResponse pollOptionEntityToResponse(PollOption option);

    default String getVoteShare(PollOption option){
        Integer totalVoteNum = option.getPoll().getOptions().stream()
                .map(pollOption -> pollOption.getVotes().size())
                .reduce(0,Integer::sum);

        Integer optionVoteNum = option.getVotes().size();

        if (totalVoteNum < 1) return "0%";

        return optionVoteNum/totalVoteNum +  "%";
    }

}
