package com.domoticore.teammanagement.application;

import java.util.List;

public record TeamAccessContext(
        Long ownerUserId,
        String segment,
        boolean teamMember,
        List<String> zones,
        String teamRole) {

    public static TeamAccessContext own(Long userId, String segment) {
        return new TeamAccessContext(userId, segment, false, List.of("global"), "owner");
    }

    public boolean canWrite() {
        return !teamMember || !"viewer".equalsIgnoreCase(teamRole);
    }
}
