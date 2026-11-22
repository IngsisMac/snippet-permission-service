package ingsis.snippet.permission.controller

import ingsis.snippet.permission.directory.UserDirectoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Resolución de nombres para `snippet-service`, que muestra el autor de cada snippet. Recibe los
 * ids de la página del listado de una sola vez: así el listado no hace una consulta por fila.
 * Solo accesible desde la red interna (NGINX responde 403 a todo `/internal`).
 */
@RestController
@RequestMapping("/internal/users")
class InternalUserController(
    private val directory: UserDirectoryService,
) {
    @GetMapping("/names")
    fun namesOf(
        @RequestParam ids: List<String>,
    ): Map<String, String> = directory.namesOf(ids)
}
