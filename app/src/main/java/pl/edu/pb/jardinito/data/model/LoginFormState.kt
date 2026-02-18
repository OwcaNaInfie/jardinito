package pl.edu.pb.jardinito.data.model

data class LoginFormState(
    val loginIdentifier: String = "",
    val loginIdentifierError: Int? = null,

    val loginPassword: String = "",
    val loginPasswordError: Int? = null
)

