package com.bookstore.dto;

import lombok.Data;

public class UserDto {

    @Data
    public static class Profile {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private Integer giftPoints;
    }
}
