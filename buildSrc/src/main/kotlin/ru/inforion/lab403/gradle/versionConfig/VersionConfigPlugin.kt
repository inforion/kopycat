@file:Suppress("UnstableApiUsage")

package ru.inforion.lab403.gradle.versionConfig

import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Repository
import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.inforion.lab403.gradle.common.doFirstTyped
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

            prj.logger.info("Repository revision '${versionConfig.gitRevisionRef}' is '$revision'")

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
                        prj.logger.lifecycle("Build information data: $buildInfo")
                        it.expand(buildInfo)
                    }
        }
    }
}