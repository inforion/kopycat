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
package ru.inforion.lab403.gradle.buildConfig.scriptgen

/**
 * Generates powershell KOPYCAT startup script
 */
class IntelliJScriptGenerator(
    val data: ScriptGeneratorData
) : IScriptGenerator {
    override val name: String = data.name
    override val description: String = data.description
    override val starterClass: String = data.starterClass

    override val classpathStr by lazy { data.classpath.joinToString(";") }

    override val arguments = linkedMapOf<String, String?>()

    val gradleBuildTaskPath by lazy {
        data.gradleBuildTask.split(":").run {
            subList(1, this.size - 1).joinToString("/")
        }
    }

    override fun generate(): String {
        val argumentsStr = arguments.map { (key, value) ->
            value?.let { "$key &quot;$it&quot;" } ?: key
        }.joinToString(" ")

        return """<component name="ProjectRunConfigurationManager">
  <configuration default="false" name="kopycat-gen-${data.kcPackageName} $name" type="JetRunConfigurationType" folderName="kopycat-gen">
    <output_file path="./temp/${data.kcPackageName}-$name-kc-last" is_save="true" />
    <option name="MAIN_CLASS_NAME" value="$starterClass" />
    <module name="kopycat-private.kopycat.main" />
    <option name="PROGRAM_PARAMETERS" value="$argumentsStr" />
    <shortenClasspath name="NONE" />
    <option name="VM_PARAMETERS" value="-server -Xms2G -Xmx8G -XX:+UseParallelGC" />
    <method v="2">
      <option name="Make" enabled="true" />
      <option name="Gradle.BeforeRunTask" enabled="true" tasks="buildKopycatModule" externalProjectPath="💲PROJECT_DIR💲/$gradleBuildTaskPath" vmOptions="" scriptParameters="" />
    </method>
  </configuration>
</component>""".replace("💲", "${'$'}")
    }

    override fun fileName(): String = "kopycat-gen-${data.kcPackageName} $name.run.xml"
    override fun dirName(): String = "intellij"
}