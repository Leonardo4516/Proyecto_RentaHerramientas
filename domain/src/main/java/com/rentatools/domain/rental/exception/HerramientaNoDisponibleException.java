package com.rentatools.domain.rental.exception;

import com.rentatools.domain.common.exception.BusinessException;

public class HerramientaNoDisponibleException extends BusinessException {
    public HerramientaNoDisponibleException(String message) {
        super(message);
    }
}
