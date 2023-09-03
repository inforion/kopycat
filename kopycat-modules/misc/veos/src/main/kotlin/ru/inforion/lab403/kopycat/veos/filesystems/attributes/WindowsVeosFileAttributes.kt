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
package ru.inforion.lab403.kopycat.veos.filesystems.attributes

import ru.inforion.lab403.common.extensions.getAclFileAttributeView
import ru.inforion.lab403.common.extensions.readDosAttribute
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.attribute.*
import java.nio.file.attribute.PosixFilePermission.*

/**
 * {EN}Emulate Posix File Attributes for Windows{EN}
 */
class WindowsVeosFileAttributes(path: Path) :
    BasicFileAttributes by path.readDosAttribute(), PosixFileAttributes {

    private val acl = path.getAclFileAttributeView()

    override fun owner(): UserPrincipal = acl.owner

    override fun group(): GroupPrincipal =
        FileSystems.getDefault().userPrincipalLookupService.lookupPrincipalByGroupName("Users")

    override fun permissions() = permissions

    private val permissions = run {
        val ownerACL = acl.acl.find { it.principal() == acl.owner }
        val usersACL = acl.acl.find { it.principal().name.endsWith("Users") }

        val permissions = mutableSetOf<PosixFilePermission>()

        ownerACL?.permissions()?.mapNotNullTo(permissions) {
            when (it) {
                AclEntryPermission.READ_DATA -> OWNER_READ
                AclEntryPermission.WRITE_DATA -> OWNER_WRITE
                AclEntryPermission.APPEND_DATA -> OWNER_WRITE
                AclEntryPermission.EXECUTE -> OWNER_EXECUTE
                else -> null
            }
        }

        usersACL?.permissions()?.flatMapTo(permissions) {
            when (it) {
                AclEntryPermission.READ_DATA -> listOf(OTHERS_READ, GROUP_READ)
                AclEntryPermission.WRITE_DATA -> listOf(OTHERS_WRITE, GROUP_WRITE)
                AclEntryPermission.APPEND_DATA -> listOf(OTHERS_WRITE, GROUP_WRITE)
                AclEntryPermission.EXECUTE -> listOf(OTHERS_EXECUTE, GROUP_EXECUTE)
                else -> listOf()
            }
        }

        permissions
    }
}