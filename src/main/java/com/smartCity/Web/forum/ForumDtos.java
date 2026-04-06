package com.smartCity.Web.forum;

public final class ForumDtos {

    private ForumDtos() {
    }

    public record ForumPostRequest(String title, String content) {
    }

    public record ForumPostResponse(Long id, String title, String content) {
    }
}
