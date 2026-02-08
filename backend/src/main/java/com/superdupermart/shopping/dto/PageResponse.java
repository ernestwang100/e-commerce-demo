package com.superdupermart.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number; // Current page number (0-indexed or 1-indexed, usually 0 in Spring Data but we
                        // use 1-based in Controller?)
                        // Let's stick to what the Controller receives.
}
