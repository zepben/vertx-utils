/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.routing.handlers.params;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
@EverythingIsNonnullByDefault
public class QueryParams {

    private final Set<QueryParamRule<?>> validRules;
    private final Map<String, List<Object>> params;

    public QueryParams(Set<QueryParamRule<?>> validRules, Map<String, List<Object>> params) {
        this.validRules = validRules;
        this.params = params;
    }

    /**
     * Get the first values passed via the query string for the given rule or the default value if no values were passed.
     *
     * @param rule The {@link QueryParamRule} to get the value for.
     * @param <T>  The value type defined by the {@code rule}.
     * @return The first values passed via the query string or the rules default value if no values were passed.
     */
    public <T> T get(QueryParamRule<T> rule) {
        return getAll(rule).get(0);
    }

    /**
     * Get the first values passed via the query string for the given rule or the specified value if no values were passed.
     *
     * @param rule  The {@link QueryParamRule} to get the value for.
     * @param other The value to use if no values were passed via the query string.
     * @param <T>   The value type defined by the {@code rule}.
     * @return The first values passed via the query string or {@code other} if no values were passed.
     */
    @Nullable
    @Contract("_, !null, -> !null")
    public <T> T getOrElse(QueryParamRule<T> rule, @Nullable T other) {
        return getAllOrElse(rule, other).get(0);
    }

    /**
     * Get all of the values passed via the query string for the given rule or the default value if no values were passed.
     *
     * @param rule The {@link QueryParamRule} to get the value for.
     * @param <T>  The value type defined by the {@code rule}.
     * @return The list of values passed via the query string or the rules default value if {@code other} is an empty list.
     */
    public <T> List<T> getAll(QueryParamRule<T> rule) {
        List<T> values = getAllValues(rule);

        if (values == null || values.isEmpty()) {
            T defaultValue = rule.defaultValue();
            if (defaultValue == null)
                throw new IllegalArgumentException(String.format("INTERNAL ERROR: Param %s has no values and no default. Either mark the param as required, provide a default or use with getOrElse or getAllOrElse.", rule.name()));
            else
                return Collections.singletonList(defaultValue);
        }

        return values;
    }

    /**
     * Get all of the values passed via the query string for the given rule or the specified value if no values were passed.
     *
     * @param rule  The {@link QueryParamRule} to get the value for.
     * @param other The value to return if no values were passed via the query string.
     * @param <T>   The value type defined by the {@code rule}.
     * @return The list of values passed via the query string or a list containing {@code other} if no values were found.
     */
    public <T> List<T> getAllOrElse(QueryParamRule<T> rule, @Nullable T other) {
        return getAllOrElse(rule, Collections.singletonList(other));
    }

    /**
     * Get all of the values passed via the query string for the given rule or the specified value if no values were passed.
     *
     * @param rule  The {@link QueryParamRule} to get the value for.
     * @param other The list of values to return if no values were passed via the query string.
     * @param <T>   The value type defined by the {@code rule}.
     * @return The list of values passed via the query string or {@code other} if no values were found.
     */
    public <T> List<T> getAllOrElse(QueryParamRule<T> rule, List<T> other) {
        List<T> values = getAllValues(rule);

        if (values == null || values.isEmpty())
            return other;
        else
            return values;
    }

    /**
     * @param rule The {@link QueryParamRule} to get the value for.
     * @param <T>  The value type defined by the {@code rule}.
     * @return True if at least one value was passed for the {@code rule} via the query string.
     */
    public <T> boolean exists(QueryParamRule<T> rule) {
        return params.containsKey(rule.name());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> List<T> getAllValues(QueryParamRule<T> rule) {
        if (!validRules.contains(rule))
            throw new IllegalArgumentException(String.format("INTERNAL ERROR: Query param %s was not registered with this route. Did you forget to register it?", rule.name()));

        return (List<T>) params.get(rule.name());
    }

}
