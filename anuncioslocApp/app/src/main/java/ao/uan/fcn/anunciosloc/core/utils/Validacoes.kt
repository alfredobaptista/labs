package ao.uan.fcn.anunciosloc.core.utils


import android.util.Patterns

fun validarNome(nome: String): String? = when {
    nome.isBlank() -> "O nome completo é obrigatório"
    nome.length < 3 -> "Mínimo 3 caracteres"
    !nome.matches(Regex("^[a-zA-ZÀ-ÿ\\s]+$")) -> "Apenas letras"
    else -> null
}

fun validarEmail(email: String): String? = when {
    email.isBlank() -> "O email é obrigatório"
    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email inválido"
    else -> null
}

fun validarSenha(senha: String): String? = when {
    senha.isBlank() -> "A palavra-passe é obrigatória"
    senha.length < 6 -> "Mínimo 6 caracteres"
    senha.length > 20 -> "Máximo 20 caracteres"
    !senha.matches(Regex(".*[A-Z].*")) -> "Pelo menos 1 maiúscula"
    !senha.matches(Regex(".*[0-9].*")) -> "Pelo menos 1 número"
    !senha.matches(Regex(".*[!@#\$%^&*(),.?\":{}|<>].*")) -> "Pelo menos 1 especial"
    else -> null
}

fun validarSenhaLogin(senha: String): String? = when {
    senha.isBlank() -> "A palavra-passe é obrigatória"
    senha.length < 6 -> "Mínimo 6 caracteres"
    else -> null
}

fun validarConfSenha(conf: String, senha: String): String? = when {
    conf.isBlank() -> "Confirme a palavra-passe"
    conf != senha -> "As palavras-passe não coincidem"
    else -> null
}