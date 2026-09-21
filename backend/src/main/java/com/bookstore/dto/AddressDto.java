package com.bookstore.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

public class AddressDto {

    @Data
    public static class AddressRequest {
        @NotBlank private String fullName;
        @NotBlank private String line1;
        private String line2;
        @NotBlank private String city;
        @NotBlank private String postcode;
        @NotBlank private String country;
    }

    @Data
    public static class AddressResponse {
        private Long id;
        private String fullName;
        private String line1;
        private String line2;
        private String city;
        private String postcode;
        private String country;
    }
}
