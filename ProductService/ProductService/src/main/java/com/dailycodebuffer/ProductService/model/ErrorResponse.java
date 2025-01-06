package com.dailycodebuffer.ProductService.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String errorMessage;
    private String errorCode;
}
