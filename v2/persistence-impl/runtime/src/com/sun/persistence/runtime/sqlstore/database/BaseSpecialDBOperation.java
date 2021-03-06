/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

package com.sun.persistence.runtime.sqlstore.database;

import java.util.List;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

import com.sun.persistence.runtime.sqlstore.database.SpecialDBOperation;

/**
 * BaseSpecialDBOperation is the base class for all classes implementing
 * DBSpecificOperation.
 * @author Shing Wai Chan
 */
public class BaseSpecialDBOperation implements SpecialDBOperation {
    /**
     * @inheritDoc
     */
    public void initialize(DatabaseMetaData metaData,
        String identifier) throws SQLException {
    }

    /**
     * @inheritDoc
     */
    public void defineColumnTypeForResult(
        PreparedStatement ps, List columns) throws SQLException {
    }

    /**
     * @inheritDoc
     */
    public void bindFixedCharColumn(PreparedStatement ps,
         int index, String strVal, int length) throws SQLException {
         ps.setString(index, strVal);
    }

}
