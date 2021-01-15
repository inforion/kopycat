/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.library.builders.text

import ru.inforion.lab403.kopycat.library.builders.api.ModuleParameterInfo
import ru.inforion.lab403.kopycat.settings

class PluginConfig(
        val top: Boolean,
        val plugin: String,
        val library: String,
        val params: Array<ParameterConfig>,
        val ports: Array<PortConfig>,
        val buses: Array<BusConfig>,
        val modules: Array<ModuleConfig>,
        val connections: Array<ConnectionConfig>,
        val reset: Array<String>?) {

    val definitions by lazy {
        params.mapIndexed { k, v -> ModuleParameterInfo(k, v.name, v.type, true, v.default) }
    }

    /**
     * {EN}
     * Substitute parent module parameters into child parameters, i.e. in this example
     * `fwMode` (and suppose specified as 0) passed from parent module to `chip` module.
     * Function will in fact replaceЖ
     * `"params": { "fwMode": "params.fwMode" }` to `"params": { "fwMode": "0" }`
     *
     * ```json
     * {
     *     "params": [
     *         { "name": "fwMode", "type": "int", "default": 0 },
     *     ],
     *
     *     {
     *         "name": "chip",
     *         "plugin": "MemoryChip",
     *         "library": "tests",
     *         "params": { "fwMode": "params.fwMode" }
     *     }
     * }
     * ```
     *
     * @param parameters child parameters map
     * @param substitute function executed to substitute value for parameter
     *
     * @return
     * {EN}
     */
    inline fun substituteParameterValues(
            parameters: Map<String, Any?>,
            substitute: (info: ModuleParameterInfo) -> Any?
    ): Map<String, Any?> {
        val pairs = parameters.map { (name, value) ->
            val actual: Any? = if (value == null) null else {
                val tmp = value.toString() // everything can be cast to string
                if (!tmp.startsWith(settings.jsonParamsSectionName)) value else {
                    val parameterName = tmp.removePrefix(settings.jsonParamsSectionName)
                    val info = definitions.first { it.name == parameterName }
                    substitute(info)
                }
            }
            name to actual
        }

        return pairs.toMap()
    }
}