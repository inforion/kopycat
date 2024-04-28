/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.gradle.buildConfig.creator

import org.gradle.api.logging.Logger
import ru.inforion.lab403.gradle.buildConfig.creator.scriptgen.*
import ru.inforion.lab403.gradle.buildConfig.dirCheckOrCreate
import java.io.File

class BashScriptCreator(genData: ScriptGeneratorData): IKopycatConfigCreator {
    override val generator: IScriptGenerator = BashScriptGenerator(genData)
}

class PowerShellScriptCreator(genData: ScriptGeneratorData): IKopycatConfigCreator {
    override val generator: IScriptGenerator = PowerShellScriptGenerator(genData)
}

class IntelliJScriptCreator(genData: ScriptGeneratorData): IKopycatConfigCreator {
    override val generator: IScriptGenerator = IntelliJScriptGenerator(genData)

    /**
     * Copies IDEA configs into the acceptable IDEA directory
     */
    override fun postHook(configFile: File, logger: Logger?) {
        val intelliJRunDir = File(this.generator.projectDir, ".idea/runConfigurations")
        intelliJRunDir.dirCheckOrCreate()

        configFile.copyTo(File(intelliJRunDir, configFile.name), true)
        logger?.info("[BuildConfig] Copied '$configFile' into '$intelliJRunDir'")
    }
}