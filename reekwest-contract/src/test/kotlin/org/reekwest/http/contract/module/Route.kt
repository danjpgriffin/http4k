package org.reekwest.http.contract.module

import org.reekwest.http.contract.module.PathBinder.Companion.Core
import org.reekwest.http.core.ContentType
import org.reekwest.http.core.Filter
import org.reekwest.http.core.HttpMessage
import org.reekwest.http.core.Method
import org.reekwest.http.core.Request
import org.reekwest.http.core.Status
import org.reekwest.http.lens.BodyLens
import org.reekwest.http.lens.Failure
import org.reekwest.http.lens.HeaderLens
import org.reekwest.http.lens.Lens
import org.reekwest.http.lens.LensFailure
import org.reekwest.http.lens.QueryLens

data class RouteResponse(val status: Status, val description: String?, val example: String?)

class Route private constructor(private val core: Core) {
    constructor(name: String, description: String? = null) : this(Core(name, description, null))

    fun header(new: HeaderLens<*>) = Route(core.copy(requestParams = core.requestParams.plus(new)))
    fun query(new: QueryLens<*>) = Route(core.copy(requestParams = core.requestParams.plus(new)))
    fun body(new: Lens<HttpMessage, *>) = Route(core.copy(body = new))
    fun returning(new: Pair<Status, String>, description: String? = null) = Route(core.copy(responses = core.responses.plus(RouteResponse(new.first, new.second, description))))
    fun producing(vararg new: ContentType) = Route(core.copy(produces = core.produces.plus(new)))
    fun consuming(vararg new: ContentType) = Route(core.copy(consumes = core.consumes.plus(new)))

    infix fun at(method: Method): PathBinder0 = PathBinder0(Core(this, method, { Root }))

    internal val validationFilter = Filter {
        next ->
        {
            val errors = core.fold(emptyList<Failure>()) { memo, next ->
                try {
                    next(it)
                    memo
                } catch (e: LensFailure) {
                    memo.plus(e.failures)
                }
            }
            if (errors.isEmpty()) next(it) else throw LensFailure(errors)
        }
    }

    companion object {
        private data class Core(val name: String,
                                val description: String?,
                                val body: BodyLens<*>?,
                                val produces: Set<ContentType> = emptySet(),
                                val consumes: Set<ContentType> = emptySet(),
                                val requestParams: List<Lens<Request, *>> = emptyList(),
                                val responses: List<RouteResponse> = emptyList()) : Iterable<Lens<Request, *>> {

            override fun iterator(): Iterator<Lens<Request, *>> = requestParams.plus(body?.let { listOf(it) } ?: emptyList<Lens<Request, *>>()).iterator()
        }
    }
}