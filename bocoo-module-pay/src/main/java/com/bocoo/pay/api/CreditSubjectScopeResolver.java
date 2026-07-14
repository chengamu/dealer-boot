package com.bocoo.pay.api;

import com.bocoo.pay.domain.credit.CreditSubject;

/** Implemented by the sales module for the current merchant or internal store. */
public interface CreditSubjectScopeResolver {
    CreditSubject current(String currency);
}
