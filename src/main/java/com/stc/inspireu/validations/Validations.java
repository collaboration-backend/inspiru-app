package com.stc.inspireu.validations;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class to define common validation logics
 */
public class Validations {

    private static final Map<String, GlobalMobileNumberValidation> MOBILE_NUMBER_VALIDATION_HASH_MAP = new HashMap<>();

    static {
        MOBILE_NUMBER_VALIDATION_HASH_MAP.put("+91", new GlobalMobileNumberValidation(10, 10));
        MOBILE_NUMBER_VALIDATION_HASH_MAP.put("+966", new GlobalMobileNumberValidation(9, 9));
    }

    public static GlobalMobileNumberValidation findValidationForISD(String isdCode) {
        return MOBILE_NUMBER_VALIDATION_HASH_MAP.get(isdCode);
    }

    /**
     * If returned value is an empty string => mobile number is valid, else error message will be returned
     *
     * @param isdCode
     * @param mobileNumber
     * @return
     */
    public static String validate(String isdCode, String mobileNumber) {
        if (Objects.isNull(isdCode) || isdCode.isEmpty())
            return "ISD Code must not be empty";
        if (Objects.isNull(mobileNumber) || mobileNumber.isEmpty())
            return "Mobile number must not be empty";
        mobileNumber = mobileNumber.trim();
        GlobalMobileNumberValidation validation = MOBILE_NUMBER_VALIDATION_HASH_MAP.get(isdCode);
        if (Objects.isNull(validation))
            validation = new GlobalMobileNumberValidation(5, 15);
        int mobileNumberCharLength = mobileNumber.length();
        if (Objects.equals(validation.getMinLength(), validation.getMaxLength())) {
            if (mobileNumberCharLength != validation.getMaxLength())
                return "Mobile number length should be " + validation.getMaxLength();
        } else {
            if (mobileNumberCharLength < validation.getMinLength())
                return "Mobile number length should be greater than or equal to " + validation.getMinLength();
            else if (mobileNumberCharLength > validation.getMaxLength())
                return "Mobile number length should be less than or equal to " + validation.getMaxLength();
        }
        return "";
    }
}
