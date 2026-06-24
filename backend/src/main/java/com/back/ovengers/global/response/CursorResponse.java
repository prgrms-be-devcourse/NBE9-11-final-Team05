package com.back.ovengers.global.response;

import java.util.List;

public record CursorResponse<T> (
        List<T> content,
        Long nextCursor,
        boolean hasNext
) {}
