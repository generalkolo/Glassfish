/*******************************************************************************
 * Copyright (c) 2006, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation
 *
 ******************************************************************************/
package org.eclipse.persistence.jpa.internal.jpql.parser;

/**
 * The <b>FROM</b> clause of a query defines the domain of the query by declaring
 * identification variables. An identification variable is an identifier
 * declared in the <b>FROM</b> clause of a query. The domain of the query may be
 * constrained by path expressions. Identification variables designate instances
 * of a particular entity abstract schema type. The <b>FROM</b> clause can
 * contain multiple identification variable declarations separated by a comma
 * (,).
 *
 * <div nowrap><b>BNF:</b> <code>subquery_from_clause ::= FROM subselect_identification_variable_declaration {, subselect_identification_variable_declaration}*</code><p>
 *
 * @version 2.3
 * @since 2.3
 * @author Pascal Filion
 */
public final class SimpleFromClause extends AbstractFromClause {

	/**
	 * Creates a new <code>SimpleFromClause</code>.
	 *
	 * @param parent The parent of this expression
	 */
	SimpleFromClause(AbstractExpression parent) {
		super(parent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void accept(ExpressionVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JPQLQueryBNF declarationBNF() {
		return queryBNF(InternalSimpleFromClauseBNF.ID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JPQLQueryBNF getQueryBNF() {
		return queryBNF(SubQueryFromClauseBNF.ID);
	}
}