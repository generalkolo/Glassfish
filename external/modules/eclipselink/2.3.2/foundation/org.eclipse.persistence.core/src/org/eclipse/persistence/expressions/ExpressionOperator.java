/*******************************************************************************
 * Copyright (c) 1998, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 *     Markus Karg - allow arguments to be specified multiple times in argumentIndices
 *     05/07/2009-1.1.1 Dave Brosius 
 *       - 263904: [PATCH] ExpressionOperator doesn't compare arrays correctly
 ******************************************************************************/  
package org.eclipse.persistence.expressions;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.*;
import java.io.*;
import org.eclipse.persistence.internal.expressions.*;
import org.eclipse.persistence.internal.helper.*;
import org.eclipse.persistence.exceptions.*;
import org.eclipse.persistence.internal.helper.ClassConstants;
import org.eclipse.persistence.internal.security.PrivilegedAccessHelper;
import org.eclipse.persistence.internal.security.PrivilegedNewInstanceFromClass;

/**
 * <p>
 * <b>Purpose</b>: ADVANCED: The expression operator is used internally to define SQL operations and functions.
 * It is possible for an advanced user to define their own operators.
 */
public class ExpressionOperator implements Serializable {

    /** Required for serialization compatibility. */
    static final long serialVersionUID = -7066100204792043980L;
    protected int selector;
    protected String[] databaseStrings;
    protected boolean isPrefix = false;
    protected boolean isRepeating = false;
    protected Class nodeClass;
    protected int type;
    protected int[] argumentIndices = null;
    protected static Map allOperators = initializeOperators();
    protected static Map platformOperatorNames = initializePlatformOperatorNames();
    protected String[] javaStrings;
    /** Allow operator to disable binding. */
    protected boolean isBindingSupported = true;

    /** Operator types */
    public static final int LogicalOperator = 1;
    public static final int ComparisonOperator = 2;
    public static final int AggregateOperator = 3;
    public static final int OrderOperator = 4;
    public static final int FunctionOperator = 5;

    /** Logical operators */
    public static final int And = 1;
    public static final int Or = 2;
    public static final int Not = 3;

    /** Comparison operators */
    public static final int Equal = 4;
    public static final int NotEqual = 5;
    public static final int EqualOuterJoin = 6;
    public static final int LessThan = 7;
    public static final int LessThanEqual = 8;
    public static final int GreaterThan = 9;
    public static final int GreaterThanEqual = 10;
    public static final int Like = 11;
    public static final int NotLike = 12;
    public static final int In = 13;
    public static final int InSubQuery = 129;
    public static final int NotIn = 14;
    public static final int NotInSubQuery = 130;
    public static final int Between = 15;
    public static final int NotBetween = 16;
    public static final int IsNull = 17;
    public static final int NotNull = 18;
    public static final int Exists = 86;
    public static final int NotExists = 88;
    public static final int LikeEscape = 89;
    public static final int NotLikeEscape = 134; 
    public static final int Decode = 105;
    public static final int Case = 117;
    public static final int NullIf = 131;
    public static final int Coalesce = 132;
    public static final int CaseCondition = 136;

    /** Aggregate operators */
    public static final int Count = 19;
    public static final int Sum = 20;
    public static final int Average = 21;
    public static final int Maximum = 22;
    public static final int Minimum = 23;
    public static final int StandardDeviation = 24;
    public static final int Variance = 25;
    public static final int Distinct = 87;

    /** Ordering operators */
    public static final int Ascending = 26;
    public static final int Descending = 27;

    /** Function operators */

    // General
    public static final int ToUpperCase = 28;
    public static final int ToLowerCase = 29;
    public static final int Chr = 30;
    public static final int Concat = 31;
    public static final int HexToRaw = 32;
    public static final int Initcap = 33;
    public static final int Instring = 34;
    public static final int Soundex = 35;
    public static final int LeftPad = 36;
    public static final int LeftTrim = 37;
    public static final int Replace = 38;
    public static final int RightPad = 39;
    public static final int RightTrim = 40;
    public static final int Substring = 41;
    public static final int ToNumber = 42;
    public static final int Translate = 43;
    public static final int Trim = 44;
    public static final int Ascii = 45;
    public static final int Length = 46;
    public static final int CharIndex = 96;
    public static final int CharLength = 97;
    public static final int Difference = 98;
    public static final int Reverse = 99;
    public static final int Replicate = 100;
    public static final int Right = 101;
    public static final int Locate = 112;
    public static final int Locate2 = 113;
    public static final int ToChar = 114;
    public static final int ToCharWithFormat = 115;
    public static final int RightTrim2 = 116;
    public static final int Any = 118;
    public static final int Some = 119;
    public static final int All = 120;
    public static final int Trim2 = 121;
    public static final int LeftTrim2 = 122;
    public static final int SubstringSingleArg = 133;

    // Date
    public static final int AddMonths = 47;
    public static final int DateToString = 48;
    public static final int LastDay = 49;
    public static final int MonthsBetween = 50;
    public static final int NextDay = 51;
    public static final int RoundDate = 52;
    public static final int ToDate = 53;
    /**
     * Function to obtain the current timestamp on the database including date
     * and time components. This corresponds to the JPQL function
     * current_timestamp.
     */
    public static final int Today = 54;
    public static final int AddDate = 90;
    public static final int DateName = 92;
    public static final int DatePart = 93;
    public static final int DateDifference = 94;
    public static final int TruncateDate = 102;
    public static final int NewTime = 103;
    public static final int Nvl = 104;
    /**
     * Function to obtain the current date on the database with date components
     * only but without time components. This corresponds to the JPQL function
     * current_date.
     */
    public static final int CurrentDate = 123;
    /**
     * Function to obtain the current time on the database with time components
     * only but without date components. This corresponds to the JPQL function
     * current_time.
     */
    public static final int CurrentTime = 128;

    // Math
    public static final int Ceil = 55;
    public static final int Cos = 56;
    public static final int Cosh = 57;
    public static final int Abs = 58;
    public static final int Acos = 59;
    public static final int Asin = 60;
    public static final int Atan = 61;
    public static final int Exp = 62;
    public static final int Sqrt = 63;
    public static final int Floor = 64;
    public static final int Ln = 65;
    public static final int Log = 66;
    public static final int Mod = 67;
    public static final int Power = 68;
    public static final int Round = 69;
    public static final int Sign = 70;
    public static final int Sin = 71;
    public static final int Sinh = 72;
    public static final int Tan = 73;
    public static final int Tanh = 74;
    public static final int Trunc = 75;
    public static final int Greatest = 76;
    public static final int Least = 77;
    public static final int Add = 78;
    public static final int Subtract = 79;
    public static final int Divide = 80;
    public static final int Multiply = 81;
    public static final int Atan2 = 91;
    public static final int Cot = 95;
    public static final int Negate = 135;

    // Object-relational
    public static final int Deref = 82;
    public static final int Ref = 83;
    public static final int RefToHex = 84;
    public static final int Value = 85;

    //XML Specific
    public static final int Extract = 106;
    public static final int ExtractValue = 107;
    public static final int ExistsNode = 108;
    public static final int GetStringVal = 109;
    public static final int GetNumberVal = 110;
    public static final int IsFragment = 111;
    
    // Spatial
    public static final int SDO_WITHIN_DISTANCE = 124;
    public static final int SDO_RELATE = 125;
    public static final int SDO_FILTER = 126;
    public static final int SDO_NN = 127;

    /**
     * ADVANCED:
     * Create a new operator.
     */
    public ExpressionOperator() {
        this.type = FunctionOperator;
        // For bug 2780072 provide default behavior to make this class more useable.
        setNodeClass(ClassConstants.FunctionExpression_Class);
    }

    /**
     * ADVANCED:
     * Create a new operator with the given name(s) and strings to print.
     */
    public ExpressionOperator(int selector, Vector newDatabaseStrings) {
        this.type = FunctionOperator;
        // For bug 2780072 provide default behavior to make this class more useable.
        setNodeClass(ClassConstants.FunctionExpression_Class);
        this.selector = selector;
        this.printsAs(newDatabaseStrings);
    }

    /**
     * PUBLIC:
     * Return if binding is compatible with this operator.
     */
    public boolean isBindingSupported() {
        return isBindingSupported;
    }

    /**
     * PUBLIC:
     * Set if binding is compatible with this operator.
     * Some databases do not allow binding, or require casting with certain operators.
     */
    public void setIsBindingSupported(boolean isBindingSupported) {
        this.isBindingSupported = isBindingSupported;
    }
    
    /**
     * INTERNAL:
     * Return if the operator is equal to the other.
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if ((object == null) || (getClass() != object.getClass())) {
            return false;
        }
        ExpressionOperator operator = (ExpressionOperator) object;
        if (getSelector() == 0) {
            return Arrays.equals(getDatabaseStrings(), operator.getDatabaseStrings());
        } else {
            return getSelector() == operator.getSelector();
        }
    }

    /**
     * INTERNAL:
     * Return the hash-code based on the unique selector.
     */
    public int hashCode() {
        return getSelector();
    }
    
    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator abs() {
        return simpleFunction(Abs, "ABS");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator acos() {
        return simpleFunction(Acos, "ACOS");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator addDate() {
        ExpressionOperator exOperator = simpleThreeArgumentFunction(AddDate, "DATEADD");
        int[] indices = new int[3];
        indices[0] = 1;
        indices[1] = 2;
        indices[2] = 0;

        exOperator.setArgumentIndices(indices);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator addMonths() {
        return simpleTwoArgumentFunction(AddMonths, "ADD_MONTHS");
    }

    /**
     * ADVANCED:
     * Add an operator to the global list of operators.
     */
    public static void addOperator(ExpressionOperator exOperator) {
        allOperators.put(Integer.valueOf(exOperator.getSelector()), exOperator);
    }

    /**
     * INTERNAL:
     * Create the AND operator.
     */
    public static ExpressionOperator and() {
        return simpleLogical(And, "AND", "and");
    }

    /**
     * INTERNAL:
     * Apply this to an object in memory.
     * Throw an error if the function is not supported.
     */
    public Object applyFunction(Object source, Vector arguments) {
        if (source instanceof String) {
            if (this.selector == ToUpperCase) {
                return ((String)source).toUpperCase();
            } else if (this.selector == ToLowerCase) {
                return ((String)source).toLowerCase();
            } else if ((this.selector == Concat) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof String)) {
                return ((String)source).concat((String)arguments.elementAt(0));
            } else if ((this.selector == Substring) && (arguments.size() == 2) && (arguments.elementAt(0) instanceof Number) && (arguments.elementAt(1) instanceof Number)) {
                // assume the first parameter to be 1-based first index of the substring, the second - substring length.
                int beginIndexInclusive = ((Number)arguments.elementAt(0)).intValue() - 1;
                int endIndexExclusive = beginIndexInclusive +  ((Number)arguments.elementAt(1)).intValue();
                return ((String)source).substring(beginIndexInclusive, endIndexExclusive);
            } else if ((this.selector == SubstringSingleArg) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                int beginIndexInclusive = ((Number)arguments.elementAt(0)).intValue() - 1;
                int endIndexExclusive = ((String)source).length();
                return ((String)source).substring(beginIndexInclusive, endIndexExclusive);
            } else if (this.selector == ToNumber) {
                return new java.math.BigDecimal((String)source);
            } else if (this.selector == Trim) {
                return ((String)source).trim();
            } else if (this.selector == Length) {
                return Integer.valueOf(((String)source).length());
            }
        } else if (source instanceof Number) {
            if (this.selector == Ceil) {
                return Double.valueOf(Math.ceil(((Number)source).doubleValue()));
            } else if (this.selector == Cos) {
                return Double.valueOf(Math.cos(((Number)source).doubleValue()));
            } else if (this.selector == Abs) {
                return Double.valueOf(Math.abs(((Number)source).doubleValue()));
            } else if (this.selector == Acos) {
                return Double.valueOf(Math.acos(((Number)source).doubleValue()));
            } else if (this.selector == Asin) {
                return Double.valueOf(Math.asin(((Number)source).doubleValue()));
            } else if (this.selector == Atan) {
                return Double.valueOf(Math.atan(((Number)source).doubleValue()));
            } else if (this.selector == Exp) {
                return Double.valueOf(Math.exp(((Number)source).doubleValue()));
            } else if (this.selector == Sqrt) {
                return Double.valueOf(Math.sqrt(((Number)source).doubleValue()));
            } else if (this.selector == Floor) {
                return Double.valueOf(Math.floor(((Number)source).doubleValue()));
            } else if (this.selector == Log) {
                return Double.valueOf(Math.log(((Number)source).doubleValue()));
            } else if ((this.selector == Power) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return Double.valueOf(Math.pow(((Number)source).doubleValue(), (((Number)arguments.elementAt(0)).doubleValue())));
            } else if (this.selector == Round) {
                return Double.valueOf(Math.round(((Number)source).doubleValue()));
            } else if (this.selector == Sin) {
                return Double.valueOf(Math.sin(((Number)source).doubleValue()));
            } else if (this.selector == Tan) {
                return Double.valueOf(Math.tan(((Number)source).doubleValue()));
            } else if ((this.selector == Greatest) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return Double.valueOf(Math.max(((Number)source).doubleValue(), (((Number)arguments.elementAt(0)).doubleValue())));
            } else if ((this.selector == Least) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return Double.valueOf(Math.min(((Number)source).doubleValue(), (((Number)arguments.elementAt(0)).doubleValue())));
            } else if ((this.selector == Add) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return Double.valueOf(((Number)source).doubleValue() + (((Number)arguments.elementAt(0)).doubleValue()));
            } else if ((this.selector == Subtract) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return Double.valueOf(((Number)source).doubleValue() - (((Number)arguments.elementAt(0)).doubleValue()));
            } else if ((this.selector == Divide) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return Double.valueOf(((Number)source).doubleValue() / (((Number)arguments.elementAt(0)).doubleValue()));
            } else if ((this.selector == Multiply) && (arguments.size() == 1) && (arguments.elementAt(0) instanceof Number)) {
                return Double.valueOf(((Number)source).doubleValue() * (((Number)arguments.elementAt(0)).doubleValue()));
            }
        }

        throw QueryException.cannotConformExpression();
    }

    /**
     * INTERNAL:
     * Create the ASCENDING operator.
     */
    public static ExpressionOperator ascending() {
        return simpleOrdering(Ascending, "ASC", "ascending");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator ascii() {
        return simpleFunction(Ascii, "ASCII");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator asin() {
        return simpleFunction(Asin, "ASIN");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator atan() {
        return simpleFunction(Atan, "ATAN");
    }

    /**
     * INTERNAL:
     * Create the AVERAGE operator.
     */
    public static ExpressionOperator average() {
        return simpleAggregate(Average, "AVG", "average");
    }

    /**
     * ADVANCED:
     * Tell the operator to be postfix, i.e. its strings start printing after
     * those of its first argument.
     */
    public void bePostfix() {
        isPrefix = false;
    }

    /**
     * ADVANCED:
     * Tell the operator to be pretfix, i.e. its strings start printing before
     * those of its first argument.
     */
    public void bePrefix() {
        isPrefix = true;
    }

    /**
     * INTERNAL:
     * Make this a repeating argument. Currently unused.
     */
    public void beRepeating() {
        isRepeating = true;
    }

    /**
     * INTERNAL:
     * Create the BETWEEN Operator
     */
    public static ExpressionOperator between() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(Between);
        result.setType(ComparisonOperator);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("(");
        v.addElement(" BETWEEN ");
        v.addElement(" AND ");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Build operator.
     * Note: This operator works differently from other operators.
     * @see Expression#caseStatement(Map, String)
     */
    public static ExpressionOperator caseStatement() {
        ListExpressionOperator exOperator = new ListExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Case);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.ArgumentListFunctionExpression_Class);
        exOperator.setIsBindingSupported(false);
        exOperator.setStartString("CASE ");
        exOperator.setSeparators(new String[]{" WHEN ", " THEN "});
        exOperator.setTerminationStrings(new String[]{" ELSE ", " END"});
        return exOperator;
    }
    

    /**
     * INTERNAL:
     * Build operator.
     * Note: This operator works differently from other operators.
     * @see Expression#caseStatement(Map, String)
     */
    public static ExpressionOperator caseConditionStatement() {
        ListExpressionOperator exOperator = new ListExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(CaseCondition);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.ArgumentListFunctionExpression_Class);
        exOperator.setIsBindingSupported(false);
        exOperator.setStartStrings(new String[]{"CASE WHEN ", " THEN "});
        exOperator.setSeparators(new String[]{" WHEN ", " THEN "});
        exOperator.setTerminationStrings(new String[]{" ELSE ", " END "});
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator ceil() {
        return simpleFunction(Ceil, "CEIL");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator charIndex() {
        return simpleTwoArgumentFunction(CharIndex, "CHARINDEX");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator charLength() {
        return simpleFunction(CharLength, "CHAR_LENGTH");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator chr() {
        return simpleFunction(Chr, "CHR");
    }

    /**
     * INTERNAL:
     * Build operator.
     * Note: This operator works differently from other operators.
     * @see Expression#caseStatement(Map, String)
     */
    public static ExpressionOperator coalesce() {
        ListExpressionOperator exOperator = new ListExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Coalesce);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.ArgumentListFunctionExpression_Class);
        exOperator.setStartString("COALESCE(");
        exOperator.setSeparator(",");
        exOperator.setTerminationString(" )");
        return exOperator;
    }
    
    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator concat() {
        ExpressionOperator operator = simpleMath(Concat, "+");
        operator.setIsBindingSupported(false);
        return operator;
    }

    /**
     * INTERNAL:
     * Compare between in memory.
     */
    public boolean conformBetween(Object left, Object right) {
        Object start = ((Vector)right).elementAt(0);
        Object end = ((Vector)right).elementAt(1);
        if ((left == null) || (start == null) || (end == null)) {
            return false;
        }
        if ((left instanceof Number) && (start instanceof Number) && (end instanceof Number)) {
            return ((((Number)left).doubleValue()) >= (((Number)start).doubleValue())) && ((((Number)left).doubleValue()) <= (((Number)end).doubleValue()));
        } else if ((left instanceof String) && (start instanceof String) && (end instanceof String)) {
            return ((((String)left).compareTo(((String)start)) > 0) || (((String)left).compareTo(((String)start)) == 0)) && ((((String)left).compareTo(((String)end)) < 0) || (((String)left).compareTo(((String)end)) == 0));
        } else if ((left instanceof java.util.Date) && (start instanceof java.util.Date) && (end instanceof java.util.Date)) {
            return (((java.util.Date)left).after(((java.util.Date)start)) || ((java.util.Date)left).equals((start))) && (((java.util.Date)left).before(((java.util.Date)end)) || ((java.util.Date)left).equals((end)));
        } else if ((left instanceof java.util.Calendar) && (start instanceof java.util.Calendar) && (end instanceof java.util.Calendar)) {
            return (((java.util.Calendar)left).after(start) || ((java.util.Calendar)left).equals((start))) && (((java.util.Calendar)left).before(end) || ((java.util.Calendar)left).equals((end)));
        }

        throw QueryException.cannotConformExpression();
    }

    /**
     * INTERNAL:
     * Compare like in memory.
     * This only works for % not _.
     * @author Christian Weeks aka ChristianLink
     */
    public boolean conformLike(Object left, Object right) {
        if ((right == null) && (left == null)) {
            return true;
        }
        if (!(right instanceof String) || !(left instanceof String)) {
            throw QueryException.cannotConformExpression();
        }
        String likeString = (String)right;
        if (likeString.indexOf("_") != -1) {
            throw QueryException.cannotConformExpression();
        }
        String value = (String)left;
        if (likeString.indexOf("%") == -1) {
            // No % symbols
            return left.equals(right);
        }
        boolean strictStart = !likeString.startsWith("%");
        boolean strictEnd = !likeString.endsWith("%");
        StringTokenizer tokens = new StringTokenizer(likeString, "%");
        int lastPosition = 0;
        String lastToken = null;
        if (strictStart) {
            lastToken = tokens.nextToken();
            if (!value.startsWith(lastToken)) {
                return false;
            }
        }
        while (tokens.hasMoreTokens()) {
            lastToken = tokens.nextToken();
            lastPosition = value.indexOf(lastToken, lastPosition);
            if (lastPosition < 0) {
                return false;
            }
        }
        if (strictEnd) {
            return value.endsWith(lastToken);
        }
        return true;
    }

    public void copyTo(ExpressionOperator operator){
        operator.selector = selector;
        operator.isPrefix = isPrefix;
        operator.isRepeating = isRepeating;
        operator.nodeClass = nodeClass;
        operator.type = type;
        operator.databaseStrings = databaseStrings == null ? null : Helper.copyStringArray(databaseStrings);
        operator.argumentIndices = argumentIndices == null ? null : Helper.copyIntArray(argumentIndices);
        operator.javaStrings = javaStrings == null ? null : Helper.copyStringArray(javaStrings);
        operator.isBindingSupported = isBindingSupported;
    }
    
    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator cos() {
        return simpleFunction(Cos, "COS");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator cosh() {
        return simpleFunction(Cosh, "COSH");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator cot() {
        return simpleFunction(Cot, "COT");
    }

    /**
     * INTERNAL:
     * Create the COUNT operator.
     */
    public static ExpressionOperator count() {
        return simpleAggregate(Count, "COUNT", "count");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator dateDifference() {
        return simpleThreeArgumentFunction(DateDifference, "DATEDIFF");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator dateName() {
        return simpleTwoArgumentFunction(DateName, "DATENAME");
    }

    /**
     * INTERNAL:
     * Oracle equivalent to DATENAME is TO_CHAR.
     */
    public static ExpressionOperator oracleDateName() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(DateName);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(3);
        v.addElement("TO_CHAR(");
        v.addElement(", '");
        v.addElement("')");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        int[] indices = { 1, 0 };
        exOperator.setArgumentIndices(indices);
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator datePart() {
        return simpleTwoArgumentFunction(DatePart, "DATEPART");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator dateToString() {
        return simpleFunction(DateToString, "TO_CHAR");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator toChar() {
        return simpleFunction(ToChar, "TO_CHAR");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator toCharWithFormat() {
        return simpleTwoArgumentFunction(ToCharWithFormat, "TO_CHAR");
    }

    /**
     * INTERNAL:
     * Build operator.
     * Note: This operator works differently from other operators.
     * @see Expression#decode(Map, String)
     */
    public static ExpressionOperator decode() {
        ExpressionOperator exOperator = new ExpressionOperator();

        exOperator.setSelector(Decode);

        exOperator.setNodeClass(FunctionExpression.class);
        exOperator.setType(FunctionOperator);
        exOperator.bePrefix();
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator deref() {
        return simpleFunction(Deref, "DEREF");
    }

    /**
     * INTERNAL:
     * Create the DESCENDING operator.
     */
    public static ExpressionOperator descending() {
        return simpleOrdering(Descending, "DESC", "descending");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator difference() {
        return simpleTwoArgumentFunction(Difference, "DIFFERENCE");
    }

    /**
     * INTERNAL:
     * Create the DISTINCT operator.
     */
    public static ExpressionOperator distinct() {
        return simpleFunction(Distinct, "DISTINCT", "distinct");
    }

    /**
     * INTERNAL:
     * Compare the values in memory.
     * Used for in-memory querying, all operators are not support.
     */
    public boolean doesRelationConform(Object left, Object right) {
        // Big ugly case statement follows.
        // Java is really verbose, the Smalltalk equivalent to this... left perform: self selector with: right
        // Note, compareTo for String returns a number <= -1 if the String is less than.  We assumed that
        // it would return -1.  The same thing for strings that are greater than (ie it returns >= 1). PWK
        // Equals

        if (this.selector == Equal || this.selector == NotEqual) {
            if ((left == null) && (right == null)) {
                return  this.selector == Equal;
            } else if ((left == null) || (right == null)) {
                return this.selector == NotEqual;
            }
            if (left instanceof Number && left instanceof Comparable && right instanceof Comparable
                && left.getClass().equals (right.getClass())) {
                return (((Comparable) left).compareTo( (Comparable) right) == 0) == (this.selector == Equal);
            }
            if (((left instanceof Number) && (right instanceof Number)) && (left.getClass() != right.getClass())) {
                double leftDouble = ((Number)left).doubleValue();
                double rightDouble = ((Number)right).doubleValue();
                if (Double.isNaN(leftDouble) && Double.isNaN(rightDouble)){
                    return this.selector == Equal;
                }
                return (leftDouble == rightDouble) == (this.selector == Equal);
            }
            return left.equals( right) == (this.selector == Equal);
        } else if (this.selector == IsNull) {
            return (left == null);
        }
        if (this.selector == NotNull) {
            return (left != null);
        }
        // Less thans, greater thans
        else if (this.selector == LessThan) {// You have got to love polymorphism in Java, NOT!!!
            if ((left == null) || (right == null)) {
                return false;
            }
            if ((left instanceof Number) && (right instanceof Number)) {
                return (((Number)left).doubleValue()) < (((Number)right).doubleValue());
            } else if ((left instanceof String) && (right instanceof String)) {
                return ((String)left).compareTo(((String)right)) < 0;
            } else if ((left instanceof java.util.Date) && (right instanceof java.util.Date)) {
                return ((java.util.Date)left).before(((java.util.Date)right));
            } else if ((left instanceof java.util.Calendar) && (right instanceof java.util.Calendar)) {
                return ((java.util.Calendar)left).before(right);
            }
        } else if (this.selector == LessThanEqual) {
            if ((left == null) && (right == null)) {
                return true;
            } else if ((left == null) || (right == null)) {
                return false;
            }
            if ((left instanceof Number) && (right instanceof Number)) {
                return (((Number)left).doubleValue()) <= (((Number)right).doubleValue());
            } else if ((left instanceof String) && (right instanceof String)) {
                int compareValue = ((String)left).compareTo(((String)right));
                return (compareValue < 0) || (compareValue == 0);
            } else if ((left instanceof java.util.Date) && (right instanceof java.util.Date)) {
                return ((java.util.Date)left).equals((right)) || ((java.util.Date)left).before(((java.util.Date)right));
            } else if ((left instanceof java.util.Calendar) && (right instanceof java.util.Calendar)) {
                return ((java.util.Calendar)left).equals((right)) || ((java.util.Calendar)left).before(right);
            }
        } else if (this.selector == GreaterThan) {
            if ((left == null) || (right == null)) {
                return false;
            }
            if ((left instanceof Number) && (right instanceof Number)) {
                return (((Number)left).doubleValue()) > (((Number)right).doubleValue());
            } else if ((left instanceof String) && (right instanceof String)) {
                int compareValue = ((String)left).compareTo(((String)right));
                return (compareValue > 0);
            } else if ((left instanceof java.util.Date) && (right instanceof java.util.Date)) {
                return ((java.util.Date)left).after(((java.util.Date)right));
            } else if ((left instanceof java.util.Calendar) && (right instanceof java.util.Calendar)) {
                return ((java.util.Calendar)left).after(right);
            }
        } else if (this.selector == GreaterThanEqual) {
            if ((left == null) && (right == null)) {
                return true;
            } else if ((left == null) || (right == null)) {
                return false;
            }
            if ((left instanceof Number) && (right instanceof Number)) {
                return (((Number)left).doubleValue()) >= (((Number)right).doubleValue());
            } else if ((left instanceof String) && (right instanceof String)) {
                int compareValue = ((String)left).compareTo(((String)right));
                return (compareValue > 0) || (compareValue == 0);
            } else if ((left instanceof java.util.Date) && (right instanceof java.util.Date)) {
                return ((java.util.Date)left).equals((right)) || ((java.util.Date)left).after(((java.util.Date)right));
            } else if ((left instanceof java.util.Calendar) && (right instanceof java.util.Calendar)) {
                return ((java.util.Calendar)left).equals((right)) || ((java.util.Calendar)left).after(right);
            }
        }
        // Between
        else if ((this.selector == Between) && (right instanceof Vector) && (((Vector)right).size() == 2)) {
            return conformBetween(left, right);
        } else if ((this.selector == NotBetween) && (right instanceof Vector) && (((Vector)right).size() == 2)) {
            return !conformBetween(left, right);
        }
        // In
        else if ((this.selector == In) && (right instanceof Collection)) {
            return ((Collection)right).contains(left);
        } else if ((this.selector == NotIn) && (right instanceof Collection)) {
            return !((Collection)right).contains(left);
        }
        // Like
        //conformLike(left, right);
        else if (((this.selector == Like) || (this.selector == NotLike)) && (right instanceof Vector) && (((Vector)right).size() == 1)) {
            Boolean doesLikeConform = JavaPlatform.conformLike(left, ((Vector)right).get(0));
            if (doesLikeConform != null) {
                if (doesLikeConform.booleanValue()) {
                    return this.selector == Like;// Negate for NotLike
                } else {
                    return this.selector != Like;// Negate for NotLike
                }
            }
        }

        throw QueryException.cannotConformExpression();
    }

    /**
     * INTERNAL:
     * Initialize the outer join operator
     * Note: This is merely a shell which is incomplete, and
     * so will be replaced by the platform's operator when we
     * go to print. We need to create this here so that the expression
     * class is correct, normally it assumes functions for unknown operators.
     */
    public static ExpressionOperator equalOuterJoin() {
        return simpleRelation(EqualOuterJoin, "=*");
    }

    /**
     * INTERNAL:
     * Create the EXISTS operator.
     */
    public static ExpressionOperator exists() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Exists);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("EXISTS" + " ");
        v.addElement(" ");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator exp() {
        return simpleFunction(Exp, "EXP");
    }

    /**
     * INTERNAL:
     * Create an expression for this operator, using the given base.
     */
    public Expression expressionFor(Expression base) {
        return expressionForArguments(base, org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(0));
    }

    /**
     * INTERNAL:
     * Create an expression for this operator, using the given base and a single argument.
     */
    public Expression expressionFor(Expression base, Object value) {
        return newExpressionForArgument(base, value);
    }

    /**
     * INTERNAL:
     * Create an expression for this operator, using the given base and a single argument.
     * Base is used last in the expression
     */
    public Expression expressionForWithBaseLast(Expression base, Object value) {
        return newExpressionForArgumentWithBaseLast(base, value);
    }

    /**
     * INTERNAL:
     * Create an expression for this operator, using the given base and arguments.
     */
    public Expression expressionForArguments(Expression base, Vector arguments) {
        return newExpressionForArguments(base, arguments);
    }

    /**
     * INTERNAL:
     * Create the extract expression operator
     */
    public static ExpressionOperator extract() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("extract(");
        v.addElement(",");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setSelector(Extract);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Create the extractValue expression operator
     */
    public static ExpressionOperator extractValue() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("extractValue(");
        v.addElement(",");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setSelector(ExtractValue);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Create the existsNode expression operator
     */
    public static ExpressionOperator existsNode() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("existsNode(");
        v.addElement(",");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.setSelector(ExistsNode);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    public static ExpressionOperator getStringVal() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement(".getStringVal()");
        result.printsAs(v);
        result.bePostfix();
        result.setSelector(GetStringVal);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    public static ExpressionOperator getNumberVal() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement(".getNumberVal()");
        result.printsAs(v);
        result.bePostfix();
        result.setSelector(GetNumberVal);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    public static ExpressionOperator isFragment() {
        ExpressionOperator result = new ExpressionOperator();
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement(".isFragment()");
        result.printsAs(v);
        result.bePostfix();
        result.setSelector(IsFragment);
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator floor() {
        return simpleFunction(Floor, "FLOOR");
    }

    /**
     * ADVANCED:
     * Return the hashtable of all operators.
     */
    public static Map getAllOperators() {
        return allOperators;
    }

    /**
     * INTERNAL:
     */
    public String[] getDatabaseStrings() {
        return databaseStrings;
    }

    /**
     * INTERNAL:
     */
    public String[] getJavaStrings() {
        return javaStrings;
    }

    /**
     * INTERNAL:
     */
    public Class getNodeClass() {
        return nodeClass;
    }

    /**
     * INTERNAL:
     * Lookup the operator with the given name.
     */
    public static ExpressionOperator getOperator(Integer selector) {
        return (ExpressionOperator)getAllOperators().get(selector);
    }

    /**
     * INTERNAL:
     * Return the selector id.
     */
    public int getSelector() {
        return selector;
    }

    /**
     * ADVANCED:
     * Return the type of function.
     * This must be one of the static function types defined in this class.
     */
    public int getType() {
        return this.type;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator greatest() {
        return simpleTwoArgumentFunction(Greatest, "GREATEST");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator hexToRaw() {
        return simpleFunction(HexToRaw, "HEXTORAW");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator ifNull() {
        return simpleTwoArgumentFunction(Nvl, "NVL");
    }

    /**
     * INTERNAL:
     * Create the IN operator.
     */
    public static ExpressionOperator in() {
        return simpleRelation(In, "IN");
    }
    
    /**
     * INTERNAL:
     * Create the IN operator taking a subquery. 
     * Note, the subquery itself comes with parenethesis, so the IN operator
     * should not add any parenethesis.
     */
    public static ExpressionOperator inSubQuery() {
        ExpressionOperator result = new ExpressionOperator();
        result.setType(ExpressionOperator.FunctionOperator);
        result.setSelector(InSubQuery);
        Vector v = new Vector(1);
        v.addElement(" IN ");
        result.printsAs(v);
        result.bePostfix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator initcap() {
        return simpleFunction(Initcap, "INITCAP");
    }

    /**
     * INTERNAL:
     */
    protected static void initializeAggregateFunctionOperators() {
        addOperator(count());
        addOperator(sum());
        addOperator(average());
        addOperator(minimum());
        addOperator(maximum());
        addOperator(distinct());
    }

    /**
     * INTERNAL:
     */
    protected static void initializeFunctionOperators() {
        addOperator(notOperator());
        addOperator(ascending());
        addOperator(descending());
        addOperator(any());
        addOperator(some());
        addOperator(all());
        addOperator(in());
        addOperator(inSubQuery());
        addOperator(notIn());
        addOperator(notInSubQuery());
        addOperator(coalesce());
        addOperator(caseStatement());
        addOperator(caseConditionStatement());
    }

    /**
     * INTERNAL:
     */
    protected static void initializeLogicalOperators() {
        addOperator(and());
        addOperator(or());
        addOperator(isNull());
        addOperator(notNull());

    }

    /**
     * INTERNAL:
     */
    public static Map initializeOperators() {
        resetOperators();
        initializeFunctionOperators();
        initializeRelationOperators();
        initializeLogicalOperators();
        initializeAggregateFunctionOperators();
        return allOperators;
    }

    /**
     * INTERNAL:
     * Initialize a mapping to the platform operator names for usage with exceptions.
     */
    public static String getPlatformOperatorName(int operator) {
        String name = (String)getPlatformOperatorNames().get(Integer.valueOf(operator));
        if (name == null) {
            name = String.valueOf(operator);
        }
        return name;
    }

    /**
     * INTERNAL:
     * Initialize a mapping to the platform operator names for usage with exceptions.
     */
    public static Map getPlatformOperatorNames() {
        return platformOperatorNames;
    }
    
    /**
     * INTERNAL:
     * Initialize a mapping to the platform operator names for usage with exceptions.
     */
    public static Map initializePlatformOperatorNames() {
        Map platformOperatorNames = new HashMap();
        platformOperatorNames.put(Integer.valueOf(ToUpperCase), "ToUpperCase");
        platformOperatorNames.put(Integer.valueOf(ToLowerCase), "ToLowerCase");
        platformOperatorNames.put(Integer.valueOf(Chr), "Chr");
        platformOperatorNames.put(Integer.valueOf(Concat), "Concat");
        platformOperatorNames.put(Integer.valueOf(Coalesce), "Coalesce");
        platformOperatorNames.put(Integer.valueOf(Case), "Case");
        platformOperatorNames.put(Integer.valueOf(CaseCondition), "Case(codition)");
        platformOperatorNames.put(Integer.valueOf(HexToRaw), "HexToRaw");
        platformOperatorNames.put(Integer.valueOf(Initcap), "Initcap");
        platformOperatorNames.put(Integer.valueOf(Instring), "Instring");
        platformOperatorNames.put(Integer.valueOf(Soundex), "Soundex");
        platformOperatorNames.put(Integer.valueOf(LeftPad), "LeftPad");
        platformOperatorNames.put(Integer.valueOf(LeftTrim), "LeftTrim");
        platformOperatorNames.put(Integer.valueOf(RightPad), "RightPad");
        platformOperatorNames.put(Integer.valueOf(RightTrim), "RightTrim");
        platformOperatorNames.put(Integer.valueOf(Substring), "Substring");
        platformOperatorNames.put(Integer.valueOf(SubstringSingleArg), "Substring");
        platformOperatorNames.put(Integer.valueOf(Translate), "Translate");
        platformOperatorNames.put(Integer.valueOf(Ascii), "Ascii");
        platformOperatorNames.put(Integer.valueOf(Length), "Length");
        platformOperatorNames.put(Integer.valueOf(CharIndex), "CharIndex");
        platformOperatorNames.put(Integer.valueOf(CharLength), "CharLength");
        platformOperatorNames.put(Integer.valueOf(Difference), "Difference");
        platformOperatorNames.put(Integer.valueOf(Reverse), "Reverse");
        platformOperatorNames.put(Integer.valueOf(Replicate), "Replicate");
        platformOperatorNames.put(Integer.valueOf(Right), "Right");
        platformOperatorNames.put(Integer.valueOf(Locate), "Locate");
        platformOperatorNames.put(Integer.valueOf(Locate2), "Locate");
        platformOperatorNames.put(Integer.valueOf(ToNumber), "ToNumber");
        platformOperatorNames.put(Integer.valueOf(ToChar), "ToChar");
        platformOperatorNames.put(Integer.valueOf(ToCharWithFormat), "ToChar");
        platformOperatorNames.put(Integer.valueOf(AddMonths), "AddMonths");
        platformOperatorNames.put(Integer.valueOf(DateToString), "DateToString");
        platformOperatorNames.put(Integer.valueOf(MonthsBetween), "MonthsBetween");
        platformOperatorNames.put(Integer.valueOf(NextDay), "NextDay");
        platformOperatorNames.put(Integer.valueOf(RoundDate), "RoundDate");
        platformOperatorNames.put(Integer.valueOf(AddDate), "AddDate");
        platformOperatorNames.put(Integer.valueOf(DateName), "DateName");
        platformOperatorNames.put(Integer.valueOf(DatePart), "DatePart");
        platformOperatorNames.put(Integer.valueOf(DateDifference), "DateDifference");
        platformOperatorNames.put(Integer.valueOf(TruncateDate), "TruncateDate");
        platformOperatorNames.put(Integer.valueOf(NewTime), "NewTime");
        platformOperatorNames.put(Integer.valueOf(Nvl), "Nvl");
        platformOperatorNames.put(Integer.valueOf(NewTime), "NewTime");
        platformOperatorNames.put(Integer.valueOf(Ceil), "Ceil");
        platformOperatorNames.put(Integer.valueOf(Cos), "Cos");
        platformOperatorNames.put(Integer.valueOf(Cosh), "Cosh");
        platformOperatorNames.put(Integer.valueOf(Abs), "Abs");
        platformOperatorNames.put(Integer.valueOf(Acos), "Acos");
        platformOperatorNames.put(Integer.valueOf(Asin), "Asin");
        platformOperatorNames.put(Integer.valueOf(Atan), "Atan");
        platformOperatorNames.put(Integer.valueOf(Exp), "Exp");
        platformOperatorNames.put(Integer.valueOf(Sqrt), "Sqrt");
        platformOperatorNames.put(Integer.valueOf(Floor), "Floor");
        platformOperatorNames.put(Integer.valueOf(Ln), "Ln");
        platformOperatorNames.put(Integer.valueOf(Log), "Log");
        platformOperatorNames.put(Integer.valueOf(Mod), "Mod");
        platformOperatorNames.put(Integer.valueOf(Power), "Power");
        platformOperatorNames.put(Integer.valueOf(Round), "Round");
        platformOperatorNames.put(Integer.valueOf(Sign), "Sign");
        platformOperatorNames.put(Integer.valueOf(Sin), "Sin");
        platformOperatorNames.put(Integer.valueOf(Sinh), "Sinh");
        platformOperatorNames.put(Integer.valueOf(Tan), "Tan");
        platformOperatorNames.put(Integer.valueOf(Tanh), "Tanh");
        platformOperatorNames.put(Integer.valueOf(Trunc), "Trunc");
        platformOperatorNames.put(Integer.valueOf(Greatest), "Greatest");
        platformOperatorNames.put(Integer.valueOf(Least), "Least");
        platformOperatorNames.put(Integer.valueOf(Add), "Add");
        platformOperatorNames.put(Integer.valueOf(Subtract), "Subtract");
        platformOperatorNames.put(Integer.valueOf(Divide), "Divide");
        platformOperatorNames.put(Integer.valueOf(Multiply), "Multiply");
        platformOperatorNames.put(Integer.valueOf(Atan2), "Atan2");
        platformOperatorNames.put(Integer.valueOf(Cot), "Cot");
        platformOperatorNames.put(Integer.valueOf(Deref), "Deref");
        platformOperatorNames.put(Integer.valueOf(Ref), "Ref");
        platformOperatorNames.put(Integer.valueOf(RefToHex), "RefToHex");
        platformOperatorNames.put(Integer.valueOf(Value), "Value");
        platformOperatorNames.put(Integer.valueOf(Extract), "Extract");
        platformOperatorNames.put(Integer.valueOf(ExtractValue), "ExtractValue");
        platformOperatorNames.put(Integer.valueOf(ExistsNode), "ExistsNode");
        platformOperatorNames.put(Integer.valueOf(GetStringVal), "GetStringVal");
        platformOperatorNames.put(Integer.valueOf(GetNumberVal), "GetNumberVal");
        platformOperatorNames.put(Integer.valueOf(IsFragment), "IsFragment");
        platformOperatorNames.put(Integer.valueOf(SDO_WITHIN_DISTANCE), "MDSYS.SDO_WITHIN_DISTANCE");
        platformOperatorNames.put(Integer.valueOf(SDO_RELATE), "MDSYS.SDO_RELATE");
        platformOperatorNames.put(Integer.valueOf(SDO_FILTER), "MDSYS.SDO_FILTER");
        platformOperatorNames.put(Integer.valueOf(SDO_NN), "MDSYS.SDO_NN");
        platformOperatorNames.put(Integer.valueOf(NullIf), "NullIf");
        return platformOperatorNames;
    }

    /**
     * INTERNAL:
     */
    protected static void initializeRelationOperators() {
        addOperator(simpleRelation(Equal, "=", "equal"));
        addOperator(simpleRelation(NotEqual, "<>", "notEqual"));
        addOperator(simpleRelation(LessThan, "<", "lessThan"));
        addOperator(simpleRelation(LessThanEqual, "<=", "lessThanEqual"));
        addOperator(simpleRelation(GreaterThan, ">", "greaterThan"));
        addOperator(simpleRelation(GreaterThanEqual, ">=", "greaterThanEqual"));

        addOperator(like());
        addOperator(likeEscape());
        addOperator(notLike());
        addOperator(notLikeEscape());
        addOperator(between());

        addOperator(exists());
        addOperator(notExists());
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator instring() {
        return simpleTwoArgumentFunction(Instring, "INSTR");
    }

    /**
     * Aggregate functions are function in the select such as COUNT.
     */
    public boolean isAggregateOperator() {
        return getType() == AggregateOperator;
    }

    /**
     * Comparison functions are functions such as = and >.
     */
    public boolean isComparisonOperator() {
        return getType() == ComparisonOperator;
    }

    /**
     * INTERNAL:
     * If we have all the required information, this operator is complete
     * and can be used as is. Otherwise we will need to look up a platform-
     * specific operator.
     */
    public boolean isComplete() {
        return (databaseStrings != null) && (databaseStrings.length != 0);
    }

    /**
     * General functions are any normal function such as UPPER.
     */
    public boolean isFunctionOperator() {
        return getType() == FunctionOperator;
    }

    /**
     * Logical functions are functions such as and and or.
     */
    public boolean isLogicalOperator() {
        return getType() == LogicalOperator;
    }

    /**
     * INTERNAL:
     * Create the ISNULL operator.
     */
    public static ExpressionOperator isNull() {
        ExpressionOperator result = new ExpressionOperator();
        result.setType(ComparisonOperator);
        result.setSelector(IsNull);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("(");
        v.addElement(" IS NULL)");
        result.printsAs(v);
        result.bePrefix();
        result.printsJavaAs(".isNull()");
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * Order functions are used in the order by such as ASC.
     */
    public boolean isOrderOperator() {
        return getType() == OrderOperator;
    }

    /**
     * ADVANCED:
     * Return true if this is a prefix operator.
     */
    public boolean isPrefix() {
        return isPrefix;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator lastDay() {
        return simpleFunction(LastDay, "LAST_DAY");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator least() {
        return simpleTwoArgumentFunction(Least, "LEAST");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator leftPad() {
        return simpleThreeArgumentFunction(LeftPad, "LPAD");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator leftTrim() {
        return simpleFunction(LeftTrim, "LTRIM");
    }

    /**
     * INTERNAL:
     * Build leftTrim operator that takes one parameter.
     */
    public static ExpressionOperator leftTrim2() {
        return simpleTwoArgumentFunction(LeftTrim2, "LTRIM");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator length() {
        return simpleFunction(Length, "LENGTH");
    }

    /**
     * INTERNAL:
     * Create the LIKE operator.
     */
    public static ExpressionOperator like() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(Like);
        result.setType(FunctionOperator);
        Vector v = NonSynchronizedVector.newInstance(3);
        v.add("");
        v.add(" LIKE ");
        v.add("");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        v = NonSynchronizedVector.newInstance(2);
        v.add(".like(");
        v.add(")");
        result.printsJavaAs(v);
        return result;
    }

    /**
     * INTERNAL:
     * Create the LIKE operator with ESCAPE.
     */
    public static ExpressionOperator likeEscape() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(LikeEscape);
        result.setType(FunctionOperator);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("");
        v.addElement(" LIKE ");
        v.addElement(" ESCAPE ");
        v.addElement("");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        result.setIsBindingSupported(false);
        return result;
    }
    /**
     * INTERNAL:
     * Create the LIKE operator with ESCAPE.
     */
    public static ExpressionOperator notLikeEscape() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(NotLikeEscape);
        result.setType(FunctionOperator);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("");
        v.addElement(" NOT LIKE ");
        v.addElement(" ESCAPE ");
        v.addElement("");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        result.setIsBindingSupported(false);
        return result;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator ln() {
        return simpleFunction(Ln, "LN");
    }

    /**
     * INTERNAL:
     * Build locate operator i.e. LOCATE("ob", t0.F_NAME)
     */
    public static ExpressionOperator locate() {
        ExpressionOperator expOperator = simpleTwoArgumentFunction(Locate, "LOCATE");
        int[] argumentIndices = new int[2];
        argumentIndices[0] = 1;
        argumentIndices[1] = 0;
        expOperator.setArgumentIndices(argumentIndices);
        expOperator.setIsBindingSupported(false);
        return expOperator;
    }

    /**
     * INTERNAL:
     * Build locate operator with 3 params i.e. LOCATE("coffee", t0.DESCRIP, 4).
     * Last parameter is a start at.
     */
    public static ExpressionOperator locate2() {
        ExpressionOperator expOperator = simpleThreeArgumentFunction(Locate2, "LOCATE");
        int[] argumentIndices = new int[3];
        argumentIndices[0] = 1;
        argumentIndices[1] = 0;
        argumentIndices[2] = 2;
        expOperator.setArgumentIndices(argumentIndices);
        expOperator.setIsBindingSupported(false);
        return expOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator log() {
        return simpleFunction(Log, "LOG");
    }

    /**
     * INTERNAL:
     * Create the MAXIMUM operator.
     */
    public static ExpressionOperator maximum() {
        return simpleAggregate(Maximum, "MAX", "maximum");
    }

    /**
     * INTERNAL:
     * Create the MINIMUM operator.
     */
    public static ExpressionOperator minimum() {
        return simpleAggregate(Minimum, "MIN", "minimum");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator mod() {
        ExpressionOperator operator = simpleTwoArgumentFunction(Mod, "MOD");
        operator.setIsBindingSupported(false);
        return operator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator monthsBetween() {
        return simpleTwoArgumentFunction(MonthsBetween, "MONTHS_BETWEEN");
    }

    /**
     * INTERNAL:
     * Create a new expression. Optimized for the single argument case.
     */
    public Expression newExpressionForArgument(Expression base, Object singleArgument) {
        if (singleArgument == null) {
            if (this.selector == Equal) {
                return base.isNull();
            } else if (this.selector == NotEqual) {
                return base.notNull();
            }
        }
        Expression node = createNode();
        node.create(base, singleArgument, this);
        return node;
    }

    /**
     * INTERNAL:
     * Instantiate an instance of the operator's node class.
     */
    protected Expression createNode() {
        // PERF: Avoid reflection for common cases.
        if (this.nodeClass == ClassConstants.ArgumentListFunctionExpression_Class){
            return new ArgumentListFunctionExpression();
        } else if (this.nodeClass == ClassConstants.FunctionExpression_Class) {
            return new FunctionExpression();

        } else if (this.nodeClass == ClassConstants.RelationExpression_Class) {
            return new RelationExpression();
        } else if (this.nodeClass == ClassConstants.LogicalExpression_Class) {
            return new LogicalExpression();
        }
        try {
            Expression node = null;
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    node = (Expression)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(getNodeClass()));
                } catch (PrivilegedActionException exception) {
                    return null;
                }
            } else {
                node = (Expression)PrivilegedAccessHelper.newInstanceFromClass(getNodeClass());
            }
            return node;
        } catch (InstantiationException exception) {
            throw new InternalError(exception.toString());
        } catch (IllegalAccessException exception) {
            throw new InternalError(exception.toString());
        }
    }

    /**
     * INTERNAL:
     * Create a new expression. Optimized for the single argument case with base last
     */
    public Expression newExpressionForArgumentWithBaseLast(Expression base, Object singleArgument) {
        if (singleArgument == null) {
            if (this.selector == Equal) {
                return base.isNull();
            } else if (this.selector == NotEqual) {
                return base.notNull();
            }
        }
        Expression node = createNode();
        node.createWithBaseLast(base, singleArgument, this);
        return node;
    }

    /**
     * INTERNAL:
     * The general case.
     */
    public Expression newExpressionForArguments(Expression base, Vector arguments) {
        if ((arguments.size() == 1) && (arguments.get(0) == null)) {
            if (this.selector == Equal) {
                return base.isNull();
            } else if (this.selector == NotEqual) {
                return base.notNull();
            }
        }
        Expression node = createNode();
        node.create(base, arguments, this);
        return node;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator negate() {
        return simpleFunction(Negate, "-");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator newTime() {
        return simpleThreeArgumentFunction(NewTime, "NEW_TIME");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator nextDay() {
        return simpleTwoArgumentFunction(NextDay, "NEXT_DAY");
    }

    /**
     * INTERNAL:
     * Create the NOT EXISTS operator.
     */
    public static ExpressionOperator notExists() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(NotExists);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("NOT EXISTS" + " ");
        v.addElement(" ");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create the NOTIN operator.
     */
    public static ExpressionOperator notIn() {
        return simpleRelation(NotIn, "NOT IN");
    }

    /**
     * INTERNAL:
     * Create the NOTIN operator taking a subQuery.
     * Note, the subquery itself comes with parenethesis, so the IN operator
     * should not add any parenethesis.
     */
    public static ExpressionOperator notInSubQuery() {
        ExpressionOperator result = new ExpressionOperator();
        result.setType(ExpressionOperator.FunctionOperator);
        result.setSelector(NotInSubQuery);
        Vector v = new Vector(1);
        v.addElement(" NOT IN ");
        result.printsAs(v);
        result.bePostfix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Create the NOTLIKE operator.
     */
    public static ExpressionOperator notLike() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(NotLike);
        result.setType(FunctionOperator);
        Vector v = NonSynchronizedVector.newInstance();
        v.add("");
        v.add(" NOT LIKE ");
        v.add("");
        result.printsAs(v);
        result.bePrefix();
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        v = NonSynchronizedVector.newInstance(2);
        v.add(".notLike(");
        v.add(")");
        result.printsJavaAs(v);
        return result;
    }

    /**
     * INTERNAL:
     * Create the NOTNULL operator.
     */
    public static ExpressionOperator notNull() {
        ExpressionOperator result = new ExpressionOperator();
        result.setType(ComparisonOperator);
        result.setSelector(NotNull);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("(");
        v.addElement(" IS NOT NULL)");
        result.printsAs(v);
        result.bePrefix();
        result.printsJavaAs(".notNull()");
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }

    /**
     * INTERNAL:
     * Create the NOT operator.
     */
    public static ExpressionOperator notOperator() {
        ExpressionOperator result = new ExpressionOperator();
        result.setSelector(Not);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance();
        v.addElement("NOT (");
        v.addElement(")");
        result.printsAs(v);
        result.bePrefix();
        result.printsJavaAs(".not()");
        result.setNodeClass(ClassConstants.FunctionExpression_Class);
        return result;
    }
    
    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator nullIf() {
        return simpleTwoArgumentFunction(NullIf, "NULLIF");
    }

    /**
     * INTERNAL:
     * Create the OR operator.
     */
    public static ExpressionOperator or() {
        return simpleLogical(Or, "OR", "or");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator power() {
        return simpleTwoArgumentFunction(Power, "POWER");
    }

    /**
     * INTERNAL: Print the collection onto the SQL stream.
     */
    public void printCollection(Vector items, ExpressionSQLPrinter printer) {
        // Certain functions don't allow binding on some platforms.
        if (printer.getPlatform().isDynamicSQLRequiredForFunctions() && !isBindingSupported()) {
            printer.getCall().setUsesBinding(false);
        }
        int dbStringIndex = 0;
        try {
            if (isPrefix()) {
                printer.getWriter().write(getDatabaseStrings()[0]);
                dbStringIndex = 1;
            } else {
                dbStringIndex = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (argumentIndices == null) {
            argumentIndices = new int[items.size()];
            for (int i = 0; i < argumentIndices.length; i++){
                argumentIndices[i] = i; 
            }
        }
        
        for (final int index : argumentIndices) {
            Expression item = (Expression)items.elementAt(index);
            if ((this.selector == Ref) || ((this.selector == Deref) && (item.isObjectExpression()))) {
                DatabaseTable alias = ((ObjectExpression)item).aliasForTable(((ObjectExpression)item).getDescriptor().getTables().firstElement());
                printer.printString(alias.getNameDelimited(printer.getPlatform()));
            } else if ((this.selector == Count) && (item.isExpressionBuilder())) {
                printer.printString("*");
            } else {
                item.printSQL(printer);
            }
            if (dbStringIndex < getDatabaseStrings().length) {
                printer.printString(getDatabaseStrings()[dbStringIndex++]);
            }
        }
    }

    /**
     * INTERNAL: Print the collection onto the SQL stream.
     */
    public void printJavaCollection(Vector items, ExpressionJavaPrinter printer) {
        int javaStringIndex = 0;

        for (int i = 0; i < items.size(); i++) {
            Expression item = (Expression)items.elementAt(i);
            item.printJava(printer);
            if (javaStringIndex < getJavaStrings().length) {
                printer.printString(getJavaStrings()[javaStringIndex++]);
            }
        }
    }

    /**
     * INTERNAL:
     * For performance, special case printing two children, since it's by far the most common
     */
    public void printDuo(Expression first, Expression second, ExpressionSQLPrinter printer) {
        // Certain functions don't allow binding on some platforms.
        if (printer.getPlatform().isDynamicSQLRequiredForFunctions() && !isBindingSupported()) {
            printer.getCall().setUsesBinding(false);
        }
        int dbStringIndex;
        if (isPrefix()) {
            printer.printString(getDatabaseStrings()[0]);
            dbStringIndex = 1;
        } else {
            dbStringIndex = 0;
        }

        first.printSQL(printer);
        if (dbStringIndex < getDatabaseStrings().length) {
            printer.printString(getDatabaseStrings()[dbStringIndex++]);
        }
        if (second != null) {
            second.printSQL(printer);
            if (dbStringIndex < getDatabaseStrings().length) {
                printer.printString(getDatabaseStrings()[dbStringIndex++]);
            }
        }
    }

    /**
     * INTERNAL:
     * For performance, special case printing two children, since it's by far the most common
     */
    public void printJavaDuo(Expression first, Expression second, ExpressionJavaPrinter printer) {
        int javaStringIndex = 0;

        first.printJava(printer);
        if (javaStringIndex < getJavaStrings().length) {
            printer.printString(getJavaStrings()[javaStringIndex++]);
        }
        if (second != null) {
            second.printJava(printer);
            if (javaStringIndex < getJavaStrings().length) {
                printer.printString(getJavaStrings()[javaStringIndex]);
            }
        }
    }

    /**
     * ADVANCED:
     * Set the single string for this operator.
     */
    public void printsAs(String s) {
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(1);
        v.addElement(s);
        printsAs(v);
    }

    /**
     * ADVANCED:
     * Set the strings for this operator.
     */
    public void printsAs(Vector dbStrings) {
        this.databaseStrings = new String[dbStrings.size()];
        for (int i = 0; i < dbStrings.size(); i++) {
            getDatabaseStrings()[i] = (String)dbStrings.elementAt(i);
        }
    }

    /**
     * ADVANCED:
     * Set the single string for this operator.
     */
    public void printsJavaAs(String s) {
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(1);
        v.addElement(s);
        printsJavaAs(v);
    }

    /**
     * ADVANCED:
     * Set the strings for this operator.
     */
    public void printsJavaAs(Vector dbStrings) {
        this.javaStrings = new String[dbStrings.size()];
        for (int i = 0; i < dbStrings.size(); i++) {
            getJavaStrings()[i] = (String)dbStrings.elementAt(i);
        }
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator ref() {
        return simpleFunction(Ref, "REF");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator refToHex() {
        return simpleFunction(RefToHex, "REFTOHEX");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator replace() {
        ExpressionOperator operator = simpleThreeArgumentFunction(Replace, "REPLACE");
        operator.setIsBindingSupported(false);
        return operator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator replicate() {
        return simpleTwoArgumentFunction(Replicate, "REPLICATE");
    }

    /**
     * INTERNAL:
     * Reset all the operators.
     */
    public static void resetOperators() {
        allOperators = new HashMap();
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator reverse() {
        return simpleFunction(Reverse, "REVERSE");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator right() {
        return simpleTwoArgumentFunction(Right, "RIGHT");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator rightPad() {
        return simpleThreeArgumentFunction(RightPad, "RPAD");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator rightTrim() {
        return simpleFunction(RightTrim, "RTRIM");
    }

    /**
     * INTERNAL:
     * Build rightTrim operator that takes one parameter.
     * @bug 2916893 rightTrim(substring) broken.
     */
    public static ExpressionOperator rightTrim2() {
        return simpleTwoArgumentFunction(RightTrim2, "RTRIM");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator round() {
        return simpleTwoArgumentFunction(Round, "ROUND");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator roundDate() {
        return simpleTwoArgumentFunction(RoundDate, "ROUND");
    }

    /**
     * ADVANCED: Set the array of indexes to use when building the SQL function.
     * 
     * The index of the array is the position in the printout, from left to right, starting with zero.
     * The value of the array entry is the number of the argument to print at that particular output position.
     * So each argument can be used zero, one or many times.
     */
    public void setArgumentIndices(int[] indices) {
        argumentIndices = indices;
    }

    /**
     * ADVANCED:
     * Set the node class for this operator. For user-defined functions this is
     * set automatically but can be changed.
     * <p>A list of Operator types, an example, and the node class used follows.
     * <p>LogicalOperator     AND        LogicalExpression
     * <p>ComparisonOperator  <>         RelationExpression
     * <p>AggregateOperator   COUNT      FunctionExpression
     * <p>OrderOperator       ASCENDING         "
     * <p>FunctionOperator    RTRIM             "
     * <p>Node classes given belong to org.eclipse.persistence.internal.expressions.
     */
    public void setNodeClass(Class nodeClass) {
        this.nodeClass = nodeClass;
    }

    /**
     * INTERNAL:
     * Set the selector id.
     */
    public void setSelector(int selector) {
        this.selector = selector;
    }

    /**
     * ADVANCED:
     * Set the type of function.
     * This must be one of the static function types defined in this class.
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator sign() {
        return simpleFunction(Sign, "SIGN");
    }

    /**
     * INTERNAL:
     * Create an operator for a simple aggregate given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleAggregate(int selector, String databaseName, String javaName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(AggregateOperator);
        exOperator.setSelector(selector);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement(databaseName + "(");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.printsJavaAs("." + javaName + "()");
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple function given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleFunction(int selector, String databaseName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement(databaseName + "(");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple function call without parentheses
     */
    public static ExpressionOperator simpleFunctionNoParentheses(int selector, String databaseName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(1);
        v.addElement(databaseName);
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }


    /**
     * INTERNAL:
     * Create an operator for a simple function given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleFunction(int selector, String databaseName, String javaName) {
        ExpressionOperator exOperator = simpleFunction(selector, databaseName);
        exOperator.printsJavaAs("." + javaName + "()");
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple logical given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleLogical(int selector, String databaseName, String javaName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(LogicalOperator);
        exOperator.setSelector(selector);
        exOperator.printsAs(" " + databaseName + " ");
        exOperator.bePostfix();
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("." + javaName + "(");
        v.addElement(")");
        exOperator.printsJavaAs(v);
        exOperator.setNodeClass(ClassConstants.LogicalExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple math operatin, i.e. +, -, *, /
     */
    public static ExpressionOperator simpleMath(int selector, String databaseName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setIsBindingSupported(false);
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(3);
        v.addElement("(");
        v.addElement(" " + databaseName + " ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple ordering given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleOrdering(int selector, String databaseName, String javaName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(OrderOperator);
        exOperator.setSelector(selector);
        exOperator.printsAs(" " + databaseName);
        exOperator.bePostfix();
        exOperator.printsJavaAs("." + javaName + "()");
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple relation given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleRelation(int selector, String databaseName) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(ComparisonOperator);
        exOperator.setSelector(selector);
        exOperator.printsAs(" " + databaseName + " ");
        exOperator.bePostfix();
        exOperator.setNodeClass(ClassConstants.RelationExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Create an operator for a simple relation given a Java name and a single
     * String for the database (parentheses will be added automatically).
     */
    public static ExpressionOperator simpleRelation(int selector, String databaseName, String javaName) {
        ExpressionOperator exOperator = simpleRelation(selector, databaseName);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("." + javaName + "(");
        v.addElement(")");
        exOperator.printsJavaAs(v);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator simpleThreeArgumentFunction(int selector, String dbString) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(4);
        v.addElement(dbString + "(");
        v.addElement(", ");
        v.addElement(", ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator simpleTwoArgumentFunction(int selector, String dbString) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(5);
        v.addElement(dbString + "(");
        v.addElement(", ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * e.g.: ... "Bob" CONCAT "Smith" ...
     * Parentheses will not be addded. [RMB - March 5 2000]
     */
    public static ExpressionOperator simpleLogicalNoParens(int selector, String dbString) {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(selector);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(5);
        v.addElement("");
        v.addElement(" " + dbString + " ");
        v.addElement("");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator sin() {
        return simpleFunction(Sin, "SIN");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator sinh() {
        return simpleFunction(Sinh, "SINH");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator soundex() {
        return simpleFunction(Soundex, "SOUNDEX");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator sqrt() {
        return simpleFunction(Sqrt, "SQRT");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator standardDeviation() {
        return simpleAggregate(StandardDeviation, "STDDEV", "standardDeviation");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator substring() {
        ExpressionOperator operator = simpleThreeArgumentFunction(Substring, "SUBSTR");
        operator.setIsBindingSupported(false);
        return operator;
    }
    
    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator substringSingleArg() {
        return simpleTwoArgumentFunction(SubstringSingleArg, "SUBSTR");
    }
    
    /**
     * INTERNAL:
     * Create the SUM operator.
     */
    public static ExpressionOperator sum() {
        return simpleAggregate(Sum, "SUM", "sum");
    }

    /**
     * INTERNAL:
     * Function, to add months to a date.
     */
    public static ExpressionOperator sybaseAddMonthsOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(AddMonths);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(3);
        v.addElement("DATEADD(month, ");
        v.addElement(", ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        int[] indices = { 1, 0 };
        exOperator.setArgumentIndices(indices);
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;

    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator sybaseAtan2Operator() {
        return ExpressionOperator.simpleTwoArgumentFunction(Atan2, "ATN2");
    }

    /**
     * INTERNAL:
     * Build instring operator
     */
    public static ExpressionOperator sybaseInStringOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Instring);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(3);
        v.addElement("CHARINDEX(");
        v.addElement(", ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        int[] indices = { 1, 0 };
        exOperator.setArgumentIndices(indices);
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;

    }

    /**
     * INTERNAL:
     * Build Sybase equivalent to TO_NUMBER.
     */
    public static ExpressionOperator sybaseToNumberOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(ToNumber);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("CONVERT(NUMERIC, ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build Sybase equivalent to TO_CHAR.
     */
    public static ExpressionOperator sybaseToDateToStringOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(DateToString);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("CONVERT(CHAR, ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build Sybase equivalent to TO_DATE.
     */
    public static ExpressionOperator sybaseToDateOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(ToDate);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("CONVERT(DATETIME, ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build Sybase equivalent to TO_CHAR.
     */
    public static ExpressionOperator sybaseToCharOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(ToChar);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(2);
        v.addElement("CONVERT(CHAR, ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build Sybase equivalent to TO_CHAR.
     */
    public static ExpressionOperator sybaseToCharWithFormatOperator() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(ToCharWithFormat);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(3);
        v.addElement("CONVERT(CHAR, ");
        v.addElement(",");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build the Sybase equivalent to Locate
     */
    public static ExpressionOperator sybaseLocateOperator() {
        ExpressionOperator result = simpleTwoArgumentFunction(ExpressionOperator.Locate, "CHARINDEX");
        int[] argumentIndices = new int[2];
        argumentIndices[0] = 1;
        argumentIndices[1] = 0;
        result.setArgumentIndices(argumentIndices);
        return result;
    }


    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator tan() {
        return simpleFunction(Tan, "TAN");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator tanh() {
        return simpleFunction(Tanh, "TANH");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator toDate() {
        return simpleFunction(ToDate, "TO_DATE");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator today() {
        return currentTimeStamp();
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator currentTimeStamp() {
        return simpleFunctionNoParentheses(Today,  "CURRENT_TIMESTAMP");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator currentDate() {
        return simpleFunctionNoParentheses(CurrentDate,  "CURRENT_DATE");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator currentTime() {
        return simpleFunctionNoParentheses(CurrentTime, "CURRENT_TIME");
    }

    /**
     * INTERNAL:
     * Create the toLowerCase operator.
     */
    public static ExpressionOperator toLowerCase() {
        return simpleFunction(ToLowerCase, "LOWER", "toLowerCase");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator toNumber() {
        return simpleFunction(ToNumber, "TO_NUMBER");
    }

    /**
     * Print a debug representation of this operator.
     */
    public String toString() {
        if ((getDatabaseStrings() == null) || (getDatabaseStrings().length == 0)) {
            //CR#... Print a useful name for the missing platform operator.
            return "platform operator - " + getPlatformOperatorName(this.selector);
        } else {
            return "operator " + Arrays.asList(getDatabaseStrings());
        }
    }

    /**
     * INTERNAL:
     * Create the TOUPPERCASE operator.
     */
    public static ExpressionOperator toUpperCase() {
        return simpleFunction(ToUpperCase, "UPPER", "toUpperCase");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator translate() {
        ExpressionOperator operator = simpleThreeArgumentFunction(Translate, "TRANSLATE");
        operator.setIsBindingSupported(false);
        return operator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator trim() {
        return simpleFunction(Trim, "TRIM");
    }
    
    /**
     * INTERNAL:
     * Build Trim operator.
     */
    public static ExpressionOperator trim2() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Trim2);
        Vector v = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(5);
        v.addElement("TRIM(");
        v.addElement(" FROM ");
        v.addElement(")");
        exOperator.printsAs(v);
        exOperator.bePrefix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator trunc() {
        return simpleTwoArgumentFunction(Trunc, "TRUNC");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator truncateDate() {
        return simpleTwoArgumentFunction(TruncateDate, "TRUNC");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator value() {
        return simpleFunction(Value, "VALUE");
    }

    /**
     * INTERNAL:
     * Build operator.
     */
    public static ExpressionOperator variance() {
        return simpleAggregate(Variance, "VARIANCE", "variance");
    }
    
    /**
     * INTERNAL:
     * Create the ANY operator.
     */
    public static ExpressionOperator any() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Any);
        exOperator.printsAs("ANY");
        exOperator.bePostfix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }
    
    /**
     * INTERNAL:
     * Create the SOME operator.
     */
    public static ExpressionOperator some() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(Some);
        exOperator.printsAs("SOME");
        exOperator.bePostfix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }
    
    /**
     * INTERNAL:
     * Create the ALL operator.
     */
    public static ExpressionOperator all() {
        ExpressionOperator exOperator = new ExpressionOperator();
        exOperator.setType(FunctionOperator);
        exOperator.setSelector(All);
        exOperator.printsAs("ALL");
        exOperator.bePostfix();
        exOperator.setNodeClass(ClassConstants.FunctionExpression_Class);
        return exOperator;
    }
    
    /**
     * INTERNAL:
     * Indicates whether operator has selector Any or Some
     */
    public boolean isAny() {
        return  selector == ExpressionOperator.Any ||
                selector == ExpressionOperator.Some;
    }
    /**
     * INTERNAL:
     * Indicates whether operator has selector All
     */
    public boolean isAll() {
        return  selector == ExpressionOperator.All;
    }
    /**
     * INTERNAL:
     * Indicates whether operator has selector Any, Some or All
     */
    public boolean isAnyOrAll() {
        return  isAny() || isAll();
    }
}