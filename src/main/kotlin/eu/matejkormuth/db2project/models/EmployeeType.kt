package eu.matejkormuth.db2project.models

/**
 * Types of employees.
 */
enum class EmployeeType {
    IKT_OFFICER,
    POLICEMAN,
    INVESTIGATOR,
    INSPECTOR,
    ANALYST;

    /**
     * Returns whether this employee type can work on misdemeanor or not.
     */
    fun canWorkOnMisdemeanors() = when (this) {
        IKT_OFFICER -> false
        POLICEMAN -> true
        INVESTIGATOR -> false
        INSPECTOR -> true
        ANALYST -> false
    }

    /**
     * Returns whether this employee type can work on protective actions or not.
     */
    fun canWorkOnProtectiveActions() = when (this) {
        IKT_OFFICER -> false
        POLICEMAN -> true
        INVESTIGATOR -> false
        INSPECTOR -> true
        ANALYST -> false
    }

    /**
     * Returns whether this employee type can work on crimes or not.
     */
    fun canWorkOnCrimes() = when (this) {
        IKT_OFFICER -> false
        else -> true
    }

    /**
     * Returns whether this employee type can be head employee or not.
     */
    fun canBeCaseHeadEmployee() = when (this) {
        IKT_OFFICER -> false
        POLICEMAN -> true
        INVESTIGATOR -> true
        INSPECTOR -> false
        ANALYST -> false
    }

    /**
     * Returns whether this employee type can create arrest warrant or not.
     */
    fun canCreateArrestWarrant() = when (this) {
        IKT_OFFICER -> false
        POLICEMAN -> true
        INVESTIGATOR -> true
        INSPECTOR -> true
        ANALYST -> false
    }

    /**
     * Returns whether this employee type can create fine or not.
     */
    fun canCreateFine() = when (this) {
        IKT_OFFICER -> false
        POLICEMAN -> false
        INVESTIGATOR -> false
        INSPECTOR -> true
        ANALYST -> false
    }

    /**
     * Returns whether this employee type can confirm connection or not.
     */
    fun canConfirmConnection() = when (this) {
        IKT_OFFICER -> false
        else -> true
    }
}
