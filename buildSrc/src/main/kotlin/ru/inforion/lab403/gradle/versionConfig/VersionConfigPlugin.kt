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

import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Repository
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File


class VersionConfigPlugin : Plugin<Project> {
    private fun openRepository(repositoryDirectory: File): Repository {
        val repo = FileRepository(repositoryDirectory)
        require(repo.objectDatabase.exists()) { "Repository doesn't exists at '$repositoryDirectory'" }
        return repo
    }

    private fun Repository.getGitRepositoryRevHash(rev: String, abbrev: Int): String {
        val ref = checkNotNull(refDatabase.findRef(rev)) { "Can't find reference '$rev' in git repository!" }
        return ref.objectId.abbreviate(abbrev).name()
    }

    override fun apply(project: Project) {
        val versionConfig = project.extensions.create(
                VersionConfigExtension.extensionIdentifier,
                VersionConfigExtension::class.java)

        project.afterEvaluate { prj ->

            // Check and post-configure extensions
            versionConfig.configure(prj)

            prj.logger.debug(versionConfig.toString())

            val repo = openRepository(versionConfig.repositoryDirectory)

            val abbrev = repo.config.getInt("core", "abbrev", versionConfig.gitAbbrevLength)
            if (abbrev != versionConfig.gitAbbrevLength) {
                prj.logger.lifecycle("Repository 'core.abbrev=$abbrev' differ VersionConfig.abbrev -> use 'core.abbrev'")
            }

            val revision = repo.getGitRepositoryRevHash(versionConfig.gitRevisionRef, abbrev)

            versionConfig
                    .processResources
                    .filesMatching(versionConfig.searchPattern) {
                        val buildInfo = mapOf(
                                "initialized" to true,
                                "name" to project.name,
                                "version" to project.version,
                                "revision" to revision,
                                "timestamp" to versionConfig.formattedDatetime(),
                                "build" to versionConfig.buildNumber)
                        it.expand(buildInfo)
                        val data = it.open().reader().readText()
                        prj.logger.lifecycle("Build information data: ${data.lines()}")
                    }

            // Force to re-run processResources
            versionConfig.processResources.outputs.upToDateWhen { false }
        }
    }
}