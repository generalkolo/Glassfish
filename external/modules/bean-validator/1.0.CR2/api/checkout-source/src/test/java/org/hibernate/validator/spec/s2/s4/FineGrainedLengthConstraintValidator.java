package org.hibernate.validator.spec.s2.s4;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidator;

/**
 * Check that a string length is between min and max
 * Error messages are using either key:
 *  - constraint.length.min if the min limit is reached
 *  - constraint.length.max if the max limit is reached
 */
public class FineGrainedLengthConstraintValidator implements ConstraintValidator<Length, String> {
    private int min;
    private int max;

    /**
     * Configure the constraint validator based on the elements
     * specified at the time it was defined.
     * @param constraint the constraint definition
     */
    public void initialize(Length constraint) {
        min = constraint.min();
        max = constraint.max();
    }

    /**
     * Validate a specified value.
     * returns false if the specified value does not conform to the definition
     * @exception IllegalArgumentException if the object is not of type String
     */
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if ( value == null ) return true;
         int length = value.length();

		//remove default error
		constraintValidatorContext.disableDefaultError();

		if (length < min) {
			//add min specific error
			constraintValidatorContext.addError( "{constraint.length.min}" );
			return false;
		}
		if (length > max) {
			//add max specific error
			constraintValidatorContext.addError( "{constraint.length.max}" );
			return false;
		}
		return true;
    }
}