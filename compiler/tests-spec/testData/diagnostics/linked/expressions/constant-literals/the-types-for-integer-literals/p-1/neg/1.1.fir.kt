// TESTCASE NUMBER: 1
val value_1 = <!ILLEGAL_CONST_EXPRESSION!>0bl<!>

// TESTCASE NUMBER: 2
val value_2 = <!ILLEGAL_CONST_EXPRESSION!>0BL<!>

// TESTCASE NUMBER: 3
val value_3 = <!ILLEGAL_CONST_EXPRESSION!>0Xl<!>

// TESTCASE NUMBER: 4
val value_4 = <!ILLEGAL_CONST_EXPRESSION!>0xL<!>

// TESTCASE NUMBER: 5
val value_5 = <!ILLEGAL_UNDERSCORE!>0b_l<!>

// TESTCASE NUMBER: 6
val value_6 = <!ILLEGAL_UNDERSCORE!>0B_L<!>

// TESTCASE NUMBER: 7
val value_7 = <!ILLEGAL_UNDERSCORE!>0X____l<!>

// TESTCASE NUMBER: 8
val value_8 = <!ILLEGAL_UNDERSCORE!>0x_L<!>
