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
@file:Suppress("UnstableApiUsage")

package ru.inforion.lab403.gradle.versionConfig

import org.gradle.api.Project
import org.gradle.language.jvm.tasks.ProcessResources
import ru.inforion.lab403.gradle.common.processResourcesTask
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

open class VersionConfigExtension {
    companion object {
        const val extensionIdentifier = "versionConfig"
        const val defaultBuildNumber = "Regular"
        const val teamcityEnvBuildNumberName = "BUILD_NUMBER"
    }

    var gitFolderName = ".git"
    var gitRevisionRef = "HEAD"
    var gitAbbrevLength = 8

    var searchPattern = "**/version.properties"
    var dateFormat = "yyyy.Mdd"

    lateinit var buildNumber: String
    lateinit var processResources: ProcessResources
    lateinit var repositoryDirectory: File

    val isRepositoryDirectorySet get() = ::repositoryDirectory.isInitialized
    val isProcessResourcesSet get() = ::processResources.isInitialized
    val isBuildNumberSet get() = ::buildNumber.isInitialized

    override fun toString() = "$extensionIdentifier:\n" +
            "\tgitFolderName='$gitFolderName'\n" +
            "\tgitRevId='$gitRevisionRef'\n" +
            "\tgitAbbrevLength=$gitAbbrevLength\n" +
            "\tdateFormat='$dateFormat'\n" +
            "\tbuildNumber='$buildNumber'\n" +
            "\tsearchPattern='$searchPattern'\n" +
            "\tprocessResources=$processResources\n" +
            "\trepositoryDirectory='$repositoryDirectory'\n"

    fun configure(project: Project) {
        if (!isRepositoryDirectorySet)
            repositoryDirectory = File(project.rootProject.projectDir, gitFolderName)

        if (!isProcessResourcesSet)
            processResources = project.processResourcesTask

        if (!isBuildNumberSet) {
            // For automatic build number from teamcity
            // see https://stackoverflow.com/questions/11208323/accessing-teamcity-build-number-in-gradle-build-script
            project.logger.info("buildNumber isn't specified -> trying TeamCity 'build.number'")
            val systemBuildNumber = System.getenv(teamcityEnvBuildNumberName)
            buildNumber = if (systemBuildNumber == null) {
                project.logger.info("TeamCity '$teamcityEnvBuildNumberName' isn't also set -> using '$defaultBuildNumber'")
                defaultBuildNumber
            } else {
                project.logger.info("TeamCity '$teamcityEnvBuildNumberName=$systemBuildNumber'")
                systemBuildNumber
            }
        }
    }

    fun formattedDatetime(): String {
        val sdf = SimpleDateFormat(dateFormat)
        return sdf.format(Date().time)
    }
}