// $Id: SizeValidatorForArraysOfPrimitives.java 17169 2009-07-20 13:57:49Z hardy.ferentschik $
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validation.constraints.impl;

import java.lang.reflect.Array;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;
import javax.validation.constraints.Size;

/**
 * Check that the length of an array is betweeb <i>min</i> and <i>max</i>
 *
 * @author Hardy Ferentschik
 */
public abstract class SizeValidatorForArraysOfPrimitives {
	protected int min;
	protected int max;

	public void initialize(Size parameters) {
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	private void validateParameters() {
		if ( min < 0 ) {
			throw new ValidationException( "The min parameter cannot be negative." );
		}
		if ( max < 0 ) {
			throw new ValidationException( "The max paramter cannot be negative." );
		}
		if ( max < min ) {
			throw new ValidationException( "The length cannot be negative." );
		}
	}
}