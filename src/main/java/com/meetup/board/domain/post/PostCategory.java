package com.meetup.board.domain.post;

public enum PostCategory {
    STUDY("스터디"),
    CERTIFICATE("자격증"),
    SIDE_PROJECT("사이드 프로젝트");

    private final String displayName;

    PostCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
