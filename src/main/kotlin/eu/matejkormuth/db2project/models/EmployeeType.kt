package eu.matejkormuth.db2project.models

enum class EmployeeType {
    IKT_OFFICER,
    POLICEMAN,
    INVESTIGATOR,
    INSPECTOR,
    ANALYST;

    fun canWorkOnMisdemeanors() = when (this) {
        IKT_OFFICER -> false
        POLICEMAN -> true
        INVESTIGATOR -> false
        INSPECTOR -> true
        ANALYST -> false
    }

    fun canWorkOnProtectiveActions() = when (this) {
        IKT_OFFICER -> false
        POLICEMAN -> true
        INVESTIGATOR -> false
        INSPECTOR -> true
        ANALYST -> false
    }

    fun canWorkOnCrimes() = when (this) {
        IKT_OFFICER -> false
        else -> true
    }

    fun canBeCaseHeadEmployee() = when (this) {
        IKT_OFFICER -> false
        POLICEMAN -> true
        INVESTIGATOR -> true
        INSPECTOR -> false
        ANALYST -> false
    }

    fun canCreateArrestWarrant() = when (this) {
        IKT_OFFICER -> false
        POLICEMAN -> true
        INVESTIGATOR -> true
        INSPECTOR -> true
        ANALYST -> false
    }

    fun canCreateFine() = when (this) {
        IKT_OFFICER -> false
        POLICEMAN -> false
        INVESTIGATOR -> false
        INSPECTOR -> true
        ANALYST -> false
    }

    fun canConfirmConnection() = when (this) {
        IKT_OFFICER -> false
        else -> true
    }
}
