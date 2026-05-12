/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.routing.handlers.params

import org.jetbrains.annotations.Contract

class QueryParams(
    private val validRules: Set<QueryParamRule<*>>,
    private val params: Map<String, List<Any>>,
) {

    /**
     * Get the first values passed via the query string for the given rule or the default value if no values were passed.
     * 
     * @param rule The [QueryParamRule] to get the value for.
     * @param <T>  The value type defined by the `rule`.
     * @return The first values passed via the query string or the rules default value if no values were passed.
     */
    operator fun <T> get(rule: QueryParamRule<T>): T? =
        getAll(rule)[0]

    /**
     * Get the first values passed via the query string for the given rule or the specified value if no values were passed.
     * 
     * @param rule  The [QueryParamRule] to get the value for.
     * @param other The value to use if no values were passed via the query string.
     * @param <T>   The value type defined by the `rule`.
     * @return The first values passed via the query string or `other` if no values were passed.
     */
    @Contract("_, !null, -> !null")
    fun <T> getOrElse(rule: QueryParamRule<T>, other: T?): T? =
        getAllOrElse(rule, other)[0]

    /**
     * Get all the values passed via the query string for the given rule or the default value if no values were passed.
     * 
     * @param rule The [QueryParamRule] to get the value for.
     * @param <T>  The value type defined by the `rule`.
     * @return The list of values passed via the query string or the rules default value if `other` is an empty list.
     */
    fun <T> getAll(rule: QueryParamRule<T>): List<T> {
        val values = getAllValues(rule)

        return if (values.isNullOrEmpty()) {
            val defaultValue = requireNotNull(rule.defaultValue) {
                "INTERNAL ERROR: Param ${rule.name} has no values and no default. Either mark the param as required, provide a default or use with getOrElse or getAllOrElse."
            }
            listOf(defaultValue)
        } else
            values
    }

    /**
     * Get all the values passed via the query string for the given rule or the specified value if no values were passed.
     * 
     * @param rule  The [QueryParamRule] to get the value for.
     * @param other The value to return if no values were passed via the query string.
     * @param <T>   The value type defined by the `rule`.
     * @return The list of values passed via the query string or a list containing `other` if no values were found.
     */
    fun <T> getAllOrElse(rule: QueryParamRule<T>, other: T?): List<T?> =
        getAllOrElse(rule, listOf(other))

    /**
     * Get all the values passed via the query string for the given rule or the specified value if no values were passed.
     * 
     * @param rule  The [QueryParamRule] to get the value for.
     * @param other The list of values to return if no values were passed via the query string.
     * @param <T>   The value type defined by the `rule`.
     * @return The list of values passed via the query string or `other` if no values were found.
     */
    fun <T> getAllOrElse(rule: QueryParamRule<T>, other: List<T?>): List<T?> =
        getAllValues(rule).takeUnless { it.isNullOrEmpty() } ?: other

    /**
     * @param rule The [QueryParamRule] to get the value for.
     * @param <T>  The value type defined by the `rule`.
     * @return True if at least one value was passed for the `rule` via the query string.
     */
    operator fun <T> contains(rule: QueryParamRule<T>): Boolean = params.containsKey(rule.name)

    private fun <T> getAllValues(rule: QueryParamRule<T>): List<T>? {
        require(validRules.contains(rule)) {
            "INTERNAL ERROR: Query param ${rule.name} was not registered with this route. Did you forget to register it?"
        }

        @Suppress("UNCHECKED_CAST")
        return params[rule.name] as? List<T>
    }

}
